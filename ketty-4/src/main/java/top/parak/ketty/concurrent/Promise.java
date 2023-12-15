package top.parak.ketty.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Writable future.
 *
 * @author Khighness
 * @since 2023-12-12
 */
public interface Promise<V> extends Runnable, Future<V> {

    /**
     * Mark this future as a success and set the result of this future.
     * <p>If this future is success or failed already, it will throw an {@link IllegalStateException}.</p>
     *
     * @param result the result to be set
     * @return current future
     * @throws IllegalStateException if it is success or failed already
     */
    Promise<V> setSuccess(V result);

    /**
     * Mark this future as a success and set the result of this future.
     * <p>If marking this future as a success is successful, it will return {@code true}.</p>
     * <p>If this future is already marked as either a success or a failure,
     * it will return {@code false}.</p>
     *
     * @param result true if
     * @return
     */
    boolean trySuccess(V result);

    /**
     * Get the result of this future immediately.
     * <p>It returns {@code null} if this future has not completed.</p>
     *
     * @return the result of this future
     */
    V getNow();

    /**
     * Wait for this future to be completed.
     *
     * @return current future
     * @throws InterruptedException if the current thread was interrupted
     */
    Promise<V> await() throws InterruptedException;

    /**
     * Wait for this future to be completed within the specified time limit.
     *
     * @param timeout  the maximum time to wait
     * @param timeUnit the time unit
     * @return true if and only the future was completed within the specified time limit
     * @throws InterruptedException if the current thread was interrupted
     */
    boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Wait for this future util it is done.
     *
     * @return current future
     * @throws InterruptedException if the current thread was interrupted
     */
    Promise<V> sync() throws InterruptedException;

}
