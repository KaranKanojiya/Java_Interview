package interview.level7_java25_26.value_types;

/**
 * ============================================================================
 * VALUE TYPES / VALUE CLASSES — Project Valhalla (JEP 401)
 * Level: 7 — Java 25/26 Awareness
 * Status: PREVIEW in Java 25 (JEP 401: Value Classes and Objects)
 * ============================================================================
 *
 * THE PROBLEM: Identity Tax
 * ─────────────────────────
 * Every Java object today has IDENTITY — a unique memory address that
 * distinguishes it from all other objects, even if they have the same data.
 *
 *   Integer a = Integer.valueOf(1000);
 *   Integer b = Integer.valueOf(1000);
 *   a == b → false (different identity, even though same value!)
 *
 * Identity enables: synchronization, ==, System.identityHashCode(), weak refs
 * But identity has a COST:
 *   - Object header: 12-16 bytes overhead per object (mark word + class pointer)
 *   - Heap allocation: even tiny objects must be heap-allocated
 *   - Indirection: arrays of objects are arrays of POINTERS, not flat data
 *   - GC pressure: more objects = more work for garbage collector
 *   - Cache misses: pointer chasing destroys CPU cache locality
 *
 * For many classes (Point, Complex, Money, Optional, LocalDate), identity is
 * USELESS. You never synchronize on a Point. You never ask "is this the
 * SAME Point object?" — you ask "does it have the same x,y?"
 *
 * THE SOLUTION: Value Classes
 * ───────────────────────────
 * "Codes like a class, works like an int."
 *
 * A value class is:
 *   - Identity-free: no object header, no synchronization, == compares fields
 *   - Flat: can be embedded directly in arrays and other objects
 *   - Potentially stack-allocated: no heap allocation for small types
 *   - Immutable: all fields are implicitly final
 *
 * ============================================================================
 * EXPECTED SYNTAX (Java 25 Preview)
 * ============================================================================
 *
 *   value class Point {           // 'value' modifier on the class
 *       private int x;
 *       private int y;
 *
 *       public Point(int x, int y) {
 *           this.x = x;
 *           this.y = y;
 *       }
 *
 *       public int x() { return x; }
 *       public int y() { return y; }
 *
 *       public double distanceTo(Point other) {
 *           int dx = this.x - other.x;
 *           int dy = this.y - other.y;
 *           return Math.sqrt(dx * dx + dy * dy);
 *       }
 *   }
 *
 * Usage:
 *   Point p1 = new Point(1, 2);
 *   Point p2 = new Point(1, 2);
 *   p1 == p2  →  TRUE  (value equality, not identity!)
 *
 * ============================================================================
 * MEMORY LAYOUT COMPARISON
 * ============================================================================
 *
 * Traditional class Point:
 *   ┌──────────────┐
 *   │ Object Header│ 12-16 bytes (mark word + class pointer)
 *   │ int x        │ 4 bytes
 *   │ int y        │ 4 bytes
 *   │ padding      │ 0-4 bytes (alignment)
 *   └──────────────┘
 *   Total: ~24 bytes per Point + pointer (8 bytes) = ~32 bytes
 *
 * Point[] (traditional): array of POINTERS
 *   ┌─────────┐    ┌────────┐
 *   │ ptr  ───┼───→│ Header │  ← each element is a separate heap object
 *   │ ptr  ───┼───→│ Header │
 *   │ ptr  ───┼───→│ Header │
 *   └─────────┘    └────────┘
 *   Random memory locations → cache misses!
 *
 * Value class Point:
 *   ┌──────────────┐
 *   │ int x        │ 4 bytes   ← NO header!
 *   │ int y        │ 4 bytes
 *   └──────────────┘
 *   Total: 8 bytes per Point
 *
 * Point[] (value): FLAT array
 *   ┌───────────────────────────────┐
 *   │ x│y│ x│y│ x│y│ x│y│ x│y│   │  ← contiguous, cache-friendly!
 *   └───────────────────────────────┘
 *   75% memory reduction, excellent cache locality
 *
 * ============================================================================
 * RESTRICTIONS ON VALUE CLASSES
 * ============================================================================
 *
 * 1. Cannot synchronize: synchronized(point) { } → compile error
 * 2. No identity: == compares field values, not references
 * 3. Implicitly final: cannot be extended
 * 4. Fields are implicitly final: immutable after construction
 * 5. Cannot be null (for value objects): use Optional or default value
 * 6. No weak/soft/phantom references to value objects
 * 7. System.identityHashCode() returns value-based hashCode
 *
 * ============================================================================
 * WHAT ABOUT EXISTING CLASSES?
 * ============================================================================
 *
 * Several JDK classes are already "value-based" (annotated @ValueBased):
 *   - Integer, Long, Double, etc. (wrapper types)
 *   - Optional, OptionalInt, OptionalDouble
 *   - LocalDate, LocalTime, Instant, Duration
 *
 * With Valhalla, these could BECOME true value classes:
 *   - Integer would have NO identity → no object header → 4 bytes flat
 *   - Optional<Point> could be flat → no extra allocation
 *   - LocalDate could be stack-allocated → zero GC pressure
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "What are value classes in Project Valhalla?"
 * A: "Value classes are identity-free classes that 'code like a class, work
 *     like an int.' They have no object header, can be flattened in arrays,
 *     potentially stack-allocated, and use value equality (== compares
 *     fields). This eliminates the 'identity tax' — the overhead of object
 *     headers, heap allocation, indirection, and GC pressure for small
 *     immutable types like Point, Money, Complex, or Optional."
 *
 * Q: "Why do we need value classes when we have records?"
 * A: "Records give you syntactic convenience (auto-generated accessors,
 *     equals, hashCode, toString) but they are still identity objects with
 *     full object headers and heap allocation. Value classes give you
 *     PERFORMANCE — flat memory layout, no headers, potentially no
 *     allocation. A value record would give you BOTH."
 *
 * Q: "What is the 'identity tax'?"
 * A: "Every Java object carries a 12-16 byte header for identity (mark word
 *     for locking/GC + class pointer). For a class like Point(int x, int y),
 *     the 8 bytes of actual data requires 24+ bytes of storage — that's the
 *     identity tax. It also means arrays of objects are arrays of pointers,
 *     destroying cache locality."
 *
 * Q: "Can existing code break with value classes?"
 * A: "Code that synchronizes on value-based classes (like Integer) would
 *     break. Java has been warning about this since Java 16 via
 *     @ValueBased. Code using == on wrapper types would change behavior
 *     (but it was already wrong — you should use equals())."
 */
public class ValueTypesOverview {

    public static void main(String[] args) {
        System.out.println("=== Value Types / Project Valhalla (JEP 401) ===\n");

        demoIdentityTax();
        demoValueClassConcept();
        demoMemorySavings();
        demoMigrationPath();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. THE IDENTITY TAX — the problem Valhalla solves
    // ─────────────────────────────────────────────────────────────────────────
    static void demoIdentityTax() {
        System.out.println("── The Identity Tax ──\n");

        // Every object has identity — demonstrated with Integer
        Integer a = Integer.valueOf(1000);
        Integer b = Integer.valueOf(1000);
        System.out.println("Integer a = 1000, b = 1000");
        System.out.println("a == b:      " + (a == b) + "  ← different identity!");
        System.out.println("a.equals(b): " + a.equals(b) + " ← same value");

        // The cost of identity
        System.out.println("\nMemory overhead per object:");
        System.out.println("  Object header:   12-16 bytes (mark word + class pointer)");
        System.out.println("  int x, y fields: 8 bytes");
        System.out.println("  Padding:         ~4 bytes");
        System.out.println("  Total:           ~24-32 bytes for 8 bytes of data!");

        // Array of Integer vs int[]
        int N = 1_000_000;
        System.out.println("\nArray of " + N + " elements:");
        System.out.println("  int[]:     " + (N * 4 / 1024 / 1024) + " MB (flat, contiguous)");
        System.out.println("  Integer[]: ~" + (N * 32 / 1024 / 1024) + " MB (pointers + objects)");
        System.out.println("  That's 8x more memory + cache misses from pointer chasing");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. WHAT A VALUE CLASS LOOKS LIKE
    // ─────────────────────────────────────────────────────────────────────────
    static void demoValueClassConcept() {
        System.out.println("── Value Class Concept ──\n");

        /*
         * TRADITIONAL CLASS vs VALUE CLASS:
         *
         * // Traditional (identity object):
         * class Point {
         *     final int x, y;
         *     Point(int x, int y) { this.x = x; this.y = y; }
         * }
         * // → 24+ bytes, heap allocated, identity-based ==
         *
         * // Value class (identity-free):
         * value class Point {
         *     int x, y;  // implicitly final
         *     Point(int x, int y) { this.x = x; this.y = y; }
         * }
         * // → 8 bytes, potentially stack-allocated, value-based ==
         *
         * // Value record (best of both worlds):
         * value record Point(int x, int y) { }
         * // → 8 bytes, auto-generated accessors/equals/hashCode/toString
         */

        // Simulating with a regular record (current Java)
        record Point(int x, int y) {}

        Point p1 = new Point(3, 4);
        Point p2 = new Point(3, 4);

        System.out.println("record Point(3,4) — current behavior:");
        System.out.println("  p1 == p2:      " + (p1 == p2) + "  ← identity comparison");
        System.out.println("  p1.equals(p2): " + p1.equals(p2) + " ← value comparison");
        System.out.println();
        System.out.println("With 'value record Point(int x, int y)':");
        System.out.println("  p1 == p2:      would be TRUE (value semantics)");
        System.out.println("  No object header, flat in arrays, potentially stack-allocated");
        System.out.println();

        // Show what can and cannot be done
        System.out.println("Value class rules:");
        System.out.println("  [OK]  == compares fields (value equality)");
        System.out.println("  [OK]  Can be method parameters and return values");
        System.out.println("  [OK]  Can implement interfaces");
        System.out.println("  [OK]  Can have methods, constructors, static members");
        System.out.println("  [NO]  Cannot synchronize (no identity for monitor)");
        System.out.println("  [NO]  Cannot extend (implicitly final)");
        System.out.println("  [NO]  Cannot have mutable fields (implicitly final)");
        System.out.println("  [NO]  Cannot be target of weak/soft references");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. MEMORY SAVINGS — the performance motivation
    // ─────────────────────────────────────────────────────────────────────────
    static void demoMemorySavings() {
        System.out.println("── Memory Savings with Value Classes ──\n");

        System.out.println("Scenario: 10 million Point objects\n");

        long pointsCount = 10_000_000;

        // Traditional class
        long traditionalPerPoint = 24; // header(16) + x(4) + y(4)
        long traditionalArray = pointsCount * (traditionalPerPoint + 8); // + pointer
        System.out.printf("Traditional class Point:%n");
        System.out.printf("  Per point: %d bytes (header + fields)%n", traditionalPerPoint);
        System.out.printf("  Array overhead: 8 bytes per pointer%n");
        System.out.printf("  Total: %.0f MB%n%n", traditionalArray / 1_048_576.0);

        // Value class
        long valuePerPoint = 8; // just x(4) + y(4)
        long valueArray = pointsCount * valuePerPoint;
        System.out.printf("Value class Point:%n");
        System.out.printf("  Per point: %d bytes (no header, flat)%n", valuePerPoint);
        System.out.printf("  Array: flat contiguous memory%n");
        System.out.printf("  Total: %.0f MB%n%n", valueArray / 1_048_576.0);

        double savings = (1.0 - (double) valueArray / traditionalArray) * 100;
        System.out.printf("Memory savings: %.0f%%%n", savings);
        System.out.println("Plus: better cache locality, less GC pressure, potential stack allocation");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. MIGRATION PATH — from current classes to value classes
    // ─────────────────────────────────────────────────────────────────────────
    static void demoMigrationPath() {
        System.out.println("── Migration Path ──\n");

        System.out.println("JDK classes that will likely become value classes:");
        System.out.println("  Wrapper types:  Integer, Long, Double, Float, ...");
        System.out.println("  Time API:       LocalDate, LocalTime, Instant, Duration");
        System.out.println("  Optional:       Optional, OptionalInt, OptionalDouble");
        System.out.println();

        System.out.println("How to prepare your code TODAY:");
        System.out.println("  1. Do NOT synchronize on value-based classes");
        System.out.println("     ✗ synchronized(Integer.valueOf(42)) — will break!");
        System.out.println("  2. Do NOT use == on wrapper types");
        System.out.println("     ✗ if (a == b) — use .equals() instead");
        System.out.println("  3. Do NOT rely on identityHashCode for wrapper types");
        System.out.println("  4. DO design immutable classes with value semantics");
        System.out.println("  5. DO override equals/hashCode based on fields");
        System.out.println();

        System.out.println("Timeline:");
        System.out.println("  Java 10-15: @ValueBased annotation + warnings");
        System.out.println("  Java 16:    Warnings on synchronizing value-based classes");
        System.out.println("  Java 25:    JEP 401 preview — value class keyword");
        System.out.println("  Future:     JDK classes migrate to value classes");

        // Warning example
        System.out.println("\n── Warning you might see today ──");
        System.out.println("  javac warning: [synchronization] attempt to synchronize");
        System.out.println("  on an instance of a value-based class");
    }
}
