package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Q16. What is peek() and when should you use it?
 *
 * peek() is an intermediate operation that performs an action on each element
 * WITHOUT modifying the stream. It returns the same stream.
 *
 * Signature: Stream<T> peek(Consumer<? super T> action)
 *
 * Primary use: DEBUGGING — see what flows through the pipeline at each stage.
 *
 * ⚠️ Important gotchas:
 *   1. peek() is lazy — won't execute without a terminal operation
 *   2. In parallel streams, peek order is non-deterministic
 *   3. Don't use peek for side effects in production — use forEach instead
 *   4. peek may be optimized away by the JVM (not guaranteed to execute)
 *   5. Don't mutate elements in peek — it's meant for observation only
 */
public class PeekDemo {

    public static void main(String[] args) {

        List<String> names = Arrays.asList("karan", "john", "alice", "bob", "charlie");

        // === peek() for debugging pipeline stages ===
        System.out.println("=== Debugging with peek() ===");
        List<String> result = names.stream()
                .peek(n -> System.out.println("  Original: " + n))
                .filter(n -> n.length() > 3)
                .peek(n -> System.out.println("  After filter: " + n))
                .map(String::toUpperCase)
                .peek(n -> System.out.println("  After map: " + n))
                .collect(Collectors.toList());
        System.out.println("Result: " + result);

        // === peek() without terminal operation — NOTHING happens ===
        System.out.println("\n=== peek() without terminal op (nothing prints) ===");
        names.stream()
                .peek(n -> System.out.println("  This will NOT print: " + n));
        System.out.println("(See? Nothing printed above)");

        // === peek() vs forEach() ===
        System.out.println("\n=== peek() vs forEach() ===");
        // peek: intermediate (returns stream, can chain)
        // forEach: terminal (returns void, cannot chain)
        List<String> peeked = names.stream()
                .peek(n -> {}) // can continue chaining
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println("peek allows chaining: " + peeked);

        // forEach is terminal — this is the end
        System.out.print("forEach is terminal: ");
        names.stream()
                .map(String::toUpperCase)
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        // === Counting with peek (debugging how many elements pass) ===
        System.out.println("\n=== Counting elements at each stage ===");
        long[] filterCount = {0};
        long[] mapCount = {0};
        List<String> tracked = names.stream()
                .filter(n -> n.length() > 3)
                .peek(n -> filterCount[0]++)
                .map(String::toUpperCase)
                .peek(n -> mapCount[0]++)
                .collect(Collectors.toList());
        System.out.println("Passed filter: " + filterCount[0]);
        System.out.println("Passed map: " + mapCount[0]);
        System.out.println("Final: " + tracked);
    }
}
