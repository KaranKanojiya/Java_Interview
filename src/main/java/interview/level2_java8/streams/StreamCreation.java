package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Q14. What is the difference between Collection.stream() and Stream.of()?
 *
 * Collection.stream():
 *   - Creates a stream FROM an existing collection (List, Set, etc.)
 *   - The collection must already exist
 *   - list.stream(), set.stream()
 *
 * Stream.of():
 *   - Creates a stream FROM individual elements or an array
 *   - No collection needed
 *   - Stream.of("a", "b", "c") or Stream.of(array)
 *
 * Other ways to create streams:
 *   - Arrays.stream(array)         → from array
 *   - Stream.empty()               → empty stream
 *   - Stream.generate(Supplier)    → infinite stream
 *   - Stream.iterate(seed, f)      → infinite stream with iteration
 *   - IntStream.range(1, 10)       → primitive int stream
 *   - "hello".chars()              → IntStream from string
 *   - Files.lines(path)            → stream of lines from file
 */
public class StreamCreation {

    public static void main(String[] args) {

        // === Collection.stream() ===
        System.out.println("=== Collection.stream() ===");
        List<String> list = Arrays.asList("Java", "Python", "Go");
        list.stream()
                .map(String::toUpperCase)
                .forEach(s -> System.out.print(s + " "));
        System.out.println();

        // === Stream.of() ===
        System.out.println("\n=== Stream.of() ===");
        Stream.of("one", "two", "three")
                .forEach(s -> System.out.print(s + " "));
        System.out.println();

        // Stream.of with array
        String[] arr = {"a", "b", "c"};
        Stream.of(arr).forEach(s -> System.out.print(s + " "));
        System.out.println();

        // === Arrays.stream() — preferred for arrays ===
        System.out.println("\n=== Arrays.stream() ===");
        int[] intArr = {10, 20, 30, 40};
        Arrays.stream(intArr)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Partial array
        Arrays.stream(intArr, 1, 3)  // elements at index 1 and 2
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // === Stream.generate() — infinite stream ===
        System.out.println("\n=== Stream.generate() (infinite, must limit) ===");
        Stream.generate(Math::random)
                .limit(5)
                .forEach(d -> System.out.printf("%.2f ", d));
        System.out.println();

        // === Stream.iterate() — infinite with seed ===
        System.out.println("\n=== Stream.iterate() ===");
        Stream.iterate(1, n -> n * 2)  // 1, 2, 4, 8, 16...
                .limit(8)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Java 9: iterate with predicate (bounded)
        Stream.iterate(1, n -> n <= 100, n -> n * 2)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // === IntStream.range() ===
        System.out.println("\n=== IntStream.range() and rangeClosed() ===");
        System.out.print("range(1,5):       ");
        IntStream.range(1, 5).forEach(n -> System.out.print(n + " "));
        System.out.print("\nrangeClosed(1,5): ");
        IntStream.rangeClosed(1, 5).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // === Stream.empty() ===
        System.out.println("\n=== Stream.empty() ===");
        long count = Stream.empty().count();
        System.out.println("Empty stream count: " + count);
    }
}
