package top.parak.ketty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author cantai
 * @since 2023-12-09
 */
public class WorkV2 implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkV2.class);

    private volatile boolean start;

    private SelectorProvider selectorProvider;
    private Selector selector;
    private Thread thread;

    public WorkV2() {
        this.selectorProvider = SelectorProvider.provider();
        this.selector = openSelector();
        this.thread = new Thread(this, "WorkThread");
    }

    public void start() {
        if (start) {
            return;
        }
        start = true;
        thread.start();
    }

    private Selector openSelector() {
        try {
            selector = selectorProvider.openSelector();
            return selector;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            logger.info("Blocking...");
            try {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int len = channel.read(buffer);
                        if (len == -1) {
                            channel.close();
                            break;
                        }
                        byte[] bytes = new byte[len];
                        buffer.flip();
                        buffer.get(bytes);
                        logger.info("[Client:{}] {}", channel.getRemoteAddress(), new String(bytes));
                    }
                }
            } catch (IOException e) {
                logger.error("IO", e);
            }
        }
    }
}
