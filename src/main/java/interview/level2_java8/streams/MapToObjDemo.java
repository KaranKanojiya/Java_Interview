package interview.level2_java8.streams;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Q20. What is mapToObj() and when do you use it?
 *
 * mapToObj() converts a primitive stream (IntStream, LongStream, DoubleStream)
 * to a Stream of objects (Stream<T>).
 *
 * Primitive streams exist for performance — they avoid autoboxing overhead.
 * But when you need to transform int → String or int → custom object, use mapToObj().
 *
 * Related conversions:
 *   Stream<Integer> → IntStream:     mapToInt(Integer::intValue)
 *   IntStream → Stream<Integer>:     boxed()
 *   IntStream → Stream<String>:      mapToObj(String::valueOf)
 *   IntStream → Stream<T>:           mapToObj(i -> new MyObj(i))
 *
 * Also: mapToInt(), mapToLong(), mapToDouble() go the other direction.
 */
public class MapToObjDemo {

    record IndexedName(int index, String name) {}

    public static void main(String[] args) {

        // === IntStream → Stream<String> using mapToObj() ===
        System.out.println("=== mapToObj: int → String ===");
        List<String> hexStrings = IntStream.rangeClosed(1, 16)
                .mapToObj(i -> String.format("0x%02X", i))
                .collect(Collectors.toList());
        System.out.println("Hex: " + hexStrings);

        // === IntStream.boxed() — convert to Stream<Integer> ===
        System.out.println("\n=== boxed(): IntStream → Stream<Integer> ===");
        List<Integer> boxed = IntStream.of(10, 20, 30, 40)
                .boxed()  // IntStream → Stream<Integer>
                .collect(Collectors.toList());
        System.out.println("Boxed: " + boxed);

        // === mapToObj for creating objects ===
        System.out.println("\n=== mapToObj: int → custom object ===");
        String[] names = {"Karan", "John", "Alice", "Bob"};
        List<IndexedName> indexed = IntStream.range(0, names.length)
                .mapToObj(i -> new IndexedName(i, names[i]))
                .collect(Collectors.toList());
        indexed.forEach(in -> System.out.println("  " + in.index() + ": " + in.name()));

        // === mapToInt: Stream<String> → IntStream ===
        System.out.println("\n=== mapToInt: Stream<String> → IntStream ===");
        int totalLength = List.of("Java", "is", "awesome").stream()
                .mapToInt(String::length)
                .sum();  // sum() is only available on IntStream, not Stream<Integer>
        System.out.println("Total length: " + totalLength);

        // === Why primitive streams? Performance ===
        System.out.println("\n=== Why use primitive streams? ===");
        // Stream<Integer> — every int is autoboxed to Integer object
        long start = System.nanoTime();
        long sum1 = IntStream.rangeClosed(1, 1_000_000).sum();
        long primitiveTime = System.nanoTime() - start;

        start = System.nanoTime();
        long sum2 = IntStream.rangeClosed(1, 1_000_000)
                .boxed()  // forces autoboxing
                .reduce(0, Integer::sum);
        long boxedTime = System.nanoTime() - start;

        System.out.println("Primitive IntStream:  " + sum1 + " in " + primitiveTime / 1000 + "μs");
        System.out.println("Boxed Stream<Integer>: " + sum2 + " in " + boxedTime / 1000 + "μs");
        System.out.println("Ratio: ~" + (boxedTime / Math.max(primitiveTime, 1)) + "x slower with boxing");

        // === String chars() → IntStream → mapToObj ===
        System.out.println("\n=== String.chars() → mapToObj ===");
        String word = "Hello";
        List<Character> chars = word.chars()                    // IntStream
                .mapToObj(c -> (char) c)                        // Stream<Character>
                .collect(Collectors.toList());
        System.out.println("Characters of '" + word + "': " + chars);
    }
}
