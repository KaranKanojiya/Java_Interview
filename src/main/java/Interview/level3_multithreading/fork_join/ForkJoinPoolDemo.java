package interview.level3_multithreading.fork_join;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * ============================================================================
 * FORK/JOIN FRAMEWORK — java.util.concurrent.ForkJoinPool
 * Level: 3 — Advanced Concurrency Deep Dive
 * ============================================================================
 *
 * WHAT IS IT?
 * ───────────
 * A specialized thread pool designed for DIVIDE-AND-CONQUER parallelism.
 * It recursively breaks a large task into smaller subtasks, processes them
 * in parallel, and combines the results.
 *
 * Core components:
 *   - ForkJoinPool: the thread pool (uses work-stealing)
 *   - ForkJoinTask: base class for tasks
 *   - RecursiveTask<V>: returns a result (like Callable)
 *   - RecursiveAction: no result (like Runnable)
 *
 * ============================================================================
 * WORK-STEALING ALGORITHM
 * ============================================================================
 *
 * Traditional ThreadPool:
 *   - Single shared work queue
 *   - All threads compete for tasks (contention)
 *   - If one thread finishes early, it sits idle
 *
 * ForkJoinPool Work-Stealing:
 *   - Each thread has its OWN deque (double-ended queue)
 *   - A thread pushes/pops tasks from its OWN deque (no contention)
 *   - When a thread's deque is empty, it STEALS from another thread's
 *     deque (from the OPPOSITE end to minimize contention)
 *
 *   Thread-0 deque: [task1, task2, task3] ← push/pop this end
 *   Thread-1 deque: [task4, task5]        ← push/pop this end
 *   Thread-2 deque: []                    → steals from Thread-0's OTHER end!
 *
 * Benefits:
 *   - Less contention (each thread mostly uses its own deque)
 *   - Better load balancing (idle threads steal work)
 *   - Naturally suits recursive decomposition
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "Explain work-stealing in ForkJoinPool."
 * A: "Each worker thread has its own deque. When a task forks subtasks, they
 *     go into the current thread's deque. The thread pops tasks from one end
 *     (LIFO). When a thread runs out of work, it steals tasks from another
 *     thread's deque (from the opposite end, FIFO). This minimizes
 *     contention and ensures good load balancing."
 *
 * Q: "How does parallel stream use ForkJoinPool?"
 * A: "parallel streams use the common ForkJoinPool (ForkJoinPool.commonPool()).
 *     The common pool has (Runtime.availableProcessors() - 1) threads by
 *     default. All parallel streams in the JVM share this pool. You can
 *     change its size via -Djava.util.concurrent.ForkJoinPool.common.parallelism=N,
 *     or submit to a custom ForkJoinPool to avoid contention."
 *
 * Q: "RecursiveTask vs RecursiveAction?"
 * A: "RecursiveTask<V> returns a result (compute() returns V).
 *     RecursiveAction has no result (compute() returns void).
 *     Use RecursiveTask for reductions (sum, max, merge sort result).
 *     Use RecursiveAction for side-effect operations (in-place sort, update)."
 *
 * Q: "When should you NOT use ForkJoinPool?"
 * A: "For I/O-bound tasks — ForkJoinPool is designed for CPU-bound work.
 *     Blocking I/O wastes threads and can cause thread starvation. For I/O,
 *     use a regular ThreadPoolExecutor or virtual threads (Java 21+)."
 */
public class ForkJoinPoolDemo {

    public static void main(String[] args) {
        System.out.println("=== ForkJoinPool Demo ===\n");

        demoRecursiveTaskSum();
        System.out.println();
        demoRecursiveActionSort();
        System.out.println();
        demoCommonPool();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. RecursiveTask: Parallel sum of a large array
    // ─────────────────────────────────────────────────────────────────────────
    static void demoRecursiveTaskSum() {
        System.out.println("── RecursiveTask: Parallel Array Sum ──\n");

        int[] array = new int[10_000_000];
        for (int i = 0; i < array.length; i++) array[i] = i + 1;

        // Sequential sum for comparison
        long seqStart = System.nanoTime();
        long seqSum = 0;
        for (int val : array) seqSum += val;
        long seqTime = System.nanoTime() - seqStart;

        // Parallel sum using ForkJoinPool
        ForkJoinPool pool = new ForkJoinPool(); // default: availableProcessors threads
        long parStart = System.nanoTime();
        long parSum = pool.invoke(new SumTask(array, 0, array.length));
        long parTime = System.nanoTime() - parStart;

        System.out.printf("  Array size: %,d%n", array.length);
        System.out.printf("  Sequential sum: %,d (%d ms)%n", seqSum, seqTime / 1_000_000);
        System.out.printf("  Parallel sum:   %,d (%d ms)%n", parSum, parTime / 1_000_000);
        System.out.printf("  Pool parallelism: %d threads%n", pool.getParallelism());
        pool.shutdown();
    }

    /**
     * RecursiveTask that computes the sum of an array segment.
     * Forks into subtasks when the segment is larger than the threshold.
     *
     * PATTERN (divide-and-conquer):
     *   if (problem is small enough)
     *       solve directly
     *   else
     *       fork subtasks
     *       join results
     *       combine
     */
    static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 100_000; // Sequential threshold
        private final int[] array;
        private final int start, end;

        SumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;

            // Base case: small enough to compute sequentially
            if (length <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }

            // Recursive case: split into two subtasks
            int mid = start + length / 2;
            SumTask left = new SumTask(array, start, mid);
            SumTask right = new SumTask(array, mid, end);

            // Fork left task (runs asynchronously in the pool)
            left.fork();

            // Compute right task in current thread (optimization!)
            long rightResult = right.compute();

            // Join left result (waits if not done yet)
            long leftResult = left.join();

            return leftResult + rightResult;

            /*
             * WHY compute() on right and fork() on left?
             * ─────────────────────────────────────────────
             * If we fork() both, the current thread just waits — wasteful.
             * By computing one branch in the current thread, we use it
             * productively. This is the standard fork/join pattern:
             *
             *   left.fork();           // schedule left in pool
             *   right.compute();       // compute right in THIS thread
             *   left.join();           // get left result when ready
             */
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. RecursiveAction: Parallel merge sort (in-place)
    // ─────────────────────────────────────────────────────────────────────────
    static void demoRecursiveActionSort() {
        System.out.println("── RecursiveAction: Parallel Merge Sort ──\n");

        int[] array = {38, 27, 43, 3, 9, 82, 10, 55, 14, 67, 31, 42};
        System.out.println("  Before: " + Arrays.toString(array));

        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new MergeSortAction(array, 0, array.length));
        pool.shutdown();

        System.out.println("  After:  " + Arrays.toString(array));
    }

    /**
     * RecursiveAction for parallel merge sort.
     * No return value — sorts the array in-place.
     */
    static class MergeSortAction extends RecursiveAction {
        private static final int THRESHOLD = 4; // Small threshold for demo
        private final int[] array;
        private final int start, end;

        MergeSortAction(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            int length = end - start;

            // Base case: small enough for insertion sort
            if (length <= THRESHOLD) {
                Arrays.sort(array, start, end);
                return;
            }

            // Split
            int mid = start + length / 2;
            MergeSortAction left = new MergeSortAction(array, start, mid);
            MergeSortAction right = new MergeSortAction(array, mid, end);

            // Fork both (for RecursiveAction, invokeAll is idiomatic)
            invokeAll(left, right);

            // Merge the two sorted halves
            merge(array, start, mid, end);
        }

        private void merge(int[] arr, int start, int mid, int end) {
            int[] temp = Arrays.copyOfRange(arr, start, mid);
            int i = 0, j = mid, k = start;

            while (i < temp.length && j < end) {
                if (temp[i] <= arr[j]) {
                    arr[k++] = temp[i++];
                } else {
                    arr[k++] = arr[j++];
                }
            }
            while (i < temp.length) {
                arr[k++] = temp[i++];
            }
            // Elements from j to end are already in place
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. COMMON POOL — shared by all parallel streams
    // ─────────────────────────────────────────────────────────────────────────
    static void demoCommonPool() {
        System.out.println("── Common ForkJoinPool (used by parallel streams) ──\n");

        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        System.out.println("  Common pool parallelism: " + commonPool.getParallelism());
        System.out.println("  Available processors: " + Runtime.getRuntime().availableProcessors());

        // Parallel stream uses the common pool
        long sum = java.util.stream.LongStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
        System.out.println("  Parallel stream sum(1..1M): " + sum);

        // Running a parallel stream in a CUSTOM pool to avoid common pool contention
        System.out.println("\n  Running parallel stream in CUSTOM ForkJoinPool:");
        ForkJoinPool customPool = new ForkJoinPool(2);  // only 2 threads
        try {
            long customSum = customPool.submit(() ->
                java.util.stream.LongStream.rangeClosed(1, 1_000_000)
                    .parallel()
                    .sum()
            ).join();
            System.out.println("  Custom pool (2 threads) sum: " + customSum);
        } finally {
            customPool.shutdown();
        }

        /*
         * WHY USE A CUSTOM POOL FOR PARALLEL STREAMS?
         *
         * Problem: ALL parallel streams share the common ForkJoinPool.
         * If one stream does I/O or slow work, it starves other streams.
         *
         * Solution: Submit the parallel stream to a custom pool:
         *   ForkJoinPool custom = new ForkJoinPool(4);
         *   custom.submit(() -> myList.parallelStream().map(...).collect(...));
         *
         * This isolates slow streams from the common pool.
         *
         * CONFIGURE COMMON POOL SIZE:
         *   -Djava.util.concurrent.ForkJoinPool.common.parallelism=8
         */
    }
}
