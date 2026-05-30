package interview.level7_java25_26.value_types;

/**
 * Q6. What is the difference between identity objects and value objects?
 *
 * Identity Objects (current Java):
 *   - Each object has a unique identity (memory address)
 *   - Two objects with same data are still DIFFERENT: new Integer(42) != new Integer(42)
 *   - Support: synchronization, System.identityHashCode(), == checks identity
 *   - Cost: object header (16 bytes), heap allocation, GC pressure, indirection
 *
 * Value Objects (Project Valhalla):
 *   - NO identity — defined purely by their data
 *   - Two value objects with same data ARE equal (like primitives)
 *   - Cannot: synchronize, use identity hashcode, meaningful ==
 *   - Benefit: can be stack-allocated, flattened into arrays, no header overhead
 *
 * Think of it as:
 *   Identity object = a person (unique even if same name/age)
 *   Value object    = a number (42 == 42, no "which 42?")
 *
 * Valhalla's goal:
 *   "Codes like a class, works like an int"
 *   value class Point { int x, y; }  → no identity, flat layout, no GC
 */
public class IdentityVsValueObjects {

    public static void main(String[] args) {

        // === Identity objects: same data, different identity ===
        System.out.println("=== Identity Objects (current Java) ===");
        Integer a = new Integer(42);
        Integer b = new Integer(42);

        System.out.println("a.equals(b): " + a.equals(b));    // true (same value)
        System.out.println("a == b:      " + (a == b));        // false (different identity!)
        System.out.println("a has identity: unique memory address, object header, synchronizable");

        // Each object has overhead: 12-16 bytes header + alignment
        // Array of 1M Integer objects: 1M * ~20 bytes = ~20MB
        // Array of 1M int primitives: 1M * 4 bytes = ~4MB

        // === Value semantics: what Valhalla will enable ===
        System.out.println("\n=== Value Objects (Project Valhalla — future) ===");
        System.out.println("// value class Point { int x, y; }");
        System.out.println("// Point p1 = new Point(1, 2);");
        System.out.println("// Point p2 = new Point(1, 2);");
        System.out.println("// p1 == p2 → true! (same data = same value)");
        System.out.println("// No object header, no identity, can be flattened");

        // === Records are close but still identity objects ===
        System.out.println("\n=== Records: value semantics, but still identity objects ===");
        record Point(int x, int y) {}
        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);
        System.out.println("p1.equals(p2): " + p1.equals(p2));  // true (value equality)
        System.out.println("p1 == p2:      " + (p1 == p2));      // false (still identity objects!)
        System.out.println("Records have value SEMANTICS but not value PERFORMANCE");

        // === Why identity is expensive ===
        System.out.println("\n=== Cost of Identity ===");
        System.out.println("Object header:    12-16 bytes per object (mark word + class pointer)");
        System.out.println("Heap allocation:  GC must track every object");
        System.out.println("Indirection:      array of Integer = array of POINTERS to objects");
        System.out.println("Cache misses:     objects scattered in heap → poor locality");

        System.out.println("\n=== Value Object Benefits (Valhalla) ===");
        System.out.println("No header:        just the data, no identity overhead");
        System.out.println("Stack allocation: can live on stack (no GC)");
        System.out.println("Flat arrays:      Point[] = [x,y,x,y,x,y,...] contiguous in memory");
        System.out.println("Cache friendly:   data is inline, no pointer chasing");

        // === What you can't do with value objects ===
        System.out.println("\n=== Value Object Restrictions ===");
        System.out.println("❌ synchronized(valueObj) — no identity to lock on");
        System.out.println("❌ System.identityHashCode() — no identity");
        System.out.println("❌ == for identity check — only value equality");
        System.out.println("❌ null (in some proposals) — value types can't be null");
    }
}
