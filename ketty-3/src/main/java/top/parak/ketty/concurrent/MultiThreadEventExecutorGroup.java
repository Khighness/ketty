package top.parak.ketty.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author Khighness
 * @since 2023-12-11
 */
public abstract class MultiThreadEventExecutorGroup implements EventExecutorGroup {

    private final EventExecutor[] eventExecutors;

    private int index = 0;

    public MultiThreadEventExecutorGroup(int threads) {
        eventExecutors = new EventExecutor[threads];
        for (int i = 0; i < threads; i++) {
            eventExecutors[i] = createEventExecutor(String.format("EventExecutor-%d", i));
        }
    }

    @Override
    public EventExecutor next() {
        return eventExecutors[index++ % eventExecutors.length];
    }

    @Override
    public void shutdownGracefully(long quietPeriod, long timeout, TimeUnit timeUnit) {
        next().shutdownGracefully(quietPeriod, timeout, timeUnit);
    }

    protected abstract EventExecutor createEventExecutor(String name);

}
