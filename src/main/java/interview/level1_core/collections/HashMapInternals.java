package interview.level1_core.collections;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: How does HashMap work internally in Java 8+?
// A: HashMap uses an array of Node buckets. Each bucket is a linked list (or TreeNode
//    after threshold). On put(key, value):
//    1. Compute hash: hash = key.hashCode() ^ (key.hashCode() >>> 16) — "spread" function
//    2. Find bucket index: index = hash & (capacity - 1)
//    3. If bucket is empty, insert new Node
//    4. If bucket has entries, traverse chain:
//       - If key matches (hash equal AND equals() true), replace value
//       - Else append to end of chain
//    5. If chain length >= TREEIFY_THRESHOLD (8), convert to Red-Black tree
//    6. If size > capacity * loadFactor, resize (double capacity, rehash all entries)
//
// Q: Why does HashMap use hash ^ (hash >>> 16)?
// A: This "perturbation function" mixes higher bits into lower bits. Since bucket index
//    uses only lower bits (hash & (capacity-1)), this reduces collisions when hashCode()
//    implementations differ only in higher bits.
//
// Q: What happens during resize (rehashing)?
// A: Capacity doubles. Each entry is re-bucketed: either stays at same index or moves to
//    index + oldCapacity. Java 8 optimizes this by checking one bit (hash & oldCapacity)
//    instead of recomputing the full index.
//
// Q: When does treeification happen?
// A: When a bucket's chain length reaches 8 (TREEIFY_THRESHOLD) AND the table capacity
//    is >= 64 (MIN_TREEIFY_CAPACITY). If capacity < 64, it resizes instead.
//    Untreeify threshold is 6 (during resize, if tree has <= 6 nodes, convert back to list).
//
// Q: Why is initial capacity a power of 2?
// A: So that (hash & (capacity - 1)) works as a fast modulo operation.
//    HashMap always rounds up to the nearest power of 2.
//
// Q: What is the default load factor and why 0.75?
// A: Default is 0.75. It's a trade-off: lower value = less collisions but more memory;
//    higher value = more collisions but less memory. 0.75 offers ~25% empty buckets
//    on average, which gives good performance for both time and space.
//
// Q: What happens if two keys have the same hashCode()?
// A: They go to the same bucket. HashMap then uses equals() to distinguish them.
//    If equals() returns false, both entries coexist in the same bucket (collision).
//    This is why BOTH hashCode() and equals() must be correctly overridden.
// ========================================================

import java.util.HashMap;
import java.util.Map;

public class HashMapInternals {

    // Custom key class to demonstrate hash collisions
    static class Key {
        private final int id;
        private final String name;

        Key(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public int hashCode() {
            // Deliberately simple hash to show collision behavior
            return id % 4;  // Only 4 possible hash values -> lots of collisions
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return id == key.id && name.equals(key.name);
        }

        @Override
        public String toString() {
            return "Key{" + id + ", " + name + "}";
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  HashMap Internals Deep Dive");
        System.out.println("========================================\n");

        // --- 1. Default capacity and load factor ---
        System.out.println("=== 1. Default Parameters ===");
        System.out.println("Default initial capacity: 16 (1 << 4)");
        System.out.println("Default load factor: 0.75");
        System.out.println("Resize threshold: capacity * loadFactor = 16 * 0.75 = 12");
        System.out.println("Treeify threshold: 8 (chain length to convert to tree)");
        System.out.println("Untreeify threshold: 6 (tree size to convert back to list)");
        System.out.println("Min treeify capacity: 64\n");

        // --- 2. Hashing demonstration ---
        System.out.println("=== 2. Hash Computation ===");
        String key = "Hello";
        int h = key.hashCode();
        int spread = h ^ (h >>> 16);
        int capacity = 16;
        int bucketIndex = spread & (capacity - 1);
        System.out.println("Key: \"" + key + "\"");
        System.out.println("hashCode():          " + h + " (binary: " + Integer.toBinaryString(h) + ")");
        System.out.println("hashCode() >>> 16:   " + (h >>> 16));
        System.out.println("Spread (h ^ h>>>16): " + spread);
        System.out.println("Bucket index (spread & 15): " + bucketIndex + "\n");

        // --- 3. Put flow step by step ---
        System.out.println("=== 3. Put Flow Simulation ===");
        HashMap<Key, String> map = new HashMap<>(16, 0.75f);

        // These keys will collide (same hashCode % 4)
        Key k1 = new Key(1, "Alice");
        Key k2 = new Key(5, "Bob");     // hashCode = 5 % 4 = 1 (same as k1)
        Key k3 = new Key(9, "Charlie"); // hashCode = 9 % 4 = 1 (same bucket!)
        Key k4 = new Key(2, "Dave");    // hashCode = 2 % 4 = 2 (different bucket)

        System.out.println("Inserting k1 (hash=" + k1.hashCode() + "): " + k1);
        map.put(k1, "Value1");
        System.out.println("  -> Bucket " + (k1.hashCode() & 15) + ": [k1]");

        System.out.println("Inserting k2 (hash=" + k2.hashCode() + "): " + k2);
        map.put(k2, "Value2");
        System.out.println("  -> Bucket " + (k2.hashCode() & 15) + ": [k1 -> k2] (COLLISION! Same bucket, linked list)");

        System.out.println("Inserting k3 (hash=" + k3.hashCode() + "): " + k3);
        map.put(k3, "Value3");
        System.out.println("  -> Bucket " + (k3.hashCode() & 15) + ": [k1 -> k2 -> k3] (Another collision!)");

        System.out.println("Inserting k4 (hash=" + k4.hashCode() + "): " + k4);
        map.put(k4, "Value4");
        System.out.println("  -> Bucket " + (k4.hashCode() & 15) + ": [k4] (No collision, different bucket)\n");

        // --- 4. Get flow ---
        System.out.println("=== 4. Get Flow ===");
        System.out.println("Getting k3 (hash=" + k3.hashCode() + "):");
        System.out.println("  1. Compute hash -> bucket " + (k3.hashCode() & 15));
        System.out.println("  2. Check first node (k1): hash match? Yes. equals()? No.");
        System.out.println("  3. Check next node (k2): hash match? Yes. equals()? No.");
        System.out.println("  4. Check next node (k3): hash match? Yes. equals()? Yes! Return value.");
        System.out.println("  Result: " + map.get(k3) + "\n");

        // --- 5. Key overwrite ---
        System.out.println("=== 5. Key Overwrite ===");
        System.out.println("Before: map.get(k1) = " + map.get(k1));
        map.put(k1, "UpdatedValue1");
        System.out.println("After put(k1, 'UpdatedValue1'): " + map.get(k1));
        System.out.println("Size unchanged: " + map.size() + " (key existed, value replaced)\n");

        // --- 6. Resize demonstration ---
        System.out.println("=== 6. Resize Behavior ===");
        HashMap<Integer, String> resizeMap = new HashMap<>(4, 0.75f);
        System.out.println("Initial capacity: 4, threshold: 4 * 0.75 = 3");
        for (int i = 0; i < 5; i++) {
            resizeMap.put(i, "val" + i);
            System.out.println("  After put(" + i + "): size=" + resizeMap.size());
            if (i == 2) {
                System.out.println("    ** Next put will trigger RESIZE: capacity 4 -> 8 **");
            }
        }
        System.out.println();

        // --- 7. Null key handling ---
        System.out.println("=== 7. Null Key Handling ===");
        HashMap<String, String> nullMap = new HashMap<>();
        nullMap.put(null, "null-value");
        nullMap.put("key", null);
        System.out.println("null key -> \"" + nullMap.get(null) + "\"");
        System.out.println("\"key\" -> null value: " + nullMap.get("key"));
        System.out.println("Null key always goes to bucket 0\n");

        // --- 8. Important methods summary ---
        System.out.println("=== 8. Key Methods & Time Complexity ===");
        System.out.println("put(K, V):        O(1) avg, O(log n) worst (tree), O(n) worst (list, pre-Java 8)");
        System.out.println("get(K):           O(1) avg, O(log n) worst (tree)");
        System.out.println("remove(K):        O(1) avg, O(log n) worst (tree)");
        System.out.println("containsKey(K):   O(1) avg");
        System.out.println("containsValue(V): O(n) — must scan all entries");
        System.out.println();

        // --- 9. Java 8+ useful methods ---
        System.out.println("=== 9. Java 8+ HashMap Methods ===");
        HashMap<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 90);

        // getOrDefault
        int bobScore = scores.getOrDefault("Bob", 0);
        System.out.println("getOrDefault('Bob', 0): " + bobScore);

        // putIfAbsent — only puts if key is not already present
        scores.putIfAbsent("Alice", 100);  // won't overwrite
        scores.putIfAbsent("Bob", 85);     // will insert
        System.out.println("putIfAbsent: " + scores);

        // compute — compute new value based on key and current value
        scores.compute("Alice", (k1_, v) -> v + 10);
        System.out.println("compute Alice +10: " + scores);

        // merge — merge with existing value
        scores.merge("Bob", 15, Integer::sum);  // Bob = 85 + 15 = 100
        System.out.println("merge Bob +15: " + scores);

        // forEach
        System.out.print("forEach: ");
        scores.forEach((k1_, v) -> System.out.print(k1_ + "=" + v + " "));
        System.out.println();
    }
}
