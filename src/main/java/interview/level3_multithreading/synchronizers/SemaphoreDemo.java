package interview.level3_multithreading.synchronizers;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================================
 * SEMAPHORE — java.util.concurrent.Semaphore
 * Level: 3 — Advanced Concurrency Deep Dive
 * ============================================================================
 *
 * WHAT IS IT?
 * ───────────
 * A counting semaphore that maintains a set of PERMITS. Threads acquire
 * permits before accessing a resource, and release them when done.
 *
 * Think of it as a parking lot:
 *   - The lot has N spaces (permits)
 *   - Cars enter (acquire) if a space is available
 *   - Cars leave (release) to free up a space
 *   - If full, cars wait until a space opens
 *
 * Key properties:
 *   - Permits are NOT tied to threads (any thread can release)
 *   - Supports fairness (FIFO ordering of waiting threads)
 *   - acquire() blocks, tryAcquire() does not
 *   - Can acquire/release multiple permits at once
 *
 * ============================================================================
 * REAL-WORLD USE CASES
 * ============================================================================
 *
 * 1. CONNECTION POOL: Limit concurrent DB connections to N
 * 2. RATE LIMITER: Allow at most N requests per time window
 * 3. RESOURCE ACCESS: Limit concurrent file reads, API calls, etc.
 * 4. BOUNDED BUFFER: Control producer-consumer with limited capacity
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "How does Semaphore differ from Lock?"
 * A: "Lock (ReentrantLock) allows exactly ONE thread — it's a binary
 *     semaphore. Semaphore allows N threads concurrently. Lock is
 *     OWNED by the locking thread (only that thread can unlock).
 *     Semaphore permits are NOT owned — any thread can release.
 *     Lock is for mutual exclusion. Semaphore is for resource counting."
 *
 * Q: "What's a binary semaphore vs mutex?"
 * A: "A binary semaphore (permits=1) looks like a mutex, but differs:
 *     a mutex has OWNERSHIP (only the locking thread can unlock), while
 *     a binary semaphore has no ownership (any thread can release).
 *     ReentrantLock is a mutex. Semaphore(1) is a binary semaphore."
 *
 * Q: "What's a fair semaphore?"
 * A: "new Semaphore(n, true) creates a fair semaphore that grants permits
 *     in FIFO order. Without fairness, a thread could starve. Fair
 *     semaphores have slightly lower throughput due to ordering overhead."
 *
 * Q: "Can you increase permits beyond the initial count?"
 * A: "Yes. release() can be called without a prior acquire(), increasing
 *     the permit count. This is useful for dynamic resource pools."
 */
public class SemaphoreDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Semaphore Demo ===\n");

        demoConnectionPool();
        System.out.println();
        demoRateLimiter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 1: CONNECTION POOL — limit concurrent access
    // ─────────────────────────────────────────────────────────────────────────
    static void demoConnectionPool() throws InterruptedException {
        System.out.println("── Scenario: Connection Pool (3 connections, 8 clients) ──\n");

        int maxConnections = 3;
        // Fair semaphore: threads served in FIFO order
        Semaphore connectionPool = new Semaphore(maxConnections, true);

        int clientCount = 8;
        Thread[] clients = new Thread[clientCount];

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            clients[i] = new Thread(() -> {
                try {
                    System.out.println("  Client-" + clientId + " requesting connection... "
                            + "(available: " + connectionPool.availablePermits() + ")");

                    // Acquire a permit (blocks if none available)
                    connectionPool.acquire();

                    System.out.println("  Client-" + clientId + " ACQUIRED connection! "
                            + "(available: " + connectionPool.availablePermits()
                            + ", waiting: " + connectionPool.getQueueLength() + ")");

                    // Simulate database work
                    Thread.sleep((long) (Math.random() * 1000 + 500));

                    System.out.println("  Client-" + clientId + " releasing connection.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    connectionPool.release();  // Always release in finally!
                }
            }, "Client-" + i);
            clients[i].start();

            // Stagger client arrivals slightly
            Thread.sleep(100);
        }

        // Wait for all clients to finish
        for (Thread client : clients) {
            client.join();
        }

        System.out.println("\n  All clients served. Available permits: "
                + connectionPool.availablePermits());

        /*
         * FLOW (with 3 permits):
         *
         *   Time →
         *   Client-0: [acquire] ████████ [release]
         *   Client-1: [acquire] ██████████████ [release]
         *   Client-2: [acquire] ████████████ [release]
         *   Client-3:           [wait...] [acquire] ████████ [release]
         *   Client-4:           [wait.......] [acquire] ██████ [release]
         *   ...
         *
         *   At most 3 clients hold connections at any time.
         */
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scenario 2: RATE LIMITER — tryAcquire with timeout
    // ─────────────────────────────────────────────────────────────────────────
    static void demoRateLimiter() throws InterruptedException {
        System.out.println("── Scenario: Rate Limiter (max 2 concurrent API calls) ──\n");

        int maxConcurrent = 2;
        Semaphore rateLimiter = new Semaphore(maxConcurrent);

        int requestCount = 6;
        Thread[] requests = new Thread[requestCount];

        for (int i = 0; i < requestCount; i++) {
            final int reqId = i;
            requests[i] = new Thread(() -> {
                try {
                    // Try to acquire with timeout (non-blocking alternative)
                    boolean acquired = rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS);

                    if (acquired) {
                        try {
                            System.out.println("  Request-" + reqId + " ACCEPTED (processing...)");
                            Thread.sleep((long) (Math.random() * 800 + 200));
                            System.out.println("  Request-" + reqId + " completed.");
                        } finally {
                            rateLimiter.release();
                        }
                    } else {
                        // Timeout — request rejected
                        System.out.println("  Request-" + reqId + " REJECTED (rate limited!)");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Request-" + i);
        }

        // Start all requests nearly simultaneously
        for (Thread request : requests) {
            request.start();
        }

        for (Thread request : requests) {
            request.join();
        }

        System.out.println("\n  Rate limiter summary:");
        System.out.println("  - Max concurrent: " + maxConcurrent);
        System.out.println("  - Total requests: " + requestCount);
        System.out.println("  - Some rejected due to timeout (rate limiting)");

        /*
         * SEMAPHORE API SUMMARY:
         *
         *   acquire()                    — blocking, decrements permit
         *   acquire(int permits)         — acquire multiple permits
         *   release()                    — increments permit (any thread)
         *   release(int permits)         — release multiple permits
         *   tryAcquire()                 — non-blocking, returns boolean
         *   tryAcquire(time, unit)       — blocks up to timeout
         *   availablePermits()           — current available permits
         *   getQueueLength()             — estimated waiting threads
         *   drainPermits()               — acquire all available permits
         *   hasQueuedThreads()           — are any threads waiting?
         */
    }
}
