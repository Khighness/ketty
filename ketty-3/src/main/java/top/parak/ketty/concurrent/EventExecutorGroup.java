package top.parak.ketty.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author Khighness
 * @since 2023-12-11
 */
public interface EventExecutorGroup {

    EventExecutor next();

    void shutdownGracefully(long quietPeriod, long timeout, TimeUnit timeUnit);

}
