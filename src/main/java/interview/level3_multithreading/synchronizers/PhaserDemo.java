package interview.level3_multithreading.synchronizers;

import java.util.concurrent.Phaser;

/**
 * ============================================================================
 * PHASER — java.util.concurrent.Phaser
 * Level: 3 — Advanced Concurrency Deep Dive
 * ============================================================================
 *
 * WHAT IS IT?
 * ───────────
 * A flexible, reusable synchronization barrier that supports DYNAMIC
 * registration/deregistration of parties (threads) and multi-phase
 * synchronization.
 *
 * Think of it as a CyclicBarrier with superpowers:
 *   - Parties can join and leave at any time (dynamic)
 *   - Supports phase-based synchronization (like CyclicBarrier)
 *   - Can be used as a CountDownLatch (one-shot) too
 *   - Supports a termination condition (via onAdvance override)
 *   - Supports tiered/tree structure for massive parallelism
 *
 * Key concepts:
 *   - PARTY: a thread registered with the phaser
 *   - PHASE: the current synchronization round (starts at 0)
 *   - ARRIVE: signal that a party has completed the current phase
 *   - ADVANCE: when all parties have arrived, the phase advances
 *
 * ============================================================================
 * PHASER vs CYCLICBARRIER vs COUNTDOWNLATCH
 * ============================================================================
 *
 *   Feature           | CountDownLatch | CyclicBarrier | Phaser
 *   ──────────────────┼────────────────┼───────────────┼──────────────
 *   Reusable          | No             | Yes           | Yes
 *   Dynamic parties   | No             | No            | Yes
 *   Phase tracking    | No             | Implicit      | Explicit
 *   Termination       | At zero        | N/A           | onAdvance()
 *   Arrive != Wait    | Yes (countDown)| No            | Yes (arrive)
 *   Tiered/Hierarchy  | No             | No            | Yes
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "Phaser vs CyclicBarrier?"
 * A: "Phaser is a more flexible CyclicBarrier. Key differences:
 *     1. Dynamic parties — threads can register/deregister at any time
 *     2. Phase numbers — you can query the current phase
 *     3. Termination control — override onAdvance() to stop after N phases
 *     4. Arrive without wait — arriveAndDeregister() or arrive()
 *     5. Tiered phasers — for scaling to thousands of parties
 *     Use CyclicBarrier for simple fixed-party barriers. Use Phaser when
 *     you need dynamic party counts or controlled termination."
 *
 * Q: "When would you use Phaser?"
 * A: "When the number of threads participating changes over time, or when
 *     you need to run a fixed number of phases and then terminate. Example:
 *     a web crawler that spawns new tasks dynamically — each new URL adds
 *     a party, completed URLs deregister."
 *
 * Q: "What does onAdvance() do?"
 * A: "It's called when the last party arrives for a phase. If it returns
 *     true, the phaser terminates (all future awaits return immediately).
 *     Override it to run after each phase or to stop after N phases."
 */
public class PhaserDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Phaser Demo ===\n");

        demoMultiPhaseWithTermination();
        System.out.println();
        demoDynamicParties();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 1: MULTI-PHASE COMPUTATION with controlled termination
    // ─────────────────────────────────────────────────────────────────────────
    static void demoMultiPhaseWithTermination() throws InterruptedException {
        System.out.println("── Scenario: Multi-Phase Computation (3 phases, then stop) ──\n");

        final int MAX_PHASES = 3;

        // Custom phaser that terminates after MAX_PHASES
        Phaser phaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                // Called when all parties arrive for this phase
                System.out.println("\n  >> Phase " + phase + " complete! "
                        + registeredParties + " parties participated.");
                // Return true to TERMINATE, false to continue
                return phase >= MAX_PHASES - 1 || registeredParties == 0;
            }
        };

        // Register the main thread as a party (so we can control flow)
        phaser.register();

        int workerCount = 3;
        for (int i = 0; i < workerCount; i++) {
            // Register each worker as a party
            phaser.register();

            final int workerId = i;
            new Thread(() -> {
                while (!phaser.isTerminated()) {
                    int phase = phaser.getPhase();
                    System.out.println("  Worker-" + workerId + " doing work for phase " + phase);

                    try {
                        Thread.sleep((long) (Math.random() * 500 + 100));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    System.out.println("  Worker-" + workerId + " arriving at phase " + phase);
                    // arriveAndAwaitAdvance = arrive + wait for others
                    phaser.arriveAndAwaitAdvance();
                }
                System.out.println("  Worker-" + workerId + " terminated.");
            }, "Worker-" + i).start();
        }

        // Main thread participates in each phase
        for (int phase = 0; phase < MAX_PHASES; phase++) {
            // Arrive and wait (main thread is also a party)
            phaser.arriveAndAwaitAdvance();
        }

        // Give workers time to print termination messages
        Thread.sleep(200);

        System.out.println("\n  Phaser terminated. Final phase: " + phaser.getPhase());
        System.out.println("  isTerminated: " + phaser.isTerminated());

        /*
         * FLOW:
         *
         *   Phase 0:
         *     Worker-0: [work] → arrive
         *     Worker-1: [work] → arrive
         *     Worker-2: [work] → arrive
         *     Main:             → arrive
         *     >> onAdvance(0, 4) → false (continue)
         *
         *   Phase 1:
         *     Worker-0: [work] → arrive
         *     Worker-1: [work] → arrive
         *     Worker-2: [work] → arrive
         *     Main:             → arrive
         *     >> onAdvance(1, 4) → false (continue)
         *
         *   Phase 2:
         *     Worker-0: [work] → arrive
         *     Worker-1: [work] → arrive
         *     Worker-2: [work] → arrive
         *     Main:             → arrive
         *     >> onAdvance(2, 4) → TRUE (terminate!)
         */
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2: DYNAMIC PARTIES — threads join and leave
    // ─────────────────────────────────────────────────────────────────────────
    static void demoDynamicParties() throws InterruptedException {
        System.out.println("── Scenario: Dynamic Parties (threads join/leave) ──\n");

        Phaser phaser = new Phaser(1); // Register main thread
        System.out.println("  Initial parties: " + phaser.getRegisteredParties());

        // Phase 0: Start with 2 workers
        System.out.println("\n  --- Phase 0: Starting with 2 workers ---");
        Thread w0 = startWorker(phaser, "Alpha", 2);
        Thread w1 = startWorker(phaser, "Beta", 2);

        phaser.arriveAndAwaitAdvance(); // Phase 0 → 1
        System.out.println("  [Main] Phase 0 done. Registered: " + phaser.getRegisteredParties());

        // Phase 1: Add a third worker dynamically
        System.out.println("\n  --- Phase 1: Adding worker Gamma dynamically ---");
        Thread w2 = startWorker(phaser, "Gamma", 1); // joins for just 1 phase

        phaser.arriveAndAwaitAdvance(); // Phase 1 → 2
        System.out.println("  [Main] Phase 1 done. Registered: " + phaser.getRegisteredParties());

        // Phase 2: Gamma has left, Alpha and Beta continue
        System.out.println("\n  --- Phase 2: Gamma left, Alpha/Beta continue ---");
        phaser.arriveAndAwaitAdvance(); // Phase 2 → 3
        System.out.println("  [Main] Phase 2 done. Registered: " + phaser.getRegisteredParties());

        // Deregister main thread
        phaser.arriveAndDeregister();

        // Wait for worker threads
        w0.join();
        w1.join();
        w2.join();

        System.out.println("\n  All workers finished.");

        /*
         * PHASER API SUMMARY:
         *
         *   register()                — add a party (thread)
         *   bulkRegister(n)           — add n parties
         *   arrive()                  — arrive without waiting
         *   arriveAndAwaitAdvance()   — arrive and wait for others
         *   arriveAndDeregister()     — arrive and leave the phaser
         *   awaitAdvance(phase)       — wait for specific phase to complete
         *   getPhase()               — current phase number
         *   getRegisteredParties()   — number of registered parties
         *   getArrivedParties()      — parties that have arrived
         *   getUnarrivedParties()    — parties that haven't arrived yet
         *   isTerminated()           — has the phaser terminated?
         *   forceTermination()       — terminate immediately
         */
    }

    /**
     * Starts a worker thread that participates in the given number of phases,
     * then deregisters from the phaser.
     */
    private static Thread startWorker(Phaser phaser, String name, int phases) {
        phaser.register(); // Register BEFORE starting the thread
        Thread t = new Thread(() -> {
            for (int i = 0; i < phases; i++) {
                int phase = phaser.getPhase();
                System.out.println("  [" + name + "] Working on phase " + phase);
                try {
                    Thread.sleep((long) (Math.random() * 300 + 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (i == phases - 1) {
                    // Last phase for this worker — deregister
                    System.out.println("  [" + name + "] Deregistering after phase " + phase);
                    phaser.arriveAndDeregister();
                } else {
                    phaser.arriveAndAwaitAdvance();
                }
            }
            System.out.println("  [" + name + "] Done.");
        }, name);
        t.start();
        return t;
    }
}
