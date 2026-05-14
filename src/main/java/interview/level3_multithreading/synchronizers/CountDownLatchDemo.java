package interview.level3_multithreading.synchronizers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================================
 * COUNTDOWNLATCH — java.util.concurrent.CountDownLatch
 * Level: 3 — Advanced Concurrency Deep Dive
 * ============================================================================
 *
 * WHAT IS IT?
 * ───────────
 * A synchronization aid that allows one or more threads to WAIT until a set
 * of operations being performed by other threads completes.
 *
 * Think of it as a gate: the gate opens when the count reaches zero.
 *
 * Key properties:
 *   - Initialized with a count (number of events to wait for)
 *   - countDown() decrements the count by 1
 *   - await() blocks until the count reaches zero
 *   - ONE-SHOT: cannot be reset after count reaches zero
 *
 * ============================================================================
 * REAL-WORLD USE CASES
 * ============================================================================
 *
 * 1. SERVICE STARTUP: Wait for all dependencies (DB, cache, message queue)
 *    to initialize before accepting traffic.
 *
 * 2. TEST SYNCHRONIZATION: Start N threads simultaneously (using a "start
 *    signal" latch), then wait for all to finish (using a "done" latch).
 *
 * 3. PARALLEL TASK COMPLETION: Submit N tasks, wait for all to complete
 *    before aggregating results.
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "CountDownLatch vs CyclicBarrier?"
 * A: "CountDownLatch is ONE-SHOT (count goes to zero, done). CyclicBarrier
 *     is REUSABLE (resets after all parties arrive). CountDownLatch separates
 *     waiters from counters (different threads). CyclicBarrier has all
 *     threads both wait AND signal. Use CountDownLatch for 'wait for N events',
 *     CyclicBarrier for 'N threads rendezvous at a point.'"
 *
 * Q: "Can you reset a CountDownLatch?"
 * A: "No. It's one-shot. Once the count reaches zero, it stays open forever.
 *     If you need reusability, use CyclicBarrier or Phaser."
 *
 * Q: "What happens if countDown() is called more times than the initial count?"
 * A: "Nothing — extra calls are no-ops. The count never goes below zero."
 *
 * Q: "Can await() time out?"
 * A: "Yes. await(long timeout, TimeUnit unit) returns true if count reached
 *     zero, false if the wait timed out."
 */
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CountDownLatch Demo ===\n");

        demoServiceStartup();
        System.out.println();
        demoStartGun();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 1: SERVICE STARTUP — wait for all dependencies
    // ─────────────────────────────────────────────────────────────────────────
    static void demoServiceStartup() throws InterruptedException {
        System.out.println("── Scenario: Service Startup (wait for 3 dependencies) ──\n");

        // 1. Create latch with count = 3 (three services must initialize)
        int serviceCount = 3;
        CountDownLatch latch = new CountDownLatch(serviceCount);

        String[] services = {"DatabaseService", "CacheService", "MessageQueue"};

        // 2. Each service initializes in its own thread
        for (String service : services) {
            new Thread(() -> {
                try {
                    System.out.println("[" + service + "] Initializing...");
                    // Simulate varying init times
                    Thread.sleep((long) (Math.random() * 2000 + 500));
                    System.out.println("[" + service + "] Ready! (count before: "
                            + latch.getCount() + ")");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();  // Decrement count — always in finally!
                }
            }, service).start();
        }

        // 3. Main thread waits for all services
        System.out.println("[Main] Waiting for all services to start...");
        boolean allReady = latch.await(10, TimeUnit.SECONDS);

        if (allReady) {
            System.out.println("[Main] All services ready! Starting to accept traffic.");
            System.out.println("[Main] Latch count: " + latch.getCount() + " (zero = open)");
        } else {
            System.out.println("[Main] Timeout! Some services failed to start.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2: START GUN — release all threads simultaneously
    // ─────────────────────────────────────────────────────────────────────────
    static void demoStartGun() throws InterruptedException {
        System.out.println("── Scenario: Start Gun (release all threads at once) ──\n");

        /*
         * Pattern: Two latches
         *   - startSignal (count=1): holds all workers until main says "go"
         *   - doneSignal (count=N): main waits for all workers to finish
         */

        int workerCount = 5;
        CountDownLatch startSignal = new CountDownLatch(1);       // start gun
        CountDownLatch doneSignal = new CountDownLatch(workerCount); // completion tracker

        // Create workers — they all wait for the start signal
        for (int i = 0; i < workerCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("  Worker-" + id + " ready, waiting for start signal...");
                    startSignal.await();  // Block until start gun fires

                    // Do work
                    long workTime = (long) (Math.random() * 1000 + 200);
                    System.out.println("  Worker-" + id + " started! (working for "
                            + workTime + "ms)");
                    Thread.sleep(workTime);
                    System.out.println("  Worker-" + id + " done!");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneSignal.countDown();  // Signal completion
                }
            }, "Worker-" + i).start();
        }

        // Give threads a moment to reach await()
        Thread.sleep(500);

        // Fire the start gun!
        System.out.println("\n  [Main] Firing start gun!\n");
        startSignal.countDown();  // All workers released simultaneously

        // Wait for all workers to finish
        doneSignal.await();
        System.out.println("\n  [Main] All workers completed!");

        /*
         * FLOW:
         *
         *   Worker-0 ──→ await(startSignal) ──→ [work] ──→ countDown(doneSignal)
         *   Worker-1 ──→ await(startSignal) ──→ [work] ──→ countDown(doneSignal)
         *   Worker-2 ──→ await(startSignal) ──→ [work] ──→ countDown(doneSignal)
         *   Worker-3 ──→ await(startSignal) ──→ [work] ──→ countDown(doneSignal)
         *   Worker-4 ──→ await(startSignal) ──→ [work] ──→ countDown(doneSignal)
         *                     ↑                                     ↓
         *   Main ────→ countDown(startSignal)          await(doneSignal) ──→ done!
         */
    }
}
