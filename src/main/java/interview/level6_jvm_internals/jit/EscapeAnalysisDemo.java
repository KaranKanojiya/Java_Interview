package interview.level6_jvm_internals.jit;

/**
 * Q7. What is Escape Analysis?
 *
 * Escape Analysis is a JIT compiler optimization that determines whether an object
 * "escapes" the scope of the method/thread that created it.
 *
 * Three escape levels:
 *   1. NoEscape      — object used only within the method
 *   2. ArgEscape     — object passed as argument but doesn't escape the thread
 *   3. GlobalEscape  — object escapes to heap (stored in field, returned, shared between threads)
 *
 * Optimizations enabled by escape analysis:
 *
 *   1. Stack Allocation:
 *      If object doesn't escape → allocate on STACK instead of HEAP
 *      → No GC needed, freed when method returns
 *      (Note: HotSpot doesn't truly stack-allocate, but uses scalar replacement)
 *
 *   2. Scalar Replacement:
 *      Replace object with its individual fields (scalars)
 *      → Object is "decomposed" into primitives on the stack
 *      → No object header, no heap allocation at all
 *
 *   3. Lock Elision (Lock Coarsening):
 *      If object doesn't escape the thread → synchronized is unnecessary
 *      → JIT removes the lock entirely
 *
 * Run with: java -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions
 *           -XX:+PrintInlining -XX:+PrintEscapeAnalysis
 *
 * Disable with: -XX:-DoEscapeAnalysis (for benchmarking)
 */
public class EscapeAnalysisDemo {

    // === Case 1: NoEscape — object stays in method ===
    static long sumWithNoEscape() {
        long sum = 0;
        for (int i = 0; i < 1_000_000; i++) {
            // This Point does NOT escape the method
            // JIT can: scalar replace (x, y on stack), eliminate allocation
            Point p = new Point(i, i + 1);
            sum += p.x + p.y;
        }
        return sum;
    }

    // === Case 2: GlobalEscape — object escapes ===
    static Point lastPoint;  // stored in static field → escapes

    static long sumWithGlobalEscape() {
        long sum = 0;
        for (int i = 0; i < 1_000_000; i++) {
            // This Point ESCAPES to static field → must be heap-allocated
            Point p = new Point(i, i + 1);
            lastPoint = p;  // escape!
            sum += p.x + p.y;
        }
        return sum;
    }

    // === Case 3: Lock elision ===
    static long sumWithUnnecessarySync() {
        long sum = 0;
        for (int i = 0; i < 1_000_000; i++) {
            Point p = new Point(i, i + 1);
            // p doesn't escape → JIT knows no other thread can access it
            // → synchronized is eliminated entirely (lock elision)
            synchronized (p) {
                sum += p.x + p.y;
            }
        }
        return sum;
    }

    static class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }

    public static void main(String[] args) {

        // Warm up JIT
        for (int i = 0; i < 5; i++) {
            sumWithNoEscape();
            sumWithGlobalEscape();
            sumWithUnnecessarySync();
        }

        System.out.println("=== Escape Analysis Performance ===");
        System.out.println("(Run multiple times — JIT optimizes after warmup)\n");

        // Benchmark: NoEscape (should be fastest — no heap allocation)
        long start = System.nanoTime();
        long result1 = sumWithNoEscape();
        long noEscapeTime = System.nanoTime() - start;

        // Benchmark: GlobalEscape (must heap-allocate)
        start = System.nanoTime();
        long result2 = sumWithGlobalEscape();
        long globalEscapeTime = System.nanoTime() - start;

        // Benchmark: Lock elision (lock should be removed by JIT)
        start = System.nanoTime();
        long result3 = sumWithUnnecessarySync();
        long lockElisionTime = System.nanoTime() - start;

        System.out.println("NoEscape (scalar replacement): " + (noEscapeTime / 1_000_000) + "ms  result=" + result1);
        System.out.println("GlobalEscape (heap alloc):     " + (globalEscapeTime / 1_000_000) + "ms  result=" + result2);
        System.out.println("Lock Elision:                  " + (lockElisionTime / 1_000_000) + "ms  result=" + result3);

        System.out.println("\n=== What JIT does ===");
        System.out.println("NoEscape:     Point is scalar-replaced → x,y as locals on stack, no object created");
        System.out.println("GlobalEscape: Point escapes to static field → must allocate on heap");
        System.out.println("Lock Elision: synchronized on non-escaping object → lock removed entirely");

        System.out.println("\n=== JVM flags for escape analysis ===");
        System.out.println("-XX:+DoEscapeAnalysis           → enabled by default (since Java 6u23)");
        System.out.println("-XX:-DoEscapeAnalysis            → disable (for benchmarking)");
        System.out.println("-XX:+EliminateAllocations        → enable scalar replacement (default on)");
        System.out.println("-XX:+EliminateLocks              → enable lock elision (default on)");
    }
}
