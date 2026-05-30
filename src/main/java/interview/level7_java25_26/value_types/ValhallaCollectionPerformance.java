package interview.level7_java25_26.value_types;

import java.util.ArrayList;
import java.util.List;

/**
 * Q7. How will Project Valhalla improve collection performance?
 *
 * Current problem: "Everything is an Object" penalty
 *
 * List<Integer> today:
 *   → ArrayList stores Object[] (array of REFERENCES)
 *   → Each Integer: 16 bytes header + 4 bytes int = 20 bytes (with alignment ~24)
 *   → Array: pointer → Integer object → int value (two hops)
 *   → 1M integers: ~24MB + references
 *
 * List<int> with Valhalla (future):
 *   → Specialized ArrayList stores int[] directly (FLAT)
 *   → Each int: 4 bytes (no header, no object, no pointer)
 *   → Array: directly contains int values (one hop)
 *   → 1M integers: ~4MB
 *
 * Performance improvements:
 *   1. Memory: 4-6x reduction (no headers, no pointers)
 *   2. Speed: better cache locality (contiguous data, no pointer chasing)
 *   3. GC: fewer objects to track (or none if stack-allocated)
 *   4. Autoboxing: eliminated (List<int> not List<Integer>)
 *
 * Also: value class Complex { double re, im; }
 *   Current Complex[]: array of pointers → scattered objects
 *   Valhalla Complex[]: [re,im,re,im,re,im,...] flat in memory
 */
public class ValhallaCollectionPerformance {

    public static void main(String[] args) {

        // === Current: Integer autoboxing overhead ===
        System.out.println("=== Current: List<Integer> autoboxing overhead ===");

        // Measure boxing overhead
        int iterations = 5_000_000;

        long start = System.nanoTime();
        int[] primitiveArray = new int[iterations];
        for (int i = 0; i < iterations; i++) primitiveArray[i] = i;
        long primitiveSum = 0;
        for (int v : primitiveArray) primitiveSum += v;
        long primitiveTime = System.nanoTime() - start;

        start = System.nanoTime();
        List<Integer> boxedList = new ArrayList<>(iterations);
        for (int i = 0; i < iterations; i++) boxedList.add(i);  // autoboxing!
        long boxedSum = 0;
        for (int v : boxedList) boxedSum += v;  // auto-unboxing!
        long boxedTime = System.nanoTime() - start;

        System.out.println("int[]:         " + (primitiveTime / 1_000_000) + "ms, sum=" + primitiveSum);
        System.out.println("List<Integer>: " + (boxedTime / 1_000_000) + "ms, sum=" + boxedSum);
        System.out.println("Ratio: ~" + (boxedTime / Math.max(primitiveTime, 1)) + "x slower with boxing\n");

        // === Memory comparison ===
        System.out.println("=== Memory Layout Comparison ===");
        System.out.println("int[1_000_000]:");
        System.out.println("  4 bytes × 1M = ~4 MB (contiguous)");
        System.out.println("\nInteger[1_000_000] (ArrayList<Integer>):");
        System.out.println("  Reference: 4-8 bytes × 1M = ~4-8 MB");
        System.out.println("  Object:    ~16 bytes × 1M = ~16 MB (header + int + padding)");
        System.out.println("  Total:     ~20-24 MB (5-6x more!) + GC overhead");

        // === Object overhead demo ===
        System.out.println("\n=== Object header overhead ===");
        System.out.println("Every Java object has a header:");
        System.out.println("  Mark word:     8 bytes (GC age, lock state, hashcode)");
        System.out.println("  Class pointer: 4 bytes (compressed) or 8 bytes");
        System.out.println("  Alignment:     padded to 8-byte boundary");
        System.out.println("  → Minimum object size: 16 bytes (even for empty object!)");
        System.out.println("  → Integer = 16 bytes header + 4 bytes int = 20 → aligned to 24 bytes");

        // === What Valhalla enables ===
        System.out.println("\n=== What Valhalla Will Enable ===");
        System.out.println("1. List<int> (primitive generics):");
        System.out.println("   → No autoboxing, direct int[] backing");
        System.out.println("   → 4 bytes per element, not 24");
        System.out.println();
        System.out.println("2. value class Point { int x, y; }");
        System.out.println("   → Point[] = [x,y,x,y,...] flat in memory");
        System.out.println("   → 8 bytes per Point, not ~32 (with headers)");
        System.out.println();
        System.out.println("3. HashMap<int, Point> (specialized):");
        System.out.println("   → No Entry objects needed");
        System.out.println("   → Keys and values inline in buckets");
        System.out.println();
        System.out.println("4. Optional<int>:");
        System.out.println("   → No boxing to Optional<Integer>");
        System.out.println("   → Can be stack-allocated");
    }
}
