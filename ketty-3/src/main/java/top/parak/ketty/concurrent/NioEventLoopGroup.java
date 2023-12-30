package top.parak.ketty.concurrent;

/**
 * @author Khighness
 * @since 2023-12-10
 */
public class NioEventLoopGroup extends MultiThreadEventLoopGroup {

    public NioEventLoopGroup(int threads) {
        super(threads);
    }

    @Override
    protected EventLoop createEventExecutor(String name) {
        return new NioEventLoop(name);
    }

}
