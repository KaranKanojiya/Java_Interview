package interview.level3_multithreading.patterns;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ============================================================================
 * DEADLOCK, LIVELOCK, AND STARVATION
 * Level: 3 — Advanced Concurrency Deep Dive
 * ============================================================================
 *
 * DEADLOCK: Two or more threads are BLOCKED forever, each waiting for a
 *           resource held by the other.
 *
 * Four conditions for deadlock (ALL must hold — Coffman conditions):
 *   1. Mutual Exclusion: resource is held exclusively
 *   2. Hold and Wait: thread holds one resource, waits for another
 *   3. No Preemption: resources can't be forcibly taken away
 *   4. Circular Wait: A waits for B, B waits for A (cycle in wait graph)
 *
 * LIVELOCK: Threads are NOT blocked but make no progress — they keep
 *           changing state in response to each other (like two people
 *           trying to pass in a hallway, each stepping aside the same way).
 *
 * STARVATION: A thread never gets CPU time or resource access because
 *             other threads always get priority (e.g., low-priority thread
 *             in a system with many high-priority threads).
 *
 * ============================================================================
 * DETECTION AND PREVENTION
 * ============================================================================
 *
 * DETECTION:
 *   1. jstack <pid> — dump thread stacks, shows "Found one Java-level deadlock"
 *   2. ThreadMXBean.findDeadlockedThreads() — programmatic detection
 *   3. JConsole / VisualVM — GUI tools, detect deadlocks button
 *   4. Thread dump (kill -3 <pid> on Unix)
 *
 * PREVENTION:
 *   1. Lock Ordering: always acquire locks in a consistent global order
 *   2. Lock Timeout: use tryLock(timeout) instead of lock()
 *   3. Single Lock: protect shared state with one lock (if possible)
 *   4. Lock-Free: use CAS / atomic operations (AtomicInteger, etc.)
 *   5. Avoid Nested Locks: minimize holding multiple locks simultaneously
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "How do you detect and prevent deadlock?"
 * A: "DETECT: Use jstack to dump threads — it reports deadlocks. Or use
 *     ThreadMXBean.findDeadlockedThreads() programmatically. JConsole and
 *     VisualVM have 'detect deadlock' buttons.
 *     PREVENT: (1) Consistent lock ordering — if all threads acquire Lock-A
 *     before Lock-B, no circular wait. (2) Use tryLock with timeout — fail
 *     fast if lock unavailable. (3) Minimize lock scope and nesting.
 *     (4) Consider lock-free algorithms (CAS, atomic variables)."
 *
 * Q: "What are the four conditions for deadlock?"
 * A: "Coffman conditions — ALL four must hold: (1) Mutual exclusion,
 *     (2) Hold and wait, (3) No preemption, (4) Circular wait.
 *     Break ANY one to prevent deadlock. Most practical: break circular
 *     wait via lock ordering, or break hold-and-wait via tryLock."
 *
 * Q: "Deadlock vs Livelock vs Starvation?"
 * A: "Deadlock: threads BLOCKED, no progress, waiting for each other.
 *     Livelock: threads NOT blocked but make NO progress (keep reacting).
 *     Starvation: thread CAN run but never gets the chance (unfairness).
 *     Deadlock is a hard stop. Livelock wastes CPU. Starvation is unfair."
 */
public class DeadlockDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Deadlock, Livelock, and Starvation Demo ===\n");

        demoDeadlock();
        System.out.println();
        demoDeadlockPrevention();
        System.out.println();
        demoLivelock();
        System.out.println();
        demoStarvation();
        System.out.println();
        demoProgrammaticDetection();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. DEADLOCK CREATION
    // ─────────────────────────────────────────────────────────────────────────
    static void demoDeadlock() throws InterruptedException {
        System.out.println("── Deadlock Creation (will be interrupted after 2s) ──\n");

        final Object lockA = new Object();
        final Object lockB = new Object();

        // Thread-1: acquires lockA, then tries lockB
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("  [T1] Acquired lockA, waiting for lockB...");
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                synchronized (lockB) {
                    System.out.println("  [T1] Acquired lockB (won't reach here in deadlock)");
                }
            }
        }, "Thread-1");

        // Thread-2: acquires lockB, then tries lockA (OPPOSITE ORDER → deadlock!)
        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println("  [T2] Acquired lockB, waiting for lockA...");
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                synchronized (lockA) {
                    System.out.println("  [T2] Acquired lockA (won't reach here in deadlock)");
                }
            }
        }, "Thread-2");

        t1.start();
        t2.start();

        // Wait a bit, then interrupt to unblock the demo
        Thread.sleep(2000);

        System.out.println("  [Main] Deadlock detected! Interrupting threads...");
        t1.interrupt();
        t2.interrupt();

        // Note: interrupt() won't break synchronized deadlock (only waiting/sleeping)
        // In real code, use ReentrantLock.lockInterruptibly() for interruptible locking
        t1.join(1000);
        t2.join(1000);

        if (t1.isAlive() || t2.isAlive()) {
            System.out.println("  [Main] Threads still deadlocked (synchronized can't be interrupted)");
            System.out.println("  [Main] In production, use ReentrantLock for interruptible locking");
            // Mark as daemon so JVM can exit
            t1.setDaemon(true);
            t2.setDaemon(true);
        }

        /*
         * DEADLOCK PATTERN:
         *
         *   T1: lock(A) → sleep → lock(B) ← BLOCKED (T2 holds B)
         *   T2: lock(B) → sleep → lock(A) ← BLOCKED (T1 holds A)
         *
         *   Circular wait: T1 → B → T2 → A → T1
         *
         * TO SEE IN JSTACK:
         *   $ jstack <pid>
         *   Found one Java-level deadlock:
         *   "Thread-1":
         *     waiting to lock monitor 0x... (object 0x..., a java.lang.Object),
         *     which is held by "Thread-2"
         *   "Thread-2":
         *     waiting to lock monitor 0x... (object 0x..., a java.lang.Object),
         *     which is held by "Thread-1"
         */
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. DEADLOCK PREVENTION — consistent lock ordering + tryLock
    // ─────────────────────────────────────────────────────────────────────────
    static void demoDeadlockPrevention() throws InterruptedException {
        System.out.println("── Deadlock Prevention ──\n");

        // STRATEGY 1: Consistent Lock Ordering
        System.out.println("  Strategy 1: Consistent Lock Ordering");
        System.out.println("  Rule: ALWAYS acquire lockA before lockB\n");

        final Object lockA = new Object();
        final Object lockB = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {          // Always lockA first
                System.out.println("  [T1] Acquired lockA");
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                synchronized (lockB) {      // Then lockB
                    System.out.println("  [T1] Acquired lockB — no deadlock!");
                }
            }
        }, "Safe-T1");

        Thread t2 = new Thread(() -> {
            synchronized (lockA) {          // SAME ORDER: lockA first
                System.out.println("  [T2] Acquired lockA");
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                synchronized (lockB) {      // Then lockB
                    System.out.println("  [T2] Acquired lockB — no deadlock!");
                }
            }
        }, "Safe-T2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // STRATEGY 2: tryLock with timeout
        System.out.println("\n  Strategy 2: tryLock with Timeout");
        System.out.println("  Rule: Give up if lock not available within timeout\n");

        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();

        Thread t3 = new Thread(() -> {
            try {
                if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        Thread.sleep(50);
                        if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                System.out.println("  [T3] Acquired both locks!");
                            } finally {
                                lock2.unlock();
                            }
                        } else {
                            System.out.println("  [T3] Couldn't get lock2, backing off.");
                        }
                    } finally {
                        lock1.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "TryLock-T3");

        Thread t4 = new Thread(() -> {
            try {
                if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        Thread.sleep(50);
                        if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                System.out.println("  [T4] Acquired both locks!");
                            } finally {
                                lock1.unlock();
                            }
                        } else {
                            System.out.println("  [T4] Couldn't get lock1, backing off.");
                        }
                    } finally {
                        lock2.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "TryLock-T4");

        t3.start();
        t4.start();
        t3.join();
        t4.join();
        System.out.println("  Both threads completed (no deadlock possible with tryLock).");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LIVELOCK
    // ─────────────────────────────────────────────────────────────────────────
    static void demoLivelock() throws InterruptedException {
        System.out.println("── Livelock Demo ──\n");

        /*
         * LIVELOCK ANALOGY:
         * Two people in a hallway. Both step left. Both step right.
         * Both step left. Neither makes progress, but neither is blocked.
         *
         * In code: two threads keep yielding to each other.
         */

        final ReentrantLock lock1 = new ReentrantLock();
        final ReentrantLock lock2 = new ReentrantLock();
        final int[] attempts = {0};
        final int MAX_ATTEMPTS = 5;

        Thread polite1 = new Thread(() -> {
            while (attempts[0] < MAX_ATTEMPTS) {
                lock1.lock();
                try {
                    System.out.println("  [Polite-1] Holds lock1, trying lock2...");
                    if (!lock2.tryLock()) {
                        System.out.println("  [Polite-1] Can't get lock2, being polite, releasing lock1");
                        attempts[0]++;
                        // Release lock1 to let the other thread proceed
                        // But the other thread does the same thing → livelock!
                    } else {
                        try {
                            System.out.println("  [Polite-1] Got both locks!");
                            return;
                        } finally {
                            lock2.unlock();
                        }
                    }
                } finally {
                    lock1.unlock();
                }
                try { Thread.sleep(10); } catch (InterruptedException e) { return; }
            }
            System.out.println("  [Polite-1] Gave up after " + MAX_ATTEMPTS + " attempts (livelock!)");
        }, "Polite-1");

        Thread polite2 = new Thread(() -> {
            while (attempts[0] < MAX_ATTEMPTS) {
                lock2.lock();
                try {
                    System.out.println("  [Polite-2] Holds lock2, trying lock1...");
                    if (!lock1.tryLock()) {
                        System.out.println("  [Polite-2] Can't get lock1, being polite, releasing lock2");
                        // Same behavior → both keep yielding → no progress
                    } else {
                        try {
                            System.out.println("  [Polite-2] Got both locks!");
                            return;
                        } finally {
                            lock1.unlock();
                        }
                    }
                } finally {
                    lock2.unlock();
                }
                try { Thread.sleep(10); } catch (InterruptedException e) { return; }
            }
            System.out.println("  [Polite-2] Gave up after max attempts (livelock!)");
        }, "Polite-2");

        polite1.start();
        polite2.start();
        polite1.join(3000);
        polite2.join(3000);

        System.out.println("\n  LIVELOCK FIX: Add random backoff delay to break symmetry.");
        System.out.println("  Thread.sleep(random(10, 100)) before retrying.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. STARVATION
    // ─────────────────────────────────────────────────────────────────────────
    static void demoStarvation() throws InterruptedException {
        System.out.println("── Starvation Demo ──\n");

        /*
         * STARVATION occurs when a thread can't access a shared resource
         * because other threads monopolize it.
         *
         * Common causes:
         *   1. Thread priority: low-priority threads rarely scheduled
         *   2. Unfair locks: one thread keeps re-acquiring the lock
         *   3. Long-running synchronized blocks
         *
         * SOLUTION: Use fair locks (new ReentrantLock(true))
         */

        // Unfair lock — can cause starvation
        ReentrantLock unfairLock = new ReentrantLock(false); // unfair (default)
        // Fair lock — prevents starvation (FIFO ordering)
        // ReentrantLock fairLock = new ReentrantLock(true);

        System.out.println("  Unfair lock: new ReentrantLock(false)");
        System.out.println("  - Thread that just released can immediately re-acquire");
        System.out.println("  - Other waiting threads may starve");
        System.out.println();
        System.out.println("  Fair lock: new ReentrantLock(true)");
        System.out.println("  - Threads served in FIFO order");
        System.out.println("  - No starvation, but ~10-20% throughput reduction");
        System.out.println();

        // Demonstrate with thread priorities
        System.out.println("  Thread priority starvation:");
        System.out.println("  - Thread.MIN_PRIORITY (1): may never run if others are busy");
        System.out.println("  - Thread.MAX_PRIORITY (10): gets CPU time first");
        System.out.println("  - FIX: Don't rely on priorities; use fair synchronization");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. PROGRAMMATIC DEADLOCK DETECTION
    // ─────────────────────────────────────────────────────────────────────────
    static void demoProgrammaticDetection() {
        System.out.println("── Programmatic Deadlock Detection ──\n");

        /*
         * USE ThreadMXBean TO DETECT DEADLOCKS AT RUNTIME:
         *
         *   ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
         *   long[] deadlockedThreadIds = mxBean.findDeadlockedThreads();
         *
         *   if (deadlockedThreadIds != null) {
         *       ThreadInfo[] threadInfos = mxBean.getThreadInfo(deadlockedThreadIds);
         *       System.out.println("DEADLOCK DETECTED!");
         *       for (ThreadInfo info : threadInfos) {
         *           System.out.println("Thread: " + info.getThreadName());
         *           System.out.println("State: " + info.getThreadState());
         *           System.out.println("Blocked on: " + info.getLockName());
         *           System.out.println("Held by: " + info.getLockOwnerName());
         *       }
         *   }
         *
         * You could run this in a monitoring thread on a schedule.
         */

        // Actually detect (there should be no deadlock right now)
        java.lang.management.ThreadMXBean mxBean =
                java.lang.management.ManagementFactory.getThreadMXBean();
        long[] deadlocked = mxBean.findDeadlockedThreads();

        if (deadlocked == null) {
            System.out.println("  No deadlocks detected (as expected).");
        } else {
            System.out.println("  DEADLOCK DETECTED! Threads: " + deadlocked.length);
        }

        System.out.println("\n── Summary: Detection Tools ──");
        System.out.println("  1. jstack <pid>                   — CLI thread dump");
        System.out.println("  2. kill -3 <pid>                  — thread dump to stdout");
        System.out.println("  3. ThreadMXBean                   — programmatic detection");
        System.out.println("  4. JConsole → Threads → Detect    — GUI tool");
        System.out.println("  5. VisualVM → Threads             — GUI tool");

        System.out.println("\n── Summary: Prevention Strategies ──");
        System.out.println("  1. Lock ordering          — break circular wait");
        System.out.println("  2. tryLock with timeout   — break hold-and-wait");
        System.out.println("  3. Single lock            — break mutual exclusion");
        System.out.println("  4. Lock-free (CAS)        — eliminate locks entirely");
        System.out.println("  5. Minimize lock scope    — reduce contention window");
        System.out.println("  6. Fair locks             — prevent starvation");
    }
}
