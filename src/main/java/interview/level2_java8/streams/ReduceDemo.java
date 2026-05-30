package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Q10. How does reduce() work in Streams?
 *
 * reduce() is a terminal operation that combines all elements into a single result.
 *
 * Three forms:
 *   1. reduce(BinaryOperator)          → Optional<T>     (no identity, may be empty)
 *   2. reduce(identity, BinaryOperator) → T               (has default value)
 *   3. reduce(identity, BiFunction, BinaryOperator) → U   (for parallel + type change)
 *
 * How it works (form 2):
 *   result = identity
 *   for each element: result = accumulator.apply(result, element)
 *
 * Identity rules:
 *   - Sum → identity = 0     (0 + x = x)
 *   - Product → identity = 1 (1 * x = x)
 *   - String concat → identity = ""
 *   - Max → no natural identity, use form 1 (returns Optional)
 */
public class ReduceDemo {

    public static void main(String[] args) {

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        // === Form 1: reduce(BinaryOperator) → Optional ===
        System.out.println("=== Form 1: No identity (returns Optional) ===");
        Optional<Integer> sum1 = numbers.stream()
                .reduce((a, b) -> a + b);
        System.out.println("Sum: " + sum1.orElse(0));

        Optional<Integer> max = numbers.stream()
                .reduce(Integer::max);
        System.out.println("Max: " + max.orElse(0));

        // Empty stream — returns Optional.empty()
        Optional<Integer> emptyResult = List.<Integer>of().stream()
                .reduce(Integer::sum);
        System.out.println("Empty stream reduce: " + emptyResult);  // Optional.empty

        // === Form 2: reduce(identity, BinaryOperator) → T ===
        System.out.println("\n=== Form 2: With identity (returns T) ===");
        int sum2 = numbers.stream()
                .reduce(0, Integer::sum);    // identity = 0
        System.out.println("Sum with identity: " + sum2);

        int product = numbers.stream()
                .reduce(1, (a, b) -> a * b);  // identity = 1
        System.out.println("Product: " + product);  // 120

        String concat = Arrays.asList("Java", "is", "great").stream()
                .reduce("", (a, b) -> a + " " + b)
                .trim();
        System.out.println("Concat: " + concat);

        // === Form 3: reduce(identity, accumulator, combiner) ===
        // Used when the result type differs from element type, or for parallel streams
        System.out.println("\n=== Form 3: Type-changing reduce ===");
        int totalLength = Arrays.asList("Java", "Streams", "Reduce").stream()
                .reduce(
                        0,                           // identity (Integer)
                        (len, str) -> len + str.length(), // accumulator: Integer + String → Integer
                        Integer::sum                 // combiner: Integer + Integer → Integer (for parallel)
                );
        System.out.println("Total string length: " + totalLength);

        // === Common interview patterns ===
        System.out.println("\n=== Common patterns ===");

        // Find min without Comparator
        Optional<Integer> min = numbers.stream()
                .reduce(Integer::min);
        System.out.println("Min: " + min.orElse(0));

        // Count elements using reduce
        int count = numbers.stream()
                .reduce(0, (c, e) -> c + 1, Integer::sum);
        System.out.println("Count via reduce: " + count);

        // Joining strings (prefer String.join or Collectors.joining in practice)
        String joined = Arrays.asList("a", "b", "c").stream()
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        System.out.println("Joined: " + joined);
    }
}
