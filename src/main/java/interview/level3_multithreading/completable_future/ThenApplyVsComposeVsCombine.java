package interview.level3_multithreading.completable_future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Q11. What is the difference between thenApply, thenCompose, and thenCombine?
 *
 * thenApply(Function<T,R>):
 *   - Transforms the result synchronously
 *   - Like Stream.map() — one-to-one
 *   - Input: T → Output: CompletableFuture<R>
 *
 * thenCompose(Function<T, CompletableFuture<R>>):
 *   - Chains two async operations sequentially
 *   - Like Stream.flatMap() — avoids nested CompletableFuture<CompletableFuture<R>>
 *   - Input: T → Output: CompletableFuture<R> (flattened)
 *
 * thenCombine(CompletableFuture<U>, BiFunction<T,U,R>):
 *   - Combines results of TWO independent CompletableFutures
 *   - Both run in parallel, result combined when both complete
 *   - Input: T + U → Output: CompletableFuture<R>
 *
 * Summary:
 *   thenApply   = map       (sync transform)
 *   thenCompose = flatMap   (async chain, sequential)
 *   thenCombine = zip/join  (async combine, parallel)
 */
public class ThenApplyVsComposeVsCombine {

    public static void main(String[] args) throws Exception {

        // === thenApply — synchronous transformation ===
        System.out.println("=== thenApply (sync transform, like map) ===");
        CompletableFuture<String> applyResult = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("  Step 1: Fetching user on " + Thread.currentThread().getName());
                    sleep(200);
                    return "Karan";
                })
                .thenApply(name -> {
                    System.out.println("  Step 2: Transforming on " + Thread.currentThread().getName());
                    return "Hello, " + name + "!";  // sync transform, no new async
                });
        System.out.println("Result: " + applyResult.get());

        // === thenCompose — chain async operations (flatMap) ===
        System.out.println("\n=== thenCompose (async chain, like flatMap) ===");
        CompletableFuture<String> composeResult = getUserAsync("karan")
                .thenCompose(user -> getOrderAsync(user));  // returns CF, not CF<CF>
        System.out.println("Result: " + composeResult.get());

        // Without thenCompose — you'd get nested CompletableFuture
        CompletableFuture<CompletableFuture<String>> nested = getUserAsync("karan")
                .thenApply(user -> getOrderAsync(user));  // BAD: CF<CF<String>>
        System.out.println("Nested (wrong): " + nested.get().get());

        // === thenCombine — combine two independent futures ===
        System.out.println("\n=== thenCombine (parallel, combine results) ===");
        long start = System.currentTimeMillis();

        CompletableFuture<String> priceFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Fetching price on " + Thread.currentThread().getName());
            sleep(500);
            return "$100";
        });

        CompletableFuture<String> discountFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Fetching discount on " + Thread.currentThread().getName());
            sleep(400);
            return "20%";
        });

        CompletableFuture<String> combined = priceFuture.thenCombine(
                discountFuture,
                (price, discount) -> "Price: " + price + ", Discount: " + discount
        );
        System.out.println("Result: " + combined.get());
        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms (parallel, ~500ms not 900ms)");

        // === Chaining all three together ===
        System.out.println("\n=== Real-world: chain + combine ===");
        CompletableFuture<String> workflow = getUserAsync("karan")
                .thenCompose(user -> getOrderAsync(user))      // sequential: user → order
                .thenCombine(                                    // parallel: order + discount
                        getDiscountAsync(),
                        (order, discount) -> order + " with " + discount
                )
                .thenApply(result -> result.toUpperCase());     // sync transform
        System.out.println("Workflow: " + workflow.get());
    }

    static CompletableFuture<String> getUserAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "User:" + id;
        });
    }

    static CompletableFuture<String> getOrderAsync(String user) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "Order-for-" + user;
        });
    }

    static CompletableFuture<String> getDiscountAsync() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "15%-off";
        });
    }

    static void sleep(long ms) {
        try { TimeUnit.MILLISECONDS.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
