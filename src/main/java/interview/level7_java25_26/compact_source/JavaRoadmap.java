package interview.level7_java25_26.compact_source;

/**
 * Q10. What is the Java roadmap beyond Java 25?
 *
 * Java follows a 6-month release cadence (since Java 10):
 *   - Feature release every March and September
 *   - LTS (Long-Term Support) every 2 years: 17, 21, 25, 29...
 *
 * === Delivered (Java 21 LTS - Sept 2023) ===
 *   - Virtual Threads (JEP 444) — finalized
 *   - Pattern Matching for switch (JEP 441) — finalized
 *   - Record Patterns (JEP 440) — finalized
 *   - Sequenced Collections (JEP 431)
 *   - String Templates (preview)
 *   - Unnamed Patterns and Variables (JEP 443)
 *   - Structured Concurrency (preview)
 *   - Scoped Values (preview)
 *
 * === Delivered / In Progress (Java 22-25) ===
 *   - Stream Gatherers (JEP 461/473) — custom intermediate stream ops
 *   - Statements before super() (JEP 447) — flexible constructors
 *   - Implicitly Declared Classes (JEP 463) — compact source files
 *   - Structured Concurrency — moving toward finalization
 *   - Scoped Values — moving toward finalization
 *   - Class-File API (JEP 457) — replace ASM for bytecode manipulation
 *   - Foreign Function & Memory API (JEP 454) — replace JNI
 *   - Vector API — SIMD operations (incubating)
 *
 * === Coming (Project Valhalla) ===
 *   - Value Classes — objects without identity
 *   - Primitive Generics — List<int>, no autoboxing
 *   - Universal Generics — generics over primitives and value types
 *   - Null-restricted types — types that can't be null
 *
 * === Coming (Other projects) ===
 *   - Project Leyden — faster startup (static images, AOT)
 *   - Project Babylon — code reflection (GPU offloading)
 *   - Project Lilliput — compact object headers (8 bytes → 4 bytes)
 *   - Project Panama — finalized FFI (Foreign Function Interface)
 *
 * === Interview talking points ===
 *   - Java is modernizing rapidly: value types, pattern matching, virtual threads
 *   - 6-month releases mean features arrive incrementally as previews
 *   - LTS releases (21, 25) are production targets
 *   - Valhalla is the biggest upcoming change since generics (Java 5)
 */
public class JavaRoadmap {

    public static void main(String[] args) {

        System.out.println("=== Java Release History (Recent) ===");
        System.out.println("Java 17 LTS (Sept 2021): Sealed classes, pattern matching instanceof");
        System.out.println("Java 21 LTS (Sept 2023): Virtual threads, record patterns, switch patterns");
        System.out.println("Java 25 LTS (Sept 2025): Stream gatherers, flexible constructors, compact source");
        System.out.println("Java 29 LTS (Sept 2027): Expected — Valhalla features?");

        System.out.println("\n=== Major Projects ===");

        System.out.println("\n--- Project Loom (DELIVERED in Java 21) ---");
        System.out.println("Virtual threads, structured concurrency, scoped values");
        System.out.println("Impact: 1M+ concurrent tasks, simple blocking code scales like async");

        System.out.println("\n--- Project Valhalla (IN PROGRESS) ---");
        System.out.println("Value classes, primitive generics, null-restricted types");
        System.out.println("Impact: List<int>, Point[], no autoboxing, 4-6x memory reduction");

        System.out.println("\n--- Project Panama (DELIVERED in Java 22) ---");
        System.out.println("Foreign Function & Memory API (replaces JNI)");
        System.out.println("Impact: Call C/C++ libraries safely, no native code needed");

        System.out.println("\n--- Project Leyden (IN PROGRESS) ---");
        System.out.println("Static images, ahead-of-time compilation, faster startup");
        System.out.println("Impact: Serverless/container-friendly Java (sub-second startup)");

        System.out.println("\n--- Project Amber (ONGOING) ---");
        System.out.println("Language productivity: records, sealed, patterns, string templates");
        System.out.println("Impact: Less boilerplate, more expressive Java code");

        System.out.println("\n=== Interview Tip ===");
        System.out.println("Mention: 'I keep up with Java's evolution — virtual threads for");
        System.out.println("concurrency, Valhalla for performance, and pattern matching for");
        System.out.println("cleaner code. I follow JEPs and preview features.'");
    }
}
