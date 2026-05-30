package interview.level6_jvm_internals.memory_leaks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Q11. How do you diagnose an OutOfMemoryError?
 *
 * Step-by-step approach:
 *
 * 1. IDENTIFY the OOM type:
 *    - "Java heap space"       → heap is full (object accumulation)
 *    - "Metaspace"             → too many classes loaded
 *    - "GC overhead limit"     → GC running >98% time, recovering <2% heap
 *    - "unable to create native thread" → too many threads (OS limit)
 *    - "Direct buffer memory"  → NIO direct buffers exhausted
 *
 * 2. CAPTURE heap dump:
 *    - Add JVM flag: -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/
 *    - Manual: jmap -dump:format=b,file=heap.hprof <pid>
 *    - Manual: jcmd <pid> GC.heap_dump /tmp/heap.hprof
 *
 * 3. ANALYZE with tools:
 *    - Eclipse MAT (Memory Analyzer Tool) — find leak suspects, dominator tree
 *    - VisualVM — live monitoring, heap dump analysis
 *    - jhat (deprecated) — web-based heap dump viewer
 *    - IntelliJ Profiler — integrated heap analysis
 *
 * 4. FIND the root cause:
 *    - Look for largest retained objects (dominator tree)
 *    - Check GC roots — what's keeping objects alive?
 *    - Look for collections that grow unbounded (caches, listeners, maps)
 *
 * 5. COMMON fixes:
 *    - Increase heap: -Xmx4g (temporary)
 *    - Fix the leak: unbounded caches, unclosed resources, static collections
 *    - Use WeakReference/SoftReference for caches
 *    - Use try-with-resources for AutoCloseable
 */
public class DiagnoseOOM {

    public static void main(String[] args) {

        // === Check current memory via MemoryMXBean ===
        System.out.println("=== Current Memory Status (MemoryMXBean) ===");
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memBean.getHeapMemoryUsage();

        System.out.println("Heap Init:      " + (heapUsage.getInit() / 1024 / 1024) + " MB");
        System.out.println("Heap Used:      " + (heapUsage.getUsed() / 1024 / 1024) + " MB");
        System.out.println("Heap Committed: " + (heapUsage.getCommitted() / 1024 / 1024) + " MB");
        System.out.println("Heap Max:       " + (heapUsage.getMax() / 1024 / 1024) + " MB");

        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();
        System.out.println("\nNon-Heap Used:  " + (nonHeap.getUsed() / 1024 / 1024) + " MB (Metaspace + code cache)");

        // === Simulating memory growth ===
        System.out.println("\n=== Simulating gradual memory growth ===");
        List<byte[]> leak = new ArrayList<>();
        Runtime rt = Runtime.getRuntime();

        for (int i = 0; i < 20; i++) {
            leak.add(new byte[1024 * 1024]);  // 1MB per iteration
            long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
            long max = rt.maxMemory() / 1024 / 1024;
            System.out.printf("  Iteration %2d: used=%dMB / max=%dMB (%.1f%%)%n",
                    i + 1, used, max, (used * 100.0 / max));
        }
        leak.clear();  // release for demo purposes

        // === Diagnostic commands ===
        System.out.println("\n=== Diagnostic Commands ===");
        System.out.println("--- Capture heap dump ---");
        System.out.println("jmap -dump:format=b,file=heap.hprof <pid>");
        System.out.println("jcmd <pid> GC.heap_dump /tmp/heap.hprof");
        System.out.println("JVM flag: -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/");

        System.out.println("\n--- Live monitoring ---");
        System.out.println("jstat -gc <pid> 1000       → GC stats every 1s");
        System.out.println("jstat -gcutil <pid> 1000    → GC utilization %");
        System.out.println("jcmd <pid> VM.native_memory → native memory tracking");

        System.out.println("\n--- Thread analysis ---");
        System.out.println("jstack <pid>               → thread dump");
        System.out.println("jcmd <pid> Thread.print     → thread dump via jcmd");

        System.out.println("\n--- Class loading ---");
        System.out.println("jcmd <pid> VM.classloaders  → classloader hierarchy");
        System.out.println("jcmd <pid> GC.class_stats   → class metadata stats");

        // === OOM types ===
        System.out.println("\n=== OOM Types and Fixes ===");
        System.out.println("1. 'Java heap space'");
        System.out.println("   → Increase -Xmx, find leak with MAT, check unbounded collections");
        System.out.println("2. 'Metaspace'");
        System.out.println("   → ClassLoader leak, too many dynamic proxies, increase -XX:MaxMetaspaceSize");
        System.out.println("3. 'GC overhead limit exceeded'");
        System.out.println("   → Heap nearly full, GC thrashing. Increase heap or fix leak");
        System.out.println("4. 'unable to create native thread'");
        System.out.println("   → Too many threads. Reduce thread count, increase ulimit -u, reduce -Xss");
        System.out.println("5. 'Direct buffer memory'");
        System.out.println("   → NIO buffers not freed. Increase -XX:MaxDirectMemorySize or fix buffer leak");

        // === JVM flags for production ===
        System.out.println("\n=== Production JVM flags ===");
        System.out.println("-Xms4g -Xmx4g                          → fixed heap (avoid resize pauses)");
        System.out.println("-XX:+HeapDumpOnOutOfMemoryError          → auto heap dump on OOM");
        System.out.println("-XX:HeapDumpPath=/var/log/java/          → heap dump location");
        System.out.println("-XX:+ExitOnOutOfMemoryError              → exit JVM on OOM (let orchestrator restart)");
        System.out.println("-XX:NativeMemoryTracking=summary         → track native memory");
        System.out.println("-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m → GC logging");
    }
}
