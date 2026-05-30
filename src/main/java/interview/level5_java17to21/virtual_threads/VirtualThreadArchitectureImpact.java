package interview.level5_java17to21.virtual_threads;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Q9. How do virtual threads impact application architecture?
 *
 * Traditional (platform threads):
 *   - Thread-per-request model limited to ~200-2000 concurrent requests
 *   - Led to reactive/async frameworks (WebFlux, Vert.x) for scalability
 *   - Complex code: callbacks, Mono/Flux, non-blocking I/O
 *
 * With virtual threads (Java 21):
 *   - Thread-per-request scales to 100K+ concurrent requests
 *   - Simple blocking code performs like async code
 *   - No need for reactive frameworks for I/O-bound workloads
 *
 * Architecture changes:
 *   1. Remove thread pools for I/O tasks → use newVirtualThreadPerTaskExecutor()
 *   2. Keep thread pools for CPU-bound tasks (virtual threads don't help here)
 *   3. Remove reactive frameworks if only used for scalability (not for streaming)
 *   4. Connection pools become the bottleneck (not threads)
 *   5. ThreadLocal → ScopedValue (virtual threads are cheap, but ThreadLocal is not)
 *
 * What NOT to change:
 *   - CPU-bound work still needs fixed thread pools (= cores)
 *   - Connection pools still needed (DB connections are expensive)
 *   - Thread-safety rules still apply (synchronized, volatile, etc.)
 */
public class VirtualThreadArchitectureImpact {

    public static void main(String[] args) throws Exception {

        // === Before: Limited by platform threads ===
        System.out.println("=== Platform threads: limited scalability ===");
        var platformExecutor = Executors.newFixedThreadPool(10);  // max 10 concurrent
        AtomicInteger platformCount = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            platformExecutor.submit(() -> {
                try { Thread.sleep(100); } catch (InterruptedException e) { }
                platformCount.incrementAndGet();
            });
        }
        platformExecutor.shutdown();
        platformExecutor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
        System.out.println("100 tasks, 10 platform threads: " + (System.currentTimeMillis() - start) + "ms");

        // === After: Virtual threads scale without limits ===
        System.out.println("\n=== Virtual threads: massive scalability ===");
        AtomicInteger virtualCount = new AtomicInteger(0);
        start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> {
                    try { Thread.sleep(100); } catch (InterruptedException e) { }
                    virtualCount.incrementAndGet();
                });
            }
        }  // auto-shutdown, waits for all tasks
        System.out.println("100 tasks, virtual threads: " + (System.currentTimeMillis() - start) + "ms");

        // === Scalability demo: 10K concurrent tasks ===
        System.out.println("\n=== 10K concurrent virtual threads ===");
        AtomicInteger counter = new AtomicInteger(0);
        start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    try { Thread.sleep(100); } catch (InterruptedException e) { }
                    counter.incrementAndGet();
                });
            }
        }
        System.out.println("10K tasks completed: " + counter.get() + " in " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("(Platform threads would need 10K threads or take 100x longer!)");

        // === Architecture decision guide ===
        System.out.println("\n=== When to use virtual threads ===");
        System.out.println("✅ I/O-bound: HTTP calls, DB queries, file I/O");
        System.out.println("✅ High-concurrency servers: web servers, API gateways");
        System.out.println("✅ Replace: CompletableFuture chains for I/O tasks");
        System.out.println("✅ Replace: Reactive frameworks (if used only for scalability)");

        System.out.println("\n❌ Not for CPU-bound: image processing, encryption, computation");
        System.out.println("❌ Not for: tasks with heavy synchronized blocks (pinning)");
        System.out.println("❌ Not for: replacing connection pools (DB connections are finite)");

        System.out.println("\n=== Migration strategy ===");
        System.out.println("1. Replace Executors.newFixedThreadPool() with newVirtualThreadPerTaskExecutor()");
        System.out.println("2. Replace synchronized with ReentrantLock (avoid pinning)");
        System.out.println("3. Replace ThreadLocal with ScopedValue");
        System.out.println("4. Keep connection pools (HikariCP) — they limit DB connections, not threads");
        System.out.println("5. Monitor with -Djdk.tracePinnedThreads=short");
    }
}
