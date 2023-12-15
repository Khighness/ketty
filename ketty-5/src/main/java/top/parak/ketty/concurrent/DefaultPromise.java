package top.parak.ketty.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Khighness
 * @since 2023-12-15
 */
public class DefaultPromise<V> implements Promise<V> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPromise.class);

    private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");
    private static final Object SUCCESS = new Object();
    private static final Object NOT_CANCELLABLE = new Object();
    private static final CauseHolder CANCELLATION_CAUSE_HOLDER = new CauseHolder(new CancellationException());

    private Callable<V> callable;
    private volatile Object result;
    private int waiters;

    private List<GenericListener<? extends Promise<? super V>>> listeners = new ArrayList<>();

    public DefaultPromise() {
    }

    public DefaultPromise(Callable<V> callable) {
        this.callable = callable;
    }

    @Override
    public void run() {
        V object;
        Callable<V> c = callable;
        try {
            object = c.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setResult(object);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (RESULT_UPDATER.compareAndSet(this, null, CANCELLATION_CAUSE_HOLDER)) {
            checkNotifyWaiters();
            notifyListeners();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled0(result);
    }

    @Override
    public boolean isDone() {
        return isDone0(result);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if (result == null) {
            await();
        }
        return getNow();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (await(timeout, unit)) {
            return getNow();
        }
        throw new TimeoutException();
    }

    @Override
    public Promise<V> setSuccess(V result) {
        if (setSuccess0(result)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    @Override
    public boolean trySuccess(V result) {
        if (setSuccess0(result)) {
            return true;
        }
        return false;
    }

    @Override
    public V getNow() {
        Object result = this.result;
        if (result instanceof CauseHolder || result == SUCCESS) {
            return null;
        }
        return (V) result;
    }

    @Override
    public Promise<V> await() throws InterruptedException {
        if (isDone()) {
            return this;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        synchronized (this) {
            while (!isDone()) {
                incWaiters();
                try {
                    wait();
                } finally {
                    decWaiters();
                }
            }
        }
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return await0(timeUnit.toNanos(timeout), true);
    }

    @Override
    public Promise<V> sync() throws InterruptedException {
        await();
        return this;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    protected StringBuilder toStringBuilder() {
        StringBuilder buf = new StringBuilder(64)
                .append(DefaultPromise.class.getSimpleName())
                .append('@')
                .append(Integer.toHexString(hashCode()));

        Object result = this.result;
        if (result == SUCCESS) {
            buf.append("(success)");
        } else if (result == NOT_CANCELLABLE) {
            buf.append("(uncancellable)");
        } else if (result != null) {
            buf.append("(success: ")
                    .append(result)
                    .append(')');
        } else {
            buf.append("(incomplete)");
        }
        return buf;
    }

    private synchronized void checkNotifyWaiters() {
        if (waiters > 0) {
            notifyAll();
        }
    }

    private void incWaiters() {
        if (waiters == Short.MAX_VALUE) {
            throw new IllegalStateException("Too many waiters: " + this);
        }
        ++waiters;
    }

    private void decWaiters() {
        --waiters;
    }

    private boolean await0(long timeoutNanos, boolean interruptable) throws InterruptedException {
        if (isDone()) {
            return true;
        }

        if (timeoutNanos <= 0) {
            return isDone();
        }

        if (interruptable && Thread.interrupted()) {
            throw new InterruptedException(toString());
        }

        long startTime = System.nanoTime();
        long waitTime = timeoutNanos;
        boolean interrupted = false;
        try {
            for (;;) {
                synchronized (this) {
                    if (isDone()) {
                        return true;
                    }
                    incWaiters();
                    try {
                        wait(waitTime / 1000000, (int) (waitTime % 1000000));
                    } catch (InterruptedException e) {
                        if (interruptable) {
                            throw e;
                        } else {
                            interrupted = true;
                        }
                    } finally {
                        decWaiters();
                    }
                }
                if (isDone()) {
                    return true;
                } else {
                    waitTime = timeoutNanos - (System.nanoTime() - startTime);
                    if (waitTime <= 0) {
                        return isDone();
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean setSuccess0(V result) {
        return setResult(result == null ? SUCCESS : result);
    }

    private boolean setResult(Object result) {
        if (RESULT_UPDATER.compareAndSet(this, null, result)
                || RESULT_UPDATER.compareAndSet(this, NOT_CANCELLABLE, result)) {
            checkNotifyWaiters();
            notifyListeners();
            return true;
        }
        return false;
    }

    private static boolean isCancelled0(Object result) {
        return result instanceof CauseHolder && ((CauseHolder) result).cause instanceof CancellationException;
    }

    private static boolean isDone0(Object result) {
        return result != null && result != NOT_CANCELLABLE;
    }

    private static final class CauseHolder {
        final Throwable cause;
        private CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }

    public Promise<V> addListener(GenericListener<? extends Promise<? super V>> listener) {
        synchronized (this) {
            listeners.add(listener);
        }
        if (isDone()) {
            notifyListeners();
        }
        return this;
    }

    private void notifyListeners() {
        for (GenericListener<? extends Promise<? super V>> listener : listeners) {
            listener.operationComplete(this);
        }
    }

}
