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
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public class SimpleServer {
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(SimpleServer.class);

        // 创建服务端 channel
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        // 设置 channel 非阻塞
        ssChannel.configureBlocking(false);
        // 获取 selector
        Selector selector = Selector.open();
        // 将服务端 channel 注册到 selector 上
        SelectionKey serverKey = ssChannel.register(selector, 0, ssChannel);
        // 给服务端 channel 设置感兴趣的事件：接受连接
        serverKey.interestOps(SelectionKey.OP_ACCEPT);
        // 给服务端 channel 绑定端口号
        InetSocketAddress bindAddr = new InetSocketAddress(8080);
        ssChannel.bind(bindAddr);
        logger.info("Listening on {}", bindAddr);

        // 接受连接，处理事件
        while (true) {
            // 当没有事件时，select() 是阻塞的
            selector.select();
            // 如果有事件到来，这里可以得到注册到该 selector 上的所有key，每一个 key 上都有一个 channel
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            // 获取到 key 的迭代器
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                // 首先移除该 key
                keyIterator.remove();
                // 处理连接事件
                if (key.isAcceptable()) {
                    // 获取服务端 channel
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    // 获取客户端 channel
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);
                    // 管理客户端 channel
                    SelectionKey clientKey = clientChannel.register(selector, 0, clientChannel);
                    // 给客户端 channel 设置感兴趣的事件：读
                    clientKey.interestOps(SelectionKey.OP_READ);
                    logger.info("[Client:{}] connect success", clientChannel.getRemoteAddress());
                    clientChannel.write(ByteBuffer.wrap("Connected".getBytes()));
                }
                // 连接读事件
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 分配字节缓冲区接受客户端传过来的数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int len = channel.read(buffer);
                    if (len == -1) {
                        channel.close();
                        break;
                    } else {
                        buffer.flip();
                        logger.info("[Client:{}] {}", channel.getRemoteAddress(), Charset.defaultCharset().decode(buffer));
                    }
                }
            }
        }
    }
}
