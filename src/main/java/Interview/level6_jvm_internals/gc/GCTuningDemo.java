package interview.level6_jvm_internals.gc;

// LEVEL: Staff / Principal

/*
 * =============================================================================================
 * GC TUNING — INTERVIEW DEEP DIVE
 * =============================================================================================
 *
 * Q: "How do you tune GC for a latency-sensitive application?"
 * A: Step-by-step approach:
 *
 *    1. MEASURE FIRST — Never tune blindly.
 *       - Enable GC logging: -Xlog:gc*:file=gc.log:time,uptime,level,tags
 *       - Use tools: GCViewer, GCEasy (gceasy.io), JFR + JMC
 *       - Establish baseline: p99 pause times, throughput, allocation rate
 *
 *    2. RIGHT-SIZE THE HEAP
 *       - -Xms = -Xmx (avoid heap resizing at runtime)
 *       - Too small -> frequent GC; too large -> long pauses (for non-concurrent GCs)
 *       - Rule of thumb: 3-4x live data set size
 *
 *    3. CHOOSE THE RIGHT COLLECTOR
 *       - Throughput (batch): Parallel GC
 *       - Balanced latency: G1 (default)
 *       - Ultra-low latency: ZGC or Shenandoah
 *
 *    4. TUNE THE CHOSEN COLLECTOR (G1 example):
 *       - -XX:MaxGCPauseMillis=100  (tighter target)
 *       - -XX:G1HeapRegionSize=N    (power of 2, 1MB-32MB)
 *       - -XX:InitiatingHeapOccupancyPercent=45  (when to start concurrent marking)
 *       - Avoid Full GC at all costs (indicates tuning failure)
 *
 *    5. REDUCE ALLOCATION RATE
 *       - Object pooling for heavy objects (but beware of complexity)
 *       - Avoid autoboxing in hot paths
 *       - Use primitive collections (Eclipse Collections, fastutil)
 *       - Reduce String concatenation (use StringBuilder)
 *
 * =============================================================================================
 * KEY JVM FLAGS REFERENCE
 * =============================================================================================
 *
 *  HEAP SIZING:
 *    -Xms512m                     Initial heap size
 *    -Xmx4g                      Maximum heap size
 *    -Xmn1g                      Young generation size (alternative to NewRatio)
 *    -XX:NewRatio=2               Old:Young ratio (2 means Old = 2x Young)
 *    -XX:SurvivorRatio=8          Eden:Survivor (8 means Eden = 8x one Survivor)
 *    -XX:MaxMetaspaceSize=512m    Cap Metaspace growth
 *
 *  COLLECTOR SELECTION:
 *    -XX:+UseSerialGC             Serial (single-threaded, STW)
 *    -XX:+UseParallelGC           Parallel (multi-threaded, STW, throughput-focused)
 *    -XX:+UseG1GC                 G1 (default since Java 9)
 *    -XX:+UseZGC                  ZGC (low-latency, Java 15+)
 *    -XX:+UseShenandoahGC         Shenandoah (OpenJDK only)
 *
 *  G1 TUNING:
 *    -XX:MaxGCPauseMillis=200             Target max pause (default 200ms)
 *    -XX:G1HeapRegionSize=4m              Region size (1-32MB, power of 2)
 *    -XX:InitiatingHeapOccupancyPercent=45  Start concurrent marking at this occupancy
 *    -XX:G1ReservePercent=10              Reserve for promotion guarantee
 *    -XX:ConcGCThreads=4                 Concurrent GC thread count
 *    -XX:ParallelGCThreads=8             Parallel (STW) GC thread count
 *
 *  ZGC TUNING:
 *    -XX:+UseZGC                  Enable ZGC
 *    -XX:+ZGenerational           Generational ZGC (default in Java 21+)
 *    -XX:SoftMaxHeapSize=4g       Hint to ZGC to try to stay below this
 *    -XX:ConcGCThreads=4          Concurrent GC threads
 *    (ZGC needs very little tuning — heap size is the main knob)
 *
 *  GC LOGGING (Java 9+ Unified Logging):
 *    -Xlog:gc*                                   All GC logs to stdout
 *    -Xlog:gc*:file=gc.log:time,uptime,level,tags   GC logs to file with timestamps
 *    -Xlog:gc+heap=debug                          Heap details at GC events
 *    -Xlog:gc+age=trace                           Tenuring distribution
 *
 *  GC LOGGING (pre-Java 9, legacy):
 *    -XX:+PrintGCDetails
 *    -XX:+PrintGCDateStamps
 *    -Xloggc:gc.log
 *
 *  OTHER USEFUL FLAGS:
 *    -XX:+HeapDumpOnOutOfMemoryError       Auto heap dump on OOM
 *    -XX:HeapDumpPath=/tmp/heapdump.hprof  Heap dump location
 *    -XX:+AlwaysPreTouch                   Touch all heap pages at startup (avoid lazy alloc)
 *    -XX:+DisableExplicitGC                Ignore System.gc() calls
 *    -XX:NativeMemoryTracking=summary      Track JVM native memory
 *
 * =============================================================================================
 * Q: "How do you read GC logs?"
 * A: Example G1 log entry (Java 17, -Xlog:gc*):
 *
 *    [2.145s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
 *    [2.145s][info][gc,heap] GC(0) Eden: 24M->0M  Survivors: 0M->3M  Old: 0M->12M
 *    [2.145s][info][gc] GC(0) Pause Young (Normal) 24M->15M(256M) 4.567ms
 *
 *    Breakdown:
 *      [2.145s]           — time since JVM start
 *      GC(0)              — GC event number
 *      Pause Young        — type (Young GC, Mixed GC, Full GC)
 *      Eden: 24M->0M      — Eden was 24MB, now 0 (all evacuated)
 *      Survivors: 0M->3M  — 3MB survived to survivor space
 *      Old: 0M->12M       — 12MB promoted to Old Gen
 *      24M->15M(256M)     — heap went from 24MB to 15MB used, out of 256MB total
 *      4.567ms            — pause duration
 *
 *    RED FLAGS in GC logs:
 *      - Frequent Full GC → heap too small or memory leak
 *      - "to-space exhausted" / "to-space overflow" → survivor space too small
 *      - "Humongous allocation" → large objects being allocated
 *      - Pause times increasing over time → possible memory leak
 *      - "concurrent-mark-abort" → heap filling faster than marking can complete
 *
 * =============================================================================================
 * Q: "What's the difference between throughput and latency tuning?"
 * A:
 *    Throughput tuning:                        Latency tuning:
 *    - Maximize work done per unit time         - Minimize pause times
 *    - Parallel GC (stop-the-world is OK)       - G1 / ZGC / Shenandoah
 *    - Larger Young Gen = fewer GCs             - Smaller but more frequent GCs
 *    - Batch processing, ETL, reports           - Web servers, trading systems
 *    - Measure: GC overhead < 5%                - Measure: p99 pause < 10ms
 *
 * =============================================================================================
 * Q: "What are common GC tuning anti-patterns?"
 * A:
 *    1. Tuning before measuring (premature optimization)
 *    2. Setting -Xms != -Xmx (causes heap resizing pauses)
 *    3. Explicitly calling System.gc() (unpredictable, use -XX:+DisableExplicitGC)
 *    4. Over-tuning G1 (it's adaptive — fewer knobs is usually better)
 *    5. Ignoring allocation rate (the real cause of frequent GC)
 *    6. Huge heap with Parallel GC (long STW pauses)
 *    7. Not using -XX:+HeapDumpOnOutOfMemoryError in production
 *
 * =============================================================================================
 */

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

public class GCTuningDemo {

    // -----------------------------------------------------------------------------------------
    // DEMO 1: Show current GC collector(s) in use
    // -----------------------------------------------------------------------------------------
    static void showCurrentGCCollectors() {
        System.out.println("=== Current GC Collectors ===\n");

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcBeans) {
            System.out.printf("  Collector: %-30s Collections: %d  Time: %dms%n",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
            System.out.print("    Memory pools: ");
            for (String pool : gc.getMemoryPoolNames()) {
                System.out.print(pool + "  ");
            }
            System.out.println();
        }
        System.out.println();

        // Interpret the collector names
        String collectors = gcBeans.stream()
                .map(GarbageCollectorMXBean::getName)
                .reduce("", (a, b) -> a + " " + b);
        if (collectors.contains("G1")) {
            System.out.println("  --> Using G1 GC (default since Java 9)");
        } else if (collectors.contains("ZGC")) {
            System.out.println("  --> Using ZGC (low-latency collector)");
        } else if (collectors.contains("PS")) {
            System.out.println("  --> Using Parallel (throughput) GC");
        } else if (collectors.contains("Copy") && collectors.contains("MarkSweepCompact")) {
            System.out.println("  --> Using Serial GC");
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 2: Show JVM flags currently in effect
    // -----------------------------------------------------------------------------------------
    static void showJvmFlags() {
        System.out.println("=== JVM Configuration ===\n");

        Runtime rt = Runtime.getRuntime();
        System.out.printf("  Max Heap (-Xmx):    %,d bytes (%.1f MB)%n",
                rt.maxMemory(), rt.maxMemory() / 1_048_576.0);
        System.out.printf("  Total Heap (current): %,d bytes (%.1f MB)%n",
                rt.totalMemory(), rt.totalMemory() / 1_048_576.0);
        System.out.printf("  Free Heap:          %,d bytes (%.1f MB)%n",
                rt.freeMemory(), rt.freeMemory() / 1_048_576.0);
        System.out.printf("  Processors:         %d%n%n", rt.availableProcessors());

        // Show input arguments (JVM flags)
        List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        System.out.println("  JVM Input Arguments:");
        if (inputArgs.isEmpty()) {
            System.out.println("    (none — using all defaults)");
        } else {
            for (String arg : inputArgs) {
                System.out.println("    " + arg);
            }
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 3: Simulate allocation pressure and monitor GC
    // -----------------------------------------------------------------------------------------
    static void demoAllocationPressure() {
        System.out.println("=== Allocation Pressure Simulation ===\n");

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        // Snapshot GC stats before
        long gcCountBefore = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
        long gcTimeBefore = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();

        // Phase 1: Short-lived allocations (exercises Young Gen GC)
        System.out.println("  Phase 1: 5 million short-lived allocations (Young Gen pressure)...");
        long start = System.nanoTime();
        for (int i = 0; i < 5_000_000; i++) {
            byte[] temp = new byte[256]; // 256 bytes, immediately garbage
        }
        long phase1Time = (System.nanoTime() - start) / 1_000_000;

        long gcCountAfterPhase1 = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
        System.out.printf("  Phase 1 completed in %dms. GC collections: %d%n%n",
                phase1Time, gcCountAfterPhase1 - gcCountBefore);

        // Phase 2: Long-lived allocations (exercises Old Gen, potential Full GC)
        System.out.println("  Phase 2: Growing a list with 100K retained objects (Old Gen pressure)...");
        start = System.nanoTime();
        List<byte[]> retained = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            retained.add(new byte[128]); // These survive GC -> get promoted to Old Gen
        }
        long phase2Time = (System.nanoTime() - start) / 1_000_000;

        long gcCountAfterPhase2 = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
        long gcTimeAfter = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();

        System.out.printf("  Phase 2 completed in %dms. GC collections: %d%n",
                phase2Time, gcCountAfterPhase2 - gcCountAfterPhase1);
        System.out.printf("  Total GC time during both phases: %dms%n%n", gcTimeAfter - gcTimeBefore);

        retained.clear(); // Release to avoid OOM in subsequent demos
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 4: Memory pool utilization — find which pool is under pressure
    // -----------------------------------------------------------------------------------------
    static void demoMemoryPoolUtilization() {
        System.out.println("=== Memory Pool Utilization ===\n");

        System.out.println("  Pool utilization (used / max):");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            long used = pool.getUsage().getUsed();
            long max = pool.getUsage().getMax();
            String utilization;
            if (max > 0) {
                utilization = String.format("%.1f%%", (used * 100.0) / max);
            } else {
                utilization = "unbounded";
            }
            System.out.printf("    %-35s %,12d / %,12d  (%s)%n",
                    pool.getName(), used, max, utilization);
        }
        System.out.println();
        System.out.println("  RED FLAGS:");
        System.out.println("    - Old Gen utilization > 80% at steady state -> possible leak");
        System.out.println("    - Metaspace growing unbounded -> class loader leak");
        System.out.println("    - Code Cache full -> JIT stops compiling, performance drops");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 5: Recommended JVM flags for common scenarios
    // -----------------------------------------------------------------------------------------
    static void showRecommendedFlags() {
        System.out.println("=== Recommended JVM Flags by Scenario ===\n");

        System.out.println("  PRODUCTION WEB SERVICE (latency-sensitive, Java 17+):");
        System.out.println("    -Xms4g -Xmx4g");
        System.out.println("    -XX:+UseZGC");
        System.out.println("    -XX:+AlwaysPreTouch");
        System.out.println("    -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("    -XX:HeapDumpPath=/tmp/heapdump.hprof");
        System.out.println("    -Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=50m");
        System.out.println();

        System.out.println("  PRODUCTION WEB SERVICE (G1, Java 11+):");
        System.out.println("    -Xms4g -Xmx4g");
        System.out.println("    -XX:+UseG1GC");
        System.out.println("    -XX:MaxGCPauseMillis=100");
        System.out.println("    -XX:+AlwaysPreTouch");
        System.out.println("    -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("    -Xlog:gc*:file=gc.log:time,uptime,level,tags");
        System.out.println();

        System.out.println("  BATCH PROCESSING (throughput-focused):");
        System.out.println("    -Xms8g -Xmx8g");
        System.out.println("    -XX:+UseParallelGC");
        System.out.println("    -XX:ParallelGCThreads=8");
        System.out.println("    -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println();

        System.out.println("  CONTAINER / KUBERNETES (memory-constrained):");
        System.out.println("    -XX:MaxRAMPercentage=75.0         (use 75% of container memory)");
        System.out.println("    -XX:InitialRAMPercentage=75.0");
        System.out.println("    -XX:+UseContainerSupport           (default since Java 10)");
        System.out.println("    -XX:+UseG1GC");
        System.out.println("    -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("    Note: Avoid -Xms/-Xmx in containers; use RAM percentage flags.");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              GC TUNING — INTERVIEW DEMO                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Which GC collector is active?
        showCurrentGCCollectors();

        // 2. Current JVM configuration
        showJvmFlags();

        // 3. Allocation pressure test
        demoAllocationPressure();

        // 4. Memory pool utilization
        demoMemoryPoolUtilization();

        // 5. Recommended flags for different scenarios
        showRecommendedFlags();

        System.out.println("=== Key Takeaways for Interviews ===");
        System.out.println("  1. Always measure before tuning. Use GC logs as the primary data source.");
        System.out.println("  2. Set -Xms = -Xmx to avoid runtime heap resizing.");
        System.out.println("  3. G1: tune MaxGCPauseMillis. ZGC: mostly just set heap size.");
        System.out.println("  4. ALWAYS enable -XX:+HeapDumpOnOutOfMemoryError in production.");
        System.out.println("  5. In containers, use -XX:MaxRAMPercentage instead of -Xmx.");
        System.out.println("  6. Monitor: allocation rate, promotion rate, pause times, Full GC frequency.");
    }
}
