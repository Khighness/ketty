package top.parak.ketty;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author cantai
 * @since 2023-12-09
 */
public abstract class SingleThreadEventExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(SingleThreadEventExecutor.class);

    protected static final int DEFAULT_MAX_PENDING_TASKS = Integer.MAX_VALUE;

    private final Queue<Runnable> taskQueue;
    private final RejectedExecutionHandler rejectedExecutionHandler;

    private Thread thread;

    private volatile boolean start = false;

    public SingleThreadEventExecutor() {
        this.taskQueue = newTaskQueue(DEFAULT_MAX_PENDING_TASKS);
        this.rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
    }

    protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
        return new LinkedBlockingQueue<>(maxPendingTasks);
    }

    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }

    @Override
    public void execute(Runnable task) {
        Preconditions.checkNotNull(task);
        addTask(task);
        startThread();
    }

    private void addTask(Runnable task) {
        if (!offerTask(task)) {
            reject(task);
        }
    }

    private boolean offerTask(Runnable task) {
        return taskQueue.offer(task);
    }

    protected boolean hasTask() {
        return ! taskQueue.isEmpty();
    }

    private void startThread() {
        if (start) {
            return;
        }

        start = true;
        thread = new Thread(this::run, "WorkThread");
        thread.start();
        logger.info("SingleThreadEventExecutor startThread");
    }

    protected void runAllTask() {
        logger.info("SingleThreadEventExecutor runAllTask: {}", taskQueue.size());
        runAllTaskFrom(taskQueue);
    }

    protected void runAllTaskFrom(Queue<Runnable> taskQueue) {
        Runnable task = pollTaskFrom(taskQueue);
        if (task == null) {
            return;
        }
        for (;;) {
            safeExecute(task);
            task = pollTaskFrom(taskQueue);
            if (task == null) {
                return;
            }
        }
    }

    protected Runnable pollTaskFrom(Queue<Runnable> taskQueue) {
        return taskQueue.poll();
    }

    private void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger.error("Task {} run error", task);
        }
    }

    protected void reject(Runnable task) {
        // rejectedExecutionHandler.rejectedExecution(task, this);
    }

    protected abstract void run();

}
