package Interview.MultiThreading.ParallelismAndConcurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyExample {

    // Shared variable accessed by multiple threads
    private static AtomicInteger totalTicketAvailable = new AtomicInteger(2);

    static class TicketBookingTask implements Runnable {
        @Override
        public void run() {
            // Booking logic
            if (totalTicketAvailable.get() > 0) {
                System.out.println(Thread.currentThread().getName() + " booked a ticket.");
                // Simulate some processing time
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Decrease the available ticket count
                totalTicketAvailable.decrementAndGet();
                System.out.println("Remaining tickets: " + totalTicketAvailable);
            } else {
                System.out.println("Sorry, " + Thread.currentThread().getName() + " No tickets available.");
            }
        }
    }

    public static void main(String[] args) {
        // Create a fixed-size thread pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Start multiple threads using Java 8 features
        for (int i = 1; i <= 5; i++) {
            executor.execute(new TicketBookingTask());
        }

        // Shutdown the executor to terminate all threads when tasks are completed
        executor.shutdown();
    }
}
