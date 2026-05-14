package interview.level5_java17to21.structured_concurrency;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: Structured Concurrency — Java 21 (Preview)
// =====================================================================
//
// Q: "What is structured concurrency?"
// A: "Structured concurrency treats concurrent tasks as a unit of work.
//     Child tasks are bound to a parent scope — if the parent fails or
//     is cancelled, all children are automatically cancelled. This
//     prevents thread leaks and makes error handling deterministic.
//     It is analogous to structured programming (no goto → no dangling
//     threads)."
//
// Q: "How does StructuredTaskScope differ from CompletableFuture?"
// A: "CompletableFuture is unstructured: if you compose futures with
//     thenApply/thenCombine, a failure in one doesn't automatically
//     cancel siblings. With StructuredTaskScope.ShutdownOnFailure,
//     if ANY subtask fails, all others are interrupted and the scope
//     throws the exception. No orphaned work."
//
// Q: "What are the two built-in policies?"
// A: "1) ShutdownOnFailure — all children must succeed; first failure
//        cancels the rest and propagates the exception.
//     2) ShutdownOnSuccess — returns as soon as ANY child succeeds;
//        cancels the rest. Useful for hedged/racing requests."
//
// Q: "Why is this a preview feature?"
// A: "The API is still being refined. It requires --enable-preview to
//     compile and run. It may change or be removed in future releases."
//
// COMPILE: javac --enable-preview --source 21 StructuredConcurrencyDemo.java
// RUN:     java --enable-preview StructuredConcurrencyDemo
//
// NOTE: StructuredTaskScope is in java.util.concurrent (preview).
//       If not available, the simulation section below shows the concept.
// =====================================================================

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

public class StructuredConcurrencyDemo {

    // ---------------------------------------------------------------
    // Simulated services (I/O-bound)
    // ---------------------------------------------------------------
    record User(String name, int id) {}
    record Order(String orderId, int userId) {}
    record Recommendation(String item, int userId) {}

    static User fetchUser(int userId) throws InterruptedException {
        Thread.sleep(200); // simulate DB call
        return new User("Karan", userId);
    }

    static Order fetchOrder(int userId) throws InterruptedException {
        Thread.sleep(300); // simulate service call
        return new Order("ORD-42", userId);
    }

    static Recommendation fetchRecommendation(int userId) throws InterruptedException {
        Thread.sleep(150); // simulate ML service
        return new Recommendation("Java 21 in Action", userId);
    }

    static Order fetchOrderThatFails(int userId) throws InterruptedException {
        Thread.sleep(100);
        throw new RuntimeException("Order service unavailable!");
    }

    // ---------------------------------------------------------------
    // 1. Structured concurrency with StructuredTaskScope (Preview API)
    // ---------------------------------------------------------------
    static void structuredTaskScopeDemo() throws Exception {
        System.out.println("=== 1. StructuredTaskScope.ShutdownOnFailure ===\n");

        // NOTE: This uses preview API. If StructuredTaskScope is not available
        // at compile time, see the simulation in section 3 below.
        //
        // The following code compiles with --enable-preview on Java 21+.
        // If your IDE/build tool doesn't support preview, the concept is
        // demonstrated in the simulation section.

        try {
            structuredShutdownOnFailureDemo();
        } catch (NoClassDefFoundError | UnsupportedOperationException e) {
            System.out.println("  StructuredTaskScope not available in this runtime.");
            System.out.println("  See simulation in section 3.\n");
        }
    }

    @SuppressWarnings("preview")
    private static void structuredShutdownOnFailureDemo() throws Exception {
        int userId = 1;
        Instant start = Instant.now();

        // All subtasks are children of this scope.
        // If any fails → all others are cancelled.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // Fork child tasks (each runs in its own virtual thread)
            StructuredTaskScope.Subtask<User> userTask =
                    scope.fork(() -> fetchUser(userId));
            StructuredTaskScope.Subtask<Order> orderTask =
                    scope.fork(() -> fetchOrder(userId));
            StructuredTaskScope.Subtask<Recommendation> recTask =
                    scope.fork(() -> fetchRecommendation(userId));

            // Wait for all to complete (or one to fail)
            scope.join();

            // Propagate exception if any subtask failed
            scope.throwIfFailed();

            // All succeeded — get results
            User user = userTask.get();
            Order order = orderTask.get();
            Recommendation rec = recTask.get();

            long elapsed = Duration.between(start, Instant.now()).toMillis();

            System.out.println("  User:           " + user);
            System.out.println("  Order:          " + order);
            System.out.println("  Recommendation: " + rec);
            System.out.println("  Total time:     " + elapsed + " ms (parallel, not 200+300+150=650ms)");
            System.out.println();
        }
    }

    // ---------------------------------------------------------------
    // 2. ShutdownOnSuccess — racing / hedging
    // ---------------------------------------------------------------
    static void shutdownOnSuccessDemo() throws Exception {
        System.out.println("=== 2. StructuredTaskScope.ShutdownOnSuccess ===\n");

        try {
            shutdownOnSuccessInternal();
        } catch (NoClassDefFoundError | UnsupportedOperationException e) {
            System.out.println("  StructuredTaskScope not available. See simulation.\n");
        }
    }

    @SuppressWarnings("preview")
    private static void shutdownOnSuccessInternal() throws Exception {
        // Race two "mirrors" — first response wins, others are cancelled
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {

            scope.fork(() -> {
                Thread.sleep(500); // slow mirror
                return "Result from Mirror-A (500ms)";
            });
            scope.fork(() -> {
                Thread.sleep(100); // fast mirror
                return "Result from Mirror-B (100ms)";
            });
            scope.fork(() -> {
                Thread.sleep(300); // medium mirror
                return "Result from Mirror-C (300ms)";
            });

            scope.join();

            String fastest = (String) scope.result();
            System.out.println("  Winner: " + fastest);
            System.out.println("  → Other tasks were cancelled automatically.\n");
        }
    }

    // ---------------------------------------------------------------
    // 3. Simulation of structured concurrency concept using
    //    CompletableFuture (for runtimes without preview API)
    // ---------------------------------------------------------------
    static void simulatedStructuredConcurrency() throws Exception {
        System.out.println("=== 3. Simulated Structured Concurrency (no preview needed) ===\n");
        System.out.println("  Concept: parent scope manages child tasks as a unit.\n");

        int userId = 1;
        Instant start = Instant.now();

        // Using CompletableFuture with virtual thread executor
        try (ExecutorService vtExec = Executors.newVirtualThreadPerTaskExecutor()) {

            CompletableFuture<User> userFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try { return fetchUser(userId); }
                        catch (InterruptedException e) { throw new CompletionException(e); }
                    }, vtExec);

            CompletableFuture<Order> orderFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try { return fetchOrder(userId); }
                        catch (InterruptedException e) { throw new CompletionException(e); }
                    }, vtExec);

            CompletableFuture<Recommendation> recFuture =
                    CompletableFuture.supplyAsync(() -> {
                        try { return fetchRecommendation(userId); }
                        catch (InterruptedException e) { throw new CompletionException(e); }
                    }, vtExec);

            // Wait for all — but if one fails, others keep running (UNSTRUCTURED!)
            CompletableFuture.allOf(userFuture, orderFuture, recFuture).join();

            long elapsed = Duration.between(start, Instant.now()).toMillis();

            System.out.println("  User:           " + userFuture.get());
            System.out.println("  Order:          " + orderFuture.get());
            System.out.println("  Recommendation: " + recFuture.get());
            System.out.println("  Total time:     " + elapsed + " ms");
            System.out.println();
            System.out.println("  PROBLEM with CompletableFuture (unstructured):");
            System.out.println("    - If orderFuture fails, userFuture and recFuture keep running.");
            System.out.println("    - No automatic cancellation of siblings.");
            System.out.println("    - Thread leaks if you forget to cancel.");
            System.out.println("    → StructuredTaskScope fixes all of these.\n");
        }
    }

    // ---------------------------------------------------------------
    // 4. Error handling comparison
    // ---------------------------------------------------------------
    static void errorHandlingComparison() throws Exception {
        System.out.println("=== 4. Error Handling: Structured vs Unstructured ===\n");

        // --- Structured (simulated): if one fails, cancel all ---
        System.out.println("  [Structured] ShutdownOnFailure behavior:");
        try {
            errorHandlingStructured();
        } catch (NoClassDefFoundError | UnsupportedOperationException e) {
            // Simulation fallback
            System.out.println("    (Simulated) Order service fails → scope shuts down.");
            System.out.println("    All sibling tasks are interrupted automatically.");
            System.out.println("    Exception propagated to caller cleanly.\n");
        }

        // --- Unstructured: CompletableFuture doesn't cancel siblings ---
        System.out.println("  [Unstructured] CompletableFuture behavior:");
        System.out.println("    Order service fails → other futures KEEP RUNNING.");
        System.out.println("    Must manually cancel each future.");
        System.out.println("    Risk of thread leaks and wasted resources.\n");
    }

    @SuppressWarnings("preview")
    private static void errorHandlingStructured() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> fetchUser(1));
            scope.fork(() -> fetchOrderThatFails(1));     // this will fail
            scope.fork(() -> fetchRecommendation(1));

            scope.join();
            scope.throwIfFailed(); // throws the exception from fetchOrderThatFails
        } catch (ExecutionException e) {
            System.out.println("    Caught: " + e.getCause().getMessage());
            System.out.println("    All sibling tasks were cancelled automatically.\n");
        }
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   Structured Concurrency Demo — Java 21 (Preview)   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        structuredTaskScopeDemo();
        shutdownOnSuccessDemo();
        simulatedStructuredConcurrency();
        errorHandlingComparison();

        System.out.println("=== Done ===");
    }
}
