package top.parak.ketty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventLoop.class);

    public void register(SocketChannel socketChannel, NioEventLoop eventLoop) {
        if (inEventLoop(Thread.currentThread())) {
            doRegister(socketChannel, eventLoop);
            logger.info("SingleThreadEventLoop register socket channel, in nio event loop");
        } else {
            eventLoop.execute(() -> {
                doRegister(socketChannel, eventLoop);
                logger.info("SingleThreadEventLoop register socket channel, not in nio event loop");
            });
        }
    }

    private void doRegister(SocketChannel socketChannel, NioEventLoop eventLoop) {
        try {
            socketChannel.configureBlocking(false);
            socketChannel.register(eventLoop.selector(), SelectionKey.OP_READ);
        } catch (Exception e) {
            logger.error("SingleThreadEventLoop doRegister error", e);
        }
    }

}
