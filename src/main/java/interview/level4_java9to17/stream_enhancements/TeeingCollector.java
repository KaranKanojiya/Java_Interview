package interview.level4_java9to17.stream_enhancements;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Q15. What is Collectors.teeing() (Java 12)?
 *
 * teeing() applies TWO collectors simultaneously to the same stream,
 * then merges their results with a BiFunction.
 *
 * Signature:
 *   Collectors.teeing(
 *       Collector<T,?,R1> downstream1,
 *       Collector<T,?,R2> downstream2,
 *       BiFunction<R1,R2,R> merger
 *   )
 *
 * Think of it as a "T-pipe" — data flows to two collectors in parallel.
 *
 * Use cases:
 *   - Min and Max in one pass
 *   - Sum and Count → average
 *   - Filter into two groups simultaneously
 *   - Any two aggregations on the same stream
 *
 * Without teeing(): you'd need to iterate twice or use a custom collector.
 */
public class TeeingCollector {

    record MinMax(int min, int max) {}
    record Stats(double average, long count) {}

    public static void main(String[] args) {

        List<Integer> numbers = List.of(3, 7, 1, 9, 4, 6, 2, 8, 5);

        // === Min and Max in one pass ===
        System.out.println("=== teeing: Min and Max ===");
        MinMax minMax = numbers.stream().collect(
                Collectors.teeing(
                        Collectors.minBy(Integer::compareTo),   // collector 1: min
                        Collectors.maxBy(Integer::compareTo),   // collector 2: max
                        (min, max) -> new MinMax(min.orElse(0), max.orElse(0))  // merge
                )
        );
        System.out.println("Min: " + minMax.min() + ", Max: " + minMax.max());

        // === Average using Sum + Count ===
        System.out.println("\n=== teeing: Average (sum/count) ===");
        Stats stats = numbers.stream().collect(
                Collectors.teeing(
                        Collectors.summingDouble(Integer::doubleValue),  // sum
                        Collectors.counting(),                           // count
                        (sum, count) -> new Stats(sum / count, count)
                )
        );
        System.out.println("Average: " + stats.average() + ", Count: " + stats.count());

        // === Partition results: short and long names ===
        System.out.println("\n=== teeing: Partition strings ===");
        List<String> names = List.of("Jo", "Karan", "Al", "Charlie", "Bob", "Alexander");

        record NameGroups(List<String> shortNames, List<String> longNames) {}

        NameGroups groups = names.stream().collect(
                Collectors.teeing(
                        Collectors.filtering(n -> n.length() <= 3, Collectors.toList()),
                        Collectors.filtering(n -> n.length() > 3, Collectors.toList()),
                        NameGroups::new
                )
        );
        System.out.println("Short (≤3): " + groups.shortNames());
        System.out.println("Long  (>3): " + groups.longNames());

        // === Count even and odd ===
        System.out.println("\n=== teeing: Even and Odd counts ===");
        record EvenOdd(long even, long odd) {}

        EvenOdd counts = numbers.stream().collect(
                Collectors.teeing(
                        Collectors.filtering(n -> n % 2 == 0, Collectors.counting()),
                        Collectors.filtering(n -> n % 2 != 0, Collectors.counting()),
                        EvenOdd::new
                )
        );
        System.out.println("Even: " + counts.even() + ", Odd: " + counts.odd());

        System.out.println("\n=== Summary ===");
        System.out.println("Collectors.teeing(collector1, collector2, merger)");
        System.out.println("  → Applies two collectors to same stream in one pass");
        System.out.println("  → Merges results with a BiFunction");
        System.out.println("  → Avoid iterating twice or writing custom collectors");
    }
}
