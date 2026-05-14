package interview.level6_jvm_internals.profiling;

// LEVEL: Staff / Principal

/*
 * =============================================================================================
 * PROFILING & DIAGNOSTICS TOOLS — INTERVIEW DEEP DIVE
 * =============================================================================================
 *
 * Q: "How do you diagnose a production performance issue?"
 * A: Systematic approach:
 *
 *    1. OBSERVE — What symptom are you seeing?
 *       - High CPU?          -> Thread dump + CPU profile
 *       - High memory/OOM?   -> Heap dump + GC logs
 *       - Slow responses?    -> Thread dump + latency tracing
 *       - Deadlock?          -> Thread dump
 *       - High GC pauses?    -> GC logs + heap analysis
 *
 *    2. GATHER DATA (non-invasive first)
 *       - GC logs: -Xlog:gc*:file=gc.log (should ALWAYS be on in production)
 *       - JFR: jcmd <pid> JFR.start duration=60s filename=profile.jfr
 *       - Thread dump: jcmd <pid> Thread.print (or kill -3 on Linux)
 *       - jstat: jstat -gcutil <pid> 1000 (GC stats every second)
 *
 *    3. ANALYZE
 *       - JFR files: open in JDK Mission Control (JMC)
 *       - Heap dumps: open in Eclipse MAT
 *       - Thread dumps: use fastthread.io or TDA (Thread Dump Analyzer)
 *       - GC logs: use GCEasy or GCViewer
 *
 *    4. HYPOTHESIZE & FIX
 *       - Apply fix, measure again, verify improvement
 *
 * =============================================================================================
 * Q: "What tools do you use for JVM profiling?"
 * A:
 *
 *    ┌──────────────────┬───────────────────────────────────────────────────────────┐
 *    │ Tool              │ Purpose                                                  │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ JFR               │ Java Flight Recorder. Low-overhead, always-on production │
 *    │ (Java Flight      │ profiling. Records events: GC, allocation, I/O, locks,   │
 *    │  Recorder)        │ CPU, threads, exceptions, class loading.                 │
 *    │                  │ Built into the JDK since Java 11 (free).                 │
 *    │                  │ Start: jcmd <pid> JFR.start duration=60s                 │
 *    │                  │ Analyze: JDK Mission Control (JMC)                       │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ jcmd              │ Swiss-army knife for JVM diagnostics.                    │
 *    │                  │ Send commands to running JVMs.                           │
 *    │                  │ Examples: Thread.print, GC.heap_dump, VM.flags,          │
 *    │                  │ JFR.start, VM.native_memory, Compiler.codecache          │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ jmap              │ Heap inspection. Dump heap, histogram of objects.        │
 *    │                  │ jmap -dump:format=b,file=heap.hprof <pid>                │
 *    │                  │ jmap -histo <pid> (object histogram without full dump)   │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ jstack            │ Thread dump. Shows all thread stack traces.              │
 *    │                  │ Detects deadlocks automatically.                         │
 *    │                  │ jstack <pid>                                             │
 *    │                  │ jstack -l <pid> (with lock info)                         │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ jstat             │ JVM statistics monitoring.                               │
 *    │                  │ jstat -gcutil <pid> 1000 (GC stats every 1s)             │
 *    │                  │ jstat -class <pid> (class loading stats)                 │
 *    │                  │ jstat -compiler <pid> (JIT compilation stats)            │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ jvisualvm         │ GUI tool for monitoring, profiling, heap dump analysis.  │
 *    │                  │ Not bundled since Java 9; download separately.            │
 *    │                  │ Good for development; too invasive for production.        │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ async-profiler    │ Open-source, low-overhead sampling profiler.             │
 *    │                  │ CPU, allocation, lock, and wall-clock profiling.          │
 *    │                  │ Generates flame graphs. Safe for production.             │
 *    │                  │ Uses perf_events (Linux) or DTrace (macOS).              │
 *    │                  │ github.com/async-profiler/async-profiler                 │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ Eclipse MAT       │ Memory Analyzer Tool. Best tool for heap dump analysis.  │
 *    │                  │ Leak suspects, dominator tree, OQL queries, histograms.  │
 *    │                  │ eclipse.dev/mat/                                         │
 *    ├──────────────────┼───────────────────────────────────────────────────────────┤
 *    │ arthas            │ Alibaba's Java diagnostic tool. Attach to live JVM.      │
 *    │                  │ Decompile classes, watch method calls, trace latency.     │
 *    │                  │ arthas.aliyun.com                                        │
 *    └──────────────────┴───────────────────────────────────────────────────────────┘
 *
 * =============================================================================================
 * Q: "What is Java Flight Recorder and when would you use it?"
 * A: JFR is a low-overhead (<2%) event recording framework built into the JDK.
 *    It records over 200+ event types: CPU, GC, I/O, allocation, lock contention,
 *    exceptions, class loading, JIT compilation, thread lifecycle, etc.
 *
 *    When to use:
 *      - ALWAYS in production (enable continuous recording with a rolling buffer)
 *      - During load testing (record full profile)
 *      - When investigating a performance regression
 *      - Post-mortem analysis (if JFR was running when the incident occurred)
 *
 *    How to start:
 *      - At JVM start: -XX:StartFlightRecording=duration=24h,maxsize=1g,filename=app.jfr
 *      - Attach to running JVM: jcmd <pid> JFR.start duration=60s filename=profile.jfr
 *      - Programmatically: jdk.jfr.Recording API
 *
 *    How to analyze:
 *      - JDK Mission Control (JMC): GUI, the gold standard
 *      - jfr CLI tool: jfr print --events jdk.GCPausePhase recording.jfr
 *      - Programmatically: jdk.jfr.consumer.RecordingFile API
 *
 * =============================================================================================
 * Q: "How do you take a heap dump?"
 * A: Multiple ways:
 *      1. jcmd <pid> GC.heap_dump /tmp/heap.hprof        (preferred — uses attach API)
 *      2. jmap -dump:format=b,file=/tmp/heap.hprof <pid>  (older approach)
 *      3. -XX:+HeapDumpOnOutOfMemoryError                  (automatic on OOM)
 *      4. JFR: jcmd <pid> JFR.start settings=profile       (includes allocation data)
 *      5. Programmatically: HotSpotDiagnosticMXBean.dumpHeap()
 *
 *    NOTE: Heap dump pauses the JVM! On a large heap (>10GB), this can take minutes.
 *    Consider using JFR allocation profiling instead for large heaps.
 *
 * =============================================================================================
 * Q: "How do you take a thread dump?"
 * A: Multiple ways:
 *      1. jcmd <pid> Thread.print                        (preferred)
 *      2. jstack <pid>                                    (with -l for lock info)
 *      3. kill -3 <pid> (Linux/Mac)                       (prints to stdout/stderr)
 *      4. Programmatically: ThreadMXBean.dumpAllThreads()
 *      5. JFR: records thread events continuously
 *
 *    What to look for:
 *      - Deadlocks (jstack detects and reports them automatically)
 *      - Many threads BLOCKED on the same monitor -> contention
 *      - Many threads WAITING -> possibly leaked threads
 *      - Threads stuck in I/O -> slow external dependency
 *      - Thread pool exhaustion (all worker threads busy)
 *
 * =============================================================================================
 * Q: "What is the difference between sampling and instrumentation profiling?"
 * A:
 *    Sampling:
 *      - Periodically capture stack traces of all threads
 *      - Low overhead (<5%), safe for production
 *      - May miss very short methods
 *      - Examples: JFR, async-profiler, jvisualvm sampler
 *
 *    Instrumentation:
 *      - Inject bytecode into methods to measure exact timing
 *      - High overhead (10-100x slowdown possible)
 *      - 100% accurate but changes the behavior being measured
 *      - NOT safe for production
 *      - Examples: jvisualvm profiler mode, YourKit instrumentation
 *
 *    RULE: Always use sampling in production. Use instrumentation only in dev/test.
 *
 * =============================================================================================
 */

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ProfilingToolsOverview {

    // -----------------------------------------------------------------------------------------
    // DEMO 1: Runtime memory stats — the simplest monitoring available
    // -----------------------------------------------------------------------------------------
    static void demoRuntimeMemoryStats() {
        System.out.println("=== Runtime Memory Stats ===\n");

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();           // -Xmx limit
        long totalMemory = rt.totalMemory();       // Currently allocated heap
        long freeMemory = rt.freeMemory();         // Free within allocated heap
        long usedMemory = totalMemory - freeMemory;

        System.out.printf("  Max memory (-Xmx):       %,12d bytes  (%.1f MB)%n",
                maxMemory, maxMemory / 1_048_576.0);
        System.out.printf("  Total memory (current):  %,12d bytes  (%.1f MB)%n",
                totalMemory, totalMemory / 1_048_576.0);
        System.out.printf("  Free memory:             %,12d bytes  (%.1f MB)%n",
                freeMemory, freeMemory / 1_048_576.0);
        System.out.printf("  Used memory:             %,12d bytes  (%.1f MB)%n",
                usedMemory, usedMemory / 1_048_576.0);
        System.out.printf("  Heap utilization:        %.1f%%%n%n",
                (usedMemory * 100.0) / maxMemory);

        // Detailed memory via MXBean
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

        System.out.printf("  MXBean Heap:     init=%,d  used=%,d  committed=%,d  max=%,d%n",
                heap.getInit(), heap.getUsed(), heap.getCommitted(), heap.getMax());
        System.out.printf("  MXBean Non-Heap: init=%,d  used=%,d  committed=%,d  max=%,d%n%n",
                nonHeap.getInit(), nonHeap.getUsed(), nonHeap.getCommitted(), nonHeap.getMax());
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 2: GC statistics
    // -----------------------------------------------------------------------------------------
    static void demoGCStats() {
        System.out.println("=== GC Statistics ===\n");

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalCollections = 0;
        long totalTime = 0;

        for (GarbageCollectorMXBean gc : gcBeans) {
            System.out.printf("  %-30s  collections: %4d  time: %6d ms%n",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
            totalCollections += gc.getCollectionCount();
            totalTime += gc.getCollectionTime();
        }

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime();
        double gcOverhead = (uptime > 0) ? (totalTime * 100.0 / uptime) : 0;

        System.out.printf("%n  Total: %d collections, %d ms total GC time%n", totalCollections, totalTime);
        System.out.printf("  JVM uptime: %d ms%n", uptime);
        System.out.printf("  GC overhead: %.2f%% (should be < 5%% in production)%n%n", gcOverhead);
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 3: Thread information
    // -----------------------------------------------------------------------------------------
    static void demoThreadInfo() {
        System.out.println("=== Thread Information ===\n");

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        System.out.printf("  Current thread count:  %d%n", threadBean.getThreadCount());
        System.out.printf("  Peak thread count:     %d%n", threadBean.getPeakThreadCount());
        System.out.printf("  Daemon thread count:   %d%n", threadBean.getDaemonThreadCount());
        System.out.printf("  Total started threads: %d%n%n", threadBean.getTotalStartedThreadCount());

        // Deadlock detection
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        if (deadlockedThreads != null) {
            System.out.println("  WARNING: DEADLOCK DETECTED!");
            for (long id : deadlockedThreads) {
                ThreadInfo info = threadBean.getThreadInfo(id);
                System.out.println("    Thread: " + info.getThreadName());
            }
        } else {
            System.out.println("  No deadlocks detected.");
        }
        System.out.println();

        // Show thread states summary
        ThreadInfo[] allThreads = threadBean.dumpAllThreads(false, false);
        int runnable = 0, waiting = 0, blocked = 0, timedWaiting = 0;
        for (ThreadInfo ti : allThreads) {
            switch (ti.getThreadState()) {
                case RUNNABLE -> runnable++;
                case WAITING -> waiting++;
                case BLOCKED -> blocked++;
                case TIMED_WAITING -> timedWaiting++;
                default -> { }
            }
        }
        System.out.println("  Thread state summary:");
        System.out.printf("    RUNNABLE:      %d%n", runnable);
        System.out.printf("    WAITING:       %d%n", waiting);
        System.out.printf("    TIMED_WAITING: %d%n", timedWaiting);
        System.out.printf("    BLOCKED:       %d  (if high, indicates lock contention)%n%n", blocked);
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 4: JVM info and uptime
    // -----------------------------------------------------------------------------------------
    static void demoJvmInfo() {
        System.out.println("=== JVM Information ===\n");

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        System.out.println("  JVM Name:    " + runtimeBean.getVmName());
        System.out.println("  JVM Vendor:  " + runtimeBean.getVmVendor());
        System.out.println("  JVM Version: " + runtimeBean.getVmVersion());
        System.out.println("  Spec Version: " + runtimeBean.getSpecVersion());

        long uptimeMs = runtimeBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeMs);
        System.out.printf("  Uptime: %dh %dm %ds%n",
                uptime.toHours(), uptime.toMinutesPart(), uptime.toSecondsPart());

        Instant startTime = Instant.ofEpochMilli(runtimeBean.getStartTime());
        System.out.println("  Start time: " + startTime);

        System.out.println("  Classpath: " + runtimeBean.getClassPath().substring(0,
                Math.min(100, runtimeBean.getClassPath().length())) + "...");
        System.out.println("  PID: " + ProcessHandle.current().pid());
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 5: Common jcmd commands reference
    // -----------------------------------------------------------------------------------------
    static void showJcmdReference() {
        System.out.println("=== jcmd Command Reference ===\n");

        long pid = ProcessHandle.current().pid();

        System.out.println("  jcmd is the preferred tool for JVM diagnostics. Key commands:\n");

        System.out.println("  # List all Java processes");
        System.out.println("    jcmd -l\n");

        System.out.println("  # List available commands for a process");
        System.out.printf("    jcmd %d help%n%n", pid);

        System.out.println("  # Thread dump (replaces jstack)");
        System.out.printf("    jcmd %d Thread.print%n%n", pid);

        System.out.println("  # Heap dump (replaces jmap)");
        System.out.printf("    jcmd %d GC.heap_dump /tmp/heap.hprof%n%n", pid);

        System.out.println("  # Object histogram (quick heap overview without full dump)");
        System.out.printf("    jcmd %d GC.class_histogram%n%n", pid);

        System.out.println("  # Force GC");
        System.out.printf("    jcmd %d GC.run%n%n", pid);

        System.out.println("  # Show VM flags");
        System.out.printf("    jcmd %d VM.flags%n%n", pid);

        System.out.println("  # Show system properties");
        System.out.printf("    jcmd %d VM.system_properties%n%n", pid);

        System.out.println("  # Start JFR recording (60 seconds)");
        System.out.printf("    jcmd %d JFR.start duration=60s filename=profile.jfr%n%n", pid);

        System.out.println("  # Check JFR recording status");
        System.out.printf("    jcmd %d JFR.check%n%n", pid);

        System.out.println("  # Dump JFR recording");
        System.out.printf("    jcmd %d JFR.dump filename=profile.jfr%n%n", pid);

        System.out.println("  # Native memory tracking (requires -XX:NativeMemoryTracking=summary at startup)");
        System.out.printf("    jcmd %d VM.native_memory summary%n%n", pid);

        System.out.println("  # Code cache stats");
        System.out.printf("    jcmd %d Compiler.codecache%n%n", pid);
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 6: Other CLI tools reference
    // -----------------------------------------------------------------------------------------
    static void showOtherToolsReference() {
        System.out.println("=== Other CLI Tools Reference ===\n");

        long pid = ProcessHandle.current().pid();

        System.out.println("  jstat — JVM statistics monitoring (lightweight, text-based)");
        System.out.printf("    jstat -gcutil %d 1000      # GC stats every 1s%n", pid);
        System.out.printf("    jstat -gccapacity %d        # GC capacity details%n", pid);
        System.out.printf("    jstat -class %d             # Class loading stats%n", pid);
        System.out.printf("    jstat -compiler %d          # JIT compilation stats%n%n", pid);

        System.out.println("  jstat -gcutil output columns:");
        System.out.println("    S0   — Survivor space 0 utilization (%%)");
        System.out.println("    S1   — Survivor space 1 utilization (%%)");
        System.out.println("    E    — Eden utilization (%%)");
        System.out.println("    O    — Old generation utilization (%%)");
        System.out.println("    M    — Metaspace utilization (%%)");
        System.out.println("    YGC  — Young generation GC count");
        System.out.println("    YGCT — Young generation GC time (seconds)");
        System.out.println("    FGC  — Full GC count");
        System.out.println("    FGCT — Full GC time (seconds)");
        System.out.println("    GCT  — Total GC time (seconds)\n");

        System.out.println("  jmap — Heap inspection");
        System.out.printf("    jmap -dump:format=b,file=heap.hprof %d    # Full heap dump%n", pid);
        System.out.printf("    jmap -histo %d                             # Object histogram%n%n", pid);

        System.out.println("  jstack — Thread dump");
        System.out.printf("    jstack %d                  # Thread dump%n", pid);
        System.out.printf("    jstack -l %d               # With lock info%n%n", pid);

        System.out.println("  jfr — JFR recording CLI (Java 14+)");
        System.out.println("    jfr print recording.jfr");
        System.out.println("    jfr print --events jdk.GarbageCollection recording.jfr");
        System.out.println("    jfr summary recording.jfr");
        System.out.println("    jfr metadata recording.jfr\n");

        System.out.println("  async-profiler (third-party, recommended for production)");
        System.out.printf("    ./asprof -d 30 -f profile.html %d          # CPU flame graph%n", pid);
        System.out.printf("    ./asprof -e alloc -d 30 -f alloc.html %d   # Allocation profile%n", pid);
        System.out.printf("    ./asprof -e lock -d 30 -f locks.html %d    # Lock contention%n%n", pid);
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 7: Programmatic heap dump (using HotSpotDiagnosticMXBean)
    // -----------------------------------------------------------------------------------------
    static void showProgrammaticHeapDump() {
        System.out.println("=== Programmatic Heap Dump ===\n");

        System.out.println("  You can trigger a heap dump from code using MXBeans:");
        System.out.println();
        System.out.println("    import javax.management.MBeanServer;");
        System.out.println("    import java.lang.management.ManagementFactory;");
        System.out.println("    import com.sun.management.HotSpotDiagnosticMXBean;");
        System.out.println();
        System.out.println("    public static void dumpHeap(String filePath, boolean live) {");
        System.out.println("        MBeanServer server = ManagementFactory.getPlatformMBeanServer();");
        System.out.println("        HotSpotDiagnosticMXBean bean = ManagementFactory.newPlatformMXBeanProxy(");
        System.out.println("            server, \"com.sun.management:type=HotSpotDiagnostic\",");
        System.out.println("            HotSpotDiagnosticMXBean.class);");
        System.out.println("        bean.dumpHeap(filePath, live); // live=true forces GC first");
        System.out.println("    }");
        System.out.println();
        System.out.println("  This is useful for building diagnostic endpoints in web services:");
        System.out.println("  GET /admin/heap-dump -> triggers heap dump and returns file path.");
        System.out.println("  Spring Boot Actuator provides this via /actuator/heapdump.\n");
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 8: Production monitoring checklist
    // -----------------------------------------------------------------------------------------
    static void showProductionChecklist() {
        System.out.println("=== Production Monitoring Checklist ===\n");

        System.out.println("  MUST-HAVE JVM flags for production:");
        System.out.println("    -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("    -XX:HeapDumpPath=/var/log/app/");
        System.out.println("    -Xlog:gc*:file=/var/log/app/gc.log:time,uptime,level,tags:filecount=5,filesize=50m");
        System.out.println("    -XX:+AlwaysPreTouch");
        System.out.println("    -Xms = -Xmx (avoid heap resizing)");
        System.out.println();

        System.out.println("  RECOMMENDED continuous JFR recording:");
        System.out.println("    -XX:StartFlightRecording=disk=true,maxage=24h,maxsize=1g,dumponexit=true,");
        System.out.println("     filename=/var/log/app/flight.jfr");
        System.out.println();

        System.out.println("  METRICS to export (to Prometheus/Grafana/Datadog):");
        System.out.println("    - Heap used / committed / max");
        System.out.println("    - GC pause times (p50, p99, max)");
        System.out.println("    - GC frequency (young GC/min, full GC/hour)");
        System.out.println("    - Thread count (total, blocked, waiting)");
        System.out.println("    - CPU usage (system + user)");
        System.out.println("    - Class loading count");
        System.out.println("    - JIT compilation time");
        System.out.println("    - Direct buffer memory");
        System.out.println();

        System.out.println("  ALERTS to set up:");
        System.out.println("    - Heap usage > 80% for sustained period -> potential leak");
        System.out.println("    - Full GC count > 0 in last hour -> investigate");
        System.out.println("    - GC overhead > 5% -> heap sizing issue");
        System.out.println("    - Thread count > expected max -> thread leak");
        System.out.println("    - Blocked thread count > 10 -> lock contention");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║        PROFILING & DIAGNOSTICS — INTERVIEW DEMO              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Runtime memory stats
        demoRuntimeMemoryStats();

        // 2. GC statistics
        demoGCStats();

        // 3. Thread information and deadlock detection
        demoThreadInfo();

        // 4. JVM info
        demoJvmInfo();

        // 5. jcmd command reference
        showJcmdReference();

        // 6. Other CLI tools
        showOtherToolsReference();

        // 7. Programmatic heap dump
        showProgrammaticHeapDump();

        // 8. Production checklist
        showProductionChecklist();

        System.out.println("=== Key Takeaways for Interviews ===");
        System.out.println("  1. JFR is the gold standard for production profiling (low overhead, always-on).");
        System.out.println("  2. jcmd is the Swiss-army knife — prefer it over jmap/jstack/jstat.");
        System.out.println("  3. Always enable GC logging and HeapDumpOnOutOfMemoryError in production.");
        System.out.println("  4. Heap dump -> Eclipse MAT for leak analysis (dominator tree + GC root path).");
        System.out.println("  5. Thread dump -> look for BLOCKED threads (contention) and deadlocks.");
        System.out.println("  6. async-profiler produces flame graphs — best for CPU/allocation analysis.");
        System.out.println("  7. Sampling profiling for production; instrumentation only in dev/test.");
    }
}
