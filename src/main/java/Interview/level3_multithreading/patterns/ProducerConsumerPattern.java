package interview.level3_multithreading.patterns;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================================
 * PRODUCER-CONSUMER PATTERN — BlockingQueue-based
 * Level: 3 — Advanced Concurrency Deep Dive
 * ============================================================================
 *
 * WHAT IS IT?
 * ───────────
 * A classic concurrency pattern where:
 *   - PRODUCERS generate data and put it into a shared buffer
 *   - CONSUMERS take data from the buffer and process it
 *   - The buffer decouples producers from consumers
 *
 * BlockingQueue is the ideal buffer:
 *   - put() blocks if the queue is full (back-pressure!)
 *   - take() blocks if the queue is empty (no busy-waiting!)
 *   - Thread-safe without explicit locking
 *
 * ============================================================================
 * BLOCKINGQUEUE IMPLEMENTATIONS
 * ============================================================================
 *
 *   ArrayBlockingQueue:
 *     - Fixed capacity, backed by array
 *     - Fair/unfair ordering
 *     - Best for known, fixed buffer size
 *
 *   LinkedBlockingQueue:
 *     - Optionally bounded (default: Integer.MAX_VALUE)
 *     - Separate locks for put and take (higher throughput)
 *     - Best for producer-consumer with variable load
 *
 *   PriorityBlockingQueue:
 *     - Unbounded, elements ordered by priority
 *     - Best for task scheduling with priorities
 *
 *   SynchronousQueue:
 *     - Zero capacity — each put() waits for a take()
 *     - Direct handoff from producer to consumer
 *     - Used by Executors.newCachedThreadPool()
 *
 *   DelayQueue:
 *     - Elements become available only after a delay
 *     - Best for scheduled task execution
 *
 * ============================================================================
 * POISON PILL SHUTDOWN PATTERN
 * ============================================================================
 *
 * Problem: How do consumers know when to stop?
 * Solution: Producers send a special "poison pill" message that signals
 *           "no more data, shut down."
 *
 * Rules:
 *   - Poison pill must be distinguishable from real data
 *   - Each consumer must receive exactly one poison pill
 *   - Producers send poison pills AFTER all real data
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "Implement producer-consumer in Java."
 * A: "Use a BlockingQueue. Producers call put() (blocks if full), consumers
 *     call take() (blocks if empty). For shutdown, use the poison pill
 *     pattern — send a special sentinel value that tells consumers to stop.
 *     BlockingQueue handles all synchronization internally."
 *
 * Q: "ArrayBlockingQueue vs LinkedBlockingQueue?"
 * A: "ArrayBlockingQueue uses a single lock for both put/take (simpler,
 *     lower throughput). LinkedBlockingQueue uses separate locks for
 *     put and take (higher throughput, more memory due to nodes).
 *     ArrayBlockingQueue is always bounded. LinkedBlockingQueue can be
 *     unbounded (dangerous — can cause OOM if consumer is slow)."
 *
 * Q: "What is back-pressure in producer-consumer?"
 * A: "When the queue is full, put() blocks the producer — this is
 *     back-pressure. It automatically slows down production when
 *     consumption can't keep up, preventing OOM and queue overflow."
 *
 * Q: "Why BlockingQueue over wait/notify?"
 * A: "BlockingQueue encapsulates all synchronization logic (wait, notify,
 *     locking). With wait/notify, you must handle spurious wakeups,
 *     proper lock ordering, and condition checks manually. BlockingQueue
 *     is higher-level, less error-prone, and more readable."
 */
public class ProducerConsumerPattern {

    // Poison pill — sentinel value that signals shutdown
    private static final String POISON_PILL = "__POISON_PILL__";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Producer-Consumer Pattern Demo ===\n");

        demoBasicProducerConsumer();
        System.out.println();
        demoMultiProducerMultiConsumer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 1: SINGLE PRODUCER, SINGLE CONSUMER
    // ─────────────────────────────────────────────────────────────────────────
    static void demoBasicProducerConsumer() throws InterruptedException {
        System.out.println("── Scenario: Single Producer, Single Consumer ──\n");

        // Bounded queue with capacity 5 (back-pressure when full)
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                String[] tasks = {"Task-A", "Task-B", "Task-C", "Task-D", "Task-E"};
                for (String task : tasks) {
                    System.out.println("  [Producer] Producing: " + task
                            + " (queue size: " + queue.size() + ")");
                    queue.put(task);  // Blocks if queue is full (back-pressure)
                    Thread.sleep(200); // Simulate production time
                }
                // Send poison pill to signal shutdown
                queue.put(POISON_PILL);
                System.out.println("  [Producer] Sent poison pill. Done.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String task = queue.take();  // Blocks if queue is empty
                    if (POISON_PILL.equals(task)) {
                        System.out.println("  [Consumer] Received poison pill. Shutting down.");
                        break;
                    }
                    System.out.println("  [Consumer] Processing: " + task);
                    Thread.sleep(500); // Simulate processing (slower than production)
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println("  Queue empty: " + queue.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2: MULTIPLE PRODUCERS, MULTIPLE CONSUMERS
    // ─────────────────────────────────────────────────────────────────────────
    static void demoMultiProducerMultiConsumer() throws InterruptedException {
        System.out.println("── Scenario: 2 Producers, 3 Consumers ──\n");

        int producerCount = 2;
        int consumerCount = 3;
        int itemsPerProducer = 5;

        // LinkedBlockingQueue: separate locks for put/take → higher throughput
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(10);

        AtomicInteger totalProduced = new AtomicInteger(0);
        AtomicInteger totalConsumed = new AtomicInteger(0);

        // Start producers
        Thread[] producers = new Thread[producerCount];
        for (int p = 0; p < producerCount; p++) {
            final int producerId = p;
            producers[p] = new Thread(() -> {
                try {
                    for (int i = 0; i < itemsPerProducer; i++) {
                        String item = "P" + producerId + "-Item" + i;
                        queue.put(item);
                        totalProduced.incrementAndGet();
                        System.out.println("  [Producer-" + producerId + "] Produced: " + item);
                        Thread.sleep((long) (Math.random() * 300 + 50));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Producer-" + p);
            producers[p].start();
        }

        // Start consumers
        Thread[] consumers = new Thread[consumerCount];
        for (int c = 0; c < consumerCount; c++) {
            final int consumerId = c;
            consumers[c] = new Thread(() -> {
                try {
                    while (true) {
                        String item = queue.take();
                        if (POISON_PILL.equals(item)) {
                            System.out.println("  [Consumer-" + consumerId
                                    + "] Received poison pill. Stopping.");
                            break;
                        }
                        totalConsumed.incrementAndGet();
                        System.out.println("  [Consumer-" + consumerId + "] Consumed: " + item);
                        Thread.sleep((long) (Math.random() * 500 + 100));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-" + c);
            consumers[c].start();
        }

        // Wait for all producers to finish
        for (Thread p : producers) {
            p.join();
        }
        System.out.println("\n  All producers done. Sending poison pills...");

        // Send one poison pill per consumer
        for (int i = 0; i < consumerCount; i++) {
            queue.put(POISON_PILL);
        }

        // Wait for all consumers to finish
        for (Thread c : consumers) {
            c.join();
        }

        System.out.println("\n  Total produced: " + totalProduced.get());
        System.out.println("  Total consumed: " + totalConsumed.get());
        System.out.println("  Queue empty: " + queue.isEmpty());

        /*
         * FLOW:
         *
         *   Producer-0 ──→ [P0-Item0, P0-Item1, ...] ──┐
         *                                                ↓
         *   Producer-1 ──→ [P1-Item0, P1-Item1, ...] ──→ BlockingQueue(10)
         *                                                ↓
         *   Consumer-0 ←── take() ←─────────────────────┤
         *   Consumer-1 ←── take() ←─────────────────────┤
         *   Consumer-2 ←── take() ←─────────────────────┘
         *
         * SHUTDOWN:
         *   Producers finish → send N poison pills → each consumer gets one
         */
    }
}
