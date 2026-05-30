package interview.level3_multithreading.executors;

import java.util.concurrent.*;

/**
 * Q22. What is the difference between submit() and execute()?
 *
 * | Feature         | execute(Runnable)         | submit(Runnable/Callable)         |
 * |----------------|---------------------------|-----------------------------------|
 * | Defined in     | Executor                  | ExecutorService                   |
 * | Return type    | void                      | Future<?> or Future<V>            |
 * | Exception      | Throws to UncaughtHandler | Wrapped in ExecutionException     |
 * | Accepts        | Runnable only             | Runnable or Callable              |
 * | Cancel task    | Cannot                    | Can via Future.cancel()           |
 *
 * Key: submit() = execute() + return Future + swallow exceptions
 *
 * DANGER: submit() silently swallows exceptions unless you call Future.get()!
 * This is a common production bug — tasks fail silently.
 */
public class SubmitVsExecute {

    public static void main(String[] args) throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // === execute() — fire and forget, exceptions propagate ===
        System.out.println("=== execute() ===");
        executor.execute(() -> {
            System.out.println("execute(): Running on " + Thread.currentThread().getName());
        });

        // execute() with exception — prints stack trace to stderr
        System.out.println("\n=== execute() with exception ===");
        executor.execute(() -> {
            System.out.println("execute(): About to throw...");
            throw new RuntimeException("Boom from execute!");
            // Exception goes to UncaughtExceptionHandler → visible in console
        });
        Thread.sleep(200);  // let it complete

        // === submit() — returns Future, can get result ===
        System.out.println("\n=== submit(Runnable) ===");
        Future<?> runnableFuture = executor.submit(() -> {
            System.out.println("submit(Runnable): Running on " + Thread.currentThread().getName());
        });
        try {
            Object result = runnableFuture.get();  // returns null for Runnable
            System.out.println("submit(Runnable) result: " + result);
        } catch (ExecutionException e) {
            System.out.println("Error: " + e.getCause());
        }

        // === submit(Callable) — returns Future with value ===
        System.out.println("\n=== submit(Callable) ===");
        Future<String> callableFuture = executor.submit(() -> {
            System.out.println("submit(Callable): Running on " + Thread.currentThread().getName());
            return "Hello from Callable!";
        });
        try {
            String value = callableFuture.get();
            System.out.println("submit(Callable) result: " + value);
        } catch (ExecutionException e) {
            System.out.println("Error: " + e.getCause());
        }

        // === DANGER: submit() swallows exceptions silently ===
        System.out.println("\n=== DANGER: submit() silently swallows exceptions ===");
        Future<?> silentFail = executor.submit(() -> {
            System.out.println("submit(): About to throw...");
            throw new RuntimeException("Boom from submit!");
            // This exception is SILENT unless you call .get()
        });
        Thread.sleep(200);
        System.out.println("No exception visible yet! Must call Future.get() to see it:");

        try {
            silentFail.get();  // NOW the exception surfaces
        } catch (ExecutionException e) {
            System.out.println("Caught via Future.get(): " + e.getCause().getMessage());
        }

        // === Future.cancel() — only possible with submit() ===
        System.out.println("\n=== Future.cancel() ===");
        Future<?> longTask = executor.submit(() -> {
            try {
                System.out.println("Long task started...");
                Thread.sleep(5000);
                System.out.println("Long task completed");  // won't print
            } catch (InterruptedException e) {
                System.out.println("Long task interrupted!");
            }
        });
        Thread.sleep(100);
        boolean cancelled = longTask.cancel(true);  // mayInterruptIfRunning = true
        System.out.println("Cancelled: " + cancelled);
        System.out.println("isCancelled: " + longTask.isCancelled());

        // === submit(Runnable, result) — returns a pre-defined result ===
        System.out.println("\n=== submit(Runnable, result) ===");
        Future<String> withResult = executor.submit(() -> {
            System.out.println("Running task...");
        }, "task-completed");
        try {
            System.out.println("Result: " + withResult.get());  // "task-completed"
        } catch (ExecutionException e) {
            System.out.println("Error: " + e.getCause());
        }

        executor.shutdown();

        System.out.println("\n=== Summary ===");
        System.out.println("execute(): simple, exceptions visible, no return value");
        System.out.println("submit():  returns Future, exceptions HIDDEN until .get()");
        System.out.println("TIP: Always call Future.get() or handle exceptions in the task itself!");
    }
}
