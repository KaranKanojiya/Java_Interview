package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Q13. What is the difference between findFirst() and findAny()?
 *
 * findFirst():
 *   - Returns the FIRST element in encounter order
 *   - Deterministic — always same result
 *   - In parallel streams, may be slower (must find the first, not just any)
 *
 * findAny():
 *   - Returns ANY element (non-deterministic in parallel streams)
 *   - In sequential streams, behaves like findFirst()
 *   - In parallel streams, returns whichever element is found first by any thread
 *   - Faster in parallel because no ordering constraint
 *
 * Both return Optional<T>.
 *
 * Rule of thumb:
 *   - Need deterministic result → findFirst()
 *   - Just need existence check / any match → findAny() (better for parallel)
 */
public class FindFirstVsFindAny {

    public static void main(String[] args) {

        List<Integer> numbers = Arrays.asList(5, 3, 8, 1, 9, 2, 7, 4, 6);

        // === Sequential: both behave the same ===
        System.out.println("=== Sequential Stream ===");
        Optional<Integer> first = numbers.stream()
                .filter(n -> n > 4)
                .findFirst();
        System.out.println("findFirst (>4): " + first.orElse(-1));  // Always 5

        Optional<Integer> any = numbers.stream()
                .filter(n -> n > 4)
                .findAny();
        System.out.println("findAny   (>4): " + any.orElse(-1));  // Also 5 (sequential)

        // === Parallel: findAny may return different results ===
        System.out.println("\n=== Parallel Stream (run multiple times to see variance) ===");
        for (int i = 0; i < 5; i++) {
            Optional<Integer> parallelFirst = numbers.parallelStream()
                    .filter(n -> n > 4)
                    .findFirst();

            Optional<Integer> parallelAny = numbers.parallelStream()
                    .filter(n -> n > 4)
                    .findAny();

            System.out.println("Run " + (i + 1)
                    + " → findFirst: " + parallelFirst.orElse(-1)
                    + ", findAny: " + parallelAny.orElse(-1));
        }

        // === Empty stream ===
        System.out.println("\n=== Empty Stream ===");
        Optional<Integer> emptyFirst = numbers.stream()
                .filter(n -> n > 100)
                .findFirst();
        System.out.println("findFirst on empty: " + emptyFirst);  // Optional.empty

        // === With short-circuit ===
        System.out.println("\n=== Short-circuit behavior ===");
        Optional<Integer> shortCircuit = numbers.stream()
                .peek(n -> System.out.print("Processing " + n + " → "))
                .filter(n -> n > 4)
                .findFirst();  // Stops after finding 5
        System.out.println("\nResult: " + shortCircuit.orElse(-1));
    }
}
