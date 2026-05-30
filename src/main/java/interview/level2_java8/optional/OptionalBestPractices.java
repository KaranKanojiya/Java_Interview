package interview.level2_java8.optional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Q11. What is Optional and how to use it correctly?
 *
 * Optional<T> is a container that may or may not hold a non-null value.
 * Introduced in Java 8 to avoid NullPointerException and make APIs explicit about nullable returns.
 *
 * Creating:
 *   Optional.empty()        → empty Optional
 *   Optional.of(value)      → throws NPE if value is null
 *   Optional.ofNullable(v)  → empty if null, present if non-null
 *
 * Key methods:
 *   isPresent() / isEmpty()        → boolean check
 *   get()                          → returns value or throws NoSuchElementException
 *   orElse(default)                → returns value or default
 *   orElseGet(Supplier)            → lazy default (only computed if empty)
 *   orElseThrow(Supplier)          → returns value or throws custom exception
 *   map(Function)                  → transform if present
 *   flatMap(Function)              → transform when function returns Optional
 *   filter(Predicate)              → empty if predicate fails
 *   ifPresent(Consumer)            → execute action if present
 *
 * Anti-patterns:
 *   ❌ optional.get() without isPresent() check
 *   ❌ Optional as method parameter
 *   ❌ Optional for class fields
 *   ❌ Optional.of(null)
 *   ❌ if (optional.isPresent()) { return optional.get(); } — use orElse instead!
 */
public class OptionalBestPractices {

    public static void main(String[] args) {

        // === Creating Optionals ===
        System.out.println("=== Creating Optionals ===");
        Optional<String> empty = Optional.empty();
        Optional<String> present = Optional.of("Java");
        Optional<String> nullable = Optional.ofNullable(null);

        System.out.println("empty: " + empty);
        System.out.println("present: " + present);
        System.out.println("nullable: " + nullable);

        // === orElse vs orElseGet ===
        System.out.println("\n=== orElse vs orElseGet ===");
        // orElse: default is ALWAYS evaluated (even if value is present)
        String result1 = present.orElse(expensiveDefault());
        System.out.println("orElse with present: " + result1);

        // orElseGet: default is ONLY evaluated when empty (lazy)
        String result2 = present.orElseGet(() -> expensiveDefault());
        System.out.println("orElseGet with present: " + result2);

        // For empty Optionals, both behave the same
        String result3 = empty.orElse("default");
        String result4 = empty.orElseGet(() -> "default");
        System.out.println("orElse with empty: " + result3);
        System.out.println("orElseGet with empty: " + result4);

        // === map() — transform value if present ===
        System.out.println("\n=== map() ===");
        Optional<Integer> length = present.map(String::length);
        System.out.println("Length of 'Java': " + length.orElse(0));

        Optional<Integer> emptyLength = empty.map(String::length);
        System.out.println("Length of empty: " + emptyLength.orElse(0));

        // === flatMap() — when transformation returns Optional ===
        System.out.println("\n=== flatMap() ===");
        Optional<String> upper = present.flatMap(s -> Optional.of(s.toUpperCase()));
        System.out.println("FlatMap uppercase: " + upper.orElse("N/A"));

        // Chaining: user → address → city
        Optional<String> city = findUser("karan")
                .flatMap(OptionalBestPractices::findAddress)
                .flatMap(OptionalBestPractices::findCity);
        System.out.println("City: " + city.orElse("Unknown"));

        // === filter() ===
        System.out.println("\n=== filter() ===");
        Optional<String> longName = present.filter(s -> s.length() > 3);
        Optional<String> shortName = present.filter(s -> s.length() > 10);
        System.out.println("Filter length > 3: " + longName);   // Optional[Java]
        System.out.println("Filter length > 10: " + shortName);  // Optional.empty

        // === ifPresent() ===
        System.out.println("\n=== ifPresent() ===");
        present.ifPresent(v -> System.out.println("Value is present: " + v));
        empty.ifPresent(v -> System.out.println("This won't print"));

        // === orElseThrow() ===
        System.out.println("\n=== orElseThrow() ===");
        try {
            String value = empty.orElseThrow(() -> new RuntimeException("Value not found!"));
        } catch (RuntimeException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // === Optional with Streams ===
        System.out.println("\n=== Optional with Streams ===");
        List<Optional<String>> optionals = Arrays.asList(
                Optional.of("A"), Optional.empty(), Optional.of("B"), Optional.empty(), Optional.of("C")
        );
        List<String> values = optionals.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        System.out.println("Filtered optionals: " + values);  // [A, B, C]
    }

    private static String expensiveDefault() {
        System.out.println("  → expensiveDefault() was called!");
        return "expensive";
    }

    private static Optional<String> findUser(String name) {
        return Optional.of("User:" + name);
    }

    private static Optional<String> findAddress(String user) {
        return Optional.of("Address:Mumbai");
    }

    private static Optional<String> findCity(String address) {
        return Optional.of("Mumbai");
    }
}
