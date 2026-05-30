package interview.level2_java8.functional_interfaces;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Q19. How do Predicate.and(), Predicate.or(), and Predicate.negate() work?
 *
 * Predicate<T> has default methods for logical composition:
 *   and(Predicate)  → logical AND (both must be true)
 *   or(Predicate)   → logical OR (at least one true)
 *   negate()        → logical NOT (invert)
 *
 * This allows building complex filters by combining simple predicates
 * instead of writing one large lambda.
 *
 * Also covers: Function.compose() and Function.andThen()
 *   compose(before)  → before.apply FIRST, then this
 *   andThen(after)   → this.apply FIRST, then after
 */
public class PredicateComposition {

    public static void main(String[] args) {

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20);

        // Define simple predicates
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isGreaterThan5 = n -> n > 5;
        Predicate<Integer> isLessThan15 = n -> n < 15;

        // === and() — both conditions must be true ===
        System.out.println("=== Predicate.and() ===");
        List<Integer> evenAndGreater = numbers.stream()
                .filter(isEven.and(isGreaterThan5))
                .collect(Collectors.toList());
        System.out.println("Even AND > 5: " + evenAndGreater);  // [6, 8, 10, 12, 20]

        // === or() — at least one condition true ===
        System.out.println("\n=== Predicate.or() ===");
        List<Integer> evenOrGreater = numbers.stream()
                .filter(isEven.or(isGreaterThan5))
                .collect(Collectors.toList());
        System.out.println("Even OR > 5: " + evenOrGreater);

        // === negate() — invert ===
        System.out.println("\n=== Predicate.negate() ===");
        List<Integer> odds = numbers.stream()
                .filter(isEven.negate())
                .collect(Collectors.toList());
        System.out.println("Not even (odds): " + odds);

        // === Chaining multiple predicates ===
        System.out.println("\n=== Chaining: even AND > 5 AND < 15 ===");
        List<Integer> filtered = numbers.stream()
                .filter(isEven.and(isGreaterThan5).and(isLessThan15))
                .collect(Collectors.toList());
        System.out.println("Result: " + filtered);  // [6, 8, 10, 12]

        // === Predicate.not() — static method (Java 11) ===
        System.out.println("\n=== Predicate.not() (Java 11) ===");
        List<String> names = Arrays.asList("Karan", "", "John", "", "Alice");
        List<String> nonEmpty = names.stream()
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.toList());
        System.out.println("Non-empty: " + nonEmpty);

        // === Function.andThen() and compose() ===
        System.out.println("\n=== Function composition ===");
        Function<Integer, Integer> doubleIt = n -> n * 2;
        Function<Integer, Integer> addTen = n -> n + 10;

        // andThen: doubleIt FIRST, then addTen
        int andThenResult = doubleIt.andThen(addTen).apply(5);
        System.out.println("doubleIt.andThen(addTen).apply(5) = " + andThenResult);  // (5*2)+10 = 20

        // compose: addTen FIRST, then doubleIt
        int composeResult = doubleIt.compose(addTen).apply(5);
        System.out.println("doubleIt.compose(addTen).apply(5) = " + composeResult);  // (5+10)*2 = 30

        // === Consumer.andThen() ===
        System.out.println("\n=== Consumer.andThen() ===");
        Consumer<String> print = System.out::println;
        Consumer<String> printUpper = s -> System.out.println("  → " + s.toUpperCase());

        print.andThen(printUpper).accept("hello");
    }
}
