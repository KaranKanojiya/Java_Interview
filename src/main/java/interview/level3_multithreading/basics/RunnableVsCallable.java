package interview.level3_multithreading.basics;

import java.util.concurrent.*;

/**
 * Q2. What is the difference between Runnable and Callable?
 *
 * | Feature      | Runnable               | Callable<V>            |
 * |-------------|------------------------|------------------------|
 * | Method      | void run()             | V call() throws Exception |
 * | Return value| No                     | Yes (returns V)        |
 * | Exception   | Cannot throw checked   | Can throw checked      |
 * | Introduced  | Java 1.0               | Java 5                 |
 * | Used with   | Thread, ExecutorService| ExecutorService only   |
 * | Returns     | void                   | Future<V>              |
 *
 * Key: Callable = Runnable + return value + checked exceptions
 *
 * Use Runnable when: fire-and-forget tasks (logging, notifications)
 * Use Callable when: you need a result or must propagate exceptions
 */
public class RunnableVsCallable {

    public static void main(String[] args) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // === Runnable: no return value, no checked exceptions ===
        System.out.println("=== Runnable ===");
        Runnable runnableTask = () -> {
            System.out.println("Runnable executing on: " + Thread.currentThread().getName());
            // Cannot return a value
            // Cannot throw checked exception (must catch internally)
        };

        // With Thread
        Thread t = new Thread(runnableTask, "MyThread");
        t.start();
        t.join();

        // With ExecutorService — returns Future<?> but get() returns null
        Future<?> runnableFuture = executor.submit(runnableTask);
        Object runnableResult = runnableFuture.get();
        System.out.println("Runnable Future.get(): " + runnableResult);  // null

        // === Callable: has return value, can throw checked exceptions ===
        System.out.println("\n=== Callable ===");
        Callable<Integer> callableTask = () -> {
            System.out.println("Callable executing on: " + Thread.currentThread().getName());
            Thread.sleep(500);  // Can throw checked exceptions!
            return 42;          // Can return a value!
        };

        // Only works with ExecutorService (not Thread directly)
        Future<Integer> callableFuture = executor.submit(callableTask);
        Integer callableResult = callableFuture.get();  // blocks until result ready
        System.out.println("Callable Future.get(): " + callableResult);  // 42

        // === Callable with exception ===
        System.out.println("\n=== Callable with exception ===");
        Callable<String> failingTask = () -> {
            throw new Exception("Something went wrong!");
        };

        Future<String> failFuture = executor.submit(failingTask);
        try {
            failFuture.get();
        } catch (ExecutionException e) {
            System.out.println("Caught: " + e.getCause().getMessage());
        }

        // === Wrapping Runnable as Callable ===
        System.out.println("\n=== Executors.callable() — wrap Runnable ===");
        Callable<Object> wrapped = Executors.callable(runnableTask, "default-result");
        Future<Object> wrappedFuture = executor.submit(wrapped);
        System.out.println("Wrapped result: " + wrappedFuture.get());  // "default-result"

        executor.shutdown();
    }
}
