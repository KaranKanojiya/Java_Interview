package interview.level1_core.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Q21. What is the difference between ArrayList and LinkedList?
 *
 * | Operation        | ArrayList          | LinkedList          |
 * |-----------------|-------------------|---------------------|
 * | get(index)      | O(1) — direct     | O(n) — traverse     |
 * | add(end)        | O(1) amortized    | O(1)                |
 * | add(middle)     | O(n) — shift      | O(1)* after finding |
 * | remove(index)   | O(n) — shift      | O(1)* after finding |
 * | Memory          | Compact (array)   | More (node + pointers) |
 * | Cache           | Cache-friendly    | Cache-unfriendly    |
 * | Implements      | List, RandomAccess| List, Deque, Queue  |
 *
 * * LinkedList add/remove is O(1) at the node, but finding the node is O(n)
 *
 * In practice: ArrayList wins almost always due to CPU cache locality.
 * Use LinkedList only when you need it as a Queue/Deque.
 */
public class ArrayListVsLinkedList {

    public static void main(String[] args) {

        int size = 100_000;

        // === Random access: ArrayList wins ===
        System.out.println("=== Random Access (get by index) ===");
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) { arrayList.add(i); linkedList.add(i); }

        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) arrayList.get(size / 2);
        long alTime = (System.nanoTime() - start) / 1_000_000;

        start = System.nanoTime();
        for (int i = 0; i < 10000; i++) linkedList.get(size / 2);
        long llTime = (System.nanoTime() - start) / 1_000_000;

        System.out.println("ArrayList get:  " + alTime + "ms");
        System.out.println("LinkedList get: " + llTime + "ms");

        // === Insert at beginning: LinkedList wins ===
        System.out.println("\n=== Insert at beginning ===");
        List<Integer> al = new ArrayList<>();
        List<Integer> ll = new LinkedList<>();

        start = System.nanoTime();
        for (int i = 0; i < 50_000; i++) al.add(0, i);  // O(n) shift each time
        alTime = (System.nanoTime() - start) / 1_000_000;

        start = System.nanoTime();
        for (int i = 0; i < 50_000; i++) ll.add(0, i);  // O(1) — prepend
        llTime = (System.nanoTime() - start) / 1_000_000;

        System.out.println("ArrayList add(0): " + alTime + "ms");
        System.out.println("LinkedList add(0): " + llTime + "ms");

        // === Add at end: Similar performance ===
        System.out.println("\n=== Add at end ===");
        al.clear(); ll.clear();

        start = System.nanoTime();
        for (int i = 0; i < size; i++) al.add(i);
        alTime = (System.nanoTime() - start) / 1_000_000;

        start = System.nanoTime();
        for (int i = 0; i < size; i++) ll.add(i);
        llTime = (System.nanoTime() - start) / 1_000_000;

        System.out.println("ArrayList add(end):  " + alTime + "ms");
        System.out.println("LinkedList add(end): " + llTime + "ms");

        // === Memory overhead ===
        System.out.println("\n=== Memory ===");
        System.out.println("ArrayList:  contiguous array, ~4 bytes/element (int)");
        System.out.println("LinkedList: each node = object header + value + prev + next pointers");
        System.out.println("            ~40+ bytes per node vs ~4 bytes in ArrayList");

        // === Summary ===
        System.out.println("\n=== When to use ===");
        System.out.println("ArrayList:  DEFAULT choice. Random access, iteration, add at end");
        System.out.println("LinkedList: Queue/Deque operations (addFirst, removeFirst)");
        System.out.println("           Better: use ArrayDeque for queue (faster than LinkedList)");
    }
}
