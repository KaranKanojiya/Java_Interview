package interview.level3_multithreading.executors;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolLifeCycle {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService= Executors.newFixedThreadPool(10);

        for(int i=0; i<100; i++){
            executorService.execute(new Task());
        }

        // initiate shutdown
        executorService.shutdown();

        // will throw RejectionExecutionException
        executorService.execute(new Task());

        // will return true, since shutdown has begun
        executorService.isShutdown();

        // will return true if all tasks are completed
        executorService.isTerminated();

        //block until all tasks are completed or if timeout occurs
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        //Will initiate shutdown and return all queued tasks

        List<Runnable> runnables=executorService.shutdownNow();

    }
}
