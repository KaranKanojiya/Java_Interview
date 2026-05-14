package interview.level1_core.collections;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: How does ConcurrentHashMap differ from Hashtable?
// A: Hashtable synchronizes every method on the entire table (one global lock).
//    ConcurrentHashMap uses fine-grained locking:
//    - Java 7: Segment-based locking (16 segments by default, each independently locked).
//    - Java 8+: Per-bucket CAS + synchronized on the first node of each bucket.
//    ConcurrentHashMap allows concurrent reads without locking and concurrent writes to
//    different buckets. Hashtable blocks everything.
//
// Q: How does ConcurrentHashMap work in Java 8+?
// A: Uses an array of Nodes (like HashMap). For writes:
//    1. If bucket is empty, use CAS (Compare-And-Swap) to insert — no lock needed.
//    2. If bucket has entries, synchronized on the first node of that bucket only.
//    3. Reads are lock-free (volatile reads of Node values).
//    This means multiple threads can write to DIFFERENT buckets simultaneously.
//
// Q: Does ConcurrentHashMap allow null keys or values?
// A: No! Unlike HashMap, ConcurrentHashMap does NOT allow null keys or null values.
//    Reason: In a concurrent context, map.get(key) returning null is ambiguous —
//    does the key not exist, or is the value null? containsKey() would be a separate
//    (non-atomic) operation, creating a race condition.
//
// Q: What are the atomic operations in ConcurrentHashMap?
// A: putIfAbsent(), remove(key, value), replace(key, oldVal, newVal),
//    compute(), computeIfAbsent(), computeIfPresent(), merge().
//    These are all atomic — no need for external synchronization.
//
// Q: What is the difference between Collections.synchronizedMap() and ConcurrentHashMap?
// A: synchronizedMap wraps a HashMap with a single mutex — every operation locks the
//    entire map. ConcurrentHashMap uses fine-grained locking and CAS. Also,
//    synchronizedMap's iterators are fail-fast; ConcurrentHashMap's are weakly consistent.
//
// Q: What does "weakly consistent" iterator mean?
// A: It reflects the state of the map at the time of creation and may (but is not
//    guaranteed to) reflect modifications made after creation. It will NEVER throw
//    ConcurrentModificationException.
// ========================================================

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentHashMapInternals {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================");
        System.out.println("  ConcurrentHashMap Deep Dive");
        System.out.println("========================================\n");

        // --- 1. Basic ConcurrentHashMap vs Hashtable ---
        System.out.println("=== 1. ConcurrentHashMap vs Hashtable vs synchronizedMap ===");
        System.out.println("Feature                  | ConcurrentHashMap    | Hashtable          | synchronizedMap");
        System.out.println("-------------------------|---------------------|--------------------|------------------");
        System.out.println("Locking                  | Per-bucket (Java 8) | Entire table       | Entire map");
        System.out.println("Null key/value           | Neither allowed     | Neither allowed    | Both allowed");
        System.out.println("Iterator                 | Weakly consistent   | Fail-fast          | Fail-fast");
        System.out.println("Performance              | High concurrency    | Low (single lock)  | Low (single lock)");
        System.out.println("ConcurrentModification   | Never thrown        | Can be thrown       | Can be thrown");
        System.out.println();

        // --- 2. Null key/value behavior ---
        System.out.println("=== 2. Null Key/Value Behavior ===");
        ConcurrentHashMap<String, String> cmap = new ConcurrentHashMap<>();
        try {
            cmap.put(null, "value");
        } catch (NullPointerException e) {
            System.out.println("put(null, value): NullPointerException! Null keys not allowed.");
        }
        try {
            cmap.put("key", null);
        } catch (NullPointerException e) {
            System.out.println("put(key, null): NullPointerException! Null values not allowed.");
        }
        System.out.println();

        // --- 3. Atomic operations demo ---
        System.out.println("=== 3. Atomic Operations ===");
        ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();

        // putIfAbsent — atomic check-and-insert
        scores.putIfAbsent("Alice", 90);
        scores.putIfAbsent("Alice", 100);  // won't overwrite — Alice already exists
        System.out.println("putIfAbsent: Alice=" + scores.get("Alice") + " (first value wins)");

        // computeIfAbsent — compute value only if key is absent
        scores.computeIfAbsent("Bob", k -> {
            System.out.println("  Computing value for " + k + "...");
            return 85;
        });
        System.out.println("computeIfAbsent: Bob=" + scores.get("Bob"));

        // compute — atomic read-modify-write
        scores.compute("Alice", (k, v) -> v + 10);
        System.out.println("compute Alice +10: " + scores.get("Alice"));

        // merge — atomic merge with existing value
        scores.merge("Bob", 15, Integer::sum);  // 85 + 15 = 100
        System.out.println("merge Bob +15: " + scores.get("Bob"));

        // replace — atomic conditional replace
        boolean replaced = scores.replace("Alice", 100, 999);
        System.out.println("replace(Alice, 100, 999): " + replaced + " -> Alice=" + scores.get("Alice"));
        System.out.println();

        // --- 4. Concurrent writes to demonstrate thread safety ---
        System.out.println("=== 4. Concurrent Write Safety ===");
        ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
        counters.put("counter", new AtomicInteger(0));

        int numThreads = 10;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counters.get("counter").incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        int expected = numThreads * incrementsPerThread;
        int actual = counters.get("counter").get();
        System.out.println("Expected: " + expected + ", Actual: " + actual +
                " -> " + (expected == actual ? "THREAD-SAFE!" : "RACE CONDITION!"));
        System.out.println();

        // --- 5. computeIfAbsent for caching pattern ---
        System.out.println("=== 5. Caching Pattern with computeIfAbsent ===");
        ConcurrentHashMap<Integer, String> cache = new ConcurrentHashMap<>();

        // Simulate expensive computation — only runs once per key
        String result1 = cache.computeIfAbsent(42, k -> {
            System.out.println("  Cache miss! Computing for key=" + k);
            return "ExpensiveResult_" + k;
        });
        String result2 = cache.computeIfAbsent(42, k -> {
            System.out.println("  This should NOT print (cache hit)");
            return "ShouldNotSeeThis";
        });
        System.out.println("First call: " + result1);
        System.out.println("Second call (cached): " + result2);
        System.out.println();

        // --- 6. Bulk operations (Java 8+) ---
        System.out.println("=== 6. Bulk Operations (parallelismThreshold) ===");
        ConcurrentHashMap<String, Integer> data = new ConcurrentHashMap<>();
        data.put("A", 10);
        data.put("B", 20);
        data.put("C", 30);
        data.put("D", 40);

        // forEach with parallelism threshold
        // threshold = 1 means always parallel; Long.MAX_VALUE means sequential
        System.out.print("forEach (parallel): ");
        data.forEach(1, (k, v) -> System.out.print(k + "=" + v + " "));
        System.out.println();

        // reduce
        int sum = data.reduceValues(1, Integer::sum);
        System.out.println("reduceValues (sum): " + sum);

        // search — returns first non-null result
        String found = data.search(1, (k, v) -> v > 25 ? k : null);
        System.out.println("search (value > 25): " + found);
        System.out.println();

        // --- 7. Weakly consistent iterator ---
        System.out.println("=== 7. Weakly Consistent Iterator ===");
        ConcurrentHashMap<String, String> iterMap = new ConcurrentHashMap<>();
        iterMap.put("one", "1");
        iterMap.put("two", "2");
        iterMap.put("three", "3");

        System.out.println("Iterating while modifying (no ConcurrentModificationException):");
        for (Map.Entry<String, String> entry : iterMap.entrySet()) {
            System.out.println("  Reading: " + entry.getKey());
            // This is safe — no CME thrown
            iterMap.put("four", "4");
        }
        System.out.println("After iteration, map has: " + iterMap.keySet());
        System.out.println();

        // --- 8. Common interview pitfall: compound operations ---
        System.out.println("=== 8. Pitfall: Non-Atomic Compound Operations ===");
        System.out.println("WRONG (race condition):");
        System.out.println("  if (!map.containsKey(key)) { map.put(key, value); }");
        System.out.println("RIGHT (atomic):");
        System.out.println("  map.putIfAbsent(key, value);");
        System.out.println();
        System.out.println("WRONG (race condition):");
        System.out.println("  int val = map.get(key); map.put(key, val + 1);");
        System.out.println("RIGHT (atomic):");
        System.out.println("  map.compute(key, (k, v) -> v + 1);");
        System.out.println("  // OR: map.merge(key, 1, Integer::sum);");
    }
}
