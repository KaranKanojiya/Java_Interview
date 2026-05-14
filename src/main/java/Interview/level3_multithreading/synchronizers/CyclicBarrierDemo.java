package interview.level3_multithreading.synchronizers;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * ============================================================================
 * CYCLICBARRIER — java.util.concurrent.CyclicBarrier
 * Level: 3 — Advanced Concurrency Deep Dive
 * ============================================================================
 *
 * WHAT IS IT?
 * ───────────
 * A synchronization aid that allows a set of threads to ALL WAIT for each
 * other to reach a common barrier point. Once all threads arrive, the barrier
 * trips and all threads are released.
 *
 * Key properties:
 *   - All N parties must call await() before any are released
 *   - REUSABLE: automatically resets after all parties are released (cyclic!)
 *   - Optional barrier action: a Runnable that executes when barrier trips
 *   - If one thread is interrupted or times out, ALL waiting threads get
 *     BrokenBarrierException
 *
 * ============================================================================
 * REAL-WORLD USE CASES
 * ============================================================================
 *
 * 1. PARALLEL COMPUTATION PHASES: Matrix multiplication where each thread
 *    computes a row, then all synchronize before the next phase.
 *
 * 2. GAME SIMULATION: All players must complete their turn before the next
 *    round starts.
 *
 * 3. ITERATIVE ALGORITHMS: Genetic algorithms, cellular automata — each
 *    generation must complete before the next begins.
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "When would you use CyclicBarrier?"
 * A: "When you have N threads that must ALL reach a synchronization point
 *     before ANY of them can proceed. Classic example: multi-phase parallel
 *     computation where each phase depends on all threads completing the
 *     previous phase. Unlike CountDownLatch, CyclicBarrier is reusable."
 *
 * Q: "CyclicBarrier vs CountDownLatch?"
 * A: "CountDownLatch: one-shot, different threads count down vs wait.
 *     CyclicBarrier: reusable, ALL threads both wait and signal.
 *     CountDownLatch = 'wait for N events'. CyclicBarrier = 'N threads
 *     rendezvous'. CyclicBarrier has a barrier action. CountDownLatch
 *     cannot be reset."
 *
 * Q: "What happens if one thread fails at the barrier?"
 * A: "The barrier is 'broken.' All threads waiting at the barrier get
 *     BrokenBarrierException. The barrier can be reset with reset()."
 *
 * Q: "What is the barrier action?"
 * A: "An optional Runnable passed to the constructor that runs after the
 *     last thread arrives but BEFORE any thread is released. Useful for
 *     aggregation, logging, or preparing for the next phase. It runs in
 *     the last thread to arrive."
 */
public class CyclicBarrierDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CyclicBarrier Demo ===\n");

        demoParallelComputation();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario: MULTI-PHASE PARALLEL COMPUTATION
    // ─────────────────────────────────────────────────────────────────────────
    // Simulate 3 workers computing in 3 phases. After each phase, they
    // synchronize at the barrier before starting the next phase.
    // ─────────────────────────────────────────────────────────────────────────

    // Shared results array — each worker writes to its own index
    private static final int WORKER_COUNT = 3;
    private static final int PHASES = 3;
    private static final int[][] results = new int[WORKER_COUNT][PHASES];

    static void demoParallelComputation() throws InterruptedException {
        System.out.println("── Scenario: Multi-Phase Parallel Computation ──\n");
        System.out.println("3 workers, 3 phases. All sync at barrier between phases.\n");

        // Barrier with action: runs after all threads arrive, before release
        CyclicBarrier barrier = new CyclicBarrier(WORKER_COUNT, () -> {
            // This runs in the LAST thread to arrive at the barrier
            System.out.println("  >> Barrier tripped! All workers synchronized.");
            System.out.println("  >> (Barrier action runs in thread: "
                    + Thread.currentThread().getName() + ")");

            // Aggregate results from completed phase
            int phaseTotal = 0;
            for (int[] workerResults : results) {
                for (int val : workerResults) phaseTotal += val;
            }
            System.out.println("  >> Running total: " + phaseTotal);
            System.out.println();
        });

        // Create and start workers
        Thread[] workers = new Thread[WORKER_COUNT];
        for (int i = 0; i < WORKER_COUNT; i++) {
            final int workerId = i;
            workers[i] = new Thread(() -> {
                try {
                    for (int phase = 0; phase < PHASES; phase++) {
                        // Simulate computation for this phase
                        int result = (workerId + 1) * (phase + 1) * 10;
                        results[workerId][phase] = result;
                        System.out.println("  Worker-" + workerId
                                + " completed phase " + phase
                                + " (result: " + result + ")");

                        Thread.sleep((long) (Math.random() * 500 + 100));

                        // Wait at barrier — blocks until all workers arrive
                        System.out.println("  Worker-" + workerId
                                + " waiting at barrier (phase " + phase + ")...");
                        barrier.await();
                        // All workers released here — barrier auto-resets for next phase!
                    }
                    System.out.println("  Worker-" + workerId + " finished all phases!");
                } catch (InterruptedException | BrokenBarrierException e) {
                    System.out.println("  Worker-" + workerId + " interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }, "Worker-" + i);
            workers[i].start();
        }

        // Wait for all workers to finish
        for (Thread worker : workers) {
            worker.join();
        }

        System.out.println("\nAll workers completed all phases!");

        // Print final results
        System.out.println("\nResults matrix (worker x phase):");
        for (int i = 0; i < WORKER_COUNT; i++) {
            System.out.print("  Worker-" + i + ": ");
            for (int j = 0; j < PHASES; j++) {
                System.out.printf("%4d ", results[i][j]);
            }
            System.out.println();
        }

        /*
         * FLOW DIAGRAM:
         *
         *   Worker-0: [phase0 work] → await() ─┐
         *   Worker-1: [phase0 work] → await() ──┤ barrier trips! → barrier action runs
         *   Worker-2: [phase0 work] → await() ─┘                  → all released
         *                                         ↓
         *   Worker-0: [phase1 work] → await() ─┐
         *   Worker-1: [phase1 work] → await() ──┤ barrier trips again! (cyclic)
         *   Worker-2: [phase1 work] → await() ─┘
         *                                         ↓
         *   Worker-0: [phase2 work] → await() ─┐
         *   Worker-1: [phase2 work] → await() ──┤ barrier trips again!
         *   Worker-2: [phase2 work] → await() ─┘
         *
         * KEY INSIGHT: The barrier RESETS automatically after each trip.
         * CountDownLatch cannot do this — it's one-shot.
         */
    }
}
