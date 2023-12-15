package top.parak.ketty.channel;

import java.nio.channels.SocketChannel;

/**
 * @author Khighness
 * @since 2023-12-11
 */
public class Bootstrap {

    private EventLoopGroup eventLoopGroup;

    public static Bootstrap group(EventLoopGroup eventLoopGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.setEventLoopGroup(eventLoopGroup);
        return bootstrap;
    }

    public void register(SocketChannel socketChannel) {
        eventLoopGroup.register(socketChannel);
    }

    public void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
    }

}
