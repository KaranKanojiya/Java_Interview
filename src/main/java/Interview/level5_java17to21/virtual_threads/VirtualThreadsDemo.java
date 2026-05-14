package interview.level5_java17to21.virtual_threads;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: Virtual Threads (Project Loom) — Java 21
// =====================================================================
//
// Q: "What problem do virtual threads solve?"
// A: "Traditional thread-per-request servers are limited by OS thread count
//     (typically ~2K-10K threads per JVM). Virtual threads decouple the Java
//     thread from the OS thread: the JVM schedules millions of lightweight
//     virtual threads onto a small pool of carrier (platform) threads.
//     This allows the thread-per-request model without the OS thread
//     overhead, enabling 1M+ concurrent connections."
//
// Q: "How are virtual threads different from platform threads?"
// A: "Platform threads are thin wrappers around OS threads (~1MB stack each).
//     Virtual threads are managed by the JVM, have a small initial stack
//     (~few KB), and are mounted/unmounted on carrier threads at blocking
//     points (I/O, sleep, locks). They are cheap to create and destroy —
//     no pooling needed."
//
// Q: "When should you NOT use virtual threads?"
// A: "CPU-bound tasks — virtual threads shine for I/O-bound work. If every
//     thread is doing pure computation, virtual threads add overhead without
//     benefit because they still need carrier threads (which are platform
//     threads) to run. Also avoid them when you rely on ThreadLocal with
//     large per-thread state (memory pressure with millions of threads)."
//
// Q: "Do virtual threads replace thread pools?"
// A: "For I/O-bound work, yes. Instead of a fixed thread pool, you create
//     a new virtual thread per task (Executors.newVirtualThreadPerTaskExecutor).
//     For CPU-bound work, you still want a bounded platform thread pool."
//
// COMPILE: javac VirtualThreadsDemo.java
// RUN:     java VirtualThreadsDemo
// =====================================================================

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualThreadsDemo {

    // ---------------------------------------------------------------
    // 1. Creating virtual threads — three ways
    // ---------------------------------------------------------------
    static void creationAPIs() throws Exception {
        System.out.println("=== 1. Virtual Thread Creation APIs ===\n");

        // Way 1: Thread.ofVirtual().start(Runnable)
        Thread vt1 = Thread.ofVirtual()
                .name("vt-direct")
                .start(() -> System.out.println("  [Way 1] Thread.ofVirtual().start() → "
                        + Thread.currentThread()));
        vt1.join();

        // Way 2: Thread.startVirtualThread(Runnable) — shorthand
        Thread vt2 = Thread.startVirtualThread(() ->
                System.out.println("  [Way 2] Thread.startVirtualThread() → "
                        + Thread.currentThread()));
        vt2.join();

        // Way 3: ExecutorService (preferred for structured workloads)
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() ->
                    System.out.println("  [Way 3] VirtualThreadPerTaskExecutor → "
                            + Thread.currentThread()));
        } // auto-closes, awaits all tasks

        // Verify it's virtual
        Thread vt3 = Thread.ofVirtual().unstarted(() -> {});
        System.out.println("\n  isVirtual() = " + vt3.isVirtual());
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 2. Throughput demo: 100K virtual threads
    // ---------------------------------------------------------------
    static void throughputDemo() throws Exception {
        System.out.println("=== 2. Throughput Demo: 100,000 Virtual Threads ===\n");

        final int TASK_COUNT = 100_000;
        AtomicInteger counter = new AtomicInteger(0);

        Instant start = Instant.now();

        // Each virtual thread simulates a short I/O wait
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < TASK_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        // Simulate I/O (sleep unmounts the virtual thread from its carrier)
                        Thread.sleep(Duration.ofMillis(10));
                        counter.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } // blocks until all 100K tasks complete

        Duration elapsed = Duration.between(start, Instant.now());
        System.out.println("  Tasks completed: " + counter.get());
        System.out.println("  Elapsed:         " + elapsed.toMillis() + " ms");
        System.out.println("  Throughput:      ~" +
                (TASK_COUNT * 1000L / Math.max(elapsed.toMillis(), 1)) + " tasks/sec");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 3. Platform threads comparison (limited count to avoid OOM)
    // ---------------------------------------------------------------
    static void platformComparison() throws Exception {
        System.out.println("=== 3. Platform vs Virtual — Quick Comparison ===\n");

        final int PLATFORM_COUNT = 2_000; // safe limit for most machines
        final int VIRTUAL_COUNT = 100_000;

        // Platform threads
        Instant pStart = Instant.now();
        Thread[] platformThreads = new Thread[PLATFORM_COUNT];
        for (int i = 0; i < PLATFORM_COUNT; i++) {
            platformThreads[i] = Thread.ofPlatform()
                    .name("platform-" + i)
                    .start(() -> {
                        try { Thread.sleep(10); }
                        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    });
        }
        for (Thread t : platformThreads) t.join();
        long pMillis = Duration.between(pStart, Instant.now()).toMillis();

        // Virtual threads
        Instant vStart = Instant.now();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < VIRTUAL_COUNT; i++) {
                exec.submit(() -> {
                    try { Thread.sleep(10); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
            }
        }
        long vMillis = Duration.between(vStart, Instant.now()).toMillis();

        System.out.println("  Platform threads (" + PLATFORM_COUNT + "): " + pMillis + " ms");
        System.out.println("  Virtual  threads (" + VIRTUAL_COUNT + "): " + vMillis + " ms");
        System.out.println("  → Virtual threads handled " + (VIRTUAL_COUNT / PLATFORM_COUNT)
                + "x more tasks in comparable time.");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 4. Virtual thread factory with naming
    // ---------------------------------------------------------------
    static void threadFactoryDemo() throws Exception {
        System.out.println("=== 4. Virtual Thread Factory ===\n");

        // Thread factory for custom naming / daemon status
        var factory = Thread.ofVirtual()
                .name("worker-", 0)  // worker-0, worker-1, worker-2 ...
                .factory();

        try (ExecutorService exec = Executors.newThreadPerTaskExecutor(factory)) {
            for (int i = 0; i < 5; i++) {
                exec.submit(() -> System.out.println("  " + Thread.currentThread().getName()
                        + " (virtual=" + Thread.currentThread().isVirtual() + ")"));
            }
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // main — run all demos
    // ---------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Virtual Threads Demo — Java 21 (Project Loom) ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        creationAPIs();
        throughputDemo();
        platformComparison();
        threadFactoryDemo();

        System.out.println("=== Done ===");
    }
}
