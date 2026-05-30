package interview.level3_multithreading.basics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Q25. What is the difference between Callable and Future?
 *
 * Callable<V>:
 *   - Represents a TASK that returns a result and can throw exceptions
 *   - Has single method: V call() throws Exception
 *   - Submitted to ExecutorService → returns a Future
 *
 * Future<V>:
 *   - Represents the RESULT of an async computation
 *   - Returned by ExecutorService.submit(Callable)
 *   - Key methods:
 *       get()               → blocks until result is ready, returns V
 *       get(timeout, unit)  → blocks with timeout, throws TimeoutException
 *       isDone()            → true if task completed (normally, exception, or cancelled)
 *       isCancelled()       → true if task was cancelled
 *       cancel(boolean)     → attempt to cancel the task
 *
 * Callable = the task definition (what to do)
 * Future   = the result handle (how to get the result)
 *
 * Limitations of Future (fixed by CompletableFuture):
 *   ❌ Cannot chain callbacks (no thenApply)
 *   ❌ Cannot combine multiple futures
 *   ❌ get() is blocking — no async notification
 *   ❌ Cannot manually complete
 */
public class CallableAndFuture {

    public static void main(String[] args) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // === Basic Callable + Future ===
        System.out.println("=== Callable returns Future ===");
        Callable<Integer> task = () -> {
            System.out.println("  Computing on: " + Thread.currentThread().getName());
            Thread.sleep(500);
            return 42;
        };

        Future<Integer> future = executor.submit(task);
        System.out.println("isDone before get(): " + future.isDone());

        Integer result = future.get();  // blocks until done
        System.out.println("isDone after get(): " + future.isDone());
        System.out.println("Result: " + result);

        // === Future.get() with timeout ===
        System.out.println("\n=== Future.get() with timeout ===");
        Future<String> slowTask = executor.submit(() -> {
            Thread.sleep(3000);
            return "Slow result";
        });

        try {
            String r = slowTask.get(1, TimeUnit.SECONDS);  // wait max 1 second
        } catch (TimeoutException e) {
            System.out.println("Timeout! Task not done in 1 second");
            slowTask.cancel(true);
        }

        // === Exception handling ===
        System.out.println("\n=== Exception in Callable ===");
        Future<Integer> failingFuture = executor.submit(() -> {
            throw new IllegalArgumentException("Invalid input!");
        });

        try {
            failingFuture.get();
        } catch (ExecutionException e) {
            System.out.println("ExecutionException wraps: " + e.getCause().getClass().getSimpleName()
                    + " — " + e.getCause().getMessage());
        }

        // === Multiple Callables with invokeAll ===
        System.out.println("\n=== invokeAll — submit multiple Callables ===");
        List<Callable<String>> tasks = List.of(
                () -> { Thread.sleep(300); return "Task-A"; },
                () -> { Thread.sleep(100); return "Task-B"; },
                () -> { Thread.sleep(200); return "Task-C"; }
        );

        List<Future<String>> futures = executor.invokeAll(tasks);  // blocks until ALL done
        for (Future<String> f : futures) {
            System.out.println("  Result: " + f.get());
        }

        // === invokeAny — returns first completed result ===
        System.out.println("\n=== invokeAny — first to complete wins ===");
        String firstResult = executor.invokeAny(tasks);
        System.out.println("First completed: " + firstResult);  // Task-B (fastest)

        // === Polling with isDone() (avoid blocking) ===
        System.out.println("\n=== Polling with isDone() ===");
        Future<String> polled = executor.submit(() -> {
            Thread.sleep(300);
            return "Polled result";
        });

        while (!polled.isDone()) {
            System.out.println("  Waiting...");
            Thread.sleep(100);
        }
        System.out.println("Polled result: " + polled.get());

        executor.shutdown();

        System.out.println("\n=== Summary ===");
        System.out.println("Callable: defines the task (what to compute)");
        System.out.println("Future:   holds the result (how to retrieve it)");
        System.out.println("get():    blocks until result ready");
        System.out.println("cancel(): attempts to stop the task");
        System.out.println("For non-blocking chaining → use CompletableFuture");
    }
}
