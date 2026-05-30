package interview.level2_java8.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Q18. What is the difference between toUnmodifiableList() and toList()?
 *
 * Collectors.toUnmodifiableList() (Java 10):
 *   - Returns a truly unmodifiable list
 *   - Does NOT allow null elements (throws NPE)
 *   - Backed by an immutable implementation
 *
 * Stream.toList() (Java 16):
 *   - Returns an unmodifiable list
 *   - DOES allow null elements
 *   - Shorter syntax (no Collectors import needed)
 *
 * Collectors.toList():
 *   - Returns a MUTABLE ArrayList
 *   - Allows nulls
 *   - Most commonly used, but result is modifiable!
 *
 * Collections.unmodifiableList(list):
 *   - Returns an unmodifiable VIEW of the original list
 *   - Changes to original list are reflected in the view!
 *   - Not a true immutable copy
 */
public class UnmodifiableListDemo {

    public static void main(String[] args) {

        List<String> source = Arrays.asList("Java", "Python", "Go");

        // === Collectors.toList() — returns mutable ArrayList ===
        System.out.println("=== Collectors.toList() → mutable ===");
        List<String> mutable = source.stream().collect(Collectors.toList());
        mutable.add("Rust");  // Works!
        System.out.println("After add: " + mutable);

        // === Collectors.toUnmodifiableList() — truly immutable, no nulls ===
        System.out.println("\n=== Collectors.toUnmodifiableList() → immutable, no nulls ===");
        List<String> unmodifiable = source.stream().collect(Collectors.toUnmodifiableList());
        try {
            unmodifiable.add("Rust");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot add to unmodifiableList: " + e.getClass().getSimpleName());
        }

        // Null rejection
        try {
            List<String> withNull = Arrays.asList("A", null, "B").stream()
                    .collect(Collectors.toUnmodifiableList());
        } catch (NullPointerException e) {
            System.out.println("toUnmodifiableList rejects nulls: " + e.getClass().getSimpleName());
        }

        // === Stream.toList() (Java 16) — immutable, allows nulls ===
        System.out.println("\n=== Stream.toList() → immutable, allows nulls ===");
        List<String> streamToList = source.stream().toList();
        try {
            streamToList.add("Rust");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot add to toList(): " + e.getClass().getSimpleName());
        }

        // Allows nulls
        List<String> withNullOk = Arrays.asList("A", null, "B").stream().toList();
        System.out.println("toList() with nulls: " + withNullOk);

        // === Collections.unmodifiableList() — VIEW, not copy! ===
        System.out.println("\n=== Collections.unmodifiableList() → view of original ===");
        List<String> original = new ArrayList<>(Arrays.asList("X", "Y", "Z"));
        List<String> view = Collections.unmodifiableList(original);

        // Modify original — view changes too!
        original.add("W");
        System.out.println("Original: " + original);
        System.out.println("View reflects change: " + view);  // [X, Y, Z, W]

        try {
            view.add("V");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify through view: " + e.getClass().getSimpleName());
        }

        // === Summary ===
        System.out.println("\n=== Summary ===");
        System.out.println("Collectors.toList()              → mutable, allows nulls");
        System.out.println("Collectors.toUnmodifiableList()  → immutable, NO nulls");
        System.out.println("Stream.toList()                  → immutable, allows nulls");
        System.out.println("Collections.unmodifiableList()   → unmodifiable VIEW (not a copy)");
    }
}
