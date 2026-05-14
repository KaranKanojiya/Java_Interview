package interview.level5_java17to21.virtual_threads;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: Virtual vs Platform Threads — Deep Comparison
// =====================================================================
//
// Q: "How do virtual threads map to OS threads?"
// A: "Virtual threads are scheduled by the JVM's ForkJoinPool (the carrier
//     pool). When a virtual thread blocks (I/O, sleep, lock), it is
//     unmounted from its carrier OS thread, freeing it for another virtual
//     thread. When the blocking operation completes, the virtual thread is
//     re-mounted onto any available carrier."
//
// Q: "What is thread pinning?"
// A: "A virtual thread is 'pinned' to its carrier when it blocks inside a
//     synchronized block/method or during native/JNI code. While pinned,
//     the carrier thread is occupied and cannot run other virtual threads.
//     This defeats the purpose of virtual threads."
//
// Q: "How do you avoid pinning?"
// A: "Replace synchronized blocks with java.util.concurrent.locks.ReentrantLock.
//     ReentrantLock is virtual-thread-friendly: the JVM can unmount the
//     virtual thread while it waits for the lock."
//
// Q: "How do you detect pinning?"
// A: "Use -Djdk.tracePinnedThreads=short (or =full) JVM flag. It prints
//     a stack trace whenever a virtual thread is pinned."
//
// COMPILE: javac VirtualVsPlatformComparison.java
// RUN:     java -Djdk.tracePinnedThreads=short VirtualVsPlatformComparison
// =====================================================================

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class VirtualVsPlatformComparison {

    private static final int THREAD_COUNT = 10_000;

    // ---------------------------------------------------------------
    // 1. Memory & time: 10K platform vs 10K virtual threads
    // ---------------------------------------------------------------
    static void memoryAndTimeComparison() throws Exception {
        System.out.println("=== 1. Memory & Time: 10K Platform vs 10K Virtual ===\n");

        // --- Platform threads ---
        System.gc();
        long memBefore = usedMemoryMB();
        Instant pStart = Instant.now();

        Thread[] platformThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            platformThreads[i] = Thread.ofPlatform()
                    .name("platform-" + i)
                    .start(() -> {
                        try { Thread.sleep(100); }
                        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    });
        }
        long memAfterCreate = usedMemoryMB();
        for (Thread t : platformThreads) t.join();
        long pMillis = Duration.between(pStart, Instant.now()).toMillis();

        System.out.println("  Platform Threads (" + THREAD_COUNT + "):");
        System.out.println("    Time:           " + pMillis + " ms");
        System.out.println("    Memory (approx): ~" + (memAfterCreate - memBefore) + " MB used during creation");

        // --- Virtual threads ---
        System.gc();
        memBefore = usedMemoryMB();
        Instant vStart = Instant.now();

        Thread[] virtualThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            virtualThreads[i] = Thread.ofVirtual()
                    .name("virtual-" + i)
                    .start(() -> {
                        try { Thread.sleep(100); }
                        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    });
        }
        long vMemAfterCreate = usedMemoryMB();
        for (Thread t : virtualThreads) t.join();
        long vMillis = Duration.between(vStart, Instant.now()).toMillis();

        System.out.println("\n  Virtual Threads (" + THREAD_COUNT + "):");
        System.out.println("    Time:           " + vMillis + " ms");
        System.out.println("    Memory (approx): ~" + (vMemAfterCreate - memBefore) + " MB used during creation");
        System.out.println("\n  Key takeaway: Virtual threads use orders of magnitude less memory");
        System.out.println("  because they don't allocate a fixed ~1MB OS thread stack.\n");
    }

    // ---------------------------------------------------------------
    // 2. Pinning demo — synchronized vs ReentrantLock
    // ---------------------------------------------------------------
    // Shared resource protected by synchronized (causes pinning)
    private static final Object SYNC_LOCK = new Object();

    // Shared resource protected by ReentrantLock (no pinning)
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    static void pinningDemo() throws Exception {
        System.out.println("=== 2. Pinning Demo: synchronized vs ReentrantLock ===\n");

        // ----- BAD: synchronized pins virtual threads to carrier -----
        System.out.println("  [BAD] synchronized block — virtual thread PINNED to carrier:");
        System.out.println("  (Run with -Djdk.tracePinnedThreads=short to see pinning warnings)\n");

        Instant syncStart = Instant.now();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 100).forEach(i -> exec.submit(() -> {
                synchronized (SYNC_LOCK) {
                    // While in synchronized, the virtual thread is pinned:
                    // the carrier OS thread CANNOT serve other virtual threads.
                    try { Thread.sleep(1); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }));
        }
        long syncMs = Duration.between(syncStart, Instant.now()).toMillis();

        // ----- GOOD: ReentrantLock allows unmounting -----
        System.out.println("  [GOOD] ReentrantLock — virtual thread CAN unmount:");

        Instant lockStart = Instant.now();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 100).forEach(i -> exec.submit(() -> {
                REENTRANT_LOCK.lock();
                try {
                    // Virtual thread can unmount while waiting for the lock
                    // and while sleeping — carrier is free for other VTs.
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    REENTRANT_LOCK.unlock();
                }
            }));
        }
        long lockMs = Duration.between(lockStart, Instant.now()).toMillis();

        System.out.println("\n  synchronized time: " + syncMs + " ms");
        System.out.println("  ReentrantLock time: " + lockMs + " ms");
        System.out.println("  → With pinning, virtual threads lose their advantage.\n");
    }

    // ---------------------------------------------------------------
    // 3. Executor comparison — fixed pool vs virtual-per-task
    // ---------------------------------------------------------------
    static void executorComparison() throws Exception {
        System.out.println("=== 3. Executor Comparison: Fixed Pool vs Virtual-Per-Task ===\n");

        int taskCount = 5_000;

        // Fixed thread pool — limited concurrency
        Instant fixedStart = Instant.now();
        try (ExecutorService exec = Executors.newFixedThreadPool(200)) {
            for (int i = 0; i < taskCount; i++) {
                exec.submit(() -> {
                    try { Thread.sleep(10); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
            }
        }
        long fixedMs = Duration.between(fixedStart, Instant.now()).toMillis();

        // Virtual thread per task — unbounded concurrency
        Instant vtStart = Instant.now();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                exec.submit(() -> {
                    try { Thread.sleep(10); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
            }
        }
        long vtMs = Duration.between(vtStart, Instant.now()).toMillis();

        System.out.println("  " + taskCount + " tasks (each sleeps 10ms):");
        System.out.println("    FixedThreadPool(200): " + fixedMs + " ms");
        System.out.println("    VirtualPerTask:       " + vtMs + " ms");
        System.out.println("  → Virtual threads achieve higher throughput for I/O-bound work.\n");
    }

    // ---------------------------------------------------------------
    // 4. Best practices summary
    // ---------------------------------------------------------------
    static void bestPractices() {
        System.out.println("=== 4. Best Practices Summary ===\n");
        System.out.println("  DO:");
        System.out.println("    - Use virtual threads for I/O-bound workloads (HTTP, DB, file I/O)");
        System.out.println("    - Use Executors.newVirtualThreadPerTaskExecutor()");
        System.out.println("    - Replace synchronized with ReentrantLock");
        System.out.println("    - Create a new virtual thread per task (no pooling needed)");
        System.out.println();
        System.out.println("  DON'T:");
        System.out.println("    - Pool virtual threads (they are cheap to create)");
        System.out.println("    - Use virtual threads for CPU-bound work");
        System.out.println("    - Store large objects in ThreadLocal (millions of VTs = OOM)");
        System.out.println("    - Use synchronized for long-held locks with virtual threads");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------
    private static long usedMemoryMB() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   Virtual vs Platform Threads — Deep Comparison     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        memoryAndTimeComparison();
        pinningDemo();
        executorComparison();
        bestPractices();

        System.out.println("=== Done ===");
    }
}
