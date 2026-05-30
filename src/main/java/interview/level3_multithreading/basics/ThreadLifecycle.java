package interview.level3_multithreading.basics;

import java.util.concurrent.TimeUnit;

/**
 * Q19. What are the different states in a Thread lifecycle?
 *
 * Thread.State enum (6 states):
 *
 *   NEW           → Thread created but start() not yet called
 *   RUNNABLE      → Thread is executing or ready to execute (in the run queue)
 *   BLOCKED       → Waiting to acquire a monitor lock (synchronized block)
 *   WAITING       → Waiting indefinitely (wait(), join(), LockSupport.park())
 *   TIMED_WAITING → Waiting with timeout (sleep(), wait(ms), join(ms))
 *   TERMINATED    → Thread finished execution (run() completed or exception thrown)
 *
 * State transitions:
 *   NEW → start() → RUNNABLE
 *   RUNNABLE → synchronized (lock contention) → BLOCKED → (lock acquired) → RUNNABLE
 *   RUNNABLE → wait()/join()/park() → WAITING → (notify/unpark/join returns) → RUNNABLE
 *   RUNNABLE → sleep(ms)/wait(ms)/join(ms) → TIMED_WAITING → (timeout/notify) → RUNNABLE
 *   RUNNABLE → run() completes → TERMINATED
 *
 * Important:
 *   - A TERMINATED thread CANNOT be restarted (calling start() again throws IllegalThreadStateException)
 *   - BLOCKED and WAITING are different: BLOCKED is waiting for a lock, WAITING is waiting for a signal
 *   - RUNNABLE includes both "running" and "ready to run" (OS decides which)
 */
public class ThreadLifecycle {

    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        // === NEW state ===
        Thread t1 = new Thread(() -> {
            System.out.println("  Thread running on: " + Thread.currentThread().getName());
        }, "DemoThread");
        System.out.println("=== After creation ===");
        System.out.println("State: " + t1.getState());  // NEW

        // === RUNNABLE state ===
        t1.start();
        // Note: by the time we check, it might already be TERMINATED
        System.out.println("\n=== After start() ===");
        System.out.println("State: " + t1.getState());  // RUNNABLE or TERMINATED
        t1.join();

        // === TERMINATED state ===
        System.out.println("\n=== After completion ===");
        System.out.println("State: " + t1.getState());  // TERMINATED

        // Cannot restart a terminated thread
        try {
            t1.start();
        } catch (IllegalThreadStateException e) {
            System.out.println("Cannot restart: " + e.getClass().getSimpleName());
        }

        // === TIMED_WAITING state ===
        System.out.println("\n=== TIMED_WAITING (sleep) ===");
        Thread sleeper = new Thread(() -> {
            try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Sleeper");
        sleeper.start();
        Thread.sleep(100);  // give it time to enter sleep
        System.out.println("Sleeper state: " + sleeper.getState());  // TIMED_WAITING
        sleeper.interrupt();  // wake it up
        sleeper.join();

        // === WAITING state ===
        System.out.println("\n=== WAITING (wait()) ===");
        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "Waiter");
        waiter.start();
        Thread.sleep(100);
        System.out.println("Waiter state: " + waiter.getState());  // WAITING
        synchronized (lock) { lock.notify(); }  // wake it up
        waiter.join();

        // === BLOCKED state ===
        System.out.println("\n=== BLOCKED (waiting for lock) ===");
        Thread holder = new Thread(() -> {
            synchronized (lock) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "LockHolder");

        Thread blocker = new Thread(() -> {
            synchronized (lock) {  // will block because holder has the lock
                System.out.println("  Blocker acquired lock");
            }
        }, "Blocker");

        holder.start();
        Thread.sleep(100);  // ensure holder acquires lock first
        blocker.start();
        Thread.sleep(100);  // ensure blocker is trying to acquire
        System.out.println("Blocker state: " + blocker.getState());  // BLOCKED
        holder.interrupt();
        holder.join();
        blocker.join();

        // === Summary ===
        System.out.println("\n=== All 6 states ===");
        System.out.println("NEW            → created, not started");
        System.out.println("RUNNABLE       → running or ready to run");
        System.out.println("BLOCKED        → waiting for monitor lock");
        System.out.println("WAITING        → wait(), join(), park()");
        System.out.println("TIMED_WAITING  → sleep(ms), wait(ms), join(ms)");
        System.out.println("TERMINATED     → done, cannot restart");
    }
}
