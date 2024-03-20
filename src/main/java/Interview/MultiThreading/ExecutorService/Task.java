package Interview.MultiThreading.ExecutorService;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Task implements Runnable {
    @Override
    public void run() {
        System.out.println("Thread Name " + Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        System.out.println("Using executorService : newFixedThreadPool");

        //submit the task for execution
        for (int i=0;i<2;i++){
            executorService.execute(new Task());
        }

        System.out.println("Initialize Thread Pool as per available Processors");
        //get count of available cores
        int count=Runtime.getRuntime().availableProcessors();
        ExecutorService executorServiceWithCpuCount = Executors.newFixedThreadPool(count);
        executorServiceWithCpuCount.execute(new Task());

    }
}