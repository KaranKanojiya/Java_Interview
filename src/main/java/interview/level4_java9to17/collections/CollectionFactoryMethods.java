package interview.level4_java9to17.collections;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Q12. What are Collection factory methods introduced in Java 9?
 *
 * Java 9 added static factory methods to create IMMUTABLE collections:
 *   List.of(...)   → immutable List
 *   Set.of(...)    → immutable Set
 *   Map.of(k,v,..) → immutable Map (up to 10 entries)
 *   Map.ofEntries(Map.entry(k,v), ...) → immutable Map (any size)
 *
 * Properties:
 *   - Immutable: add/remove/set throw UnsupportedOperationException
 *   - No nulls: null elements/keys/values throw NullPointerException
 *   - No duplicates in Set.of() and Map.of() keys (throws IllegalArgumentException)
 *   - Serializable
 *   - Iteration order: unspecified for Set and Map (may differ between JVM runs)
 *
 * Before Java 9:
 *   List<String> list = Collections.unmodifiableList(Arrays.asList("a", "b"));
 * After Java 9:
 *   List<String> list = List.of("a", "b");
 */
public class CollectionFactoryMethods {

    public static void main(String[] args) {

        // === List.of() ===
        System.out.println("=== List.of() ===");
        List<String> list = List.of("Java", "Python", "Go");
        System.out.println("List: " + list);
        try { list.add("Rust"); } catch (UnsupportedOperationException e) {
            System.out.println("Cannot add: immutable");
        }
        try { List.of("A", null); } catch (NullPointerException e) {
            System.out.println("Cannot have null: " + e.getClass().getSimpleName());
        }

        // === Set.of() ===
        System.out.println("\n=== Set.of() ===");
        Set<String> set = Set.of("A", "B", "C");
        System.out.println("Set: " + set);
        try { Set.of("A", "A"); } catch (IllegalArgumentException e) {
            System.out.println("No duplicates in Set.of(): " + e.getMessage());
        }

        // === Map.of() — up to 10 key-value pairs ===
        System.out.println("\n=== Map.of() ===");
        Map<String, Integer> map = Map.of("Java", 1, "Python", 2, "Go", 3);
        System.out.println("Map: " + map);

        // === Map.ofEntries() — any number of entries ===
        System.out.println("\n=== Map.ofEntries() ===");
        Map<String, Integer> bigMap = Map.ofEntries(
                Map.entry("one", 1),
                Map.entry("two", 2),
                Map.entry("three", 3),
                Map.entry("four", 4)
        );
        System.out.println("Big map: " + bigMap);

        // === List.copyOf(), Set.copyOf(), Map.copyOf() (Java 10) ===
        System.out.println("\n=== copyOf() (Java 10) ===");
        var mutableList = new java.util.ArrayList<>(List.of("X", "Y"));
        List<String> copy = List.copyOf(mutableList);
        mutableList.add("Z");
        System.out.println("Original: " + mutableList);  // [X, Y, Z]
        System.out.println("Copy (unchanged): " + copy);  // [X, Y]

        System.out.println("\n=== Summary ===");
        System.out.println("List.of(e1, e2, ...)         → immutable, no nulls");
        System.out.println("Set.of(e1, e2, ...)          → immutable, no nulls, no duplicates");
        System.out.println("Map.of(k1,v1, k2,v2, ...)    → immutable, up to 10 pairs");
        System.out.println("Map.ofEntries(entry(k,v),..) → immutable, any size");
        System.out.println("*.copyOf(collection)         → immutable copy (Java 10)");
    }
}
