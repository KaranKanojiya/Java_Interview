package interview.level3_multithreading.basics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q24. What is the happens-before relationship in Java Memory Model?
 *
 * The Java Memory Model (JMM) defines happens-before rules that guarantee
 * memory visibility between threads. Without these guarantees, one thread's
 * writes may NEVER be visible to another thread (due to CPU caches, reordering).
 *
 * Happens-before rules (key ones):
 *
 *   1. Program order:     Within a thread, each statement happens-before the next
 *   2. Monitor lock:      unlock() happens-before subsequent lock() on same monitor
 *   3. Volatile:          Write to volatile happens-before subsequent read of same volatile
 *   4. Thread.start():    start() call happens-before any action in the started thread
 *   5. Thread.join():     All actions in a thread happen-before join() returns
 *   6. Thread interrupt:  interrupt() happens-before interrupted thread detects it
 *   7. Constructor:       End of constructor happens-before finalize()
 *   8. Transitivity:      If A happens-before B, and B happens-before C, then A happens-before C
 *
 * Without happens-before:
 *   - Compiler/CPU can reorder instructions
 *   - Thread may read stale cached values
 *   - Changes may NEVER become visible to other threads
 *
 * Practical implication:
 *   To make one thread's write visible to another, you MUST use one of these mechanisms:
 *   synchronized, volatile, Atomic*, Lock, CountDownLatch, etc.
 */
public class HappensBeforeDemo {

    // === Problem: No happens-before ===
    private static boolean noVisibilityFlag = false;  // no volatile, no sync
    private static int noVisibilityData = 0;

    // === Solution 1: volatile ===
    private static volatile boolean volatileFlag = false;
    private static int volatileData = 0;  // piggybacking on volatile's happens-before

    public static void main(String[] args) throws InterruptedException {

        // === Rule 3: Volatile guarantee ===
        System.out.println("=== Volatile happens-before ===");
        Thread writer = new Thread(() -> {
            volatileData = 42;          // write non-volatile data FIRST
            volatileFlag = true;        // write volatile — creates happens-before edge
        });

        Thread reader = new Thread(() -> {
            while (!volatileFlag) { }   // read volatile — happens-after the write
            // Because volatileFlag write happens-before this read,
            // volatileData = 42 is GUARANTEED to be visible here
            System.out.println("  volatileData = " + volatileData);  // always 42
        });

        writer.start();
        reader.start();
        writer.join();
        reader.join();

        // === Rule 2: synchronized (monitor lock) ===
        System.out.println("\n=== synchronized happens-before ===");
        final Object lock = new Object();
        final int[] sharedData = {0};

        Thread syncWriter = new Thread(() -> {
            synchronized (lock) {
                sharedData[0] = 100;
                System.out.println("  Writer set data = 100");
            }  // unlock happens-before next lock
        });

        syncWriter.start();
        syncWriter.join();

        Thread syncReader = new Thread(() -> {
            synchronized (lock) {  // lock happens-after previous unlock
                System.out.println("  Reader sees data = " + sharedData[0]);  // guaranteed 100
            }
        });
        syncReader.start();
        syncReader.join();

        // === Rule 4 & 5: Thread.start() and Thread.join() ===
        System.out.println("\n=== start() and join() happens-before ===");
        int[] result = {0};

        Thread t = new Thread(() -> {
            result[0] = 999;  // happens-before join() returns
        });
        // Everything before start() happens-before the thread's run()
        t.start();
        t.join();
        // Everything in the thread happens-before join() returns
        System.out.println("  After join(), result = " + result[0]);  // guaranteed 999

        // === Atomic operations — also have happens-before ===
        System.out.println("\n=== Atomic happens-before ===");
        AtomicInteger atomicVal = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        Thread atomicWriter = new Thread(() -> {
            atomicVal.set(42);  // atomic write creates happens-before
            latch.countDown();
        });

        Thread atomicReader = new Thread(() -> {
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("  Atomic value: " + atomicVal.get());  // guaranteed 42
        });

        atomicWriter.start();
        atomicReader.start();
        atomicWriter.join();
        atomicReader.join();

        // === Summary ===
        System.out.println("\n=== Happens-before guarantees ===");
        System.out.println("synchronized:  unlock → lock (same monitor)");
        System.out.println("volatile:      write → read (same variable)");
        System.out.println("Thread.start:  caller → new thread's run()");
        System.out.println("Thread.join:   thread's run() → caller after join()");
        System.out.println("Atomic ops:    write → read (same AtomicXxx)");
        System.out.println("Lock:          unlock → lock (same Lock object)");
        System.out.println("\nWithout these → NO visibility guarantee!");
    }
}
