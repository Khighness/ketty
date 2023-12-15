package top.parak.ketty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public class SimpleClient {
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(SimpleClient.class);

        // 创建客户端 channel
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        // 获取 selector
        Selector selector = Selector.open();
        // 将客户端 channel 注册到 selector 上
        SelectionKey clientKey = channel.register(selector, 0);
        // 给客户端 channel 设置感兴趣的世界：连接
        clientKey.interestOps(SelectionKey.OP_CONNECT);
        // 连接服务器
        channel.connect(new InetSocketAddress(8080));

        // 轮询事件
        while (true) {
            // 无事件则阻塞
            selector.select();
            // 获取事件迭代器
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                // 处理连接成功事件
                if (key.isConnectable()) {
                    if (channel.finishConnect()) {
                        channel.register(selector, SelectionKey.OP_READ);
                        channel.write(ByteBuffer.wrap("Hello".getBytes()));
                    }
                }
                // 处理读事件
                if (key.isReadable()) {
                    SocketChannel curChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int len = curChannel.read(buffer);
                    byte[] bytes = new byte[len];
                    buffer.flip();
                    buffer.get(bytes);
                    logger.info("[Server:{}] {}", channel.getRemoteAddress(), new String(bytes));
                }
            }
        }
    }

}
