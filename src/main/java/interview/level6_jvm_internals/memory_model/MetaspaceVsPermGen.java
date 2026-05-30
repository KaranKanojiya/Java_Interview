package interview.level6_jvm_internals.memory_model;

/**
 * Q10. What is the difference between Metaspace and PermGen?
 *
 * PermGen (Java 7 and earlier):
 *   - Fixed-size memory region within the JVM
 *   - Stored: class metadata, interned strings, static variables
 *   - Default size: 64MB (configurable with -XX:MaxPermSize)
 *   - Problem: java.lang.OutOfMemoryError: PermGen space
 *     → Common in app servers with many class reloads (Tomcat, JBoss)
 *   - String pool was in PermGen (moved to heap in Java 7)
 *
 * Metaspace (Java 8+):
 *   - Replaced PermGen entirely
 *   - Uses NATIVE memory (not JVM heap)
 *   - Auto-grows by default (limited only by OS memory)
 *   - Stores: class metadata, method bytecode, constant pool
 *   - String pool is in heap (since Java 7)
 *   - Static variables moved to heap (since Java 8)
 *   - Error: java.lang.OutOfMemoryError: Metaspace
 *
 * | Feature            | PermGen (≤ Java 7)      | Metaspace (Java 8+)        |
 * |-------------------|-------------------------|----------------------------|
 * | Location          | JVM heap (fixed region)  | Native memory (OS)         |
 * | Default max size  | 64MB                    | Unlimited (OS limit)       |
 * | Auto-resize       | No                       | Yes                        |
 * | String pool       | PermGen (≤ Java 6)       | Heap (since Java 7)        |
 * | Static variables  | PermGen                  | Heap (since Java 8)        |
 * | GC               | Full GC only             | Concurrent with class unloading |
 * | Configure         | -XX:MaxPermSize          | -XX:MaxMetaspaceSize       |
 * | Common error      | OOM: PermGen space       | OOM: Metaspace             |
 *
 * Why was PermGen removed?
 *   1. Fixed size was hard to tune — too small → OOM, too large → waste
 *   2. Class unloading was tied to Full GC (slow)
 *   3. Merging HotSpot and JRockit VMs (JRockit never had PermGen)
 */
public class MetaspaceVsPermGen {

    public static void main(String[] args) {

        System.out.println("=== PermGen vs Metaspace ===\n");

        // === What's in Metaspace ===
        System.out.println("=== What lives in Metaspace ===");
        System.out.println("1. Class metadata (class name, methods, fields, access flags)");
        System.out.println("2. Method bytecode");
        System.out.println("3. Constant pool (compile-time constants)");
        System.out.println("4. Annotations");
        System.out.println("5. Method counters (for JIT compilation decisions)");

        // === What's NOT in Metaspace (moved to heap) ===
        System.out.println("\n=== What moved to Heap (since Java 7/8) ===");
        System.out.println("1. String pool → Heap (since Java 7)");
        System.out.println("2. Static variables → Heap (since Java 8)");
        System.out.println("3. Class statics (java.lang.Class instances) → Heap");

        // === Verify: Static variables are on heap ===
        System.out.println("\n=== Static vs Instance storage ===");
        System.out.println("Static 'counter' lives on the HEAP (attached to Class object)");
        System.out.println("Metaspace only holds the class METADATA (structure, not data)");

        // === Runtime info ===
        System.out.println("\n=== Current JVM Memory Info ===");
        Runtime rt = Runtime.getRuntime();
        System.out.println("Heap max:  " + (rt.maxMemory() / 1024 / 1024) + " MB");
        System.out.println("Heap used: " + ((rt.totalMemory() - rt.freeMemory()) / 1024 / 1024) + " MB");

        // Metaspace info requires JMX or jcmd
        System.out.println("\nTo check Metaspace usage:");
        System.out.println("  jcmd <pid> VM.metaspace");
        System.out.println("  jcmd <pid> GC.class_stats");

        // === Common Metaspace OOM causes ===
        System.out.println("\n=== Common Metaspace OOM causes ===");
        System.out.println("1. Too many dynamically generated classes (CGLIB, Reflection proxies)");
        System.out.println("2. ClassLoader leaks (classes can't be unloaded if loader is referenced)");
        System.out.println("3. Hot-redeploying apps without restarting server");
        System.out.println("4. Excessive use of Groovy/Scala/Kotlin scripts at runtime");

        // === JVM flags ===
        System.out.println("\n=== JVM flags ===");
        System.out.println("-XX:MetaspaceSize=128m        → initial size (triggers GC when exceeded)");
        System.out.println("-XX:MaxMetaspaceSize=512m     → hard limit (default: unlimited)");
        System.out.println("-XX:MinMetaspaceFreeRatio=40  → min free ratio after GC");
        System.out.println("-XX:MaxMetaspaceFreeRatio=70  → max free ratio after GC");
        System.out.println("\nOld PermGen flags (IGNORED in Java 8+):");
        System.out.println("-XX:PermSize=64m              → ignored");
        System.out.println("-XX:MaxPermSize=256m          → ignored");
    }
}
