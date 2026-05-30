package interview.level3_multithreading.basics;

/**
 * Q20. What is the difference between sleep() and wait()?
 *
 * | Feature          | Thread.sleep(ms)          | Object.wait()                    |
 * |-----------------|---------------------------|----------------------------------|
 * | Class           | Thread (static)            | Object (instance)                |
 * | Lock release    | NO — holds the lock        | YES — releases the monitor lock  |
 * | Wake up         | After timeout expires      | notify()/notifyAll() or timeout  |
 * | Must hold lock  | No                         | Yes (must be in synchronized)    |
 * | Purpose         | Pause execution            | Inter-thread communication       |
 * | Spurious wakeup | No                         | Yes (must use while loop)        |
 *
 * Key difference: sleep() HOLDS the lock, wait() RELEASES it.
 * This is the #1 most-asked distinction in interviews.
 */
public class SleepVsWait {

    private static final Object monitor = new Object();

    public static void main(String[] args) throws InterruptedException {

        // === sleep() does NOT release the lock ===
        System.out.println("=== sleep() holds the lock ===");
        Thread sleepHolder = new Thread(() -> {
            synchronized (monitor) {
                System.out.println("  [sleepHolder] Acquired lock, sleeping for 1s...");
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("  [sleepHolder] Woke up, still have lock, releasing now");
            }
        });

        Thread sleepWaiter = new Thread(() -> {
            System.out.println("  [sleepWaiter] Trying to acquire lock...");
            long start = System.currentTimeMillis();
            synchronized (monitor) {
                long waited = System.currentTimeMillis() - start;
                System.out.println("  [sleepWaiter] Got lock after " + waited + "ms (blocked while other slept)");
            }
        });

        sleepHolder.start();
        Thread.sleep(50);  // ensure sleepHolder gets lock first
        sleepWaiter.start();
        sleepHolder.join();
        sleepWaiter.join();

        // === wait() RELEASES the lock ===
        System.out.println("\n=== wait() releases the lock ===");
        Thread waitHolder = new Thread(() -> {
            synchronized (monitor) {
                System.out.println("  [waitHolder] Acquired lock, calling wait()...");
                try { monitor.wait(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("  [waitHolder] Woke up from wait, re-acquired lock");
            }
        });

        Thread waitWaiter = new Thread(() -> {
            System.out.println("  [waitWaiter] Trying to acquire lock...");
            long start = System.currentTimeMillis();
            synchronized (monitor) {
                long waited = System.currentTimeMillis() - start;
                System.out.println("  [waitWaiter] Got lock after " + waited + "ms (available because wait() released it)");
                monitor.notify();  // wake up waitHolder
            }
        });

        waitHolder.start();
        Thread.sleep(50);
        waitWaiter.start();
        waitHolder.join();
        waitWaiter.join();

        // === wait() must be in synchronized block ===
        System.out.println("\n=== wait() without synchronized throws exception ===");
        try {
            monitor.wait();  // not in synchronized block!
        } catch (IllegalMonitorStateException e) {
            System.out.println("Caught: " + e.getClass().getSimpleName()
                    + " — wait() requires synchronized block");
        }

        // === Spurious wakeup — why use while loop ===
        System.out.println("\n=== Spurious wakeup protection ===");
        System.out.println("WRONG:  if (condition) { obj.wait(); }");
        System.out.println("RIGHT:  while (condition) { obj.wait(); }");
        System.out.println("Reason: Thread can wake up without notify (spurious wakeup)");

        // === sleep(0) vs yield() ===
        System.out.println("\n=== sleep() vs yield() ===");
        System.out.println("sleep(0): Gives up remaining time slice, goes to TIMED_WAITING briefly");
        System.out.println("yield():  Hint to scheduler, may be ignored. Thread stays RUNNABLE");
    }
}
