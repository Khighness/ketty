package top.parak.ketty.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.parak.ketty.concurrent.DefaultPromise;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Khighness
 * @since 2023-12-12
 */
public class DefaultPromiseTest {
    public static void main(String[] args) {
        Callable<Integer> callable = () -> {
            TimeUnit.SECONDS.sleep(6);
            return 3;
        };
        DefaultPromise<Integer> promise = new DefaultPromise<>(callable);
        Thread thread = new Thread(promise);
        thread.start();
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        threadPool.submit(() -> {
            try {
                System.out.println(promise.get(5, TimeUnit.SECONDS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Logger logger = LoggerFactory.getLogger(DefaultPromiseTest.class);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> logger.info("{}", System.nanoTime()), 1, 1, TimeUnit.SECONDS);
    }
}
