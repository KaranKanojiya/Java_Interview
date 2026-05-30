package interview.level5_java17to21.virtual_threads;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q10. What happens when a virtual thread uses synchronized?
 *
 * PINNING: When a virtual thread enters a synchronized block/method, it gets
 * "pinned" to its carrier (platform) thread and CANNOT be unmounted.
 *
 * Normal virtual thread behavior:
 *   Virtual thread blocks on I/O → unmounts from carrier → carrier handles other VTs
 *
 * Pinned virtual thread:
 *   Virtual thread in synchronized + blocks on I/O → stays on carrier → carrier is wasted
 *   This defeats the purpose of virtual threads!
 *
 * Why does pinning happen?
 *   - synchronized uses monitors (JVM intrinsic locks)
 *   - Monitor is tied to the OS thread, not the virtual thread
 *   - JVM can't unmount while holding a monitor lock
 *
 * Solution: Replace synchronized with ReentrantLock
 *   - ReentrantLock works correctly with virtual threads
 *   - Virtual threads can unmount while waiting on a ReentrantLock
 *
 * Detect pinning: -Djdk.tracePinnedThreads=short (or =full for stack traces)
 */
public class SynchronizedPinning {

    private static final Object monitor = new Object();
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws Exception {

        System.out.println("=== Synchronized PINNING Problem ===\n");

        // === BAD: synchronized causes pinning ===
        System.out.println("--- BAD: synchronized (pins virtual thread) ---");
        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    synchronized (monitor) {  // PINNED! Carrier thread is blocked
                        try {
                            Thread.sleep(50);  // I/O simulation — can't unmount because pinned
                        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    }
                });
            }
        }
        long pinnedTime = System.currentTimeMillis() - start;
        System.out.println("synchronized: " + pinnedTime + "ms (serial — one VT at a time per carrier)");

        // === GOOD: ReentrantLock avoids pinning ===
        System.out.println("\n--- GOOD: ReentrantLock (no pinning) ---");
        start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) {
                executor.submit(() -> {
                    lock.lock();
                    try {
                        Thread.sleep(50);  // Virtual thread unmounts here — carrier is free!
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                });
            }
        }
        long unpinnedTime = System.currentTimeMillis() - start;
        System.out.println("ReentrantLock: " + unpinnedTime + "ms (concurrent — VTs unmount while waiting)");

        // === When synchronized is OK ===
        System.out.println("\n=== When synchronized is FINE with virtual threads ===");
        System.out.println("✅ Short critical sections (no I/O inside synchronized)");
        System.out.println("✅ Quick in-memory operations (increment counter, update map)");
        System.out.println("✅ No blocking calls inside synchronized");

        System.out.println("\n=== When to replace with ReentrantLock ===");
        System.out.println("❌ synchronized + Thread.sleep()");
        System.out.println("❌ synchronized + I/O (HTTP call, DB query, file read)");
        System.out.println("❌ synchronized + blocking queue operations");
        System.out.println("❌ Long-running synchronized blocks");

        System.out.println("\n=== Detection ===");
        System.out.println("Run with: -Djdk.tracePinnedThreads=short");
        System.out.println("Output shows: Thread[#N,VirtualThread-X] <== pinned");
    }
}
