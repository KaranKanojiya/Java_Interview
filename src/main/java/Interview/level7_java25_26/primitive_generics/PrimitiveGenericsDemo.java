package interview.level7_java25_26.primitive_generics;

import java.util.*;

/**
 * ============================================================================
 * PRIMITIVE GENERICS — Project Valhalla
 * Level: 7 — Java 25/26 Awareness
 * Status: FUTURE / INCUBATOR — not yet in any Java release as of Java 25
 * ============================================================================
 *
 * THE PROBLEM: Autoboxing Overhead
 * ─────────────────────────────────
 * Java generics only work with reference types. You CANNOT write:
 *   List<int>          ← compile error
 *   Map<int, double>   ← compile error
 *   Optional<int>      ← compile error
 *
 * Instead you must use wrapper types:
 *   List<Integer>      ← works, but each int is boxed into an Integer object
 *   Map<Integer, Double>
 *   Optional<Integer>  ← or use OptionalInt (specialized, but limited)
 *
 * The cost of autoboxing:
 *   - int:     4 bytes, stack-allocated, no GC
 *   - Integer: 16+ bytes (object header + value), heap-allocated, GC-managed
 *   - That's 4x memory overhead + allocation + GC pressure
 *
 * For collections of primitives, this is devastating:
 *   - int[1M]:         ~4 MB, contiguous, cache-friendly
 *   - List<Integer> with 1M elements: ~24 MB (pointers + objects), scattered
 *
 * TODAY'S WORKAROUNDS:
 *   1. Specialized types: IntStream, LongStream, DoubleStream
 *      But: only 3 primitive types, limited API, no Map/Set/List
 *   2. Third-party libs: Eclipse Collections, HPPC, Trove
 *      But: not standard, different APIs, dependency
 *   3. Arrays: int[], double[]
 *      But: fixed size, no Collection API, manual management
 *
 * ============================================================================
 * THE VALHALLA SOLUTION: Generic Specialization
 * ============================================================================
 *
 * With Valhalla, generics will be SPECIALIZED for primitive and value types:
 *
 *   List<int> intList = new ArrayList<>();     // No boxing!
 *   intList.add(42);                           // Stored as raw int
 *   int value = intList.get(0);                // No unboxing!
 *
 *   Map<int, double> scores = new HashMap<>(); // Flat keys and values
 *   scores.put(1, 99.5);
 *
 *   Optional<int> maybe = Optional.of(42);     // No Integer wrapper
 *
 * HOW IT WORKS (conceptually):
 *   - The JVM creates a specialized version of the generic class
 *   - List<int> gets a backing int[] instead of Object[]
 *   - No boxing, no indirection, flat memory layout
 *   - The type parameter is "reified" — the JVM knows it's int at runtime
 *
 * ============================================================================
 * COMPARISON TABLE
 * ============================================================================
 *
 *   Feature              | List<Integer> (today)  | List<int> (Valhalla)
 *   ─────────────────────┼────────────────────────┼──────────────────────
 *   Memory per element   | ~24 bytes              | 4 bytes
 *   Storage              | Pointer → heap object  | Flat in backing array
 *   Cache behavior       | Pointer chasing        | Sequential access
 *   GC pressure          | 1M objects to scan     | Zero extra objects
 *   Autoboxing           | Required               | None
 *   Null support         | Yes (can store null)   | No (primitives can't be null)
 *
 * ============================================================================
 * WHAT ABOUT TYPE ERASURE?
 * ============================================================================
 *
 * Today, Java uses TYPE ERASURE for generics:
 *   - List<String> and List<Integer> are the same class at runtime
 *   - Generic type info is erased to Object
 *   - This is why you can't do: new T(), instanceof List<String>, etc.
 *
 * For primitive generics to work, erasure must be partially relaxed:
 *   - List<int> CANNOT erase to List<Object> (int is not Object)
 *   - The JVM must know at runtime that this is a List specialized for int
 *   - This is called "reification" or "specialization"
 *
 * Valhalla's approach:
 *   - Reference generics: keep erasure (backward compatible)
 *   - Primitive/value generics: use specialization (new behavior)
 *   - Gradual migration, no breaking existing code
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "Why can't Java generics use primitives today?"
 * A: "Due to type erasure. Generics erase to Object at runtime, and
 *     primitives don't extend Object. So List<int> would erase to List,
 *     but int can't be stored in an Object[]. Valhalla solves this via
 *     generic specialization — the JVM creates a specialized version
 *     with int[] backing instead of Object[]."
 *
 * Q: "What's the performance cost of autoboxing in collections?"
 * A: "Each boxed Integer is ~16 bytes (4x the raw int), heap-allocated,
 *     and creates GC pressure. An ArrayList<Integer> with 1M elements
 *     uses ~24 MB vs ~4 MB for int[]. There's also cache-locality loss
 *     from pointer chasing — the Integer objects are scattered in heap."
 *
 * Q: "How do developers work around this today?"
 * A: "Three main approaches:
 *     1. Specialized streams (IntStream, LongStream) — but limited to 3 types
 *     2. Primitive arrays (int[]) — but no Collection API
 *     3. Third-party libraries (Eclipse Collections, HPPC) — but not standard
 *     Valhalla will make all three workarounds unnecessary."
 *
 * Q: "What is the relationship between value classes and primitive generics?"
 * A: "They're both part of Project Valhalla. Value classes make user-defined
 *     types identity-free (flat, no header). Primitive generics let those
 *     types (and Java's built-in primitives) be used as type parameters.
 *     Together: value class Point + List<Point> = flat array of x,y pairs."
 */
public class PrimitiveGenericsDemo {

    public static void main(String[] args) {
        System.out.println("=== Primitive Generics — Project Valhalla ===\n");

        demoCurrentProblem();
        demoProposedSolution();
        demoPerformanceBenchmark();
        demoImpactOnAPIs();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. THE CURRENT PROBLEM: Boxing overhead
    // ─────────────────────────────────────────────────────────────────────────
    static void demoCurrentProblem() {
        System.out.println("── Current Problem: Autoboxing Overhead ──\n");

        // Can't do: List<int> — must use List<Integer>
        List<Integer> boxedList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            boxedList.add(i);  // autoboxing: int → Integer (heap allocation!)
        }

        int sum = 0;
        for (Integer val : boxedList) {
            sum += val;  // auto-unboxing: Integer → int
        }

        System.out.println("List<Integer>: " + boxedList + ", sum = " + sum);
        System.out.println();

        // Show the boxing visually
        System.out.println("What happens when you do boxedList.add(42):");
        System.out.println("  1. Compiler inserts: boxedList.add(Integer.valueOf(42))");
        System.out.println("  2. Integer.valueOf(42) allocates an Integer object on heap");
        System.out.println("  3. Object stored as pointer in Object[] backing array");
        System.out.println("  4. When you read: int x = list.get(0)");
        System.out.println("  5. Compiler inserts: int x = list.get(0).intValue()");
        System.out.println("  6. Follow pointer → read Integer object → extract int value");
        System.out.println();

        // Memory comparison
        System.out.println("Memory layout — List<Integer> of 4 elements:");
        System.out.println("  Object[] backing array: [ptr][ptr][ptr][ptr]  (32 bytes)");
        System.out.println("  Each pointer →");
        System.out.println("    ┌──────────────┐");
        System.out.println("    │ Object Header│ 12 bytes");
        System.out.println("    │ int value    │ 4 bytes");
        System.out.println("    └──────────────┘");
        System.out.println("  Total: 32 + 4×16 = 96 bytes for 4 ints (16 bytes useful)");
        System.out.println();

        System.out.println("Memory layout — int[] of 4 elements (primitive array):");
        System.out.println("  [42][43][44][45]  → 16 bytes, flat, contiguous");
        System.out.println("  6x less memory, perfect cache locality");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. THE PROPOSED SOLUTION: Primitive type parameters
    // ─────────────────────────────────────────────────────────────────────────
    static void demoProposedSolution() {
        System.out.println("── Proposed Solution: List<int>, Map<int, double> ──\n");

        /*
         * WITH VALHALLA (future syntax):
         *
         *   // Direct primitive generics — no boxing
         *   List<int> numbers = new ArrayList<>();
         *   numbers.add(42);           // stored as raw int in int[] backing
         *   int x = numbers.get(0);    // direct access, no unboxing
         *
         *   // Maps with primitive keys and values
         *   Map<int, double> scores = new HashMap<>();
         *   scores.put(1, 99.5);       // no Integer, no Double
         *   double score = scores.get(1);
         *
         *   // Optional without boxing
         *   Optional<int> maybe = Optional.of(42);  // replaces OptionalInt
         *   int value = maybe.orElse(0);
         *
         *   // Streams unified — no more IntStream vs Stream<Integer>
         *   Stream<int> intStream = numbers.stream();
         *   int total = intStream.reduce(0, Integer::sum);
         *
         *   // Generic algorithms work with primitives
         *   <T> T max(List<T> list, Comparator<T> cmp) { ... }
         *   int biggest = max(intList, Integer::compare);  // no boxing!
         *
         * NULL HANDLING:
         *   List<int> list = ...;
         *   list.add(null);  // COMPILE ERROR — int can't be null
         *   // If you need nullable: List<Integer> still works
         */

        System.out.println("Future API examples:");
        System.out.println("  List<int> nums = new ArrayList<>();");
        System.out.println("  nums.add(42);           // no boxing");
        System.out.println("  int x = nums.get(0);    // no unboxing");
        System.out.println();
        System.out.println("  Map<int, double> scores = new HashMap<>();");
        System.out.println("  scores.put(1, 99.5);    // flat storage");
        System.out.println();
        System.out.println("  Stream<int> replaces IntStream (unified API)");
        System.out.println("  Optional<int> replaces OptionalInt");
        System.out.println();

        System.out.println("The specialized API zoo becomes unnecessary:");
        System.out.println("  IntStream      → Stream<int>");
        System.out.println("  LongStream     → Stream<long>");
        System.out.println("  DoubleStream   → Stream<double>");
        System.out.println("  OptionalInt    → Optional<int>");
        System.out.println("  IntFunction<R> → Function<int, R>");
        System.out.println("  ToIntFunction  → Function<T, int>");
        System.out.println("  IntConsumer    → Consumer<int>");
        System.out.println("  IntSupplier    → Supplier<int>");
        System.out.println("  IntPredicate   → Predicate<int>");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. PERFORMANCE BENCHMARK: Boxed vs Primitive
    // ─────────────────────────────────────────────────────────────────────────
    static void demoPerformanceBenchmark() {
        System.out.println("── Performance: Boxed vs Primitive ──\n");

        int N = 10_000_000;

        // Benchmark: sum using int[]
        int[] primitiveArray = new int[N];
        for (int i = 0; i < N; i++) primitiveArray[i] = i;

        long startPrimitive = System.nanoTime();
        long primSum = 0;
        for (int val : primitiveArray) primSum += val;
        long primTime = System.nanoTime() - startPrimitive;

        // Benchmark: sum using Integer[] (simulates List<Integer>)
        Integer[] boxedArray = new Integer[N];
        for (int i = 0; i < N; i++) boxedArray[i] = i;  // boxing

        long startBoxed = System.nanoTime();
        long boxSum = 0;
        for (Integer val : boxedArray) boxSum += val;    // unboxing
        long boxedTime = System.nanoTime() - startBoxed;

        System.out.printf("Sum of %,d elements:%n", N);
        System.out.printf("  int[]     → %d ms (result: %d)%n", primTime / 1_000_000, primSum);
        System.out.printf("  Integer[] → %d ms (result: %d)%n", boxedTime / 1_000_000, boxSum);
        System.out.printf("  Ratio: Integer[] is ~%.1fx slower%n",
                (double) boxedTime / primTime);

        System.out.println("\nMemory comparison:");
        System.out.printf("  int[%dM]:     ~%d MB%n", N / 1_000_000, (long) N * 4 / 1_048_576);
        System.out.printf("  Integer[%dM]: ~%d MB (including objects)%n",
                N / 1_000_000, (long) N * 24 / 1_048_576);
        System.out.println("  With Valhalla List<int>: same performance as int[]");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. IMPACT ON JAVA APIs
    // ─────────────────────────────────────────────────────────────────────────
    static void demoImpactOnAPIs() {
        System.out.println("── Impact on Java APIs ──\n");

        System.out.println("APIs that will be simplified or unified:");
        System.out.println();
        System.out.println("  TODAY (42 functional interfaces for 3 primitive types):");
        System.out.println("    IntFunction, LongFunction, DoubleFunction");
        System.out.println("    IntConsumer, LongConsumer, DoubleConsumer");
        System.out.println("    IntSupplier, LongSupplier, DoubleSupplier");
        System.out.println("    IntPredicate, LongPredicate, DoublePredicate");
        System.out.println("    IntUnaryOperator, LongUnaryOperator, DoubleUnaryOperator");
        System.out.println("    IntBinaryOperator, LongBinaryOperator, DoubleBinaryOperator");
        System.out.println("    ObjIntConsumer, ObjLongConsumer, ObjDoubleConsumer");
        System.out.println("    ToIntFunction, ToLongFunction, ToDoubleFunction");
        System.out.println("    IntToLongFunction, IntToDoubleFunction, ...");
        System.out.println("    ... and more");
        System.out.println();
        System.out.println("  AFTER VALHALLA (just the generic versions):");
        System.out.println("    Function<int, R>, Function<long, R>, Function<T, int>");
        System.out.println("    Consumer<int>, Consumer<long>, Consumer<double>");
        System.out.println("    Supplier<int>, Supplier<long>, Supplier<double>");
        System.out.println("    Predicate<int>, Predicate<long>");
        System.out.println("    UnaryOperator<int>, BinaryOperator<int>");
        System.out.println("    ... all from the SAME generic interface!");
        System.out.println();

        System.out.println("Combined with value classes:");
        System.out.println("  value record Point(int x, int y) {}");
        System.out.println("  List<Point> → flat array: [x,y,x,y,x,y,...] — zero overhead");
        System.out.println("  Map<int, Point> → flat keys, flat values — maximum efficiency");
    }
}
