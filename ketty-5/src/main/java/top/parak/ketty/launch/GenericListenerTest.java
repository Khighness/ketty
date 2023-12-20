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
 * @author cantai
 * @since 2023-12-20
 */
public class GenericListenerTest {
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(GenericListenerTest.class);

        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        DefaultPromise<Object> promise = new DefaultPromise<>();
        promise.addListener(p -> {
            try {
                logger.info("bind start");
                serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8080));
                logger.info("bind end");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Runnable runnable = () -> {
            try {
                logger.info("register start");
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                logger.info("register end");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            promise.setSuccess(null);
        };
        new Thread(runnable).start();
    }
}
