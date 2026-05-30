package interview.level3_multithreading.patterns;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q18. What is the difference between wait/notify and Lock/Condition?
 *
 * | Feature            | wait/notify (Object)     | Lock/Condition (java.util.concurrent) |
 * |-------------------|--------------------------|---------------------------------------|
 * | Lock type         | Intrinsic (synchronized) | Explicit (ReentrantLock)              |
 * | Multiple conditions| No (one wait set)       | Yes (multiple Condition objects)       |
 * | Fairness          | No control               | Configurable (fair lock)              |
 * | Interruptible     | Yes (InterruptedException)| Yes + tryLock with timeout            |
 * | Must hold lock    | Yes (synchronized block) | Yes (lock.lock())                     |
 * | Spurious wakeups  | Must use while loop      | Must use while loop                   |
 *
 * Key advantage of Condition: You can have MULTIPLE conditions on one lock.
 * Example: Producer-Consumer with separate "notFull" and "notEmpty" conditions.
 * With wait/notify, you can only have ONE wait set per object.
 */
public class WaitNotifyVsLockCondition {

    // === Approach 1: wait/notify (old way) ===
    static class WaitNotifyQueue {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity = 3;

        synchronized void produce(int item) throws InterruptedException {
            while (queue.size() == capacity) {
                System.out.println("  [wait/notify] Queue full, producer waiting...");
                wait();  // releases the intrinsic lock
            }
            queue.add(item);
            System.out.println("  [wait/notify] Produced: " + item + " | Queue: " + queue);
            notifyAll();  // wakes ALL waiting threads (both producers and consumers)
        }

        synchronized int consume() throws InterruptedException {
            while (queue.isEmpty()) {
                System.out.println("  [wait/notify] Queue empty, consumer waiting...");
                wait();
            }
            int item = queue.poll();
            System.out.println("  [wait/notify] Consumed: " + item + " | Queue: " + queue);
            notifyAll();  // wakes ALL — wasteful, wakes producers AND consumers
            return item;
        }
    }

    // === Approach 2: Lock/Condition (better way) ===
    static class LockConditionQueue {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity = 3;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();   // separate condition for producers
        private final Condition notEmpty = lock.newCondition();  // separate condition for consumers

        void produce(int item) throws InterruptedException {
            lock.lock();
            try {
                while (queue.size() == capacity) {
                    System.out.println("  [Lock/Condition] Queue full, producer waiting on notFull...");
                    notFull.await();  // only producers wait here
                }
                queue.add(item);
                System.out.println("  [Lock/Condition] Produced: " + item + " | Queue: " + queue);
                notEmpty.signal();  // wake ONLY a consumer, not all threads
            } finally {
                lock.unlock();
            }
        }

        int consume() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) {
                    System.out.println("  [Lock/Condition] Queue empty, consumer waiting on notEmpty...");
                    notEmpty.await();  // only consumers wait here
                }
                int item = queue.poll();
                System.out.println("  [Lock/Condition] Consumed: " + item + " | Queue: " + queue);
                notFull.signal();  // wake ONLY a producer
                return item;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // === Demo 1: wait/notify ===
        System.out.println("=== wait/notify approach ===");
        WaitNotifyQueue wnQueue = new WaitNotifyQueue();

        Thread wnProducer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) { wnQueue.produce(i); Thread.sleep(50); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread wnConsumer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) { wnQueue.consume(); Thread.sleep(100); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        wnProducer.start(); wnConsumer.start();
        wnProducer.join(); wnConsumer.join();

        // === Demo 2: Lock/Condition ===
        System.out.println("\n=== Lock/Condition approach (better) ===");
        LockConditionQueue lcQueue = new LockConditionQueue();

        Thread lcProducer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) { lcQueue.produce(i); Thread.sleep(50); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread lcConsumer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) { lcQueue.consume(); Thread.sleep(100); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        lcProducer.start(); lcConsumer.start();
        lcProducer.join(); lcConsumer.join();

        System.out.println("\n=== Summary ===");
        System.out.println("wait/notify:    notifyAll() wakes ALL threads (wasteful)");
        System.out.println("Lock/Condition: signal() wakes only the RIGHT type of thread (targeted)");
    }
}
