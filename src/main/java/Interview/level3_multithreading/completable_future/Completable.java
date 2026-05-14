package interview.level3_multithreading.completable_future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Completable {

    public static void main(String[] args) {
        for (int i = 1; i <=100; i++) {
            ExecutorService cpuBound = Executors.newFixedThreadPool(4);
            ExecutorService ioBound = Executors.newFixedThreadPool(4); // Fixed thread pool with 4 threads

            CompletableFuture.supplyAsync(() -> getOrder(), ioBound) // will run with the Thread present in ioBound ThreadPool
                    .thenApplyAsync(order -> {
                        System.out.println("Enriching order: " + order + " on Thread: " + Thread.currentThread().getName());
                        return enrich(order);
                    }, cpuBound) // will run with the Thread present in cpuBound ThreadPool
                    .thenApplyAsync(order -> {
                        System.out.println("Performing payment for order: " + order + " on Thread: " + Thread.currentThread().getName());
                        return performPayment(order);
                    }, ioBound)
                    .thenApplyAsync(order -> {
                        System.out.println("Enriching order again: " + order + " on Thread: " + Thread.currentThread().getName());
                        return enrich(order);
                    }, cpuBound)
                    .thenAccept(order -> {
                        System.out.println("Sending email for order: " + order + " on Thread: " + Thread.currentThread().getName());
                        sendEmail(order);
                    });
        }

    }

    // Placeholder methods
    private static String getOrder() {
        return "SomeOrder";
    }

    private static String enrich(String order) {
        return order + " (Enriched)";
    }

    private static String performPayment(String order) {
        return order + " (Payment Done)";
    }

    private static void sendEmail(String order) {
        System.out.println("Email sent for order: " + order);
    }
}

