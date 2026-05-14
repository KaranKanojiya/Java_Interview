package interview.level6_jvm_internals.jit;

// LEVEL: Staff / Principal

/*
 * =============================================================================================
 * JIT COMPILATION — INTERVIEW DEEP DIVE
 * =============================================================================================
 *
 * Q: "What is JIT compilation?"
 * A: JIT (Just-In-Time) compilation is the process by which the JVM converts frequently
 *    executed bytecode into native machine code at runtime, achieving near-native performance.
 *
 *    Execution pipeline:
 *      .java -> javac -> .class (bytecode) -> JVM interpreter -> JIT compiler -> native code
 *
 *    The JVM starts by interpreting bytecode. When it detects "hot" methods (invoked many
 *    times) or "hot" loops (iterated many times), it compiles them to optimized native code.
 *    This is called "adaptive optimization" — optimize what matters, skip what doesn't.
 *
 *    Advantages over AOT (Ahead-of-Time) compilation:
 *      - Can use runtime profiling data to make better optimization decisions
 *      - Can speculate and de-optimize if assumptions are violated
 *      - Can inline polymorphic calls based on observed receiver types
 *      - Adapts to actual workload (not hypothetical worst case)
 *
 * =============================================================================================
 * Q: "What is tiered compilation?"
 * A: Since Java 8 (default), the JVM uses tiered compilation with 5 levels:
 *
 *    Level 0: Interpreter
 *      - Executes bytecode directly. Collects basic profiling info.
 *
 *    Level 1: C1 with full optimization (no profiling)
 *      - Simple methods that are fully optimizable. No profiling overhead.
 *
 *    Level 2: C1 with invocation/backedge counters
 *      - Methods compiled with limited profiling. Counts invocations.
 *
 *    Level 3: C1 with full profiling
 *      - Compiled by C1 with full profiling data collection.
 *      - This data feeds into C2 optimizations.
 *
 *    Level 4: C2 with full optimization
 *      - Server compiler. Aggressive optimizations: inlining, escape analysis,
 *        loop unrolling, vectorization, dead code elimination.
 *      - Slower to compile but produces the fastest code.
 *
 *    Typical path: 0 -> 3 -> 4 (interpret, C1 with profiling, then C2 optimized)
 *    Trivial methods: 0 -> 1 (interpret, then C1 simple compile, skip C2)
 *
 *    Flag: -XX:+TieredCompilation (default true since Java 8)
 *    Flag: -XX:TieredStopAtLevel=1 (use only C1 — faster startup, less peak perf)
 *
 * =============================================================================================
 * Q: "What are the key JIT optimizations?"
 * A:
 *    1. METHOD INLINING — Replace method call with method body.
 *       - Eliminates call overhead, enables further optimizations.
 *       - -XX:MaxInlineSize=35 (bytes, default) for always-inline.
 *       - -XX:FreqInlineSize=325 (bytes, default) for hot methods.
 *       - Virtual calls can be inlined if profiling shows only one receiver type
 *         (monomorphic inline cache). If a new type appears, the JIT "deoptimizes"
 *         and falls back to interpretation.
 *
 *    2. ESCAPE ANALYSIS — Determine if an object "escapes" the method/thread.
 *       - If NO escape: allocate on stack (no GC needed) or eliminate entirely.
 *       - Scalar replacement: break object into its fields, store in registers.
 *       - Lock elision: remove synchronization on non-escaping objects.
 *       - -XX:+DoEscapeAnalysis (default true)
 *       Example:
 *         Point p = new Point(x, y);  // if p doesn't escape...
 *         return p.x + p.y;           // ...replaced with: return x + y; (no allocation!)
 *
 *    3. LOOP OPTIMIZATIONS
 *       - Loop unrolling: reduce loop overhead by duplicating loop body.
 *       - Loop-invariant code motion: move constant expressions out of loops.
 *       - Range check elimination: remove array bounds checks when provably safe.
 *       - Vectorization (SIMD): process multiple array elements in parallel.
 *
 *    4. DEAD CODE ELIMINATION — Remove code that has no effect on output.
 *       - Constant folding: evaluate constant expressions at compile time.
 *       - Unreachable code removal.
 *
 *    5. NULL CHECK ELIMINATION — Remove redundant null checks.
 *       - Uses implicit null checks via SIGSEGV handler (free on most paths).
 *
 *    6. BRANCH PREDICTION HINTS — Reorder code to favor the common path.
 *       - Based on profiling data collected during interpretation.
 *
 * =============================================================================================
 * Q: "What is escape analysis?"
 * A: Escape analysis determines whether an object reference escapes its creation scope:
 *
 *    NoEscape: object is used only within the method.
 *      -> Stack allocation, scalar replacement, lock elision.
 *
 *    ArgEscape: object is passed as argument but doesn't escape the calling thread.
 *      -> Some optimizations still possible.
 *
 *    GlobalEscape: object is stored in a static field, returned from method,
 *                  or shared with another thread.
 *      -> Must be heap-allocated, no lock elision.
 *
 *    Example of NoEscape:
 *      int sum(int a, int b) {
 *          Point p = new Point(a, b); // p doesn't escape
 *          return p.x + p.y;          // JIT eliminates allocation entirely
 *      }
 *
 * =============================================================================================
 * Q: "What is deoptimization?"
 * A: The JIT makes speculative optimizations based on profiling data. If the assumptions
 *    are later violated, the JIT "deoptimizes" — discards the compiled code and falls
 *    back to interpretation. Common triggers:
 *      - Monomorphic call site becomes polymorphic (new subclass appears)
 *      - Uncommon trap: branch that was never taken is suddenly taken
 *      - Class loading invalidates inlined code
 *    Deoptimization is a strength, not a weakness — it lets the JIT be aggressive
 *    with optimizations while maintaining correctness.
 *
 * =============================================================================================
 * Q: "What is the code cache?"
 * A: The code cache is a native memory area that stores JIT-compiled code.
 *    If it fills up, the JIT stops compiling and performance degrades.
 *    - -XX:ReservedCodeCacheSize=256m (default varies, ~240MB in Java 17)
 *    - In Java 9+, segmented: non-method, profiled, non-profiled code.
 *    - Monitor with: jcmd <pid> Compiler.codecache
 *
 * =============================================================================================
 * Q: "What about GraalVM and AOT?"
 * A: GraalVM offers:
 *    - Graal JIT: alternative C2 compiler written in Java. Better for some workloads.
 *      Enabled with -XX:+UseJVMCICompiler.
 *    - Native Image: AOT compilation to standalone binary. Fast startup, low memory,
 *      but no runtime JIT, restricted reflection, closed-world assumption.
 *    - Java 9-17 had jaotc (experimental AOT), removed in Java 17.
 *    - Project Leyden (ongoing): aims to bring constrained AOT to mainline JDK.
 *
 * =============================================================================================
 * USEFUL FLAGS:
 *    -XX:+PrintCompilation               Print methods as they are JIT-compiled
 *    -XX:+UnlockDiagnosticVMOptions
 *    -XX:+PrintInlining                  Show inlining decisions
 *    -XX:+PrintAssembly                  Print generated assembly (needs hsdis)
 *    -XX:CompileThreshold=10000          Invocations before C2 compile (non-tiered)
 *    -XX:+TieredCompilation              Enable tiered (default)
 *    -XX:TieredStopAtLevel=1             Only use C1 (fast startup)
 * =============================================================================================
 */

public class JitCompilationDemo {

    // -----------------------------------------------------------------------------------------
    // DEMO 1: Warm-up effect — method gets faster after JIT compilation
    //
    // The first invocations are interpreted (slow). After the JIT threshold is reached,
    // the method is compiled to native code and subsequent calls are much faster.
    // -----------------------------------------------------------------------------------------
    static long computeSum(int n) {
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += i;
        }
        return sum;
    }

    static void demoWarmupEffect() {
        System.out.println("=== JIT Warm-up Effect ===\n");
        System.out.println("  Calling computeSum(1_000_000) repeatedly.");
        System.out.println("  Early calls are interpreted; later calls use JIT-compiled code.\n");

        int iterations = 1_000_000;
        long result = 0;

        for (int round = 1; round <= 20; round++) {
            long start = System.nanoTime();
            result = computeSum(iterations);
            long elapsed = System.nanoTime() - start;

            // Show selected rounds to highlight the warm-up curve
            if (round <= 5 || round == 10 || round == 15 || round == 20) {
                System.out.printf("  Round %2d: %,8d ns  (result=%d)%n", round, elapsed, result);
            }
        }

        System.out.println();
        System.out.println("  Observation: later rounds are typically faster because:");
        System.out.println("    1. The interpreter collected profiling data");
        System.out.println("    2. C1 compiled the method with profiling");
        System.out.println("    3. C2 compiled it with full optimizations");
        System.out.println("    4. Loop unrolling, range check elimination, and possibly vectorization");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 2: Method inlining — small methods are inlined by the JIT
    // -----------------------------------------------------------------------------------------
    static int add(int a, int b) {
        return a + b; // Trivial method — will be inlined
    }

    static int multiply(int a, int b) {
        return a * b; // Trivial method — will be inlined
    }

    // This method calls add() and multiply() millions of times.
    // After JIT compilation, the call overhead is eliminated by inlining.
    static long computeWithInlining(int n) {
        long result = 0;
        for (int i = 0; i < n; i++) {
            result += add(i, multiply(i, 2)); // Both calls will be inlined
        }
        return result;
    }

    static void demoMethodInlining() {
        System.out.println("=== Method Inlining ===\n");

        System.out.println("  computeWithInlining() calls add() and multiply() in a loop.");
        System.out.println("  After JIT compilation, these small methods are inlined.");
        System.out.println("  The compiled code becomes: result += i + (i * 2)\n");

        long result = 0;
        for (int round = 1; round <= 10; round++) {
            long start = System.nanoTime();
            result = computeWithInlining(1_000_000);
            long elapsed = System.nanoTime() - start;

            if (round == 1 || round == 5 || round == 10) {
                System.out.printf("  Round %2d: %,8d ns%n", round, elapsed);
            }
        }
        System.out.println("  Result: " + result);
        System.out.println();
        System.out.println("  To see inlining decisions:");
        System.out.println("    java -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining ...");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 3: Escape analysis — objects that don't escape can be stack-allocated
    // -----------------------------------------------------------------------------------------

    // Simple Point class used to demonstrate escape analysis
    static class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
        int distanceSquared() { return x * x + y * y; }
    }

    // The Point object does NOT escape this method.
    // With escape analysis, the JIT may:
    //   1. Eliminate the allocation entirely (scalar replacement)
    //   2. Store x and y in registers instead of heap
    static int computeDistanceNoEscape(int a, int b) {
        Point p = new Point(a, b); // Does not escape
        return p.distanceSquared();
        // After optimization: return a*a + b*b (no Point allocated)
    }

    // The Point object DOES escape (returned to caller).
    // Cannot use scalar replacement — must heap-allocate.
    static Point computeDistanceEscapes(int a, int b) {
        Point p = new Point(a, b); // Escapes! Returned to caller
        return p;
    }

    static void demoEscapeAnalysis() {
        System.out.println("=== Escape Analysis ===\n");

        System.out.println("  Non-escaping case: Point is created and used locally.");
        System.out.println("  JIT can eliminate the allocation entirely (scalar replacement).\n");

        // Warm up to trigger JIT compilation
        int result = 0;
        for (int i = 0; i < 100_000; i++) {
            result += computeDistanceNoEscape(i, i + 1);
        }

        // Measure non-escaping version
        long start = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            result += computeDistanceNoEscape(i, i + 1);
        }
        long noEscapeTime = System.nanoTime() - start;

        // Measure escaping version
        Point p = null;
        start = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            p = computeDistanceEscapes(i, i + 1);
        }
        long escapesTime = System.nanoTime() - start;

        System.out.printf("  Non-escaping (scalar replacement possible): %,d ns%n", noEscapeTime);
        System.out.printf("  Escaping (must heap-allocate):              %,d ns%n", escapesTime);
        System.out.println("  Result: " + result + ", last point: " + p.x);
        System.out.println();
        System.out.println("  Non-escaping is often faster because:");
        System.out.println("    1. No heap allocation (no GC pressure)");
        System.out.println("    2. Fields stored in registers (cache-friendly)");
        System.out.println("    3. No object header overhead");
        System.out.println();
        System.out.println("  Enable/disable with: -XX:+DoEscapeAnalysis (default: on)");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 4: Dead code elimination
    // -----------------------------------------------------------------------------------------
    static void demoDeadCodeElimination() {
        System.out.println("=== Dead Code Elimination ===\n");

        System.out.println("  CAUTION: When benchmarking, the JIT may eliminate 'dead' code");
        System.out.println("  (code whose result is never used). This gives misleadingly fast results.\n");

        // BAD benchmark: result is never used -> JIT may eliminate the entire loop
        long start = System.nanoTime();
        for (int i = 0; i < 10_000_000; i++) {
            Math.sin(i); // Result discarded -> JIT may remove this entirely
        }
        long deadCodeTime = System.nanoTime() - start;

        // GOOD benchmark: result is consumed, so JIT cannot eliminate the computation
        double sum = 0;
        start = System.nanoTime();
        for (int i = 0; i < 10_000_000; i++) {
            sum += Math.sin(i); // Result accumulated -> must be computed
        }
        long liveCodeTime = System.nanoTime() - start;

        System.out.printf("  Discarded result (may be eliminated): %,d ns%n", deadCodeTime);
        System.out.printf("  Consumed result (must be computed):   %,d ns%n", liveCodeTime);
        System.out.println("  Sum: " + sum + " (prevents dead code elimination)");
        System.out.println();
        System.out.println("  For proper benchmarks, use JMH (Java Microbenchmark Harness).");
        System.out.println("  JMH uses Blackhole.consume() to prevent dead code elimination.\n");
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 5: Show compilation info from Runtime
    // -----------------------------------------------------------------------------------------
    static void showCompilationInfo() {
        System.out.println("=== Compilation Info ===\n");

        var compilationBean = java.lang.management.ManagementFactory.getCompilationMXBean();
        if (compilationBean != null) {
            System.out.println("  JIT Compiler:       " + compilationBean.getName());
            if (compilationBean.isCompilationTimeMonitoringSupported()) {
                System.out.printf("  Total compile time: %,d ms%n", compilationBean.getTotalCompilationTime());
            }
        }

        System.out.println();
        System.out.println("  To see JIT compilation in action:");
        System.out.println("    java -XX:+PrintCompilation interview.level6_jvm_internals.jit.JitCompilationDemo");
        System.out.println();
        System.out.println("  PrintCompilation output columns:");
        System.out.println("    timestamp compile_id attributes tier method_name size deopt");
        System.out.println("    e.g.: 123  45  b  3  com.Foo::bar (25 bytes)");
        System.out.println("    Attributes: b=blocking, %=on-stack-replacement (OSR), !=exception handler");
        System.out.println("    Tier: 1-4 (see tiered compilation levels above)");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           JIT COMPILATION — INTERVIEW DEMO                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Warm-up effect: interpreted -> C1 -> C2
        demoWarmupEffect();

        // 2. Method inlining
        demoMethodInlining();

        // 3. Escape analysis and scalar replacement
        demoEscapeAnalysis();

        // 4. Dead code elimination pitfall
        demoDeadCodeElimination();

        // 5. Compilation info from MXBean
        showCompilationInfo();

        System.out.println("=== Key Takeaways for Interviews ===");
        System.out.println("  1. JIT compiles hot methods to native code at runtime (adaptive optimization).");
        System.out.println("  2. Tiered: Interpreter -> C1 (fast compile, profiling) -> C2 (slow compile, best code).");
        System.out.println("  3. Key optimizations: inlining, escape analysis, loop unrolling, dead code elimination.");
        System.out.println("  4. Escape analysis can eliminate heap allocations entirely (scalar replacement).");
        System.out.println("  5. JIT can deoptimize if assumptions are violated (e.g., new subclass loaded).");
        System.out.println("  6. Use JMH for benchmarks — prevents dead code elimination and warm-up issues.");
        System.out.println("  7. Code cache stores compiled code; if full, JIT stops compiling.");
    }
}
