package top.parak.ketty.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventLoop.class);

    public SingleThreadEventLoop(String threadName) {
        super(threadName);
    }

    @Override
    public EventLoop next() {
        return this;
    }

    @Override
    public void register(SocketChannel socketChannel) {
        execute(() -> {
            try {
                socketChannel.configureBlocking(false);
                socketChannel.register(selector(), SelectionKey.OP_READ);
            } catch (IOException e) {
                logger.error("SingleThreadEventLoop register channel error", e);
            }
        });
    }

    public abstract Selector selector();

}
