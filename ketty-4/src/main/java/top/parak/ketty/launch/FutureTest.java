package top.parak.ketty.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Khighness
 * @since 2023-12-11
 */
public class FutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        Callable<Integer> callable = () -> 3;
//        FutureTask<Integer> future = new FutureTask<>(callable);
//        ExecutorService executorService = Executors.newCachedThreadPool();
//        Future<?> result = executorService.submit(future);
//        System.out.println(result.get());

        Thread t1 = new Thread(FutureTest::main1);
        Thread t2 = new Thread(FutureTest::main2);
        t1.start();
        t2.start();
    }

    private static final Logger logger = LoggerFactory.getLogger(FutureTest.class);
    private static final Object O = new Object();

    public static void main1() {
        synchronized (O) {
            logger.info("main1 start");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("main1 end");
        }
    }

    public static void main2() {
        synchronized (O) {
            logger.info("main2 start");
            logger.info("main2 end");
        }
    }
}
