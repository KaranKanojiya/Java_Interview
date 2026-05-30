package interview.level1_core.collections;

import java.util.*;
import java.util.concurrent.*;

/**
 * Q20. What collections have you used in your project and why?
 *
 * This is a frequently asked behavioral + technical question.
 * Interviewers want to know you chose the RIGHT collection for the RIGHT use case.
 *
 * Collection decision tree:
 *   Need key-value?
 *     Yes → Need ordering?
 *       Yes → TreeMap (sorted) or LinkedHashMap (insertion order)
 *       No  → HashMap (fastest) or ConcurrentHashMap (thread-safe)
 *     No → Need duplicates?
 *       Yes → Need ordering?
 *         Yes → ArrayList (index) or LinkedList (frequent insert/delete)
 *         No  → ArrayList (default choice)
 *       No → Need ordering?
 *         Yes → TreeSet (sorted) or LinkedHashSet (insertion order)
 *         No  → HashSet (fastest)
 *   Need thread-safety?
 *     Yes → ConcurrentHashMap, CopyOnWriteArrayList, ConcurrentLinkedQueue, BlockingQueue
 */
public class CollectionsInProject {

    public static void main(String[] args) {

        // === ArrayList — most common, random access ===
        System.out.println("=== ArrayList (most used) ===");
        List<String> users = new ArrayList<>();  // O(1) get, O(1) amortized add
        users.add("Karan");
        users.add("John");
        System.out.println("Get by index: " + users.get(0));
        System.out.println("Use case: ordered list, frequent reads, infrequent middle inserts");

        // === HashMap — most common map ===
        System.out.println("\n=== HashMap ===");
        Map<String, Integer> cache = new HashMap<>();  // O(1) get/put
        cache.put("user:1", 42);
        cache.put("user:2", 99);
        System.out.println("Lookup: " + cache.get("user:1"));
        System.out.println("Use case: caching, lookups, counting (no ordering needed)");

        // === LinkedHashMap — insertion-ordered map ===
        System.out.println("\n=== LinkedHashMap (insertion order) ===");
        Map<String, String> lruCache = new LinkedHashMap<>(16, 0.75f, true);  // access-order
        lruCache.put("a", "1");
        lruCache.put("b", "2");
        lruCache.put("c", "3");
        lruCache.get("a");  // moves "a" to end (access order)
        System.out.println("Access-order: " + lruCache.keySet());  // b, c, a
        System.out.println("Use case: LRU cache, maintain insertion/access order");

        // === HashSet — unique elements ===
        System.out.println("\n=== HashSet (unique) ===");
        Set<String> visited = new HashSet<>();
        visited.add("page1");
        visited.add("page2");
        visited.add("page1");  // duplicate, ignored
        System.out.println("Visited: " + visited);
        System.out.println("Use case: deduplication, membership check");

        // === TreeMap — sorted keys ===
        System.out.println("\n=== TreeMap (sorted) ===");
        TreeMap<Integer, String> leaderboard = new TreeMap<>();  // O(log n)
        leaderboard.put(95, "Alice");
        leaderboard.put(87, "Bob");
        leaderboard.put(99, "Karan");
        System.out.println("Sorted: " + leaderboard);
        System.out.println("Top scorer: " + leaderboard.lastEntry());
        System.out.println("Use case: sorted data, range queries, leaderboards");

        // === ConcurrentHashMap — thread-safe map ===
        System.out.println("\n=== ConcurrentHashMap (thread-safe) ===");
        ConcurrentMap<String, Integer> metrics = new ConcurrentHashMap<>();
        metrics.put("requests", 0);
        metrics.compute("requests", (k, v) -> v + 1);  // atomic update
        System.out.println("Requests: " + metrics.get("requests"));
        System.out.println("Use case: shared counters, concurrent caches");

        // === Queue / Deque ===
        System.out.println("\n=== ArrayDeque (stack/queue) ===");
        Deque<String> taskQueue = new ArrayDeque<>();  // faster than LinkedList as queue
        taskQueue.offer("task1");
        taskQueue.offer("task2");
        System.out.println("Poll: " + taskQueue.poll());
        System.out.println("Use case: BFS, task processing, undo history (as stack)");

        // === PriorityQueue ===
        System.out.println("\n=== PriorityQueue (min-heap) ===");
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.offer(30);
        pq.offer(10);
        pq.offer(20);
        System.out.println("Min element: " + pq.poll());  // 10
        System.out.println("Use case: top-K, scheduling, Dijkstra's algorithm");

        // === Summary table ===
        System.out.println("\n=== Quick Reference ===");
        System.out.println("ArrayList:          ordered, index access, O(1) get");
        System.out.println("LinkedList:         frequent insert/delete at ends");
        System.out.println("HashMap:            fast lookup, no order");
        System.out.println("LinkedHashMap:      insertion/access order, LRU cache");
        System.out.println("TreeMap:            sorted keys, range queries");
        System.out.println("HashSet:            unique elements, O(1) contains");
        System.out.println("TreeSet:            sorted unique elements");
        System.out.println("ConcurrentHashMap:  thread-safe map");
        System.out.println("ArrayDeque:         stack/queue (faster than LinkedList)");
        System.out.println("PriorityQueue:      min/max heap");
    }
}
