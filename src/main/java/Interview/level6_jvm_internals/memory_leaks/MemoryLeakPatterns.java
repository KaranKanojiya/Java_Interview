package interview.level6_jvm_internals.memory_leaks;

// LEVEL: Staff / Principal

/*
 * =============================================================================================
 * MEMORY LEAK PATTERNS — INTERVIEW DEEP DIVE
 * =============================================================================================
 *
 * Q: "How do you identify a memory leak in Java?"
 * A: A memory leak in Java means objects are unintentionally kept reachable (from GC roots),
 *    preventing garbage collection, causing heap usage to grow over time.
 *
 *    Identification steps:
 *      1. MONITOR — Watch heap usage over time (Grafana, JMX, JFR).
 *         Symptom: sawtooth pattern with rising baseline = leak.
 *
 *      2. GC LOGS — Enable -Xlog:gc*. Look for:
 *         - Full GC frequency increasing
 *         - Post-GC heap usage trending upward
 *         - Eventually: OutOfMemoryError: Java heap space
 *
 *      3. HEAP DUMP — Capture with:
 *         - jmap -dump:format=b,file=heap.hprof <pid>
 *         - jcmd <pid> GC.heap_dump heap.hprof
 *         - -XX:+HeapDumpOnOutOfMemoryError (automatic on OOM)
 *
 *      4. ANALYZE — Open heap dump in Eclipse MAT, VisualVM, or YourKit.
 *         - Find the "Leak Suspects" report (MAT does this automatically).
 *         - Look at the "Dominator Tree" — which objects retain the most memory.
 *         - Follow the "GC Root Path" to find WHY objects are retained.
 *         - Look for unexpectedly large collections or growing data structures.
 *
 *      5. COMPARE — Take two heap dumps at different times.
 *         - Diff them: which classes have significantly more instances?
 *         - That delta points to the leak.
 *
 * =============================================================================================
 * Q: "Name 5 common causes of memory leaks in Java."
 * A:
 *    1. Static collections that grow unboundedly
 *    2. Unclosed resources (streams, connections, result sets)
 *    3. Event listeners / callbacks never removed
 *    4. ThreadLocal not cleaned up (especially in thread pools)
 *    5. Inner classes holding implicit reference to outer class
 *    6. String.intern() abuse filling the string pool
 *    7. Custom class loader leaks (common in app servers during redeploy)
 *    8. Caches without eviction policy (unbounded maps)
 *    9. Long-lived sessions in web frameworks
 *   10. Forgotten timers / scheduled tasks holding references
 *
 * =============================================================================================
 * Q: "How does a ThreadLocal leak work?"
 * A: In a thread pool, threads are reused. ThreadLocal values are stored in a map
 *    inside each Thread object (Thread.threadLocals). If you set a ThreadLocal value
 *    but never call remove(), the value persists for the lifetime of the thread
 *    (which in a pool = forever).
 *
 *    Worse: The ThreadLocalMap entry has a WeakReference to the ThreadLocal key,
 *    but a STRONG reference to the value. If the ThreadLocal key is GC'd (no more
 *    strong refs to it), the entry becomes "stale" (key=null) but the value is still
 *    retained. The value is only cleaned up lazily on the next get/set/remove call.
 *    In a thread pool, this may never happen -> leak.
 *
 *    FIX: ALWAYS call threadLocal.remove() in a finally block, especially in
 *    servlet filters, interceptors, or any code running in a thread pool.
 *
 * =============================================================================================
 * Q: "How does an inner class leak work?"
 * A: A non-static inner class holds an implicit reference to its enclosing (outer) class
 *    instance. If the inner class instance outlives the outer class (e.g., registered as
 *    a listener), the outer class cannot be garbage collected.
 *
 *    FIX: Use static inner classes unless you genuinely need access to outer instance fields.
 *    This is why the "Effective Java" rule says: "Prefer static member classes over
 *    non-static" (Item 24).
 *
 * =============================================================================================
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MemoryLeakPatterns {

    // -----------------------------------------------------------------------------------------
    // PATTERN 1: Static Collection — Unbounded Growth
    //
    // A static collection lives for the lifetime of the class (which lives for the
    // lifetime of the class loader, which is typically the entire JVM process).
    // If you keep adding to it without removing, memory grows forever.
    // -----------------------------------------------------------------------------------------

    // BAD: This list grows forever. It's a GC root (static field -> loaded class -> class loader).
    private static final List<Object> staticLeakList = new ArrayList<>();

    static void demoStaticCollectionLeak() {
        System.out.println("=== Pattern 1: Static Collection Leak ===\n");

        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long before = rt.totalMemory() - rt.freeMemory();

        // Simulate adding data that is never removed
        for (int i = 0; i < 10_000; i++) {
            staticLeakList.add(new byte[1024]); // 1KB each, never removed
        }

        long after = rt.totalMemory() - rt.freeMemory();
        System.out.printf("  Added 10,000 entries to static list. Memory delta: %,d bytes%n", after - before);
        System.out.println("  This list is a GC root — entries will NEVER be collected.");
        System.out.println();
        System.out.println("  FIX: Use bounded collections (LRU cache), WeakHashMap,");
        System.out.println("       or explicitly remove entries when no longer needed.");
        System.out.println();

        // Cleanup for the demo
        staticLeakList.clear();
    }

    // -----------------------------------------------------------------------------------------
    // PATTERN 2: Unclosed Resources
    //
    // Streams, connections, and similar resources may hold native memory or file handles.
    // If not closed, they accumulate until the finalizer runs (unreliable) or the
    // process runs out of file descriptors.
    // -----------------------------------------------------------------------------------------
    static void demoUnclosedResourceLeak() {
        System.out.println("=== Pattern 2: Unclosed Resource Leak ===\n");

        // BAD: Stream is not closed. In real code, this could be a database connection,
        // file stream, or network socket.
        System.out.println("  BAD (stream not closed):");
        System.out.println("    InputStream is = new FileInputStream(\"data.txt\");");
        System.out.println("    // ... use is ... but never close it");
        System.out.println("    // File handle leaks! OS has limited file descriptors (ulimit -n).\n");

        // GOOD: try-with-resources guarantees close()
        System.out.println("  GOOD (try-with-resources):");
        System.out.println("    try (InputStream is = new FileInputStream(\"data.txt\")) {");
        System.out.println("        // ... use is ...");
        System.out.println("    } // Automatically closed here, even on exception\n");

        // Live demo with ByteArrayInputStream (safe, no real file)
        try (InputStream is = new ByteArrayInputStream("demo".getBytes())) {
            int data = is.read();
            System.out.println("  Demo: read byte " + data + " from stream (properly closed).");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  Common unclosed resources:");
        System.out.println("    - JDBC Connection, Statement, ResultSet");
        System.out.println("    - FileInputStream / FileOutputStream");
        System.out.println("    - HttpURLConnection / HttpClient");
        System.out.println("    - BufferedReader / BufferedWriter");
        System.out.println("    - Lock.lock() without unlock in finally");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // PATTERN 3: Listener / Callback Not Removed
    //
    // If you register an event listener but never unregister it, the event source
    // holds a strong reference to the listener, preventing GC of the listener
    // and everything it references.
    // -----------------------------------------------------------------------------------------
    interface EventListener {
        void onEvent(String event);
    }

    static class EventSource {
        private final List<EventListener> listeners = new ArrayList<>();

        void addListener(EventListener listener) {
            listeners.add(listener);
        }

        void removeListener(EventListener listener) {
            listeners.remove(listener);
        }

        int getListenerCount() {
            return listeners.size();
        }
    }

    static void demoListenerLeak() {
        System.out.println("=== Pattern 3: Listener Not Removed ===\n");

        EventSource source = new EventSource();

        // Simulate adding listeners without removing them
        for (int i = 0; i < 1000; i++) {
            // Each listener holds a reference to a 10KB byte array
            final byte[] data = new byte[10_240];
            source.addListener(event -> {
                // Use 'data' to prevent optimization
                if (data.length < 0) System.out.println("never");
            });
        }

        System.out.println("  Registered 1,000 listeners (each holding 10KB data).");
        System.out.println("  Listener count: " + source.getListenerCount());
        System.out.println("  None will be GC'd because the EventSource holds strong refs.\n");

        System.out.println("  FIX options:");
        System.out.println("    1. Always removeListener() when done (e.g., in close/dispose).");
        System.out.println("    2. Use WeakReference<EventListener> in the source.");
        System.out.println("    3. Use an event bus with automatic lifecycle management.");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // PATTERN 4: ThreadLocal Not Cleaned
    // -----------------------------------------------------------------------------------------
    private static final ThreadLocal<byte[]> threadLocalData = new ThreadLocal<>();

    static void demoThreadLocalLeak() {
        System.out.println("=== Pattern 4: ThreadLocal Leak ===\n");

        // BAD: Set ThreadLocal but never remove it
        System.out.println("  BAD pattern (in a thread pool / servlet container):");
        System.out.println("    threadLocal.set(heavyObject);");
        System.out.println("    // ... process request ...");
        System.out.println("    // Forgot to call threadLocal.remove()!");
        System.out.println("    // The value persists because pool threads live forever.\n");

        // GOOD: Always remove in a finally block
        System.out.println("  GOOD pattern:");
        System.out.println("    try {");
        System.out.println("        threadLocal.set(heavyObject);");
        System.out.println("        // ... process request ...");
        System.out.println("    } finally {");
        System.out.println("        threadLocal.remove(); // CRITICAL in thread pools!");
        System.out.println("    }\n");

        // Live demo
        try {
            threadLocalData.set(new byte[1024 * 1024]); // 1MB
            System.out.println("  Set 1MB ThreadLocal data.");
            System.out.println("  ThreadLocal data exists: " + (threadLocalData.get() != null));
        } finally {
            threadLocalData.remove(); // Always clean up!
            System.out.println("  After remove(): " + (threadLocalData.get() == null) + " (cleaned up)");
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // PATTERN 5: Non-Static Inner Class Holding Outer Reference
    // -----------------------------------------------------------------------------------------
    static void demoInnerClassLeak() {
        System.out.println("=== Pattern 5: Inner Class Holding Outer Reference ===\n");

        System.out.println("  A non-static inner class holds an implicit reference to its outer");
        System.out.println("  class instance. If the inner class outlives the outer, leak.\n");

        System.out.println("  BAD:");
        System.out.println("    class Server {  // 500MB of state");
        System.out.println("        class Handler implements Runnable {");
        System.out.println("            // Implicitly holds reference to Server.this");
        System.out.println("            public void run() { /* ... */ }");
        System.out.println("        }");
        System.out.println("        void start() {");
        System.out.println("            executor.submit(new Handler());");
        System.out.println("            // Even after Server is 'done', Handler still holds it!");
        System.out.println("        }");
        System.out.println("    }\n");

        System.out.println("  GOOD:");
        System.out.println("    class Server {");
        System.out.println("        static class Handler implements Runnable {");
        System.out.println("            // No implicit outer reference");
        System.out.println("            private final Config config; // only what's needed");
        System.out.println("            Handler(Config config) { this.config = config; }");
        System.out.println("            public void run() { /* ... */ }");
        System.out.println("        }");
        System.out.println("    }\n");

        System.out.println("  ALSO APPLIES TO:");
        System.out.println("    - Anonymous classes (hold outer reference unless in static context)");
        System.out.println("    - Lambdas do NOT hold outer reference unless they capture 'this'");
        System.out.println("    - Method references: Foo::bar does not capture this; this::bar does");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // PATTERN 6: String.intern() Abuse
    // -----------------------------------------------------------------------------------------
    static void demoStringInternLeak() {
        System.out.println("=== Pattern 6: String.intern() Abuse ===\n");

        // String.intern() adds the string to the JVM's string pool.
        // In Java 7+, the string pool is on the heap (not in PermGen), so it's GC'd.
        // But if you intern too many unique strings, the pool grows huge.

        System.out.println("  String.intern() returns the canonical instance from the string pool.");
        System.out.println("  Useful for deduplication, but dangerous with user-generated data.\n");

        // BAD: interning user input or random strings
        System.out.println("  BAD:");
        System.out.println("    for (String userId : userIds) {");
        System.out.println("        cache.put(userId.intern(), userData);");
        System.out.println("        // If millions of unique userIds, string pool grows huge");
        System.out.println("    }\n");

        // GOOD: Use a bounded deduplication map
        System.out.println("  GOOD: Use a bounded ConcurrentHashMap for deduplication, or");
        System.out.println("  use Guava's Interners.newWeakInterner() which uses WeakReferences.");
        System.out.println();

        // Demo: intern a few strings
        String a = new String("intern-demo"); // new String to avoid constant pool
        String b = a.intern();
        String c = "intern-demo"; // constant pool -> same as interned version
        System.out.println("  a == b (new vs interned): " + (a == b)); // false (different objects)
        System.out.println("  b == c (interned vs literal): " + (b == c)); // true (same pool entry)
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // PATTERN 7: Custom ClassLoader Leak
    // -----------------------------------------------------------------------------------------
    static void demoClassLoaderLeak() {
        System.out.println("=== Pattern 7: ClassLoader Leak ===\n");

        System.out.println("  Common in application servers during hot redeploy.");
        System.out.println("  Each deployment creates a new WebAppClassLoader.");
        System.out.println("  If ANY reference leaks from the web app to the parent,");
        System.out.println("  the entire class loader (and all its classes) cannot be GC'd.\n");

        System.out.println("  Common causes of class loader leaks:");
        System.out.println("    1. JDBC drivers registered in DriverManager (static, parent-loaded)");
        System.out.println("    2. ThreadLocal values set by web app code on pool threads");
        System.out.println("    3. Shutdown hooks registered by web app code");
        System.out.println("    4. Static fields in JDK classes referencing web app objects");
        System.out.println("    5. Logging frameworks (log4j) holding references");
        System.out.println("    6. java.beans.Introspector cache");
        System.out.println();
        System.out.println("  Diagnosis: Heap dump -> search for instances of the old ClassLoader.");
        System.out.println("  Follow GC root path to find what's holding it alive.\n");
        System.out.println("  Prevention:");
        System.out.println("    - Deregister JDBC drivers in contextDestroyed()");
        System.out.println("    - Clear ThreadLocals in servlet filter");
        System.out.println("    - Call Introspector.flushCaches() on undeploy");
        System.out.println("    - Use Tomcat's memory leak detection (logs warnings)");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // BONUS: WeakHashMap — GC-friendly cache (but has caveats)
    // -----------------------------------------------------------------------------------------
    static void demoWeakHashMapBehavior() {
        System.out.println("=== Bonus: WeakHashMap Behavior ===\n");

        WeakHashMap<Object, String> weakMap = new WeakHashMap<>();

        // Keys are weakly referenced — when key has no strong refs, entry is eligible for GC
        Object key1 = new Object(); // strong ref held in 'key1' variable
        Object key2 = new Object(); // strong ref held in 'key2' variable
        weakMap.put(key1, "value1");
        weakMap.put(key2, "value2");

        System.out.println("  Before GC: size = " + weakMap.size()); // 2

        // Remove the strong reference to key2
        key2 = null; // Now key2's entry is eligible for GC

        System.gc(); // Encourage GC

        System.out.println("  After GC (key2=null): size = " + weakMap.size()); // likely 1

        System.out.println();
        System.out.println("  CAVEAT: WeakHashMap weakly references KEYS, not VALUES.");
        System.out.println("  If a value strongly references its key, the entry never goes away!");
        System.out.println("  Also: String literal keys are never GC'd (they live in the string pool).");
        System.out.println("  So weakMap.put(\"literal\", value) is effectively a strong entry.\n");
    }

    // -----------------------------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          MEMORY LEAK PATTERNS — INTERVIEW DEMO               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Static collection leak
        demoStaticCollectionLeak();

        // 2. Unclosed resources
        demoUnclosedResourceLeak();

        // 3. Listener not removed
        demoListenerLeak();

        // 4. ThreadLocal not cleaned
        demoThreadLocalLeak();

        // 5. Inner class holding outer reference
        demoInnerClassLeak();

        // 6. String.intern() abuse
        demoStringInternLeak();

        // 7. ClassLoader leak
        demoClassLoaderLeak();

        // Bonus: WeakHashMap
        demoWeakHashMapBehavior();

        System.out.println("=== Key Takeaways for Interviews ===");
        System.out.println("  1. Memory leaks in Java = unintended strong references preventing GC.");
        System.out.println("  2. Top causes: static collections, unclosed resources, ThreadLocals, listeners.");
        System.out.println("  3. Diagnosis: GC logs (rising baseline) -> heap dump -> MAT analysis.");
        System.out.println("  4. Always use try-with-resources. Always call ThreadLocal.remove().");
        System.out.println("  5. Prefer static inner classes (no implicit outer reference).");
        System.out.println("  6. Use bounded caches (Caffeine, Guava) instead of unbounded maps.");
        System.out.println("  7. Enable -XX:+HeapDumpOnOutOfMemoryError in all production JVMs.");
    }
}
