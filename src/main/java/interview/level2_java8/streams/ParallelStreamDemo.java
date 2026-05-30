package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Q8. What is the difference between Stream and Parallel Stream?
 *
 * Sequential Stream:
 *   - Processes elements one by one, in encounter order
 *   - Uses a single thread (the calling thread)
 *   - Predictable, ordered output
 *
 * Parallel Stream:
 *   - Splits data into chunks, processes in parallel using ForkJoinPool.commonPool()
 *   - Default parallelism = Runtime.getRuntime().availableProcessors() - 1
 *   - Order may NOT be preserved (unless you use forEachOrdered)
 *
 * When to use Parallel Stream:
 *   ✅ Large dataset (10,000+ elements)
 *   ✅ CPU-intensive operations (no I/O, no shared mutable state)
 *   ✅ Stateless, independent operations
 *
 * When NOT to use:
 *   ❌ Small datasets (overhead > benefit)
 *   ❌ I/O operations (threads block, no speedup)
 *   ❌ Order-dependent operations
 *   ❌ Shared mutable state (race conditions!)
 *   ❌ LinkedList (poor splittability — use ArrayList or arrays)
 */
public class ParallelStreamDemo {

    public static void main(String[] args) {

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // === Sequential Stream ===
        System.out.println("=== Sequential Stream ===");
        numbers.stream()
                .map(n -> {
                    System.out.println("Processing " + n + " on " + Thread.currentThread().getName());
                    return n * 2;
                })
                .forEach(n -> {});

        // === Parallel Stream ===
        System.out.println("\n=== Parallel Stream ===");
        numbers.parallelStream()
                .map(n -> {
                    System.out.println("Processing " + n + " on " + Thread.currentThread().getName());
                    return n * 2;
                })
                .forEach(n -> {});

        // === Order difference ===
        System.out.println("\n=== forEach vs forEachOrdered (parallel) ===");
        System.out.print("forEach (unordered):    ");
        numbers.parallelStream().forEach(n -> System.out.print(n + " "));

        System.out.print("\nforEachOrdered (ordered): ");
        numbers.parallelStream().forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        // === Performance comparison ===
        System.out.println("\n=== Performance: Large dataset ===");
        List<Integer> largeList = Arrays.asList(new Integer[1_000_000]);
        for (int i = 0; i < largeList.size(); i++) largeList.set(i, i);

        long start = System.currentTimeMillis();
        long seqSum = largeList.stream()
                .mapToLong(n -> (long) n * n)
                .sum();
        long seqTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        long parSum = largeList.parallelStream()
                .mapToLong(n -> (long) n * n)
                .sum();
        long parTime = System.currentTimeMillis() - start;

        System.out.println("Sequential sum: " + seqSum + " in " + seqTime + "ms");
        System.out.println("Parallel sum:   " + parSum + " in " + parTime + "ms");

        // === DANGER: Shared mutable state ===
        System.out.println("\n=== DANGER: Shared mutable state with parallel stream ===");
        List<Integer> unsafeList = new java.util.ArrayList<>();
        List<Integer> safeList = new CopyOnWriteArrayList<>();

        // This is WRONG — race condition with ArrayList
        numbers.parallelStream().forEach(unsafeList::add);
        System.out.println("Unsafe (ArrayList) size: " + unsafeList.size() + " (may be < 10!)");

        // Correct approach 1: thread-safe collection
        numbers.parallelStream().forEach(safeList::add);
        System.out.println("Safe (COWAL) size: " + safeList.size());

        // Correct approach 2: use collect() — the BEST way
        List<Integer> collected = numbers.parallelStream()
                .map(n -> n * 2)
                .collect(Collectors.toList());
        System.out.println("Collected size: " + collected.size() + " → " + collected);
    }
}
