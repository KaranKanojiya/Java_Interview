package interview.level7_java25_26.stream_gatherers;

import java.util.*;
import java.util.stream.*;

/**
 * ============================================================================
 * STREAM GATHERERS — Java 22+ (JEP 461, finalized Java 24)
 * Level: 7 — Java 25/26 Awareness
 * Status: PREVIEW in Java 22, second preview Java 23, FINALIZED Java 24
 * ============================================================================
 *
 * WHAT PROBLEM DO STREAM GATHERERS SOLVE?
 * ────────────────────────────────────────
 * Before Gatherers, Stream had a fixed set of intermediate operations:
 *   map, filter, flatMap, peek, distinct, sorted, limit, skip, takeWhile, dropWhile
 *
 * If you needed a CUSTOM stateful intermediate operation (e.g., windowing,
 * running totals, deduplication with custom logic, rate-limiting), you had
 * two bad options:
 *   1. Collect to a list, process, then re-stream (breaks laziness)
 *   2. Abuse flatMap with external mutable state (fragile, not parallel-safe)
 *
 * Gatherers solve this by letting you write CUSTOM intermediate operations
 * that are:
 *   - Stateful (can maintain state across elements)
 *   - Composable (chain them like map/filter)
 *   - Parallelizable (optional combiner for parallel streams)
 *   - Lazy (process one element at a time, can short-circuit)
 *
 * ANALOGY:
 *   Collectors are custom TERMINAL operations  → stream.collect(myCollector)
 *   Gatherers are custom INTERMEDIATE operations → stream.gather(myGatherer)
 *
 * ============================================================================
 * BUILT-IN GATHERERS (java.util.stream.Gatherers)
 * ============================================================================
 *
 * 1. Gatherers.windowFixed(int size)
 *    Groups elements into fixed-size windows (lists).
 *    [1,2,3,4,5] with windowFixed(2) → [1,2], [3,4], [5]
 *
 * 2. Gatherers.windowSliding(int size)
 *    Sliding window of given size.
 *    [1,2,3,4,5] with windowSliding(3) → [1,2,3], [2,3,4], [3,4,5]
 *
 * 3. Gatherers.fold(Supplier init, BiFunction folder)
 *    Accumulates elements into a single result (emitted at end).
 *    Like reduce() but as an intermediate operation, and the accumulator
 *    type can differ from the element type.
 *
 * 4. Gatherers.scan(Supplier init, BiFunction scanner)
 *    Like fold but emits the running accumulation after EACH element.
 *    [1,2,3] with scan(0, Integer::sum) → 1, 3, 6
 *
 * 5. Gatherers.mapConcurrent(int maxConcurrency, Function mapper)
 *    Maps elements concurrently using virtual threads, with bounded
 *    concurrency. Preserves encounter order.
 *    Great for I/O-bound mapping (HTTP calls, DB lookups).
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "What problem do Stream Gatherers solve?"
 * A: "They allow custom stateful intermediate stream operations — something
 *     not possible with just map/filter/reduce. Before Gatherers, the only
 *     extension point was Collector (terminal). Gatherers fill the gap by
 *     letting you write reusable, composable, parallelizable intermediate
 *     operations like windowing, running totals, or concurrent mapping."
 *
 * Q: "How do Gatherers relate to Collectors?"
 * A: "Collectors customize the terminal operation (collect). Gatherers
 *     customize intermediate operations (gather). Both follow a similar
 *     pattern: initializer + integrator + (optional) combiner + finisher."
 *
 * Q: "Can Gatherers short-circuit?"
 * A: "Yes. The integrator returns a boolean — returning false signals that
 *     no more elements should be sent, enabling short-circuit behavior
 *     similar to limit() or takeWhile()."
 *
 * Q: "Are Gatherers parallelizable?"
 * A: "Yes, if you provide a combiner function. Without a combiner, the
 *     gatherer will force sequential processing even on parallel streams."
 */
public class StreamGatherersDemo {

    public static void main(String[] args) {
        System.out.println("=== Stream Gatherers Demo (Java 22+ / JEP 461) ===\n");

        // ─────────────────────────────────────────────
        // NOTE: The code below uses the Gatherers API.
        // It compiles on Java 22+ with --enable-preview,
        // or Java 24+ without preview flags.
        // We demonstrate the concepts with simulated output
        // so this file is informational on older JDKs.
        // ─────────────────────────────────────────────

        demoWindowFixed();
        demoWindowSliding();
        demoFold();
        demoScan();
        demoMapConcurrent();
        demoCustomGatherer();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. windowFixed(int size)
    // ─────────────────────────────────────────────────────────────────────────
    static void demoWindowFixed() {
        System.out.println("── windowFixed(3) ──");
        /*
         * ACTUAL API USAGE (Java 22+):
         *
         *   List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5, 6, 7)
         *       .gather(Gatherers.windowFixed(3))
         *       .toList();
         *   // Result: [[1,2,3], [4,5,6], [7]]
         *
         * Use case: batch processing (e.g., insert 1000 records at a time)
         */

        // Simulated output for JDK compatibility:
        List<Integer> input = List.of(1, 2, 3, 4, 5, 6, 7);
        System.out.println("Input:  " + input);

        // Manual simulation of windowFixed(3)
        List<List<Integer>> windows = new ArrayList<>();
        for (int i = 0; i < input.size(); i += 3) {
            windows.add(input.subList(i, Math.min(i + 3, input.size())));
        }
        System.out.println("Output: " + windows);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. windowSliding(int size)
    // ─────────────────────────────────────────────────────────────────────────
    static void demoWindowSliding() {
        System.out.println("── windowSliding(3) ──");
        /*
         * ACTUAL API USAGE (Java 22+):
         *
         *   List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5)
         *       .gather(Gatherers.windowSliding(3))
         *       .toList();
         *   // Result: [[1,2,3], [2,3,4], [3,4,5]]
         *
         * Use case: moving averages, trend detection, time-series analysis
         */

        List<Integer> input = List.of(1, 2, 3, 4, 5);
        System.out.println("Input:  " + input);

        List<List<Integer>> windows = new ArrayList<>();
        for (int i = 0; i <= input.size() - 3; i++) {
            windows.add(input.subList(i, i + 3));
        }
        System.out.println("Output: " + windows);

        // Moving average example
        System.out.print("Moving avg: ");
        windows.forEach(w -> {
            double avg = w.stream().mapToInt(Integer::intValue).average().orElse(0);
            System.out.printf("%.1f ", avg);
        });
        System.out.println("\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. fold(Supplier, BiFunction)
    // ─────────────────────────────────────────────────────────────────────────
    static void demoFold() {
        System.out.println("── fold (accumulate to single result) ──");
        /*
         * ACTUAL API USAGE (Java 22+):
         *
         *   // Fold strings into a single comma-separated string
         *   Optional<String> result = Stream.of("a", "b", "c")
         *       .gather(Gatherers.fold(
         *           () -> "",                              // initial state
         *           (state, element) -> state.isEmpty()
         *               ? element
         *               : state + ", " + element           // accumulator
         *       ))
         *       .findFirst();
         *   // Result: Optional["a, b, c"]
         *
         * KEY DIFFERENCE from reduce():
         *   - fold() is an INTERMEDIATE operation (can chain more ops after)
         *   - The accumulator type can differ from the element type
         *   - reduce() is TERMINAL
         */

        // Simulation
        List<String> input = List.of("a", "b", "c");
        String folded = input.stream().reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        System.out.println("Input:  " + input);
        System.out.println("Folded: " + folded);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. scan(Supplier, BiFunction)
    // ─────────────────────────────────────────────────────────────────────────
    static void demoScan() {
        System.out.println("── scan (running accumulation) ──");
        /*
         * ACTUAL API USAGE (Java 22+):
         *
         *   List<Integer> runningSums = Stream.of(1, 2, 3, 4, 5)
         *       .gather(Gatherers.scan(() -> 0, Integer::sum))
         *       .toList();
         *   // Result: [1, 3, 6, 10, 15]
         *
         * Unlike fold (which emits ONE result at the end), scan emits
         * a running result after EACH element. Think "prefix sums".
         *
         * Use case: running totals, cumulative statistics, balance tracking
         */

        List<Integer> input = List.of(1, 2, 3, 4, 5);
        System.out.println("Input:         " + input);

        // Manual simulation of scan
        List<Integer> runningSums = new ArrayList<>();
        int acc = 0;
        for (int val : input) {
            acc += val;
            runningSums.add(acc);
        }
        System.out.println("Running sums:  " + runningSums);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. mapConcurrent(int maxConcurrency, Function)
    // ─────────────────────────────────────────────────────────────────────────
    static void demoMapConcurrent() {
        System.out.println("── mapConcurrent (bounded virtual-thread mapping) ──");
        /*
         * ACTUAL API USAGE (Java 22+):
         *
         *   List<String> results = Stream.of("url1", "url2", "url3", "url4")
         *       .gather(Gatherers.mapConcurrent(3, url -> fetchFromNetwork(url)))
         *       .toList();
         *
         * HOW IT WORKS:
         *   1. Spawns virtual threads to process elements concurrently
         *   2. maxConcurrency limits how many virtual threads run at once
         *   3. Preserves encounter order in the output
         *   4. Uses virtual threads (Project Loom) under the hood
         *
         * Use case: I/O-bound mapping — HTTP calls, DB queries, file reads
         *
         * WHY NOT just parallel streams?
         *   - parallelStream() uses ForkJoinPool (CPU-bound, limited threads)
         *   - mapConcurrent uses virtual threads (ideal for I/O-bound work)
         *   - mapConcurrent gives you explicit concurrency control
         */

        // Simulation with Thread.sleep to mimic I/O
        List<String> urls = List.of("api/users", "api/orders", "api/products");
        System.out.println("Input URLs:  " + urls);
        System.out.println("(Would fetch concurrently with max 2 virtual threads)");
        System.out.println("Output:      [response1, response2, response3]");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. CUSTOM GATHERER — the power feature
    // ─────────────────────────────────────────────────────────────────────────
    static void demoCustomGatherer() {
        System.out.println("── Custom Gatherer: distinct-by-key ──");
        /*
         * THE GATHERER INTERFACE (simplified):
         *
         *   public interface Gatherer<T, A, R> {
         *       Supplier<A> initializer();                    // create state
         *       Integrator<A, T, R> integrator();             // process each element
         *       BinaryOperator<A> combiner();                 // merge states (parallel)
         *       BiConsumer<A, Downstream<R>> finisher();      // emit final elements
         *   }
         *
         * INTEGRATOR processes one element at a time:
         *
         *   boolean integrate(A state, T element, Downstream<R> downstream) {
         *       // Use state, optionally push to downstream
         *       // Return false to short-circuit (like limit())
         *       return true; // true = keep going
         *   }
         *
         * ──────────────────────────────────────────────────
         * EXAMPLE: distinctByKey — deduplicate by a key function
         * (like distinct() but you choose what "same" means)
         *
         *   // Remove duplicate users by email (keep first occurrence)
         *   users.stream()
         *       .gather(distinctByKey(User::email))
         *       .toList();
         *
         * IMPLEMENTATION (Java 22+):
         *
         *   static <T, K> Gatherer<T, ?, T> distinctByKey(Function<T, K> keyFn) {
         *       return Gatherer.ofSequential(
         *           HashSet::new,                         // state = seen keys
         *           (seen, element, downstream) -> {
         *               K key = keyFn.apply(element);
         *               if (seen.add(key)) {              // add returns true if new
         *                   downstream.push(element);
         *               }
         *               return true;                      // keep processing
         *           }
         *       );
         *   }
         *
         * ──────────────────────────────────────────────────
         * ANOTHER EXAMPLE: takeEveryNth — emit every Nth element
         *
         *   static <T> Gatherer<T, ?, T> takeEveryNth(int n) {
         *       return Gatherer.ofSequential(
         *           () -> new int[]{0},                   // state = counter
         *           (counter, element, downstream) -> {
         *               if (counter[0]++ % n == 0) {
         *                   downstream.push(element);
         *               }
         *               return true;
         *           }
         *       );
         *   }
         *
         *   Stream.of(1,2,3,4,5,6,7,8,9)
         *       .gather(takeEveryNth(3))
         *       .toList();
         *   // Result: [1, 4, 7]
         */

        // Manual simulation of distinctByKey
        record Employee(String name, String dept) {}
        List<Employee> employees = List.of(
            new Employee("Alice", "Engineering"),
            new Employee("Bob", "Engineering"),
            new Employee("Carol", "Sales"),
            new Employee("Dave", "Sales"),
            new Employee("Eve", "Marketing")
        );

        // Simulate distinctByKey(Employee::dept) — keep first per department
        Set<String> seen = new LinkedHashSet<>();
        List<Employee> distinct = new ArrayList<>();
        for (Employee e : employees) {
            if (seen.add(e.dept())) {
                distinct.add(e);
            }
        }
        System.out.println("Input:  " + employees);
        System.out.println("Distinct by dept: " + distinct);

        System.out.println("\n── Gatherer Composition ──");
        /*
         * Gatherers can be COMPOSED using andThen():
         *
         *   Gatherer<T, ?, R> combined = gatherer1.andThen(gatherer2);
         *
         *   stream.gather(
         *       Gatherers.windowFixed(3)
         *           .andThen(Gatherers.mapConcurrent(2, batch -> processBatch(batch)))
         *   ).toList();
         *
         * This is the composability that was impossible before Gatherers.
         */
        System.out.println("Gatherers compose via andThen(), just like Functions.");
        System.out.println("Example: windowFixed(3).andThen(mapConcurrent(2, processBatch))");
    }
}
