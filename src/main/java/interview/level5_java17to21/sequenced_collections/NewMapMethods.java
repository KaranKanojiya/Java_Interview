package interview.level5_java17to21.sequenced_collections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Q13. What are the new Map methods added since Java 9?
 *
 * Java 8:
 *   getOrDefault(key, default)         → returns default if key absent
 *   putIfAbsent(key, value)            → only puts if key is absent
 *   computeIfAbsent(key, function)     → compute + put if key absent (lazy)
 *   computeIfPresent(key, biFunction)  → compute + put if key present
 *   compute(key, biFunction)           → always compute, put result
 *   merge(key, value, remappingFn)     → merge with existing value
 *   replaceAll(biFunction)             → replace all values
 *   forEach(biConsumer)                → iterate key-value pairs
 *
 * Java 9:
 *   Map.of(k, v, ...)                 → immutable map factory (up to 10 pairs)
 *   Map.ofEntries(entry(k,v), ...)    → immutable map factory (any size)
 *   Map.entry(k, v)                   → create immutable entry
 *   Map.copyOf(map)                   → immutable copy (Java 10)
 */
public class NewMapMethods {

    public static void main(String[] args) {

        Map<String, Integer> scores = new HashMap<>();
        scores.put("Karan", 90);
        scores.put("John", 85);

        // === getOrDefault ===
        System.out.println("=== getOrDefault ===");
        int karanScore = scores.getOrDefault("Karan", 0);
        int unknownScore = scores.getOrDefault("Unknown", 0);
        System.out.println("Karan: " + karanScore + ", Unknown: " + unknownScore);

        // === putIfAbsent ===
        System.out.println("\n=== putIfAbsent ===");
        scores.putIfAbsent("John", 100);   // John exists → no change
        scores.putIfAbsent("Alice", 95);   // Alice absent → inserted
        System.out.println("John (unchanged): " + scores.get("John"));
        System.out.println("Alice (new): " + scores.get("Alice"));

        // === computeIfAbsent — LAZY computation ===
        System.out.println("\n=== computeIfAbsent (lazy) ===");
        // Great for initializing collections in maps
        Map<String, java.util.List<String>> groups = new HashMap<>();
        groups.computeIfAbsent("teamA", k -> new java.util.ArrayList<>()).add("Karan");
        groups.computeIfAbsent("teamA", k -> new java.util.ArrayList<>()).add("John");
        System.out.println("teamA: " + groups.get("teamA"));  // [Karan, John]

        // === computeIfPresent ===
        System.out.println("\n=== computeIfPresent ===");
        scores.computeIfPresent("Karan", (k, v) -> v + 10);  // exists → 90+10=100
        scores.computeIfPresent("Nobody", (k, v) -> v + 10);  // absent → no-op
        System.out.println("Karan updated: " + scores.get("Karan"));

        // === compute — always applies ===
        System.out.println("\n=== compute ===");
        scores.compute("Bob", (k, v) -> (v == null) ? 50 : v + 10);
        System.out.println("Bob (new): " + scores.get("Bob"));
        scores.compute("Bob", (k, v) -> v + 10);
        System.out.println("Bob (updated): " + scores.get("Bob"));

        // === merge — combine with existing ===
        System.out.println("\n=== merge ===");
        // Word counting pattern
        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = {"java", "python", "java", "go", "java", "python"};
        for (String word : words) {
            wordCount.merge(word, 1, Integer::sum);  // if exists: old + 1, if new: just 1
        }
        System.out.println("Word counts: " + wordCount);

        // === replaceAll ===
        System.out.println("\n=== replaceAll ===");
        Map<String, Integer> prices = new HashMap<>(Map.of("A", 100, "B", 200, "C", 300));
        prices.replaceAll((k, v) -> (int) (v * 1.1));  // 10% increase
        System.out.println("After 10% increase: " + prices);

        // === forEach ===
        System.out.println("\n=== forEach ===");
        scores.forEach((name, score) -> System.out.println("  " + name + ": " + score));

        // === ConcurrentHashMap extras ===
        System.out.println("\n=== ConcurrentHashMap extras ===");
        ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>(scores);
        // Atomic compute — no external synchronization needed
        concMap.compute("Karan", (k, v) -> v + 5);
        System.out.println("Atomic update: Karan=" + concMap.get("Karan"));
    }
}
