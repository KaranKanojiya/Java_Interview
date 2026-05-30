package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Q15. Can Streams be reused?
 *
 * NO. A stream can only be consumed ONCE. After a terminal operation, the stream is closed.
 * Attempting to reuse throws IllegalStateException.
 *
 * Why? Streams are designed as pipelines, not data structures. They don't store data.
 *
 * Workaround: Create a new stream from the source each time, or use a Supplier<Stream>.
 *
 * Also covers: Stream laziness
 *   - Intermediate operations (map, filter, etc.) are LAZY — not executed until a terminal operation is called
 *   - Terminal operations (collect, forEach, count, etc.) trigger the pipeline
 *   - This enables short-circuiting (findFirst, limit stop early)
 */
public class StreamReuseAndLaziness {

    public static void main(String[] args) {

        List<String> names = Arrays.asList("Karan", "John", "Alice", "Bob");

        // === Streams cannot be reused ===
        System.out.println("=== Stream reuse throws exception ===");
        Stream<String> stream = names.stream().filter(n -> n.length() > 3);

        // First terminal operation — works fine
        long count = stream.count();
        System.out.println("Count: " + count);

        // Second terminal operation — throws IllegalStateException
        try {
            stream.forEach(System.out::println);
        } catch (IllegalStateException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // === Workaround: Supplier<Stream> ===
        System.out.println("\n=== Workaround: Supplier<Stream> ===");
        java.util.function.Supplier<Stream<String>> streamSupplier =
                () -> names.stream().filter(n -> n.length() > 3);

        System.out.println("Count: " + streamSupplier.get().count());
        System.out.print("Names: ");
        streamSupplier.get().forEach(n -> System.out.print(n + " "));
        System.out.println();

        // === Laziness demo ===
        System.out.println("\n=== Stream laziness ===");

        // Without terminal operation — NOTHING executes
        System.out.println("Setting up pipeline (no terminal op)...");
        Stream<String> lazy = names.stream()
                .filter(n -> {
                    System.out.println("  filter: " + n);
                    return n.length() > 3;
                })
                .map(n -> {
                    System.out.println("  map: " + n);
                    return n.toUpperCase();
                });
        System.out.println("Pipeline set up. Nothing printed above because it's lazy!");

        // Now trigger with terminal operation
        System.out.println("\nTriggering with collect():");
        List<String> result = names.stream()
                .filter(n -> {
                    System.out.println("  filter: " + n);
                    return n.length() > 3;
                })
                .map(n -> {
                    System.out.println("  map: " + n);
                    return n.toUpperCase();
                })
                .toList();
        System.out.println("Result: " + result);

        // === Short-circuit: elements processed one-by-one, stops early ===
        System.out.println("\n=== Short-circuit with findFirst() ===");
        names.stream()
                .filter(n -> {
                    System.out.println("  Checking: " + n);
                    return n.startsWith("A");
                })
                .findFirst()
                .ifPresent(n -> System.out.println("Found: " + n));
        // Only processes until it finds "Alice" — "Bob" is never checked
    }
}
