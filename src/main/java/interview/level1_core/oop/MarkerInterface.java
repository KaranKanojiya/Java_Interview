package interview.level1_core.oop;

import java.io.Serializable;

/**
 * Q15. What is a Marker Interface?
 *
 * A marker interface has NO methods or fields.
 * It "marks" a class as having a certain property that the JVM or framework checks at runtime.
 *
 * Built-in marker interfaces:
 *   - Serializable  → JVM allows serialization/deserialization
 *   - Cloneable     → Object.clone() works without CloneNotSupportedException
 *   - Remote        → RMI (Remote Method Invocation) marker
 *
 * How it works:
 *   JVM/framework uses instanceof to check: if (obj instanceof Serializable)
 *
 * Marker Interface vs Annotation (modern alternative):
 *   | Feature        | Marker Interface       | Annotation            |
 *   |---------------|------------------------|-----------------------|
 *   | Type check    | instanceof at runtime  | Reflection at runtime |
 *   | Compile-time  | Type system enforced   | Not type-checked      |
 *   | Inheritance   | Subclasses inherit it  | Depends on @Inherited |
 *   | Metadata      | No                     | Can carry data        |
 *   | Example       | Serializable           | @Entity, @Deprecated  |
 *
 * When to use marker interface over annotation:
 *   - When you want compile-time type checking (method parameter type)
 *   - When all subclasses should automatically inherit the marking
 */
public class MarkerInterface {

    // Custom marker interface
    interface Deletable { }  // no methods — just a marker

    // Classes that implement the marker
    static class TempFile implements Deletable {
        String name;
        TempFile(String name) { this.name = name; }
    }

    static class ImportantFile {
        String name;
        ImportantFile(String name) { this.name = name; }
    }

    // Method that uses the marker for type safety
    static void deleteIfAllowed(Object obj) {
        if (obj instanceof Deletable) {
            System.out.println("  Deleting: " + obj);
        } else {
            System.out.println("  Cannot delete: " + obj.getClass().getSimpleName() + " is not Deletable");
        }
    }

    // Compile-time type safety with marker interface as parameter
    static void safeDelete(Deletable item) {
        System.out.println("  Safely deleting: " + item);
    }

    public static void main(String[] args) {

        // === Serializable — built-in marker ===
        System.out.println("=== Serializable (built-in marker) ===");
        String s = "Hello";
        Integer n = 42;
        Object obj = new Object();

        System.out.println("String is Serializable: " + (s instanceof Serializable));   // true
        System.out.println("Integer is Serializable: " + (n instanceof Serializable));   // true
        System.out.println("Object is Serializable: " + (obj instanceof Serializable));  // false

        // === Cloneable — built-in marker ===
        System.out.println("\n=== Cloneable (built-in marker) ===");
        System.out.println("Without Cloneable → clone() throws CloneNotSupportedException");
        System.out.println("With Cloneable    → clone() works");

        // === Custom marker interface ===
        System.out.println("\n=== Custom Marker Interface ===");
        TempFile temp = new TempFile("cache.tmp");
        ImportantFile important = new ImportantFile("data.db");

        deleteIfAllowed(temp);        // allowed (implements Deletable)
        deleteIfAllowed(important);   // not allowed

        // Compile-time safety: safeDelete only accepts Deletable
        safeDelete(temp);
        // safeDelete(important);  // COMPILE ERROR — not Deletable

        // === Why marker interface over annotation? ===
        System.out.println("\n=== Marker Interface vs Annotation ===");
        System.out.println("Marker Interface:");
        System.out.println("  ✅ void process(Serializable s) — compile-time type check");
        System.out.println("  ✅ Subclasses automatically inherit the marker");
        System.out.println("  ❌ Cannot carry metadata");
        System.out.println("\nAnnotation:");
        System.out.println("  ✅ @Cacheable(ttl=300) — can carry data");
        System.out.println("  ✅ Can target methods, fields, not just classes");
        System.out.println("  ❌ No compile-time type checking");
    }
}
