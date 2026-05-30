package interview.level1_core.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Q23. Can you add elements to a final List?
 *
 * YES! final means the REFERENCE cannot change, but the object itself CAN be modified.
 *
 * final List<String> list = new ArrayList<>();
 *   ✅ list.add("hello")       — modifying the object is allowed
 *   ✅ list.remove(0)          — modifying the object is allowed
 *   ❌ list = new ArrayList<>() — reassigning the reference is NOT allowed
 *
 * To make a truly unmodifiable list:
 *   - Collections.unmodifiableList(list) — view (changes to original reflected)
 *   - List.of("a", "b")                 — truly immutable (Java 9+)
 *   - List.copyOf(list)                 — immutable copy (Java 10+)
 */
public class FinalListDemo {

    public static void main(String[] args) {

        // === final reference — object still mutable ===
        System.out.println("=== final List — reference is fixed, content is mutable ===");
        final List<String> list = new ArrayList<>();
        list.add("Java");     // OK
        list.add("Python");   // OK
        list.remove(0);       // OK
        System.out.println("Modified final list: " + list);

        // list = new ArrayList<>();  // COMPILE ERROR: cannot reassign final variable

        // === Truly immutable lists ===
        System.out.println("\n=== List.of() — truly immutable (Java 9+) ===");
        List<String> immutable = List.of("A", "B", "C");
        try {
            immutable.add("D");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot add to List.of(): " + e.getClass().getSimpleName());
        }

        System.out.println("\n=== Collections.unmodifiableList() — view ===");
        List<String> original = new ArrayList<>(List.of("X", "Y"));
        List<String> view = Collections.unmodifiableList(original);
        original.add("Z");  // original modified
        System.out.println("View reflects original changes: " + view);  // [X, Y, Z]

        // === Summary ===
        System.out.println("\n=== Summary ===");
        System.out.println("final List:                  reference fixed, content mutable");
        System.out.println("Collections.unmodifiableList: unmodifiable VIEW of mutable list");
        System.out.println("List.of():                   truly immutable, no nulls");
        System.out.println("List.copyOf():               immutable copy, no nulls");
    }
}
