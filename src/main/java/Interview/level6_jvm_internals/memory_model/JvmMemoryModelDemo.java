package interview.level6_jvm_internals.memory_model;

// LEVEL: Staff / Principal

/*
 * =============================================================================================
 * JVM MEMORY MODEL — INTERVIEW DEEP DIVE
 * =============================================================================================
 *
 * Q: "Explain JVM memory areas."
 * A: The JVM divides memory into several runtime data areas:
 *
 *    ┌─────────────────────────────────────────────────────────────────────┐
 *    │                         JVM MEMORY LAYOUT                         │
 *    ├─────────────────────────────────────────────────────────────────────┤
 *    │                                                                     │
 *    │  ┌───────────────────────────────────────────────────────────┐     │
 *    │  │                      HEAP (shared)                        │     │
 *    │  │  ┌─────────────────────────┐  ┌───────────────────────┐  │     │
 *    │  │  │      YOUNG GENERATION    │  │    OLD GENERATION      │  │     │
 *    │  │  │  ┌──────┬─────┬─────┐   │  │  (Tenured Space)       │  │     │
 *    │  │  │  │ Eden │ S0  │ S1  │   │  │                         │  │     │
 *    │  │  │  └──────┴─────┴─────┘   │  │  Objects that survived  │  │     │
 *    │  │  │  New objects allocated   │  │  multiple GC cycles     │  │     │
 *    │  │  │  here. S0/S1 are        │  │  end up here.           │  │     │
 *    │  │  │  survivor spaces.       │  │                         │  │     │
 *    │  │  └─────────────────────────┘  └───────────────────────┘  │     │
 *    │  └───────────────────────────────────────────────────────────┘     │
 *    │                                                                     │
 *    │  ┌───────────────────────────────────────────────────────────┐     │
 *    │  │               METASPACE (off-heap, native)                │     │
 *    │  │  Class metadata, method bytecode, constant pools          │     │
 *    │  │  Replaced PermGen in Java 8. Grows dynamically.           │     │
 *    │  └───────────────────────────────────────────────────────────┘     │
 *    │                                                                     │
 *    │  ┌─────────────────────┐  (one per thread)                        │
 *    │  │   THREAD STACK       │                                          │
 *    │  │  - Stack frames      │  Each method call creates a frame        │
 *    │  │  - Local variables   │  containing locals, operand stack,       │
 *    │  │  - Operand stack     │  and return address.                     │
 *    │  │  - Frame data        │                                          │
 *    │  └─────────────────────┘                                          │
 *    │                                                                     │
 *    │  ┌─────────────────────┐  (one per thread)                        │
 *    │  │  PROGRAM COUNTER     │  Points to current bytecode              │
 *    │  │  (PC Register)       │  instruction being executed.             │
 *    │  └─────────────────────┘                                          │
 *    │                                                                     │
 *    │  ┌─────────────────────┐  (one per thread)                        │
 *    │  │ NATIVE METHOD STACK  │  For native (JNI) method calls.          │
 *    │  └─────────────────────┘                                          │
 *    │                                                                     │
 *    └─────────────────────────────────────────────────────────────────────┘
 *
 * =============================================================================================
 * Q: "What replaced PermGen?"
 * A: Metaspace (Java 8+). Key differences:
 *    1. PermGen had a fixed maximum size (-XX:MaxPermSize), causing
 *       java.lang.OutOfMemoryError: PermGen space.
 *    2. Metaspace lives in native memory, not the Java heap.
 *    3. Metaspace grows automatically (capped by -XX:MaxMetaspaceSize if set).
 *    4. Class metadata is now garbage-collected when the class loader that loaded
 *       those classes is itself garbage-collected.
 *
 * =============================================================================================
 * Q: "What causes StackOverflowError vs OutOfMemoryError?"
 * A:
 *    StackOverflowError:
 *      - Caused by too-deep recursion (stack frames exceed -Xss limit).
 *      - Each thread has its own stack; default is 512KB-1MB depending on OS.
 *      - Fix: eliminate unbounded recursion, increase -Xss (rarely the right answer).
 *
 *    OutOfMemoryError:
 *      - "Java heap space"    — heap is full, GC cannot reclaim enough memory.
 *      - "Metaspace"          — too many classes loaded (common with dynamic proxies,
 *                                hot-deploy in app servers).
 *      - "GC overhead limit"  — GC is spending >98% of time collecting but reclaiming <2%.
 *      - "unable to create new native thread" — OS thread limit reached.
 *      - "Direct buffer memory" — too many direct ByteBuffers allocated off-heap.
 *
 * =============================================================================================
 * Q: "Explain the difference between stack and heap memory."
 * A:
 *    Stack:                                 Heap:
 *    - Per-thread, fast allocation           - Shared across all threads
 *    - Stores primitives & references        - Stores objects
 *    - LIFO order, auto-cleanup on return    - Managed by GC
 *    - Fixed size (-Xss)                     - Growable (-Xms to -Xmx)
 *    - StackOverflowError if exceeded        - OutOfMemoryError if exceeded
 *
 * =============================================================================================
 * Q: "What is the object header in HotSpot JVM?"
 * A: Every object on the heap has a header:
 *    - Mark Word (8 bytes on 64-bit): hashCode, GC age (4 bits, max 15 before
 *      promotion), lock state, biased locking info.
 *    - Klass Pointer (4 or 8 bytes): pointer to class metadata in Metaspace.
 *      Compressed oops (-XX:+UseCompressedOops, default) uses 4 bytes.
 *    - Array Length (4 bytes): only for arrays.
 *    Minimum object size = 16 bytes (header) on 64-bit with compressed oops.
 *
 * =============================================================================================
 * Q: "What is TLAB?"
 * A: Thread-Local Allocation Buffer. Each thread gets a private chunk of Eden space
 *    so it can allocate objects without synchronization. When the TLAB is full, a new
 *    one is obtained (with synchronization). This makes allocation almost as fast as
 *    incrementing a pointer. -XX:+UseTLAB is on by default.
 *
 * =============================================================================================
 * USEFUL JVM FLAGS FOR MEMORY INSPECTION:
 *    -Xms512m                     Initial heap size
 *    -Xmx2g                      Maximum heap size
 *    -Xss512k                    Thread stack size
 *    -XX:MetaspaceSize=128m       Initial Metaspace threshold for GC
 *    -XX:MaxMetaspaceSize=512m    Maximum Metaspace size
 *    -XX:NewRatio=2               Old/Young ratio (Old = 2x Young)
 *    -XX:SurvivorRatio=8          Eden/Survivor ratio (Eden = 8x one Survivor)
 *    -XX:+PrintGCDetails          (pre-Java 9) Print detailed GC info
 *    -Xlog:gc*                    (Java 9+) Unified logging for GC
 *    -XX:NativeMemoryTracking=summary  Track native memory usage
 * =============================================================================================
 */

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

public class JvmMemoryModelDemo {

    // -----------------------------------------------------------------------------------------
    // DEMO 1: Query memory areas using MXBeans
    // -----------------------------------------------------------------------------------------
    static void demoMemoryPools() {
        System.out.println("=== JVM Memory Pools ===\n");

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        // Heap memory — this is where your objects live
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        System.out.println("HEAP MEMORY:");
        System.out.printf("  Init:      %,d bytes%n", heapUsage.getInit());
        System.out.printf("  Used:      %,d bytes%n", heapUsage.getUsed());
        System.out.printf("  Committed: %,d bytes%n", heapUsage.getCommitted());
        System.out.printf("  Max:       %,d bytes%n%n", heapUsage.getMax());

        // Non-heap memory — Metaspace, Code Cache, etc.
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        System.out.println("NON-HEAP MEMORY (Metaspace + Code Cache + etc.):");
        System.out.printf("  Init:      %,d bytes%n", nonHeapUsage.getInit());
        System.out.printf("  Used:      %,d bytes%n", nonHeapUsage.getUsed());
        System.out.printf("  Committed: %,d bytes%n", nonHeapUsage.getCommitted());
        System.out.printf("  Max:       %,d bytes%n%n", nonHeapUsage.getMax());

        // Individual memory pools — shows Eden, Survivors, Old Gen, Metaspace, etc.
        System.out.println("INDIVIDUAL MEMORY POOLS:");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage usage = pool.getUsage();
            System.out.printf("  %-35s [%s]  used=%,12d  max=%,12d%n",
                    pool.getName(),
                    pool.getType(),
                    usage.getUsed(),
                    usage.getMax());
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 2: Show heap impact of object allocation
    //
    // Interview insight: Each Object has ~16 bytes of overhead (mark word + klass pointer).
    // An int[] of length N uses 16 (header + length) + 4*N bytes, aligned to 8.
    // -----------------------------------------------------------------------------------------
    static void demoObjectAllocation() {
        System.out.println("=== Object Allocation Impact ===\n");

        Runtime rt = Runtime.getRuntime();

        // Force GC to get a baseline (best-effort)
        rt.gc();
        long before = rt.totalMemory() - rt.freeMemory();
        System.out.printf("  Heap used before allocation: %,d bytes%n", before);

        // Allocate 100,000 objects — each is an int[100] => ~416 bytes each
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            list.add(new int[100]); // ~400 bytes payload + ~16 bytes header
        }

        long after = rt.totalMemory() - rt.freeMemory();
        System.out.printf("  Heap used after allocating 100k int[100]: %,d bytes%n", after);
        System.out.printf("  Delta: %,d bytes (~%.1f MB)%n", (after - before), (after - before) / 1_048_576.0);
        System.out.printf("  Average per object: ~%d bytes%n%n", (after - before) / 100_000);

        // Keep reference so GC does not collect them (demonstrates strong references)
        System.out.println("  (Objects are strongly reachable via 'list', so GC cannot collect them.)");
        list.clear(); // Now eligible for GC
        System.out.println("  After list.clear(), objects become eligible for GC.\n");
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 3: StackOverflowError — deep recursion exhausts the thread stack
    // -----------------------------------------------------------------------------------------
    static int recursionDepth = 0;

    static void causeStackOverflow() {
        recursionDepth++;
        causeStackOverflow(); // Each call adds a stack frame (~hundreds of bytes)
    }

    static void demoStackOverflow() {
        System.out.println("=== StackOverflowError Demo ===\n");
        recursionDepth = 0;
        try {
            causeStackOverflow();
        } catch (StackOverflowError e) {
            // StackOverflowError is an Error, not an Exception.
            // It means the thread's stack (default -Xss = 512K-1M) was exhausted.
            System.out.printf("  StackOverflowError after %,d recursive calls.%n", recursionDepth);
            System.out.println("  Each stack frame held local variables + operand stack + frame data.");
            System.out.println("  Fix: convert recursion to iteration, or increase -Xss (rarely right).\n");
        }
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 4: Class loader and Metaspace visibility
    // -----------------------------------------------------------------------------------------
    static void demoMetaspaceInfo() {
        System.out.println("=== Metaspace Info ===\n");

        // Show class loader hierarchy for this class
        ClassLoader cl = JvmMemoryModelDemo.class.getClassLoader();
        System.out.println("  Class loader hierarchy for JvmMemoryModelDemo:");
        while (cl != null) {
            System.out.println("    -> " + cl);
            cl = cl.getParent();
        }
        System.out.println("    -> Bootstrap ClassLoader (null in Java, implemented in native code)");
        System.out.println();

        // Count loaded classes
        // In Java 9+, the class loading MXBean still works
        long loadedClasses = ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
        long totalLoaded = ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount();
        long unloaded = ManagementFactory.getClassLoadingMXBean().getUnloadedClassCount();
        System.out.printf("  Currently loaded classes: %,d%n", loadedClasses);
        System.out.printf("  Total loaded (all time):  %,d%n", totalLoaded);
        System.out.printf("  Unloaded classes:         %,d%n", unloaded);
        System.out.println("  (Each class's metadata lives in Metaspace, not the heap.)\n");
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 5: Runtime memory summary — quick reference for interviews
    // -----------------------------------------------------------------------------------------
    static void demoRuntimeMemory() {
        System.out.println("=== Runtime Memory Summary ===\n");
        Runtime rt = Runtime.getRuntime();
        System.out.printf("  Available processors: %d%n", rt.availableProcessors());
        System.out.printf("  Max memory (-Xmx):    %,d bytes (%.1f MB)%n",
                rt.maxMemory(), rt.maxMemory() / 1_048_576.0);
        System.out.printf("  Total memory (current heap): %,d bytes (%.1f MB)%n",
                rt.totalMemory(), rt.totalMemory() / 1_048_576.0);
        System.out.printf("  Free memory (in current heap): %,d bytes (%.1f MB)%n",
                rt.freeMemory(), rt.freeMemory() / 1_048_576.0);
        System.out.printf("  Used memory: %,d bytes (%.1f MB)%n%n",
                (rt.totalMemory() - rt.freeMemory()),
                (rt.totalMemory() - rt.freeMemory()) / 1_048_576.0);
    }

    // -----------------------------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           JVM MEMORY MODEL — INTERVIEW DEMO                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Show all memory pools (Eden, Survivor, Old Gen, Metaspace, Code Cache)
        demoMemoryPools();

        // 2. Demonstrate heap impact of allocating objects
        demoObjectAllocation();

        // 3. StackOverflowError from deep recursion
        demoStackOverflow();

        // 4. Metaspace and class loading info
        demoMetaspaceInfo();

        // 5. Quick runtime memory summary
        demoRuntimeMemory();

        System.out.println("=== Key Takeaways for Interviews ===");
        System.out.println("  1. Heap = Young (Eden + S0 + S1) + Old. Objects start in Eden.");
        System.out.println("  2. Metaspace replaced PermGen in Java 8 — lives in native memory.");
        System.out.println("  3. Each thread has its own stack, PC register, and native method stack.");
        System.out.println("  4. StackOverflowError = stack exhausted; OOM = heap/metaspace exhausted.");
        System.out.println("  5. Use -Xlog:gc* (Java 9+) to see GC behavior and memory pool sizes.");
        System.out.println();
        System.out.println("  Run with these flags to see detailed memory info:");
        System.out.println("    java -Xms128m -Xmx512m -Xlog:gc* -XX:NativeMemoryTracking=summary \\");
        System.out.println("         interview.level6_jvm_internals.memory_model.JvmMemoryModelDemo");
    }
}
