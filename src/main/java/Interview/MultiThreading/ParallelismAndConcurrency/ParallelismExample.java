package Interview.MultiThreading.ParallelismAndConcurrency;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelismExample {


    static class Task implements Runnable {

        private int taskId;

        public Task(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void run() {
            System.out.println("Task " + taskId + " is running in thread " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {

        for (int i = 1; i <= 5; i++) {
            Thread thread = new Thread(new Task(i));
            thread.start();
        }

        // Create a fixed-size thread pool with 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Submit tasks to the executor
        for (int i = 1; i <= 5; i++) {
            executor.submit(new Task(i));
        }

        // Shutdown the executor to terminate all threads when tasks are completed
        executor.shutdown();

    }
}
