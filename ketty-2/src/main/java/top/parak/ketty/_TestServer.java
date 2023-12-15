package top.parak.ketty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public class _TestServer {
    private static final Logger logger = LoggerFactory.getLogger(_TestServer.class);

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.configureBlocking(false);
        Selector selector = Selector.open();
        SelectionKey selectionKey = ssChannel.register(selector, 0, ssChannel);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        InetSocketAddress bindAddr = new InetSocketAddress(8080);
        ssChannel.bind(bindAddr);
        logger.info("Listening on {}", bindAddr);
        _SingleThreadEventExecutor singleThreadEventExecutor = new _SingleThreadEventExecutor();
        while (true) {
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    singleThreadEventExecutor.register(clientChannel);
                    logger.info("[Client:{}] connect success", clientChannel.getRemoteAddress());
                }
            }
        }
    }
}
