package interview.level5_java17to21.structured_concurrency;

import java.util.concurrent.StructuredTaskScope;

/**
 * Q11. What is the difference between ShutdownOnFailure and ShutdownOnSuccess?
 *
 * StructuredTaskScope policies:
 *
 * ShutdownOnFailure:
 *   - "I need ALL tasks to succeed"
 *   - If ANY task fails → cancels remaining tasks immediately
 *   - Use case: fetch user + orders + preferences (all required)
 *   - After join: call throwIfFailed() to propagate the first exception
 *
 * ShutdownOnSuccess:
 *   - "I need just ONE task to succeed"
 *   - As soon as ANY task succeeds → cancels remaining tasks
 *   - Use case: query multiple replicas, fastest wins
 *   - After join: call result() to get the first successful result
 *
 * Both ensure:
 *   - All forked tasks complete (or are cancelled) before scope exits
 *   - No task is left running after the try-with-resources block
 *   - Parent thread owns the lifecycle of child tasks
 */
public class ShutdownPolicies {

    public static void main(String[] args) throws Exception {

        // === ShutdownOnFailure: ALL must succeed ===
        System.out.println("=== ShutdownOnFailure (all must succeed) ===");
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var userTask = scope.fork(() -> {
                Thread.sleep(100);
                System.out.println("  Fetched user");
                return "User:Karan";
            });
            var orderTask = scope.fork(() -> {
                Thread.sleep(200);
                System.out.println("  Fetched orders");
                return "Orders:[A,B,C]";
            });

            scope.join();           // wait for all
            scope.throwIfFailed();  // throws if any task failed

            System.out.println("User: " + userTask.get());
            System.out.println("Orders: " + orderTask.get());
        }

        // === ShutdownOnFailure: when one fails ===
        System.out.println("\n=== ShutdownOnFailure (one fails → all cancelled) ===");
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var good = scope.fork(() -> {
                Thread.sleep(500);  // slow — will be cancelled
                System.out.println("  This should NOT print (cancelled)");
                return "good";
            });
            var bad = scope.fork(() -> {
                Thread.sleep(50);
                throw new RuntimeException("DB connection failed!");
            });

            scope.join();
            try {
                scope.throwIfFailed();
            } catch (Exception e) {
                System.out.println("Caught failure: " + e.getMessage());
                System.out.println("Good task was cancelled before completion");
            }
        }

        // === ShutdownOnSuccess: first wins ===
        System.out.println("\n=== ShutdownOnSuccess (first to succeed wins) ===");
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            scope.fork(() -> {
                Thread.sleep(300);
                System.out.println("  Replica-1 responded (slow)");
                return "Result from Replica-1";
            });
            scope.fork(() -> {
                Thread.sleep(100);
                System.out.println("  Replica-2 responded (fast)");
                return "Result from Replica-2";
            });
            scope.fork(() -> {
                Thread.sleep(200);
                System.out.println("  Replica-3 responded (medium)");
                return "Result from Replica-3";
            });

            scope.join();
            String fastest = scope.result();  // returns first successful result
            System.out.println("Winner: " + fastest);
            // Other tasks are automatically cancelled
        }

        System.out.println("\n=== Summary ===");
        System.out.println("ShutdownOnFailure:");
        System.out.println("  → All tasks must succeed");
        System.out.println("  → Any failure cancels the rest");
        System.out.println("  → Use: parallel fetch of required data");
        System.out.println("\nShutdownOnSuccess:");
        System.out.println("  → First success wins");
        System.out.println("  → Cancels remaining after first success");
        System.out.println("  → Use: racing replicas, hedged requests");
    }
}
