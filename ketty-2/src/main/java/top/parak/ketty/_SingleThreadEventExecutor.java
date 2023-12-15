package top.parak.ketty;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Khighness
 * @since 2023-12-09
 */
public class _SingleThreadEventExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(_SingleThreadEventExecutor.class);

    protected static final int DEFAULT_MAX_PENDING_TASKS = Integer.MAX_VALUE;

    private final Queue<Runnable> taskQueue;

    private final RejectedExecutionHandler rejectedExecutionHandler;

    private final SelectorProvider selectorProvider;

    private Selector selector;

    private Thread thread;

    private volatile boolean start = false;

    public _SingleThreadEventExecutor() {
        this.selectorProvider = SelectorProvider.provider();
        this.taskQueue = new LinkedBlockingQueue<>(DEFAULT_MAX_PENDING_TASKS);
        this.rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
        this.selector = openSelector();
    }

    private Selector openSelector() {
        try {
            selector = selectorProvider.openSelector();
            return selector;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }

    public void register(SocketChannel channel) {
        if (inEventLoop(Thread.currentThread())) {
            doRegister(channel);
            logger.info("register socket channel in event loop");
        } else {
            execute(() -> {
                doRegister(channel);
                logger.info("register socket channel not in event loop");
            });
        }
    }

    private void doRegister(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            logger.error("doRegister error", e);
        }
    }

    @Override
    public void execute(Runnable task) {
        Preconditions.checkNotNull(task, "command");
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
        logger.info("Thread start");
    }

    private void run() {
        while (true) {
            try {
                select();
                processSelectedKeys(selector.selectedKeys());
            } catch (Exception e) {
                logger.error("Run method error", e);
            } finally {
                runAllTask();
            }
        }
    }

    private void select() throws IOException {
        Selector selector = this.selector;
        for (;;) {
            logger.info("Select timeout 3S...");
            int selectedKeys = selector.select(3000);
            if (selectedKeys != 0 || hasTask()) {
                break;
            }
        }
    }

    private void processSelectedKeys(Set<SelectionKey> selectionKeys) throws IOException {
        if (selectionKeys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            processSelectedKey(key);
        }
    }

    private void processSelectedKey(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int len = channel.read(buffer);
            if (len == -1) {
                channel.close();
                return;
            }
            logger.info("[Client:{}] {}", channel.getRemoteAddress(), Charset.defaultCharset().decode(buffer));
        }
    }

    private void runAllTask() {
        runAllTaskFrom(taskQueue);
    }

    private void runAllTaskFrom(Queue<Runnable> taskQueue) {
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

    private Runnable pollTaskFrom(Queue<Runnable> taskQueue) {
        return taskQueue.poll();
    }

    private void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger.error("Task {} run error", task);
        }
    }

    private void reject(Runnable task) {
        // rejectedExecutionHandler.rejectedExecution(task, this);
    }

}
