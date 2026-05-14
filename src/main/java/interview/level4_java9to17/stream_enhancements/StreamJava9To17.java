package interview.level4_java9to17.stream_enhancements;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Stream API Enhancements (Java 9-17)
// =============================================================================
//
// Q: "What's the difference between Stream.toList() and Collectors.toList()?"
// A: "Stream.toList() (Java 16) returns an unmodifiable list — you cannot add,
//     remove, or set elements. Collectors.toList() returns a modifiable ArrayList.
//     Also, Stream.toList() does not allow null elements in practice (the
//     unmodifiable list factory rejects nulls). Use toList() by default; use
//     Collectors.toList() only when you need mutability."
//
// Q: "How does takeWhile differ from filter?"
// A: "filter() checks every element independently. takeWhile() takes elements
//     from the beginning of the stream while the predicate is true, then STOPS.
//     On an ordered stream, once the predicate fails, no more elements are taken.
//     Think of it as a short-circuiting prefix filter."
//
// Q: "When would you use mapMulti instead of flatMap?"
// A: "mapMulti is more efficient when you want to emit zero or few elements per
//     input element, because it avoids creating intermediate Stream objects.
//     It's imperative-style (you push elements into a Consumer) vs. flatMap's
//     functional style (you return a Stream)."
//
// =============================================================================

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamJava9To17 {

    // -------------------------------------------------------------------------
    // 1. Stream.ofNullable (Java 9) — null-safe stream creation
    // -------------------------------------------------------------------------
    static void streamOfNullable() {
        System.out.println("=== 1. Stream.ofNullable (Java 9) ===");

        // BEFORE: manual null check
        String value = null;
        Stream<String> oldWay = value == null ? Stream.empty() : Stream.of(value);
        System.out.println("  Before: " + oldWay.toList());

        // AFTER: Stream.ofNullable
        Stream<String> newWay = Stream.ofNullable(value);
        System.out.println("  After (null): " + newWay.toList());  // []

        Stream<String> withValue = Stream.ofNullable("Hello");
        System.out.println("  After (value): " + withValue.toList()); // [Hello]

        // Real use case: flatMap with nullable map lookups
        Map<String, List<String>> cityPeople = Map.of(
                "NYC", List.of("Alice", "Bob"),
                "LA", List.of("Charlie")
        );

        List<String> result = Stream.of("NYC", "SF", "LA")
                .flatMap(city -> Stream.ofNullable(cityPeople.get(city)))
                .flatMap(Collection::stream)
                .toList();
        System.out.println("  FlatMap with nullable: " + result);
    }

    // -------------------------------------------------------------------------
    // 2. takeWhile / dropWhile (Java 9) — ordered prefix operations
    // -------------------------------------------------------------------------
    static void takeWhileDropWhile() {
        System.out.println("\n=== 2. takeWhile / dropWhile (Java 9) ===");

        var numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // takeWhile: takes elements WHILE predicate is true, stops at first false
        var taken = numbers.stream()
                .takeWhile(n -> n <= 5)
                .toList();
        System.out.println("  takeWhile(n <= 5): " + taken);  // [1, 2, 3, 4, 5]

        // dropWhile: drops elements WHILE predicate is true, takes the rest
        var dropped = numbers.stream()
                .dropWhile(n -> n <= 5)
                .toList();
        System.out.println("  dropWhile(n <= 5): " + dropped); // [6, 7, 8, 9, 10]

        // IMPORTANT: different from filter!
        var mixed = List.of(1, 2, 5, 3, 4, 1);

        var filteredResult = mixed.stream()
                .filter(n -> n <= 3)
                .toList();
        System.out.println("  filter(n <= 3):    " + filteredResult);  // [1, 2, 3, 1]

        var takeResult = mixed.stream()
                .takeWhile(n -> n <= 3)
                .toList();
        System.out.println("  takeWhile(n <= 3): " + takeResult);  // [1, 2] — stops at 5!

        // Real use case: processing sorted data
        record Transaction(String id, double amount) {}
        var transactions = List.of(
                new Transaction("T1", 10.0),
                new Transaction("T2", 20.0),
                new Transaction("T3", 50.0),
                new Transaction("T4", 100.0),
                new Transaction("T5", 200.0)
        );

        // Get transactions while cumulative amount < 100 (sorted input)
        var affordable = transactions.stream()
                .takeWhile(t -> t.amount() <= 50)
                .toList();
        System.out.println("  Affordable (<=50): " + affordable);
    }

    // -------------------------------------------------------------------------
    // 3. Collectors.toUnmodifiableList/Set/Map (Java 10)
    // -------------------------------------------------------------------------
    static void unmodifiableCollectors() {
        System.out.println("\n=== 3. Collectors.toUnmodifiable* (Java 10) ===");

        var names = List.of("Alice", "Bob", "Charlie");

        // Collectors.toList() -> mutable
        List<String> mutableList = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        mutableList.add("EXTRA"); // OK
        System.out.println("  Mutable list: " + mutableList);

        // Collectors.toUnmodifiableList() -> immutable
        List<String> immutableList = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toUnmodifiableList());
        try {
            immutableList.add("EXTRA");
        } catch (UnsupportedOperationException e) {
            System.out.println("  Unmodifiable list: " + immutableList + " (add -> UOE)");
        }

        // Unmodifiable map
        Map<String, Integer> nameLen = names.stream()
                .collect(Collectors.toUnmodifiableMap(n -> n, String::length));
        System.out.println("  Unmodifiable map: " + nameLen);
    }

    // -------------------------------------------------------------------------
    // 4. Stream.toList() (Java 16) — convenience method
    // -------------------------------------------------------------------------
    static void streamToList() {
        System.out.println("\n=== 4. Stream.toList() (Java 16) ===");

        var numbers = List.of(1, 2, 3, 4, 5);

        // BEFORE: verbose
        List<Integer> before = numbers.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());

        // AFTER: concise (Java 16)
        List<Integer> after = numbers.stream()
                .filter(n -> n % 2 == 0)
                .toList();

        System.out.println("  Collectors.toList(): " + before + " (mutable: " + isMutable(before) + ")");
        System.out.println("  Stream.toList():     " + after + " (mutable: " + isMutable(after) + ")");

        System.out.println("\n  Comparison:");
        System.out.println("  ┌───────────────────────────┬───────────┬───────────────┐");
        System.out.println("  │ Method                    │ Mutable?  │ Null allowed? │");
        System.out.println("  ├───────────────────────────┼───────────┼───────────────┤");
        System.out.println("  │ Collectors.toList()       │ Yes       │ Yes           │");
        System.out.println("  │ Collectors.toUnmodifiable  │ No        │ No            │");
        System.out.println("  │ Stream.toList()           │ No        │ Yes*          │");
        System.out.println("  └───────────────────────────┴───────────┴───────────────┘");
        System.out.println("  * Stream.toList() allows nulls (unlike List.of())");
    }

    private static boolean isMutable(List<?> list) {
        try {
            list.add(null);
            list.removeLast();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // 5. mapMulti (Java 16) — imperative flatMap alternative
    // -------------------------------------------------------------------------
    static void mapMultiDemo() {
        System.out.println("\n=== 5. mapMulti (Java 16) ===");

        var numbers = List.of(1, 2, 3, 4, 5);

        // BEFORE: flatMap — creates intermediate streams
        List<Integer> flatMapped = numbers.stream()
                .flatMap(n -> n % 2 == 0 ? Stream.of(n, n * 10) : Stream.empty())
                .toList();
        System.out.println("  flatMap (even*10): " + flatMapped);

        // AFTER: mapMulti — push elements imperatively, no intermediate streams
        List<Integer> multiMapped = numbers.stream()
                .<Integer>mapMulti((n, consumer) -> {
                    if (n % 2 == 0) {
                        consumer.accept(n);
                        consumer.accept(n * 10);
                    }
                })
                .toList();
        System.out.println("  mapMulti (even*10): " + multiMapped);

        // Use case: one-to-many with type conversion
        List<Object> mixed = List.of("hello", 42, "world", 3.14, "!");

        List<String> strings = mixed.stream()
                .<String>mapMulti((obj, consumer) -> {
                    if (obj instanceof String s) {
                        consumer.accept(s.toUpperCase());
                    }
                })
                .toList();
        System.out.println("  mapMulti (strings only): " + strings);

        // Use case: expanding ranges
        record Range(int start, int end) {}
        var ranges = List.of(new Range(1, 3), new Range(10, 12));

        List<Integer> expanded = ranges.stream()
                .<Integer>mapMulti((range, consumer) -> {
                    for (int i = range.start(); i <= range.end(); i++) {
                        consumer.accept(i);
                    }
                })
                .toList();
        System.out.println("  mapMulti (expand ranges): " + expanded);
    }

    // -------------------------------------------------------------------------
    // 6. Stream.iterate enhancements (Java 9)
    // -------------------------------------------------------------------------
    static void streamIterate() {
        System.out.println("\n=== 6. Stream.iterate Enhancement (Java 9) ===");

        // Java 8: Stream.iterate(seed, unaryOp) — infinite, needs limit()
        var oldWay = Stream.iterate(1, n -> n * 2)
                .limit(5)
                .toList();
        System.out.println("  Java 8 iterate + limit: " + oldWay);

        // Java 9: Stream.iterate(seed, predicate, unaryOp) — like a for loop
        var newWay = Stream.iterate(1, n -> n <= 16, n -> n * 2)
                .toList();
        System.out.println("  Java 9 iterate (<=16):  " + newWay);

        // Equivalent to: for (int n = 1; n <= 16; n *= 2) { ... }

        // Real use case: date range
        var dates = Stream.iterate(
                java.time.LocalDate.of(2024, 1, 1),
                d -> d.isBefore(java.time.LocalDate.of(2024, 1, 8)),
                d -> d.plusDays(1)
        ).toList();
        System.out.println("  Date range: " + dates);
    }

    // -------------------------------------------------------------------------
    // 7. Collectors.teeing (Java 12)
    // -------------------------------------------------------------------------
    static void collectorsTeeing() {
        System.out.println("\n=== 7. Collectors.teeing (Java 12) ===");

        var numbers = List.of(10, 20, 30, 40, 50);

        // teeing: apply two collectors simultaneously, merge results
        record MinMax(int min, int max) {}

        var minMax = numbers.stream()
                .collect(Collectors.teeing(
                        Collectors.<Integer>minBy(Comparator.naturalOrder()),
                        Collectors.<Integer>maxBy(Comparator.naturalOrder()),
                        (min, max) -> new MinMax(min.orElse(0), max.orElse(0))
                ));
        System.out.println("  MinMax: " + minMax);

        // Average and count in one pass
        record Stats(long count, double avg) {}

        var stats = numbers.stream()
                .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.averagingInt(n -> n),
                        Stats::new
                ));
        System.out.println("  Stats: " + stats);

        // Sum and product in one pass
        record SumProduct(int sum, long product) {}

        var sp = numbers.stream()
                .collect(Collectors.teeing(
                        Collectors.summingInt(n -> n),
                        Collectors.reducing(1L, Long::valueOf, (a, b) -> a * b),
                        SumProduct::new
                ));
        System.out.println("  SumProduct: " + sp);
    }

    // -------------------------------------------------------------------------
    // 8. Comprehensive before/after comparison
    // -------------------------------------------------------------------------
    static void comprehensiveComparison() {
        System.out.println("\n=== 8. Before/After Comparison Summary ===");

        System.out.println("  ┌─────────────────────────┬────────────────────────────────────┐");
        System.out.println("  │ Feature                 │ Java Version                       │");
        System.out.println("  ├─────────────────────────┼────────────────────────────────────┤");
        System.out.println("  │ Stream.ofNullable()     │ Java 9                             │");
        System.out.println("  │ takeWhile / dropWhile   │ Java 9                             │");
        System.out.println("  │ Stream.iterate (3-arg)  │ Java 9                             │");
        System.out.println("  │ toUnmodifiable*()       │ Java 10                            │");
        System.out.println("  │ Collectors.teeing()     │ Java 12                            │");
        System.out.println("  │ Stream.toList()         │ Java 16                            │");
        System.out.println("  │ mapMulti()              │ Java 16                            │");
        System.out.println("  └─────────────────────────┴────────────────────────────────────┘");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Stream API Enhancements: Java 9-17            ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        streamOfNullable();
        takeWhileDropWhile();
        unmodifiableCollectors();
        streamToList();
        mapMultiDemo();
        streamIterate();
        collectorsTeeing();
        comprehensiveComparison();
    }
}
