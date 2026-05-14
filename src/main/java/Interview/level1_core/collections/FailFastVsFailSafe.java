package interview.level1_core.collections;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: What is a fail-fast iterator?
// A: An iterator that throws ConcurrentModificationException if the collection is
//    structurally modified (add/remove) during iteration. It uses a modCount field:
//    if modCount changes between iterations, CME is thrown. ArrayList, HashMap, HashSet
//    all have fail-fast iterators.
//
// Q: What is a fail-safe (weakly consistent) iterator?
// A: An iterator that does NOT throw ConcurrentModificationException. It works on a
//    copy/snapshot of the data (CopyOnWriteArrayList) or tolerates concurrent modifications
//    (ConcurrentHashMap). Safe for concurrent access but may not reflect latest changes.
//
// Q: How does CopyOnWriteArrayList work?
// A: Every write (add/set/remove) creates a new copy of the internal array. Reads are
//    lock-free and operate on the current snapshot. Iterators work on the snapshot at
//    the time of iterator creation.
//    Use when: reads >>> writes (e.g., listener lists, configuration).
//
// Q: Can you modify a collection during iteration with Iterator.remove()?
// A: Yes! Iterator.remove() is the ONLY safe way to remove elements during iteration
//    with fail-fast iterators. It updates modCount internally, preventing CME.
//
// Q: What is the difference between ConcurrentHashMap.keySet() and HashMap.keySet()?
// A: HashMap.keySet() returns a fail-fast view — modifying the map during iteration
//    throws CME. ConcurrentHashMap.keySet() returns a weakly consistent view — no CME,
//    but may or may not reflect concurrent modifications.
// ========================================================

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FailFastVsFailSafe {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Fail-Fast vs Fail-Safe Iterators");
        System.out.println("========================================\n");

        // --- 1. Fail-Fast: ArrayList ---
        System.out.println("=== 1. Fail-Fast: ArrayList (ConcurrentModificationException) ===");
        List<String> arrayList = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        try {
            for (String item : arrayList) {
                System.out.println("  Reading: " + item);
                if ("B".equals(item)) {
                    arrayList.remove(item);  // structural modification during iteration!
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("  CAUGHT: ConcurrentModificationException!");
            System.out.println("  Reason: modCount changed during iteration.\n");
        }

        // --- 2. Fail-Fast: HashMap ---
        System.out.println("=== 2. Fail-Fast: HashMap ===");
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("one", 1);
        hashMap.put("two", 2);
        hashMap.put("three", 3);
        try {
            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                System.out.println("  Reading: " + entry);
                if ("two".equals(entry.getKey())) {
                    hashMap.put("four", 4);  // structural modification!
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("  CAUGHT: ConcurrentModificationException!\n");
        }

        // --- 3. Safe removal using Iterator.remove() ---
        System.out.println("=== 3. Safe Removal: Iterator.remove() ===");
        List<String> safeList = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E"));
        System.out.println("  Before: " + safeList);

        Iterator<String> it = safeList.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if ("B".equals(item) || "D".equals(item)) {
                it.remove();  // safe — updates modCount internally
                System.out.println("  Removed: " + item);
            }
        }
        System.out.println("  After:  " + safeList);
        System.out.println();

        // --- 4. Java 8 removeIf (also safe) ---
        System.out.println("=== 4. Java 8: removeIf() (Also Safe) ===");
        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        System.out.println("  Before: " + numbers);
        numbers.removeIf(n -> n % 2 == 0);  // remove even numbers — no CME
        System.out.println("  After removeIf(even): " + numbers);
        System.out.println();

        // --- 5. Fail-Safe: CopyOnWriteArrayList ---
        System.out.println("=== 5. Fail-Safe: CopyOnWriteArrayList ===");
        CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(
                Arrays.asList("X", "Y", "Z")
        );
        System.out.println("  Initial: " + cowList);

        // Iterate while modifying — NO ConcurrentModificationException!
        for (String item : cowList) {
            System.out.println("  Reading: " + item);
            if ("Y".equals(item)) {
                cowList.add("W");      // modifies a NEW copy of the array
                cowList.remove("Z");   // modifies a NEW copy
            }
        }
        System.out.println("  After iteration: " + cowList);
        System.out.println("  Note: Iterator saw the ORIGINAL snapshot (X, Y, Z)");
        System.out.println("        Modifications happened on a new copy.\n");

        // --- 6. CopyOnWriteArrayList iterator doesn't support remove() ---
        System.out.println("=== 6. CopyOnWriteArrayList Iterator Limitations ===");
        try {
            Iterator<String> cowIt = cowList.iterator();
            cowIt.next();
            cowIt.remove();  // throws UnsupportedOperationException!
        } catch (UnsupportedOperationException e) {
            System.out.println("  Iterator.remove() -> UnsupportedOperationException!");
            System.out.println("  CopyOnWrite iterators work on a snapshot — can't modify via iterator.\n");
        }

        // --- 7. Fail-Safe: ConcurrentHashMap ---
        System.out.println("=== 7. Fail-Safe: ConcurrentHashMap ===");
        ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
        concMap.put("a", 1);
        concMap.put("b", 2);
        concMap.put("c", 3);

        System.out.println("  Iterating and modifying simultaneously:");
        for (Map.Entry<String, Integer> entry : concMap.entrySet()) {
            System.out.println("  Reading: " + entry);
            if ("b".equals(entry.getKey())) {
                concMap.put("d", 4);  // safe — no CME
                System.out.println("  Added 'd' during iteration (no CME!)");
            }
        }
        System.out.println("  Final map: " + concMap);
        System.out.println("  Note: 'd' may or may not appear in the same iteration (weakly consistent).\n");

        // --- 8. Comparison table ---
        System.out.println("=== 8. Comparison Summary ===");
        System.out.println("Collection              | Iterator Type    | CME?  | Thread-Safe?");
        System.out.println("------------------------|-----------------|-------|-------------");
        System.out.println("ArrayList               | Fail-Fast       | Yes   | No");
        System.out.println("HashMap                 | Fail-Fast       | Yes   | No");
        System.out.println("HashSet                 | Fail-Fast       | Yes   | No");
        System.out.println("LinkedList              | Fail-Fast       | Yes   | No");
        System.out.println("CopyOnWriteArrayList    | Fail-Safe       | No    | Yes");
        System.out.println("ConcurrentHashMap       | Weakly Consist. | No    | Yes");
        System.out.println("ConcurrentSkipListMap   | Weakly Consist. | No    | Yes");
        System.out.println();

        // --- 9. When to use which ---
        System.out.println("=== 9. Decision Guide ===");
        System.out.println("Use ArrayList + Iterator.remove(): Single-threaded, need to remove during iteration.");
        System.out.println("Use removeIf():                    Single-threaded, bulk removal with predicate.");
        System.out.println("Use CopyOnWriteArrayList:          Multi-threaded, reads >> writes (e.g., listeners).");
        System.out.println("Use ConcurrentHashMap:             Multi-threaded map with fine-grained locking.");
        System.out.println("Use Collections.synchronizedList:  Legacy — wraps with single mutex (prefer COWAL).");
    }
}
