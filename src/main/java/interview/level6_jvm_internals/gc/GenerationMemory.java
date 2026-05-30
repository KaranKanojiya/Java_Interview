package interview.level6_jvm_internals.gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Q13. What is the difference between Young and Old generation?
 *
 * JVM Heap Layout (Generational GC):
 *
 *   ┌─────────────────────────────────────────────────┐
 *   │                    HEAP                          │
 *   │  ┌──────────────────────┐  ┌──────────────────┐ │
 *   │  │   Young Generation   │  │  Old Generation  │ │
 *   │  │  ┌─────┐ ┌────────┐ │  │  (Tenured)       │ │
 *   │  │  │Eden │ │Survivor│ │  │                   │ │
 *   │  │  │     │ │ S0  S1 │ │  │                   │ │
 *   │  │  └─────┘ └────────┘ │  │                   │ │
 *   │  └──────────────────────┘  └──────────────────┘ │
 *   └─────────────────────────────────────────────────┘
 *
 * Young Generation (default ~1/3 of heap):
 *   - Eden: where NEW objects are allocated
 *   - Survivor (S0/S1): objects that survived at least one minor GC
 *   - GC type: Minor GC (fast, stop-the-world but short pause)
 *   - Most objects die here (weak generational hypothesis: most objects are short-lived)
 *
 * Old Generation (default ~2/3 of heap):
 *   - Objects promoted from Young after surviving N GC cycles (default N=15)
 *   - Large objects may go directly to Old (exceeding -XX:PretenureSizeThreshold)
 *   - GC type: Major GC / Full GC (slower, longer pauses)
 *
 * Object lifecycle:
 *   1. new Object() → allocated in Eden
 *   2. Eden fills up → Minor GC triggered
 *   3. Surviving objects → copied to Survivor (S0 or S1, alternating)
 *   4. Object age incremented each Minor GC it survives
 *   5. Age reaches threshold (default 15) → promoted to Old Gen
 *   6. Old Gen fills up → Major GC (or Full GC) triggered
 *
 * | Feature        | Young Gen           | Old Gen              |
 * |---------------|---------------------|----------------------|
 * | Object age    | 0 to threshold      | > threshold          |
 * | GC frequency  | Frequent            | Infrequent           |
 * | GC pause      | Short (ms)          | Long (seconds)       |
 * | GC algorithm  | Copying collector   | Mark-Compact/Sweep   |
 * | Size          | ~1/3 heap           | ~2/3 heap            |
 */
public class GenerationMemory {

    public static void main(String[] args) {

        System.out.println("=== JVM Memory Pools ===");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            long used = pool.getUsage().getUsed() / 1024;
            long max = pool.getUsage().getMax() / 1024;
            String maxStr = max < 0 ? "unlimited" : max + "KB";
            System.out.printf("  %-30s type=%-10s used=%dKB  max=%s%n",
                    pool.getName(), pool.getType(), used, maxStr);
        }

        System.out.println("\n=== Garbage Collectors ===");
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("  %-25s collections=%d  time=%dms  pools=%s%n",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime(),
                    String.join(", ", gc.getMemoryPoolNames()));
        }

        // === Trigger Minor GC by filling Eden ===
        System.out.println("\n=== Triggering Minor GCs (watch young gen) ===");
        List<byte[]> survivors = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            // Short-lived objects → die in Eden (collected by Minor GC)
            byte[] shortLived = new byte[100_000];

            // Every 10th object survives → eventually promotes to Old Gen
            if (i % 10 == 0) {
                survivors.add(new byte[100_000]);
            }
        }

        System.out.println("Created 50 objects (5 kept alive → will promote to Old Gen)");

        // === GC stats after allocation ===
        System.out.println("\n=== GC Stats After Allocation ===");
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("  %-25s collections=%d  time=%dms%n",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }

        survivors.clear();

        // === JVM flags ===
        System.out.println("\n=== Key JVM Flags ===");
        System.out.println("-Xmn256m                       → Young Gen size");
        System.out.println("-XX:NewRatio=2                  → Old:Young = 2:1 (default)");
        System.out.println("-XX:SurvivorRatio=8             → Eden:S0:S1 = 8:1:1 (default)");
        System.out.println("-XX:MaxTenuringThreshold=15     → promotions after 15 Minor GCs (default)");
        System.out.println("-XX:+UseAdaptiveSizePolicy      → JVM auto-tunes gen sizes (default on)");
        System.out.println("-XX:PretenureSizeThreshold=1m   → objects > 1MB go directly to Old");

        System.out.println("\n=== GC Algorithm by Generation ===");
        System.out.println("G1GC (default Java 9+): Both gens, region-based, predictable pauses");
        System.out.println("ZGC  (Java 15+):        No generational distinction (until Gen ZGC in 21)");
        System.out.println("Shenandoah:             Concurrent, low-pause, no generational distinction");
    }
}
