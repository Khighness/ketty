package top.parak.ketty.launch;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author Khighness
 * @since 2023-12-11
 */
public class FutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<Integer> callable = () -> 3;
        FutureTask<Integer> future = new FutureTask<>(callable);
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<?> result = executorService.submit(future);
        System.out.println(result.get());
    }
}
