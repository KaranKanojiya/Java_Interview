package interview.level6_jvm_internals.profiling;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q12. How do you read and analyze a thread dump?
 *
 * A thread dump is a snapshot of all threads in a JVM at a point in time.
 *
 * How to capture:
 *   - jstack <pid>
 *   - jcmd <pid> Thread.print
 *   - kill -3 <pid>  (SIGQUIT on Unix — prints to stdout)
 *   - In code: Thread.getAllStackTraces() or ThreadMXBean
 *
 * Thread dump format:
 *   "thread-name" #id daemon prio=5 os_prio=0 tid=0x... nid=0x... STATE
 *       at com.example.MyClass.myMethod(MyClass.java:42)
 *       - waiting to lock <0x...> (a java.lang.Object)
 *       - locked <0x...> (a java.lang.Object)
 *
 * Key thread states to look for:
 *   RUNNABLE       → thread is executing (or ready)
 *   BLOCKED        → waiting for monitor lock → look for lock contention
 *   WAITING        → wait(), join(), park() → check what it's waiting for
 *   TIMED_WAITING  → sleep(), wait(ms) → usually OK (pooled threads)
 *
 * Red flags:
 *   1. Many threads BLOCKED on same lock → lock contention
 *   2. Deadlock detected → circular lock dependency
 *   3. Thread count growing → thread leak
 *   4. Many threads in same method → bottleneck
 *   5. RUNNABLE doing I/O → thread may be stuck on network/disk
 */
public class ThreadDumpReading {

    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) throws InterruptedException {

        // === Programmatic thread dump ===
        System.out.println("=== Programmatic Thread Dump ===");
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threads = threadBean.dumpAllThreads(true, true);

        for (ThreadInfo ti : threads) {
            System.out.println("Thread: " + ti.getThreadName());
            System.out.println("  State: " + ti.getThreadState());
            if (ti.getLockName() != null) {
                System.out.println("  Waiting on: " + ti.getLockName());
            }
            StackTraceElement[] stack = ti.getStackTrace();
            for (int i = 0; i < Math.min(3, stack.length); i++) {
                System.out.println("    at " + stack[i]);
            }
            if (stack.length > 3) System.out.println("    ... " + (stack.length - 3) + " more");
            System.out.println();
        }

        // === Create a deadlock scenario for detection ===
        System.out.println("=== Creating deadlock for detection ===");
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                synchronized (lockB) { System.out.println("T1 acquired both locks"); }
            }
        }, "DeadlockThread-1");

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                synchronized (lockA) { System.out.println("T2 acquired both locks"); }
            }
        }, "DeadlockThread-2");

        t1.start();
        t2.start();
        Thread.sleep(500);  // let deadlock form

        // === Detect deadlock programmatically ===
        System.out.println("=== Deadlock Detection ===");
        long[] deadlockedIds = threadBean.findDeadlockedThreads();
        if (deadlockedIds != null) {
            System.out.println("DEADLOCK DETECTED! Threads involved:");
            ThreadInfo[] deadlocked = threadBean.getThreadInfo(deadlockedIds, true, true);
            for (ThreadInfo ti : deadlocked) {
                System.out.println("  Thread: " + ti.getThreadName());
                System.out.println("  State:  " + ti.getThreadState());
                System.out.println("  Waiting to lock: " + ti.getLockName());
                System.out.println("  Held by: " + ti.getLockOwnerName());
                System.out.println();
            }
        } else {
            System.out.println("No deadlock detected");
        }

        // Clean up deadlocked threads
        t1.interrupt();
        t2.interrupt();

        // === Thread count monitoring ===
        System.out.println("=== Thread Statistics ===");
        System.out.println("Live threads:   " + threadBean.getThreadCount());
        System.out.println("Peak threads:   " + threadBean.getPeakThreadCount());
        System.out.println("Daemon threads: " + threadBean.getDaemonThreadCount());
        System.out.println("Total started:  " + threadBean.getTotalStartedThreadCount());

        // === How to read a thread dump (guide) ===
        System.out.println("\n=== Thread Dump Reading Guide ===");
        System.out.println("1. Capture: jstack <pid> > threaddump.txt");
        System.out.println("2. Search for 'BLOCKED' → lock contention");
        System.out.println("3. Search for 'deadlock' → JVM auto-detects deadlocks");
        System.out.println("4. Count threads per state:");
        System.out.println("   grep 'java.lang.Thread.State' threaddump.txt | sort | uniq -c");
        System.out.println("5. Find hot methods:");
        System.out.println("   grep -A1 'RUNNABLE' threaddump.txt | grep 'at ' | sort | uniq -c | sort -rn");
        System.out.println("6. Tools: fastthread.io (online analyzer), TDA (Thread Dump Analyzer)");
    }
}
