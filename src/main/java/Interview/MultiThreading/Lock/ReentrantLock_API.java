package Interview.MultiThreading.Lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLock_API {

    private static Lock lock = new ReentrantLock(true); // Fair ReentrantLock by default

    public static void main(String[] args) throws InterruptedException {
        // Example for lock.getHoldCount()
        lock.lock();
        try {
            int holdCount = ((ReentrantLock) lock).getHoldCount();
            System.out.println("Hold count: " + holdCount);
        } finally {
            lock.unlock();
        }

        // Example for Fairness
        // Use lock as defined above, it's already a fair lock

        // Example for tryLock()
        if (lock.tryLock()) {
            try {
                // Lock acquired successfully
                System.out.println("Lock acquired successfully");
            } finally {
                lock.unlock();
            }
        } else {
            // Lock not acquired
            System.out.println("Lock not acquired");
        }

        // Example for Timeout tryLock(time)
        boolean lockAcquired = lock.tryLock(5, TimeUnit.SECONDS);
        if (lockAcquired) {
            try {
                // Lock acquired successfully
                System.out.println("Lock acquired within 5 seconds");
            } finally {
                lock.unlock();
            }
        } else {
            // Lock not acquired within 5 seconds
            System.out.println("Lock not acquired within 5 seconds");
        }
    }

}
