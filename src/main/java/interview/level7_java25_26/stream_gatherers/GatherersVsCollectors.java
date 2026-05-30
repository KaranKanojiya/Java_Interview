package interview.level7_java25_26.stream_gatherers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Q9. What is the difference between Gatherers and Collectors?
 *
 * Collectors (Java 8):
 *   - TERMINAL operation: stream.collect(Collectors.toList())
 *   - Aggregates ALL elements into a final result
 *   - Runs at the END of the pipeline
 *   - Examples: toList(), groupingBy(), joining(), counting()
 *
 * Gatherers (Java 22+, finalized Java 24):
 *   - INTERMEDIATE operation: stream.gather(myGatherer)
 *   - Transforms elements as they flow through (can filter, expand, aggregate partially)
 *   - Runs in the MIDDLE of the pipeline
 *   - Can maintain state across elements
 *   - Can be short-circuiting
 *   - Built-in: Gatherers.windowFixed(), scan(), fold(), mapConcurrent()
 *
 * | Feature        | Collector                | Gatherer                    |
 * |---------------|--------------------------|------------------------------|
 * | Position      | Terminal (end)            | Intermediate (middle)        |
 * | Output        | Single result             | Stream of elements           |
 * | State         | Accumulates to final      | Can accumulate + emit        |
 * | Short-circuit | No                        | Yes                          |
 * | Custom ops    | Custom aggregation        | Custom intermediate transform|
 *
 * Note: Gatherers API requires Java 22+ with --enable-preview (or Java 24+ finalized).
 * This demo shows the CONCEPT using manual implementations that compile on Java 21.
 *
 * With Gatherers API (Java 24+), the code would be:
 *   stream.gather(Gatherers.windowFixed(3))
 *   stream.gather(Gatherers.scan(() -> 0, Integer::sum))
 *   stream.gather(Gatherers.fold(() -> 0, Integer::sum))
 */
public class GatherersVsCollectors {

    // === Manual windowing (what Gatherers.windowFixed does) ===
    static <T> List<List<T>> windowFixed(List<T> input, int size) {
        List<List<T>> windows = new ArrayList<>();
        for (int i = 0; i < input.size(); i += size) {
            windows.add(input.subList(i, Math.min(i + size, input.size())));
        }
        return windows;
    }

    // === Manual scan (what Gatherers.scan does) ===
    static List<Integer> scan(List<Integer> input, int identity) {
        List<Integer> result = new ArrayList<>();
        int acc = identity;
        for (int val : input) {
            acc += val;
            result.add(acc);
        }
        return result;
    }

    // === Manual sliding window (what Gatherers.windowSliding does) ===
    static <T> List<List<T>> windowSliding(List<T> input, int size) {
        List<List<T>> windows = new ArrayList<>();
        for (int i = 0; i <= input.size() - size; i++) {
            windows.add(input.subList(i, i + size));
        }
        return windows;
    }

    public static void main(String[] args) {

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // === Collector: terminal, aggregates everything ===
        System.out.println("=== Collectors (terminal — Java 8) ===");
        int sum = numbers.stream().collect(Collectors.summingInt(Integer::intValue));
        System.out.println("Sum (Collector): " + sum);

        String joined = numbers.stream().map(String::valueOf).collect(Collectors.joining(", "));
        System.out.println("Joined (Collector): " + joined);

        // === Gatherer concept: windowing ===
        System.out.println("\n=== Gatherer concept: windowFixed(3) ===");
        // With Gatherers API: numbers.stream().gather(Gatherers.windowFixed(3))
        List<List<Integer>> windows = windowFixed(numbers, 3);
        windows.forEach(w -> System.out.println("  Window: " + w));

        // === Gatherer concept: sliding window ===
        System.out.println("\nwindowSliding(3):");
        List<List<Integer>> sliding = windowSliding(numbers, 3);
        sliding.forEach(w -> System.out.println("  Window: " + w));

        // === Gatherer concept: scan (running accumulation) ===
        System.out.println("\nscan (running sum):");
        // With Gatherers API: numbers.stream().gather(Gatherers.scan(() -> 0, Integer::sum))
        List<Integer> runningSum = scan(numbers, 0);
        System.out.println("  " + runningSum);

        // === Gatherer in middle of pipeline ===
        System.out.println("\n=== Gatherer in middle of pipeline ===");
        // With Gatherers API:
        //   numbers.stream()
        //     .gather(Gatherers.windowFixed(3))
        //     .map(window -> window.stream().max(...))
        //     .toList()
        List<Integer> windowMaxes = windowFixed(numbers, 3).stream()
                .map(window -> window.stream().max(Integer::compareTo).orElse(0))
                .toList();
        System.out.println("Max per window of 3: " + windowMaxes);

        // === Key difference ===
        System.out.println("\n=== Summary ===");
        System.out.println("Collector: terminal, produces ONE result from stream");
        System.out.println("  → collect(toList()), collect(groupingBy()), collect(joining())");
        System.out.println("\nGatherer:  intermediate, produces STREAM from stream");
        System.out.println("  → gather(windowFixed(3)), gather(scan()), gather(fold())");
        System.out.println("\nCollector fills the gap: 'how to aggregate'");
        System.out.println("Gatherer fills the gap:  'how to transform in ways map/filter can't'");
        System.out.println("  → windowing, scanning, stateful mapping, rate limiting");

        System.out.println("\n=== Gatherers API (Java 24+) ===");
        System.out.println("Gatherers.windowFixed(n)     → fixed-size windows");
        System.out.println("Gatherers.windowSliding(n)   → sliding windows");
        System.out.println("Gatherers.scan(init, fn)     → running accumulation (emits each step)");
        System.out.println("Gatherers.fold(init, fn)     → reduce as intermediate (emits final)");
        System.out.println("Gatherers.mapConcurrent(n,fn)→ parallel mapping with concurrency limit");
    }
}
