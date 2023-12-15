package top.parak.ketty.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.parak.ketty.concurrent.DefaultPromise;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * @author Khighness
 * @since 2023-12-14
 */
public class BootstrapDefaultPromiseTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        Logger logger = LoggerFactory.getLogger(BootstrapDefaultPromiseTest.class);
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        DefaultPromise<Boolean> promise = new DefaultPromise<>();
        Runnable runnable = () -> {
            try {
                logger.info("register start");
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            promise.setSuccess(null);
        };
        new Thread(runnable).start();
        promise.sync();
        logger.info("sync finish");
        serverSocketChannel.bind(new InetSocketAddress(8080));
    }
}
