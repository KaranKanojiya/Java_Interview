package interview.level3_multithreading.basics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Q21. What is AtomicInteger and how does it work?
 *
 * AtomicInteger provides lock-free, thread-safe operations on an int value
 * using CAS (Compare-And-Swap) CPU instructions.
 *
 * CAS operation:
 *   1. Read current value
 *   2. Compute new value
 *   3. Atomically: if current == expected, set to new value; else retry
 *   → No locking, no blocking, no context switching
 *
 * Key methods:
 *   get()                   → read value
 *   set(int)                → write value
 *   getAndIncrement()       → i++ (returns old value)
 *   incrementAndGet()       → ++i (returns new value)
 *   getAndAdd(int)          → adds delta, returns old
 *   compareAndSet(expect, update) → CAS operation
 *   updateAndGet(IntUnaryOperator) → atomic custom update
 *
 * When to use:
 *   AtomicInteger  → single counter/flag, low-medium contention
 *   LongAdder      → high-contention counters (better than AtomicLong)
 *   synchronized   → multiple variables must change atomically together
 *
 * Atomic classes: AtomicInteger, AtomicLong, AtomicBoolean, AtomicReference<V>,
 *                 AtomicIntegerArray, AtomicStampedReference (ABA problem)
 */
public class AtomicOperations {

    private static int unsafeCounter = 0;
    private static final AtomicInteger safeCounter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // === Problem: non-atomic increment is NOT thread-safe ===
        System.out.println("=== Race condition with plain int ===");
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10000; i++) {
            executor.submit(() -> unsafeCounter++);  // NOT atomic: read-modify-write
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Unsafe counter (expected 10000): " + unsafeCounter);  // likely < 10000

        // === Solution: AtomicInteger ===
        System.out.println("\n=== AtomicInteger — thread-safe ===");
        ExecutorService executor2 = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10000; i++) {
            executor2.submit(() -> safeCounter.incrementAndGet());  // atomic CAS
        }
        executor2.shutdown();
        executor2.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Atomic counter (expected 10000): " + safeCounter.get());  // always 10000

        // === Key methods ===
        System.out.println("\n=== AtomicInteger methods ===");
        AtomicInteger ai = new AtomicInteger(10);
        System.out.println("Initial: " + ai.get());
        System.out.println("getAndIncrement(): " + ai.getAndIncrement() + " → now: " + ai.get());
        System.out.println("incrementAndGet(): " + ai.incrementAndGet());
        System.out.println("getAndAdd(5): " + ai.getAndAdd(5) + " → now: " + ai.get());
        System.out.println("addAndGet(3): " + ai.addAndGet(3));

        // === compareAndSet (CAS) ===
        System.out.println("\n=== compareAndSet (CAS) ===");
        AtomicInteger cas = new AtomicInteger(100);
        boolean success1 = cas.compareAndSet(100, 200);  // expect 100, set 200
        System.out.println("CAS(100→200): " + success1 + ", value: " + cas.get());

        boolean success2 = cas.compareAndSet(100, 300);  // expect 100, but it's 200 now
        System.out.println("CAS(100→300): " + success2 + ", value: " + cas.get());  // false, unchanged

        // === updateAndGet — custom atomic update ===
        System.out.println("\n=== updateAndGet ===");
        AtomicInteger updater = new AtomicInteger(5);
        int squared = updater.updateAndGet(x -> x * x);
        System.out.println("5 squared atomically: " + squared);

        // === AtomicReference — atomic operations on objects ===
        System.out.println("\n=== AtomicReference ===");
        AtomicReference<String> ref = new AtomicReference<>("Hello");
        ref.updateAndGet(s -> s + " World");
        System.out.println("AtomicReference: " + ref.get());

        // === LongAdder — better for high-contention counters ===
        System.out.println("\n=== LongAdder (high contention) ===");
        LongAdder adder = new LongAdder();
        ExecutorService executor3 = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100000; i++) {
            executor3.submit(adder::increment);
        }
        executor3.shutdown();
        executor3.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("LongAdder (expected 100000): " + adder.sum());

        System.out.println("\n=== When to use what ===");
        System.out.println("AtomicInteger: simple counter, low contention");
        System.out.println("LongAdder:     high-throughput counter (uses striped cells internally)");
        System.out.println("synchronized:  multiple fields must update atomically together");
    }
}
