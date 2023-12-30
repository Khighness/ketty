package top.parak.ketty.concurrent;

import java.nio.channels.SocketChannel;

/**
 * @author Khighness
 * @since 2023-12-11
 */
public abstract class MultiThreadEventLoopGroup extends MultiThreadEventExecutorGroup implements EventLoopGroup {

    public MultiThreadEventLoopGroup(int threads) {
        super(threads);
    }

    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }

    @Override
    public void register(SocketChannel channel) {
        next().register(channel);
    }

    protected abstract EventLoop createEventExecutor(String name);

}
