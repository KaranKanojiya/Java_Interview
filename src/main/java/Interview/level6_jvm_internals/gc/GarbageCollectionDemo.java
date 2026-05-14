package interview.level6_jvm_internals.gc;

// LEVEL: Staff / Principal

/*
 * =============================================================================================
 * GARBAGE COLLECTION — INTERVIEW DEEP DIVE
 * =============================================================================================
 *
 * Q: "Explain the Generational Hypothesis."
 * A: Most objects die young. Empirically, ~80-98% of objects become garbage shortly after
 *    allocation. This observation drives generational GC design:
 *      - Young Generation: frequent, fast collections (Minor GC)
 *      - Old Generation: infrequent, slower collections (Major/Full GC)
 *    Objects that survive multiple Minor GCs are "promoted" (tenured) to Old Gen.
 *    The promotion threshold is controlled by -XX:MaxTenuringThreshold (default 15 for G1).
 *
 * =============================================================================================
 * Q: "What are GC Roots?"
 * A: GC roots are the starting points for reachability analysis. Objects reachable from
 *    any GC root are "alive"; everything else is garbage. GC roots include:
 *      1. Local variables and parameters on thread stacks
 *      2. Active threads themselves
 *      3. Static fields of loaded classes
 *      4. JNI references
 *      5. Synchronization monitors (objects being used as locks)
 *      6. JVM internal references (system class loader, etc.)
 *
 *    The GC performs a "mark" phase starting from roots, then "sweeps" unmarked objects.
 *
 * =============================================================================================
 * Q: "Explain Mark-and-Sweep."
 * A: The fundamental GC algorithm:
 *      1. MARK — traverse object graph from GC roots, mark every reachable object.
 *      2. SWEEP — scan the heap, reclaim memory of unmarked objects.
 *      3. (Optional) COMPACT — move surviving objects together to eliminate fragmentation.
 *    Variants: mark-sweep, mark-compact, mark-copy (used in Young Gen — copy survivors
 *    from Eden+S0 to S1, which inherently compacts).
 *
 * =============================================================================================
 * Q: "Describe the GC algorithms available in HotSpot JVM."
 * A:
 *    ┌──────────────┬──────────────────────────────────────────────────────────────┐
 *    │ Collector     │ Characteristics                                             │
 *    ├──────────────┼──────────────────────────────────────────────────────────────┤
 *    │ Serial        │ Single-threaded, stop-the-world. -XX:+UseSerialGC           │
 *    │              │ Good for: small heaps, single-core, client apps.             │
 *    ├──────────────┼──────────────────────────────────────────────────────────────┤
 *    │ Parallel      │ Multi-threaded stop-the-world. -XX:+UseParallelGC           │
 *    │ (Throughput)  │ Good for: batch processing, throughput-focused workloads.   │
 *    │              │ Was default before Java 9.                                   │
 *    ├──────────────┼──────────────────────────────────────────────────────────────┤
 *    │ CMS           │ Concurrent Mark-Sweep. -XX:+UseConcMarkSweepGC              │
 *    │ (DEPRECATED)  │ Low-pause but fragmentation issues. Removed in Java 14.     │
 *    ├──────────────┼──────────────────────────────────────────────────────────────┤
 *    │ G1            │ Garbage-First. -XX:+UseG1GC (default since Java 9)          │
 *    │              │ Region-based, concurrent marking, predictable pauses.        │
 *    │              │ Targets -XX:MaxGCPauseMillis=200 by default.                 │
 *    ├──────────────┼──────────────────────────────────────────────────────────────┤
 *    │ ZGC           │ -XX:+UseZGC (production since Java 15)                      │
 *    │              │ Sub-millisecond pauses, handles multi-TB heaps.              │
 *    │              │ Uses colored pointers and load barriers.                     │
 *    │              │ Generational mode default since Java 21.                     │
 *    ├──────────────┼──────────────────────────────────────────────────────────────┤
 *    │ Shenandoah    │ -XX:+UseShenandoahGC (OpenJDK only, not Oracle JDK)         │
 *    │              │ Low-pause, concurrent compaction. Similar goals to ZGC.      │
 *    │              │ Uses Brooks forwarding pointers.                             │
 *    └──────────────┴──────────────────────────────────────────────────────────────┘
 *
 * =============================================================================================
 * Q: "Explain G1 GC in detail."
 * A: G1 (Garbage-First) divides the heap into equal-sized regions (1-32MB each).
 *    Each region can be Eden, Survivor, Old, or Humongous (for objects > 50% of region).
 *
 *    Phases:
 *      1. Young GC (STW): evacuate live objects from Eden/Survivor regions.
 *      2. Concurrent Marking: find which Old regions have the most garbage.
 *      3. Mixed GC (STW): collect Young + selected Old regions with most garbage.
 *         "Garbage-First" = prioritize regions with the most reclaimable space.
 *      4. Full GC (STW, fallback): if Mixed GC can't keep up, falls back to
 *         a serial full compacting collection — you want to avoid this.
 *
 *    Key tuning: -XX:MaxGCPauseMillis (target pause, default 200ms).
 *    G1 dynamically adjusts the number of regions collected per pause to meet the target.
 *
 * =============================================================================================
 * Q: "When would you use ZGC?"
 * A: Use ZGC when:
 *      - You need consistently low latency (sub-millisecond GC pauses).
 *      - You have a large heap (multi-GB to multi-TB).
 *      - Your application is latency-sensitive (trading, real-time bidding, gaming).
 *    ZGC achieves this through concurrent relocation using colored/tagged pointers
 *    (metadata stored in pointer bits) and load barriers (checks on every object load).
 *    Trade-off: slightly more CPU overhead than G1 for the concurrent work.
 *
 * =============================================================================================
 * Q: "What is the difference between Minor GC, Major GC, and Full GC?"
 * A:
 *    - Minor GC: collects Young Generation only. Fast (milliseconds).
 *    - Major GC: collects Old Generation. Often used interchangeably with Full GC
 *      but technically just the old generation.
 *    - Full GC: collects ENTIRE heap (Young + Old + Metaspace). Slow, stop-the-world.
 *      Triggered when Old Gen is full or System.gc() is called.
 *
 * =============================================================================================
 * Q: "Is finalize() still used?"
 * A: No. finalize() was deprecated in Java 9 and deprecated-for-removal in Java 18.
 *    Problems: unpredictable timing, performance overhead, can resurrect objects,
 *    runs on a single Finalizer thread (bottleneck). Use instead:
 *      - try-with-resources (AutoCloseable)
 *      - java.lang.ref.Cleaner (Java 9+)
 *      - PhantomReference + ReferenceQueue
 *
 * =============================================================================================
 */

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GarbageCollectionDemo {

    // -----------------------------------------------------------------------------------------
    // DEMO 1: Create garbage and observe GC behavior
    // -----------------------------------------------------------------------------------------
    static void demoGarbageCreation() {
        System.out.println("=== Garbage Creation & Collection ===\n");

        Runtime rt = Runtime.getRuntime();
        rt.gc(); // Best-effort GC to get baseline
        long baseline = rt.totalMemory() - rt.freeMemory();
        System.out.printf("  Baseline heap used: %,d bytes%n", baseline);

        // Create a lot of short-lived objects (typical Young Gen pattern)
        // These will be allocated in Eden and should be collected in Minor GC
        System.out.println("  Creating 1 million short-lived String objects...");
        for (int i = 0; i < 1_000_000; i++) {
            // Each iteration creates a String that immediately becomes garbage
            // (no reference kept). This exercises the Young Gen collector.
            String temp = "garbage-" + i;
        }

        long afterGarbage = rt.totalMemory() - rt.freeMemory();
        System.out.printf("  Heap used after creating garbage: %,d bytes%n", afterGarbage);

        // Request GC — this is advisory only; JVM may ignore it
        System.out.println("  Calling System.gc() (advisory — JVM may ignore)...");
        System.gc();

        long afterGC = rt.totalMemory() - rt.freeMemory();
        System.out.printf("  Heap used after System.gc(): %,d bytes%n", afterGC);
        System.out.printf("  Reclaimed approximately: %,d bytes%n%n", afterGarbage - afterGC);
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 2: Reference types and their GC behavior
    //
    // Interview insight: Understanding reference types is critical for cache implementations
    // and memory-sensitive applications.
    //
    //   Strong > Soft > Weak > Phantom
    //
    //   Strong:   default. Object won't be collected while strongly reachable.
    //   Soft:     collected only when JVM is low on memory. Good for caches.
    //   Weak:     collected at next GC regardless of memory pressure. WeakHashMap uses these.
    //   Phantom:  cannot access the referent. Used for cleanup actions after finalization.
    // -----------------------------------------------------------------------------------------
    static void demoReferenceTypes() {
        System.out.println("=== Reference Types ===\n");

        // Strong reference — object stays alive
        Object strongRef = new Object();
        System.out.println("  Strong ref: " + strongRef + " (won't be GC'd while ref exists)");

        // Soft reference — collected only when memory is low
        SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024 * 1024]); // 1MB
        System.out.println("  Soft ref alive? " + (softRef.get() != null));
        // Soft refs are great for caches: they let the JVM reclaim memory under pressure
        // but keep the data around if memory is plentiful.

        // Weak reference — collected at next GC
        WeakReference<Object> weakRef = new WeakReference<>(new Object());
        System.out.println("  Weak ref before GC: " + (weakRef.get() != null));
        System.gc(); // After GC, weakly-reachable object should be collected
        System.out.println("  Weak ref after GC:  " + (weakRef.get() != null) + " (likely collected)");

        // Phantom reference — used for post-mortem cleanup
        ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
        Object phantomTarget = new Object();
        PhantomReference<Object> phantomRef = new PhantomReference<>(phantomTarget, refQueue);
        // phantomRef.get() ALWAYS returns null — you cannot access the referent
        System.out.println("  Phantom ref.get(): " + phantomRef.get() + " (always null)");
        phantomTarget = null; // Make eligible for GC
        System.gc();
        // After GC, the phantom reference is enqueued in refQueue
        // You would poll refQueue to perform cleanup actions
        System.out.println("  Phantom ref enqueued? " + phantomRef.isEnqueued());
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 3: Object lifecycle through generations
    // -----------------------------------------------------------------------------------------
    static void demoObjectLifecycle() {
        System.out.println("=== Object Lifecycle Through Generations ===\n");

        System.out.println("  Simulating object promotion from Young -> Old Gen:");
        System.out.println("  1. Object allocated in Eden space");
        System.out.println("  2. Survives Minor GC -> moved to Survivor space (S0 or S1)");
        System.out.println("  3. Age incremented each survival (stored in object header, 4 bits)");
        System.out.println("  4. When age >= MaxTenuringThreshold (default 15), promoted to Old Gen");
        System.out.println("  5. Dynamic tenure: if a single age fills >50% of Survivor, that age");
        System.out.println("     and all older objects are promoted immediately.");
        System.out.println();

        // Create long-lived objects that would eventually be promoted
        List<byte[]> longLived = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            longLived.add(new byte[64 * 1024]); // 64KB each
            System.gc(); // Each GC ages the surviving objects
        }
        System.out.println("  Created 10 long-lived objects, each surviving multiple GC cycles.");
        System.out.println("  These would be promoted to Old Gen after reaching tenure threshold.\n");

        // Humongous objects go directly to Old Gen (or Humongous regions in G1)
        System.out.println("  Humongous allocation:");
        byte[] humongous = new byte[4 * 1024 * 1024]; // 4MB — likely > 50% of a G1 region
        System.out.println("  Allocated 4MB array — goes directly to Old Gen (or Humongous region in G1).");
        System.out.println("  In G1, humongous = object > 50% of region size.\n");
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 4: Cleaner as replacement for finalize()
    // -----------------------------------------------------------------------------------------
    static void demoCleanerReplacement() {
        System.out.println("=== Cleaner (Modern Replacement for finalize()) ===\n");

        // finalize() is deprecated. Use Cleaner instead.
        // Cleaner runs cleanup actions when the object becomes phantom-reachable.
        // Unlike finalize(), Cleaner:
        //   - Does not resurrect the object
        //   - Can use multiple cleaner threads
        //   - Is more predictable

        System.out.println("  // Example pattern using Cleaner:");
        System.out.println("  // ");
        System.out.println("  // private static final Cleaner CLEANER = Cleaner.create();");
        System.out.println("  // ");
        System.out.println("  // public class ManagedResource implements AutoCloseable {");
        System.out.println("  //     private final Cleaner.Cleanable cleanable;");
        System.out.println("  //     private final ResourceState state; // must NOT reference 'this'");
        System.out.println("  // ");
        System.out.println("  //     ManagedResource() {");
        System.out.println("  //         state = new ResourceState();");
        System.out.println("  //         cleanable = CLEANER.register(this, state);");
        System.out.println("  //     }");
        System.out.println("  // ");
        System.out.println("  //     @Override public void close() { cleanable.clean(); }");
        System.out.println("  // ");
        System.out.println("  //     static class ResourceState implements Runnable {");
        System.out.println("  //         @Override public void run() { /* release native resource */ }");
        System.out.println("  //     }");
        System.out.println("  // }");
        System.out.println();

        // Important: the cleaning action (ResourceState) must NOT hold a reference to
        // the registered object, or it will never become phantom-reachable.
        System.out.println("  KEY RULE: The cleaning action must NOT reference the object being cleaned.");
        System.out.println("  That's why we use a static inner class, not a lambda capturing 'this'.\n");
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 5: GC roots demonstration
    // -----------------------------------------------------------------------------------------

    // Static field — this IS a GC root
    private static Object staticRoot = new Object();

    static void demoGCRoots() {
        System.out.println("=== GC Roots Demo ===\n");

        // 1. Local variable on thread stack — GC root
        Object localVar = new Object();
        System.out.println("  1. Local variable 'localVar': " + localVar + " (GC root: stack frame)");

        // 2. Static field — GC root (reachable through loaded class)
        System.out.println("  2. Static field 'staticRoot': " + staticRoot + " (GC root: static field)");

        // 3. Active thread — GC root
        Thread currentThread = Thread.currentThread();
        System.out.println("  3. Active thread: " + currentThread.getName() + " (GC root: active thread)");

        // 4. Synchronized monitor — GC root
        Object lockObj = new Object();
        synchronized (lockObj) {
            System.out.println("  4. Lock object: " + lockObj + " (GC root: monitor)");
        }

        System.out.println();
        System.out.println("  When an object is reachable from any GC root via any chain of");
        System.out.println("  references, it is 'alive' and will NOT be collected.");
        System.out.println("  Unreachable objects are garbage and eligible for collection.\n");
    }

    // -----------------------------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          GARBAGE COLLECTION — INTERVIEW DEMO                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Create garbage and see it collected
        demoGarbageCreation();

        // 2. Reference types: Strong, Soft, Weak, Phantom
        demoReferenceTypes();

        // 3. Object lifecycle through generational spaces
        demoObjectLifecycle();

        // 4. Cleaner as modern replacement for finalize()
        demoCleanerReplacement();

        // 5. GC roots
        demoGCRoots();

        System.out.println("=== Key Takeaways for Interviews ===");
        System.out.println("  1. Generational hypothesis: most objects die young -> Young Gen GC is fast.");
        System.out.println("  2. GC roots: stack locals, static fields, threads, monitors, JNI refs.");
        System.out.println("  3. G1 is default since Java 9. Region-based, targets pause time goals.");
        System.out.println("  4. ZGC for sub-ms pauses on large heaps. Shenandoah is the OpenJDK alternative.");
        System.out.println("  5. finalize() is dead. Use Cleaner, try-with-resources, or PhantomReference.");
        System.out.println("  6. Reference types: Strong > Soft > Weak > Phantom.");
        System.out.println();
        System.out.println("  Run with: java -Xlog:gc* -XX:+UseG1GC interview.level6_jvm_internals.gc.GarbageCollectionDemo");
    }
}
