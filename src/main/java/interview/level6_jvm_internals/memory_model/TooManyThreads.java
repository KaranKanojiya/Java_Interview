package interview.level6_jvm_internals.memory_model;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Q15. What is the impact of too many threads on JVM performance?
 *
 * Each thread consumes:
 *   - Stack memory: default 512KB-1MB per thread (-Xss)
 *     → 1000 threads × 1MB = ~1GB just for stacks!
 *   - OS resources: native thread, file descriptors
 *   - CPU context switching overhead
 *
 * Problems with too many threads:
 *
 *   1. Memory exhaustion:
 *      - OOM: "unable to create native thread"
 *      - Each thread's stack is outside the heap (native memory)
 *      - Formula: max threads ≈ (OS memory - heap - metaspace) / stack_size
 *
 *   2. Context switching overhead:
 *      - More threads than CPU cores → OS must context switch
 *      - Each switch: save/restore registers, flush caches
 *      - Throughput DECREASES beyond optimal thread count
 *
 *   3. Lock contention:
 *      - More threads contending for same locks → more blocking
 *      - Amdahl's Law: speedup limited by serial portion of code
 *
 *   4. GC pressure:
 *      - More threads = more allocation = more frequent GC
 *      - GC must scan all thread stacks as GC roots
 *
 * Solutions:
 *   - Use thread pools (ExecutorService) — bounded thread count
 *   - Virtual Threads (Java 21) — lightweight, 1000s of concurrent tasks
 *   - Reactive/async programming — fewer threads, non-blocking I/O
 *   - Reduce -Xss if default stack is too large for your workload
 *
 * Optimal thread count guidelines:
 *   CPU-bound: threads = number of CPU cores
 *   I/O-bound: threads = cores × (1 + wait_time/compute_time)
 */
public class TooManyThreads {

    public static void main(String[] args) throws InterruptedException {

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Runtime rt = Runtime.getRuntime();

        System.out.println("=== System Info ===");
        System.out.println("Available processors: " + rt.availableProcessors());
        System.out.println("Max memory: " + (rt.maxMemory() / 1024 / 1024) + " MB");
        System.out.println("Initial threads: " + threadBean.getThreadCount());

        // === Demo: Creating many threads ===
        System.out.println("\n=== Creating threads (watch memory & count) ===");
        List<Thread> threads = new ArrayList<>();
        CountDownLatch keepAlive = new CountDownLatch(1);

        int targetCount = 200;  // safe number for demo
        for (int i = 0; i < targetCount; i++) {
            Thread t = new Thread(() -> {
                try { keepAlive.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "Worker-" + i);
            t.setDaemon(true);
            t.start();
            threads.add(t);

            if ((i + 1) % 50 == 0) {
                long usedMemory = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
                System.out.printf("  Threads: %d | Live: %d | Memory used: %d MB%n",
                        i + 1, threadBean.getThreadCount(), usedMemory);
            }
        }

        // === Context switching demo ===
        System.out.println("\n=== Context Switching Impact ===");
        int cores = rt.availableProcessors();

        // Optimal: threads == cores
        long optimalTime = benchmarkWork(cores, 1_000_000);
        System.out.println("Threads = cores (" + cores + "):   " + optimalTime + "ms");

        // Too many: threads >> cores
        long excessiveTime = benchmarkWork(cores * 10, 1_000_000);
        System.out.println("Threads = 10×cores (" + (cores * 10) + "): " + excessiveTime + "ms");

        // Clean up
        keepAlive.countDown();
        for (Thread t : threads) t.join(100);

        // === Memory calculation ===
        System.out.println("\n=== Thread Memory Calculation ===");
        System.out.println("Default stack size (-Xss): ~1MB");
        System.out.println("100 threads  → ~100MB  stack memory");
        System.out.println("1000 threads → ~1GB    stack memory");
        System.out.println("10K threads  → ~10GB   stack memory (probably OOM!)");
        System.out.println("\nMax threads ≈ (OS memory - Xmx - metaspace) / Xss");

        // === Solutions ===
        System.out.println("\n=== Solutions ===");
        System.out.println("1. Thread pools:     Executors.newFixedThreadPool(cores)");
        System.out.println("2. Virtual threads:  Thread.ofVirtual().start(task) (Java 21+)");
        System.out.println("3. Reduce stack:     -Xss256k (if code doesn't need deep recursion)");
        System.out.println("4. Async/reactive:   CompletableFuture, WebFlux, Vert.x");

        System.out.println("\n=== Optimal Thread Count ===");
        System.out.println("CPU-bound: " + cores + " threads (= available cores)");
        System.out.println("I/O-bound: " + (cores * 2) + "-" + (cores * 10)
                + " threads (cores × (1 + wait/compute ratio))");
    }

    static long benchmarkWork(int threadCount, int workPerThread) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(threadCount);
        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                long sum = 0;
                for (int j = 0; j < workPerThread; j++) sum += j;
                done.countDown();
            });
            t.setDaemon(true);
            t.start();
        }
        done.await();
        return System.currentTimeMillis() - start;
    }
}
