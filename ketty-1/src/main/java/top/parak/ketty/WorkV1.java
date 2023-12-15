package top.parak.ketty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public class WorkV1 implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WorkV1.class);

    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final Thread thread;

    public WorkV1(ServerSocketChannel serverChannel) throws IOException {
        this.serverChannel = serverChannel;
        this.selector = Selector.open();
        this.thread = new Thread(this, "WorkThread");
    }

    public void start() {
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            logger.info("Blocking...");
            try {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        logger.info("[Client:{}] connect success", clientChannel.getRemoteAddress());
                        clientChannel.write(ByteBuffer.wrap("Connected".getBytes()));
                    }
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
            } catch (IOException ex) {
                logger.error("IO", ex);
            }
        }
    }

}
