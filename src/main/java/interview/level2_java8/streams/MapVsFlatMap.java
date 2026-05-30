package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Q7. What is the difference between map() and flatMap()?
 *
 * map()     → One-to-one transformation. Each element → exactly one result.
 *             Stream<T> → Stream<R>
 *
 * flatMap() → One-to-many transformation + flattening. Each element → a stream of results,
 *             then all streams are merged into one.
 *             Stream<T> → Stream<R>  (flattens Stream<Stream<R>> into Stream<R>)
 *
 * Rule of thumb:
 *   - Use map() when each element maps to ONE value
 *   - Use flatMap() when each element maps to MULTIPLE values (or a collection/stream)
 */
public class MapVsFlatMap {

    public static void main(String[] args) {

        // === map(): one-to-one ===
        List<String> names = Arrays.asList("karan", "john", "alice");

        System.out.println("=== map() — one-to-one transformation ===");
        List<String> upperNames = names.stream()
                .map(String::toUpperCase)     // each String → one String
                .collect(Collectors.toList());
        System.out.println("Uppercase: " + upperNames);

        List<Integer> nameLengths = names.stream()
                .map(String::length)          // each String → one Integer
                .collect(Collectors.toList());
        System.out.println("Lengths: " + nameLengths);

        // === flatMap(): one-to-many + flatten ===
        List<List<Integer>> nested = Arrays.asList(
                Arrays.asList(1, 2, 3),
                Arrays.asList(4, 5),
                Arrays.asList(6, 7, 8, 9)
        );

        System.out.println("\n=== flatMap() — flatten nested lists ===");
        // Without flatMap: Stream<List<Integer>> — NOT what we want
        System.out.println("With map (wrong): " + nested.stream()
                .map(list -> list.toString())
                .collect(Collectors.toList()));

        // With flatMap: Stream<Integer> — flattened!
        List<Integer> flat = nested.stream()
                .flatMap(List::stream)        // each List<Integer> → Stream<Integer>, then flatten
                .collect(Collectors.toList());
        System.out.println("With flatMap: " + flat);

        // === Real-world example: split sentences into words ===
        List<String> sentences = Arrays.asList(
                "Java is great",
                "Streams are powerful",
                "FlatMap flattens"
        );

        System.out.println("\n=== flatMap() — split sentences into words ===");
        List<String> words = sentences.stream()
                .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
                .collect(Collectors.toList());
        System.out.println("Words: " + words);

        // === flatMap with Optional (common interview follow-up) ===
        System.out.println("\n=== flatMap() — with Stream.of() ===");
        List<String> result = Stream.of("a,b,c", "d,e", "f")
                .flatMap(s -> Arrays.stream(s.split(",")))
                .collect(Collectors.toList());
        System.out.println("Split and flatten: " + result);  // [a, b, c, d, e, f]
    }
}
