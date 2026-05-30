package interview.level6_jvm_internals.gc;

import java.util.ArrayList;
import java.util.List;

/**
 * Q14. What is String Deduplication in G1 GC?
 *
 * Problem: Strings often consume 25-50% of heap. Many are duplicates
 * (same char[] content, different String objects).
 *
 * String Deduplication (G1 only, Java 8u20+):
 *   - G1 GC identifies String objects with identical char[]/byte[] arrays
 *   - Points duplicate Strings to the SAME underlying array
 *   - The String objects remain separate, only the internal array is shared
 *   - Happens during GC, runs concurrently (low overhead)
 *
 * How it works:
 *   1. During GC, G1 finds Strings that survived to Old Gen
 *   2. Computes hash of the char[]/byte[] content
 *   3. If another String has same content → share the array
 *   4. Duplicate array becomes garbage → collected
 *
 * Enable: -XX:+UseStringDeduplication (only works with G1GC)
 * Note: Enabled by default in some JDK distributions since Java 18
 *
 * This is DIFFERENT from String.intern():
 *
 * | Feature            | String.intern()         | G1 String Dedup          |
 * |-------------------|-------------------------|--------------------------|
 * | What's shared     | String object reference  | Internal char[]/byte[]   |
 * | When              | Explicitly called        | Automatically during GC  |
 * | Scope             | String pool              | Old gen Strings          |
 * | Overhead          | Lookup cost per call     | GC overhead (low)        |
 * | Control           | Developer                | GC                       |
 * | Works with        | Any GC                   | G1 only                  |
 */
public class StringDeduplication {

    public static void main(String[] args) {

        // === Demo: Duplicate strings in memory ===
        System.out.println("=== String Deduplication Demo ===\n");

        // These are SEPARATE objects with SAME content
        String s1 = new String("Hello World");  // new object, new char[]
        String s2 = new String("Hello World");  // new object, new char[]
        String s3 = "Hello World";              // from String pool

        System.out.println("s1 == s2: " + (s1 == s2));       // false (different objects)
        System.out.println("s1.equals(s2): " + s1.equals(s2)); // true (same content)
        System.out.println("s1 == s3: " + (s1 == s3));       // false
        System.out.println("s3 == \"Hello World\": " + (s3 == "Hello World")); // true (pool)

        // With G1 dedup enabled:
        // s1 and s2 remain separate String objects,
        // but their internal byte[] arrays get merged into one

        // === Simulating duplicate strings (common in real apps) ===
        System.out.println("\n=== Common duplicate sources ===");
        List<String> names = new ArrayList<>();
        // Simulating reading from database/CSV — many duplicate values
        for (int i = 0; i < 10000; i++) {
            names.add(new String("John"));    // 10K separate "John" objects
            names.add(new String("Alice"));
            names.add(new String("Bob"));
        }
        System.out.println("Created " + names.size() + " strings (30K objects, only 3 unique values)");
        System.out.println("Without dedup: 30K char[] arrays");
        System.out.println("With dedup:    3 char[] arrays (shared)");

        // === intern() — manual dedup (old approach) ===
        System.out.println("\n=== String.intern() — manual approach ===");
        List<String> interned = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            interned.add(new String("John").intern());   // returns pool reference
            interned.add(new String("Alice").intern());
            interned.add(new String("Bob").intern());
        }
        // Now all "John" entries point to the SAME String object
        System.out.println("interned[0] == interned[3]: " + (interned.get(0) == interned.get(3)));  // true

        names.clear();
        interned.clear();

        // === When to use what ===
        System.out.println("\n=== When to use what ===");
        System.out.println("G1 String Dedup (-XX:+UseStringDeduplication):");
        System.out.println("  ✅ Large heap with many duplicate strings");
        System.out.println("  ✅ No code changes needed");
        System.out.println("  ✅ Automatic, concurrent, low overhead");
        System.out.println("  ❌ Only with G1 GC");
        System.out.println("  ❌ Only deduplicates char[]/byte[], not String objects");

        System.out.println("\nString.intern():");
        System.out.println("  ✅ Works with any GC");
        System.out.println("  ✅ Shares String objects (more memory saved)");
        System.out.println("  ❌ Native string pool — can cause performance issues if overused");
        System.out.println("  ❌ Requires code changes");

        // === JVM flags ===
        System.out.println("\n=== JVM Flags ===");
        System.out.println("-XX:+UseG1GC                    → use G1 (default since Java 9)");
        System.out.println("-XX:+UseStringDeduplication       → enable dedup (G1 only)");
        System.out.println("-XX:StringDeduplicationAgeThreshold=3 → dedup after 3 GC cycles");
        System.out.println("-XX:+PrintStringDeduplicationStatistics → print dedup stats");
    }
}
