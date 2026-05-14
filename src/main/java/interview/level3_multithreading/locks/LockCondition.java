package interview.level3_multithreading.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockCondition {

    private Lock lock = new ReentrantLock();
    private Condition added = lock.newCondition();
    private Condition removed = lock.newCondition();
    private int count = 0; // Track the count of items in the buffer

    // Placeholder methods for adding and getting data
    private void addData() {
        // Logic to add data to the buffer
        System.out.println("Data added to buffer.");
        count++;
    }

    private String getData() {
        // Logic to get data from the buffer
        System.out.println("Data retrieved from buffer.");
        count--;
        return "SomeData"; // Placeholder for actual data retrieval
    }

    // Producer
    public void produce() throws InterruptedException {
        lock.lock();
        try {
            while (count == Integer.MAX_VALUE) {
                removed.await();
            }
            addData();
            added.signal();
        } finally {
            lock.unlock();
        }
    }

    // Consumer
    public void consume() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                added.await();
            }
            String data = getData();
            removed.signal();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        LockCondition lockCondition = new LockCondition();
        // Example usage: start producer and consumer threads
        new Thread(() -> {
            try {
                lockCondition.produce();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                lockCondition.consume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
