package interview.level7_java25_26.compact_source;

/**
 * ============================================================================
 * COMPACT SOURCE FILES / UNNAMED CLASSES — Java 21+ (JEP 463)
 * Level: 7 — Java 25/26 Awareness
 * Status: PREVIEW in Java 21 (JEP 463), second preview Java 22, FINALIZED Java 25
 * ============================================================================
 *
 * WHAT CHANGED?
 * ─────────────
 * Java 21 introduced "Implicitly Declared Classes and Instance Main Methods"
 * to make Java more approachable for beginners and scripting use cases.
 *
 * Two key simplifications:
 *   1. Instance main methods: void main() { } — no more public static void main(String[])
 *   2. Unnamed classes: no class declaration needed for simple programs
 *   3. Module import declarations: import module java.base (Java 23+, JEP 476)
 *
 * ============================================================================
 * BEFORE vs AFTER
 * ============================================================================
 *
 * BEFORE (traditional Java — every beginner's first hurdle):
 *
 *   public class HelloWorld {
 *       public static void main(String[] args) {
 *           System.out.println("Hello, World!");
 *       }
 *   }
 *
 * Things a beginner must understand before printing "Hello":
 *   - public (access modifier)
 *   - class (OOP concept)
 *   - static (class vs instance)
 *   - void (return types)
 *   - String[] args (arrays, command line args)
 *   - System.out.println (static field access, method call)
 *
 * AFTER (Java 21+ with unnamed class):
 *
 *   void main() {
 *       println("Hello, World!");
 *   }
 *
 * Or even simpler (Java 23+ with module import):
 *
 *   import module java.base;
 *
 *   void main() {
 *       println("Hello, World!");
 *   }
 *
 * ============================================================================
 * MAIN METHOD SELECTION (launch protocol)
 * ============================================================================
 *
 * Java now selects the main method with the following priority:
 *
 *   1. static void main(String[] args)          ← traditional (highest priority)
 *   2. static void main()                       ← static, no args
 *   3. void main(String[] args)                 ← instance, with args
 *   4. void main()                              ← instance, no args (simplest)
 *
 * For instance main methods, the JVM creates an instance of the class
 * using the no-arg constructor, then calls main() on it.
 *
 * ============================================================================
 * UNNAMED CLASSES
 * ============================================================================
 *
 * If a .java file contains methods/fields but no class declaration,
 * it becomes an "unnamed class":
 *
 *   // File: Hello.java (no class declaration!)
 *   String greeting = "Hello";
 *
 *   String greet(String name) {
 *       return greeting + ", " + name + "!";
 *   }
 *
 *   void main() {
 *       println(greet("World"));
 *   }
 *
 * Rules for unnamed classes:
 *   - Cannot be referenced by name from other code
 *   - Always in the unnamed package
 *   - Cannot have constructors (just a default one)
 *   - Can have instance fields and methods
 *   - Intended for scripts and small programs, NOT production code
 *
 * ============================================================================
 * MODULE IMPORT DECLARATIONS (JEP 476, Java 23+)
 * ============================================================================
 *
 *   import module java.base;
 *   // Imports ALL public top-level classes from java.base module:
 *   // java.lang.*, java.util.*, java.io.*, java.math.*, java.net.*,
 *   // java.time.*, java.util.stream.*, java.util.concurrent.*, etc.
 *
 *   void main() {
 *       // No individual imports needed!
 *       var list = List.of(1, 2, 3);
 *       var map = Map.of("a", 1);
 *       var now = Instant.now();
 *       var path = Path.of("file.txt");
 *   }
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "What are unnamed classes and instance main methods?"
 * A: "Java 21+ allows a simpler program entry point: void main() {} without
 *     public, static, or String[] args. Files without a class declaration
 *     become 'unnamed classes.' Combined with module imports (import module
 *     java.base), this lets beginners and scripters write Java with minimal
 *     ceremony — like Python or scripting languages."
 *
 * Q: "Does this change how production Java is written?"
 * A: "No. Unnamed classes are for scripts, experiments, and education.
 *     Production code still uses explicit class declarations, proper
 *     packages, and the traditional main method. But it makes Java more
 *     competitive for scripting and learning."
 *
 * Q: "How does the JVM select which main method to invoke?"
 * A: "Priority order: (1) static main(String[]), (2) static main(),
 *     (3) instance main(String[]), (4) instance main(). The JVM picks the
 *     first match. For instance methods, it creates an instance first."
 */
public class CompactSourceDemo {

    // NOTE: This file uses a traditional class declaration with static main
    // because it's inside a package (unnamed classes can't be in a package).
    // The comments show what compact source looks like in practice.

    public static void main(String[] args) {
        System.out.println("=== Compact Source Files (JEP 463) ===\n");

        demoTraditionalVsCompact();
        demoMainMethodPriority();
        demoModuleImports();
        demoUseCases();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. TRADITIONAL vs COMPACT
    // ─────────────────────────────────────────────────────────────────────────
    static void demoTraditionalVsCompact() {
        System.out.println("── Traditional vs Compact Source ──\n");

        System.out.println("TRADITIONAL (every Java dev knows this):");
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ public class HelloWorld {               │");
        System.out.println("  │     public static void main(String[] a) │");
        System.out.println("  │         System.out.println(\"Hello!\");   │");
        System.out.println("  │     }                                   │");
        System.out.println("  │ }                                       │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println("  Concepts needed: public, class, static, void, String[], System.out");
        System.out.println();

        System.out.println("COMPACT (Java 21+ unnamed class):");
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ void main() {                           │");
        System.out.println("  │     println(\"Hello!\");                  │");
        System.out.println("  │ }                                       │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println("  Concepts needed: method, string — that's it!");
        System.out.println();

        System.out.println("WITH MODULE IMPORT (Java 23+):");
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │ import module java.base;                │");
        System.out.println("  │                                         │");
        System.out.println("  │ void main() {                           │");
        System.out.println("  │     var list = List.of(1, 2, 3);        │");
        System.out.println("  │     var now = Instant.now();            │");
        System.out.println("  │     println(list + \" at \" + now);       │");
        System.out.println("  │ }                                       │");
        System.out.println("  └─────────────────────────────────────────┘");
        System.out.println("  No class, no individual imports, no static, no String[] args");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. MAIN METHOD SELECTION PRIORITY
    // ─────────────────────────────────────────────────────────────────────────
    static void demoMainMethodPriority() {
        System.out.println("── Main Method Selection Priority ──\n");

        System.out.println("The JVM tries these in order, picks the first it finds:\n");

        System.out.println("  Priority 1 (traditional):  public static void main(String[] args)");
        System.out.println("  Priority 2 (static noarg): static void main()");
        System.out.println("  Priority 3 (instance arg): void main(String[] args)");
        System.out.println("  Priority 4 (simplest):     void main()");
        System.out.println();

        System.out.println("Example — which main() runs?");
        System.out.println("  class Example {");
        System.out.println("      static void main() { println(\"static\"); }");
        System.out.println("      void main(String[] args) { println(\"instance\"); }");
        System.out.println("  }");
        System.out.println("  Answer: static main() runs (priority 2 > priority 3)");
        System.out.println();

        // Demonstrate instance main concept
        System.out.println("For instance main methods:");
        System.out.println("  1. JVM creates instance via no-arg constructor");
        System.out.println("  2. Calls main() on that instance");
        System.out.println("  3. Instance can use 'this', instance fields, etc.");
        System.out.println();

        /*
         * INSTANCE MAIN EXAMPLE:
         *
         *   // File: Counter.java (unnamed class)
         *   int count = 0;
         *
         *   void increment() { count++; }
         *
         *   void main() {
         *       increment();
         *       increment();
         *       println("Count: " + count);  // prints 2
         *   }
         */
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. MODULE IMPORTS
    // ─────────────────────────────────────────────────────────────────────────
    static void demoModuleImports() {
        System.out.println("── Module Import Declarations (JEP 476) ──\n");

        System.out.println("BEFORE (typical Java file header):");
        System.out.println("  import java.util.List;");
        System.out.println("  import java.util.Map;");
        System.out.println("  import java.util.stream.Collectors;");
        System.out.println("  import java.time.Instant;");
        System.out.println("  import java.io.IOException;");
        System.out.println("  import java.nio.file.Path;");
        System.out.println("  import java.nio.file.Files;");
        System.out.println();

        System.out.println("AFTER (one module import):");
        System.out.println("  import module java.base;");
        System.out.println("  // Imports ALL exported packages from java.base module");
        System.out.println();

        System.out.println("What 'import module java.base' includes:");
        System.out.println("  java.lang.*                  (always implicit anyway)");
        System.out.println("  java.util.*                  (List, Map, Set, Optional...)");
        System.out.println("  java.util.stream.*           (Stream, Collectors...)");
        System.out.println("  java.util.concurrent.*       (ExecutorService, Future...)");
        System.out.println("  java.util.function.*         (Function, Predicate...)");
        System.out.println("  java.io.*                    (InputStream, File...)");
        System.out.println("  java.nio.file.*              (Path, Files...)");
        System.out.println("  java.time.*                  (Instant, LocalDate...)");
        System.out.println("  java.math.*                  (BigDecimal, BigInteger...)");
        System.out.println("  java.net.*                   (URL, HttpClient...)");
        System.out.println("  ... and every other java.base package");
        System.out.println();

        System.out.println("Notes:");
        System.out.println("  - Works for any module, not just java.base");
        System.out.println("  - import module java.sql;  → java.sql.*, javax.sql.*");
        System.out.println("  - Ambiguous names require explicit import to resolve");
        System.out.println("  - Meant for scripts and small programs; production code");
        System.out.println("    should still use explicit imports for clarity");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. REAL-WORLD USE CASES
    // ─────────────────────────────────────────────────────────────────────────
    static void demoUseCases() {
        System.out.println("── Use Cases for Compact Source ──\n");

        System.out.println("1. EDUCATION — teach programming without boilerplate");
        System.out.println("   void main() { println(\"Hello, student!\"); }");
        System.out.println();

        System.out.println("2. SCRIPTING — quick utilities, like Python scripts");
        System.out.println("   // save as cleanup.java, run with: java cleanup.java");
        System.out.println("   import module java.base;");
        System.out.println("   void main() throws Exception {");
        System.out.println("       Files.walk(Path.of(\".\"))");
        System.out.println("           .filter(p -> p.toString().endsWith(\".tmp\"))");
        System.out.println("           .forEach(p -> { try { Files.delete(p); }");
        System.out.println("               catch(Exception e) {} });");
        System.out.println("   }");
        System.out.println();

        System.out.println("3. PROTOTYPING — try out an idea without ceremony");
        System.out.println("   import module java.base;");
        System.out.println("   void main() {");
        System.out.println("       var data = List.of(3,1,4,1,5,9,2,6);");
        System.out.println("       var sorted = data.stream().sorted().toList();");
        System.out.println("       println(sorted);");
        System.out.println("   }");
        System.out.println();

        System.out.println("4. JSHELL GRADUATION — move from REPL to source file");
        System.out.println("   Scripts grow naturally from jshell snippets");
        System.out.println();

        System.out.println("NOT FOR:");
        System.out.println("  - Production applications (use proper classes & packages)");
        System.out.println("  - Libraries (unnamed classes can't be referenced)");
        System.out.println("  - Multi-file projects (unnamed classes are single-file)");
    }
}
