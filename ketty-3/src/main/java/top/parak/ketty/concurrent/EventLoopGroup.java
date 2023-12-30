package top.parak.ketty.concurrent;

import java.nio.channels.SocketChannel;

/**
 * @author Khighness
 * @since 2023-12-10
 */
public interface EventLoopGroup extends EventExecutorGroup {

    @Override
    EventLoop next();

    void register(SocketChannel channel);

}
