package interview.level1_core.collections;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: How does TreeMap work internally?
// A: TreeMap uses a Red-Black tree (self-balancing BST). All operations (get, put, remove)
//    are O(log n). Keys must be Comparable or a Comparator must be provided.
//    Entries are always sorted by key. It implements NavigableMap interface.
//
// Q: When would you use TreeMap over HashMap?
// A: When you need sorted key order, range queries (subMap, headMap, tailMap),
//    floor/ceiling/higher/lower operations, or NavigableMap functionality.
//    HashMap is O(1) avg; TreeMap is O(log n) — use TreeMap only when ordering matters.
//
// Q: What is the difference between floorKey() and lowerKey()?
// A: floorKey(k) returns the greatest key <= k (inclusive).
//    lowerKey(k) returns the greatest key < k (exclusive).
//    Similarly: ceilingKey(k) returns smallest key >= k; higherKey(k) returns smallest key > k.
//
// Q: How does PriorityQueue work internally?
// A: PriorityQueue uses a binary min-heap (array-based). The head is always the smallest
//    element (or largest, if a reverse Comparator is used).
//    - offer/add: O(log n) — adds to end, then sifts up.
//    - poll/remove: O(log n) — removes head, then sifts down.
//    - peek: O(1) — just returns the root.
//    - contains/remove(Object): O(n) — must scan the array.
//    Note: Iterator does NOT return elements in sorted order!
//
// Q: Is PriorityQueue thread-safe?
// A: No. Use PriorityBlockingQueue for thread-safe priority queues.
//
// Q: TreeMap vs TreeSet?
// A: TreeSet is backed by a TreeMap internally. TreeSet stores only keys (values are a
//    dummy constant). Both use Red-Black tree, both are sorted.
// ========================================================

import java.util.*;

public class TreeMapAndPriorityQueue {

    // Simple task class for PriorityQueue demo
    static class Task implements Comparable<Task> {
        String name;
        int priority;  // lower number = higher priority

        Task(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public int compareTo(Task other) {
            return Integer.compare(this.priority, other.priority);
        }

        @Override
        public String toString() {
            return name + "(p=" + priority + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  TreeMap & PriorityQueue Deep Dive");
        System.out.println("========================================\n");

        // ==================== TREEMAP ====================
        System.out.println("=== 1. TreeMap Basics (Sorted by Key) ===");
        TreeMap<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("Charlie", 3);
        treeMap.put("Alice", 1);
        treeMap.put("Eve", 5);
        treeMap.put("Bob", 2);
        treeMap.put("Dave", 4);
        System.out.println("TreeMap (auto-sorted): " + treeMap);
        System.out.println("First key: " + treeMap.firstKey());
        System.out.println("Last key: " + treeMap.lastKey());
        System.out.println();

        // --- 2. NavigableMap methods ---
        System.out.println("=== 2. NavigableMap Methods ===");
        TreeMap<Integer, String> navMap = new TreeMap<>();
        navMap.put(10, "ten");
        navMap.put(20, "twenty");
        navMap.put(30, "thirty");
        navMap.put(40, "forty");
        navMap.put(50, "fifty");
        System.out.println("Map: " + navMap);

        // floor/ceiling (inclusive)
        System.out.println("floorKey(25):   " + navMap.floorKey(25));    // 20 (greatest key <= 25)
        System.out.println("floorKey(30):   " + navMap.floorKey(30));    // 30 (inclusive)
        System.out.println("ceilingKey(25): " + navMap.ceilingKey(25));  // 30 (smallest key >= 25)
        System.out.println("ceilingKey(30): " + navMap.ceilingKey(30));  // 30 (inclusive)

        // lower/higher (exclusive)
        System.out.println("lowerKey(30):   " + navMap.lowerKey(30));    // 20 (greatest key < 30)
        System.out.println("higherKey(30):  " + navMap.higherKey(30));   // 40 (smallest key > 30)

        // First/Last entries
        System.out.println("firstEntry():   " + navMap.firstEntry());
        System.out.println("lastEntry():    " + navMap.lastEntry());

        // Poll (remove and return) first/last
        System.out.println("pollFirstEntry(): " + navMap.pollFirstEntry());
        System.out.println("After poll: " + navMap);
        navMap.put(10, "ten");  // put it back
        System.out.println();

        // --- 3. SubMap, HeadMap, TailMap ---
        System.out.println("=== 3. Range Views ===");
        navMap.put(10, "ten");
        System.out.println("Full map: " + navMap);
        System.out.println("subMap(20, 40):     " + navMap.subMap(20, 40));       // [20, 40) exclusive end
        System.out.println("subMap(20, true, 40, true): " + navMap.subMap(20, true, 40, true));  // [20, 40] inclusive
        System.out.println("headMap(30):        " + navMap.headMap(30));           // keys < 30
        System.out.println("headMap(30, true):  " + navMap.headMap(30, true));     // keys <= 30
        System.out.println("tailMap(30):        " + navMap.tailMap(30));           // keys >= 30
        System.out.println("tailMap(30, false): " + navMap.tailMap(30, false));    // keys > 30
        System.out.println();

        // --- 4. Descending order ---
        System.out.println("=== 4. Descending Order ===");
        System.out.println("descendingMap(): " + navMap.descendingMap());
        System.out.println("descendingKeySet(): " + navMap.descendingKeySet());
        System.out.println();

        // --- 5. Custom Comparator TreeMap ---
        System.out.println("=== 5. Custom Comparator (Reverse Order) ===");
        TreeMap<String, Integer> reverseMap = new TreeMap<>(Comparator.reverseOrder());
        reverseMap.put("A", 1);
        reverseMap.put("C", 3);
        reverseMap.put("B", 2);
        System.out.println("Reverse order: " + reverseMap);
        System.out.println();

        // ==================== PRIORITYQUEUE ====================
        System.out.println("=== 6. PriorityQueue (Min-Heap) ===");
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.addAll(Arrays.asList(30, 10, 50, 20, 40));
        System.out.println("Added: [30, 10, 50, 20, 40]");
        System.out.println("peek() (min element): " + minHeap.peek());

        System.out.print("Poll order (sorted): ");
        while (!minHeap.isEmpty()) {
            System.out.print(minHeap.poll() + " ");
        }
        System.out.println("\n");

        // --- 7. Max-Heap ---
        System.out.println("=== 7. PriorityQueue (Max-Heap) ===");
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.addAll(Arrays.asList(30, 10, 50, 20, 40));
        System.out.print("Poll order (descending): ");
        while (!maxHeap.isEmpty()) {
            System.out.print(maxHeap.poll() + " ");
        }
        System.out.println("\n");

        // --- 8. Custom Comparator PriorityQueue ---
        System.out.println("=== 8. PriorityQueue with Custom Objects ===");
        PriorityQueue<Task> taskQueue = new PriorityQueue<>();
        taskQueue.add(new Task("Low priority task", 3));
        taskQueue.add(new Task("Critical task", 1));
        taskQueue.add(new Task("Medium task", 2));

        System.out.println("Processing tasks by priority:");
        while (!taskQueue.isEmpty()) {
            System.out.println("  Processing: " + taskQueue.poll());
        }
        System.out.println();

        // --- 9. PriorityQueue with lambda Comparator ---
        System.out.println("=== 9. PriorityQueue with Lambda Comparator ===");
        // Sort by string length, then alphabetically
        PriorityQueue<String> pq = new PriorityQueue<>(
                Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())
        );
        pq.addAll(Arrays.asList("banana", "fig", "cherry", "date", "apple"));
        System.out.print("By length then alpha: ");
        while (!pq.isEmpty()) {
            System.out.print(pq.poll() + " ");
        }
        System.out.println("\n");

        // --- 10. When to use which ---
        System.out.println("=== 10. Decision Guide ===");
        System.out.println("Use HashMap when:        You need O(1) key-value lookup, no ordering needed.");
        System.out.println("Use TreeMap when:         You need sorted keys, range queries, floor/ceiling.");
        System.out.println("Use LinkedHashMap when:   You need insertion-order (or access-order) iteration.");
        System.out.println("Use PriorityQueue when:   You need to process elements by priority (min/max first).");
        System.out.println("Use TreeSet when:         You need a sorted set with O(log n) operations.");
    }
}
