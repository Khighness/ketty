package top.parak.ketty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public class TestServer {
    private static final Logger logger = LoggerFactory.getLogger(TestServer.class);

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        ssChannel.configureBlocking(false);
        Selector selector = Selector.open();
        SelectionKey selectionKey = ssChannel.register(selector, 0, ssChannel);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        InetSocketAddress bindAddr = new InetSocketAddress(8080);
        ssChannel.bind(bindAddr);
        logger.info("Listening on {}", bindAddr);
        NioEventLoop nioEventLoop = new NioEventLoop();
        while (true) {
            selector.select();
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    nioEventLoop.register(clientChannel, nioEventLoop);
                    logger.info("[Client:{}] connect success", clientChannel.getRemoteAddress());
                    clientChannel.write(ByteBuffer.wrap("Connected".getBytes()));
                }
            }
        }
    }
}
