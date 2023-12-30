package top.parak.ketty.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public class NioEventLoop extends SingleThreadEventLoop implements EventLoop {

    private static final Logger logger = LoggerFactory.getLogger(NioEventLoop.class);

    private final SelectorProvider selectorProvider;
    private final Selector selector;

    public NioEventLoop(String name) {
        super(name);
        this.selectorProvider = SelectorProvider.provider();
        this.selector = openSelector();
    }

    public Selector selector() {
        return selector;
    }

    private Selector openSelector() {
        try {
            return selectorProvider.openSelector();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open selector", e);
        }
    }

    @Override
    protected void run() {
        for (;;) {
            try {
                select();
                processSelectedKeys(selector.selectedKeys());
            } catch (Exception e) {
                logger.error("NioEventLoop run error", e);
            } finally {
                runAllTask();
            }
        }
    }

    private void select() throws IOException {
        Selector selector = this.selector;
        for (;;) {
            logger.info("NioEventLoop select timeout 3S...");
            int selectCount = selector.select(3000);
            if (selectCount != 0 || hasTask()) {
                break;
            }
        }
    }

    private void processSelectedKeys(Set<SelectionKey> selectionKes) throws IOException {
        logger.info("NioEventLoop processSelectedKeys {}", selectionKes.size());
        Iterator<SelectionKey> keyIterator = selectionKes.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey selectionKey = keyIterator.next();
            keyIterator.remove();
            processSelectedKey(selectionKey);
        }
    }

    private void processSelectedKey(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int len = socketChannel.read(buffer);
            if (len == -1) {
                socketChannel.close();
                return;
            }
            byte[] bytes = new byte[len];
            buffer.flip();
            buffer.get(bytes);
            logger.info("[Client:{}] {}", socketChannel.getRemoteAddress(), new String(bytes));
        }
    }

    @Override
    public void shutdownGracefully(long quietPeriod, long timeout, TimeUnit timeUnit) {
    }

}
