package interview.level1_core.strings;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: What is the String constant pool?
// A: A special memory area in the JVM heap (since Java 7, it was in PermGen before)
//    that stores unique String literals. When you write String s = "hello", the JVM checks
//    the pool — if "hello" exists, s points to it; otherwise, it creates a new entry.
//    This saves memory by reusing identical strings.
//
// Q: What is the difference between == and .equals() for Strings?
// A: == compares references (memory addresses). Two String variables may hold the same
//    content but point to different objects.
//    .equals() compares the actual character content.
//    String literals from the pool share the same reference, so == works for them.
//    But new String("hello") creates a separate object on the heap.
//
// Q: What does String.intern() do?
// A: intern() checks if the string's value exists in the pool. If yes, returns the pool
//    reference. If no, adds it to the pool and returns that reference.
//    After interning, == comparison works for content equality.
//
// Q: How many objects are created by: String s = new String("hello")?
// A: Up to 2 objects:
//    1. The literal "hello" in the string pool (if not already there).
//    2. A new String object on the heap (from the new keyword).
//    The variable s points to the heap object, NOT the pool.
//
// Q: Why is String immutable in Java?
// A: 1. String pool: Immutability allows safe sharing of pooled strings.
//    2. Thread safety: Immutable objects are inherently thread-safe.
//    3. Security: Strings used for class loading, file paths, network connections.
//    4. Hashing: hashCode() can be cached (used in HashMap keys).
//
// Q: StringBuilder vs StringBuffer?
// A: Both are mutable. StringBuffer is synchronized (thread-safe but slower).
//    StringBuilder is NOT synchronized (faster, for single-threaded use).
//    Always prefer StringBuilder unless you need thread safety.
// ========================================================

public class StringPoolAndIntern {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  String Pool & Intern Deep Dive");
        System.out.println("========================================\n");

        // --- 1. String literal vs new String ---
        System.out.println("=== 1. Literal vs new String() ===");
        String s1 = "hello";          // goes to string pool
        String s2 = "hello";          // reuses same pool reference
        String s3 = new String("hello");  // creates new object on heap

        System.out.println("s1 = \"hello\" (literal)");
        System.out.println("s2 = \"hello\" (literal)");
        System.out.println("s3 = new String(\"hello\") (heap)");
        System.out.println();
        System.out.println("s1 == s2:       " + (s1 == s2));       // true  — same pool reference
        System.out.println("s1 == s3:       " + (s1 == s3));       // false — different objects
        System.out.println("s1.equals(s3):  " + s1.equals(s3));    // true  — same content
        System.out.println();

        // --- 2. String.intern() ---
        System.out.println("=== 2. String.intern() ===");
        String s4 = new String("world");  // heap object
        String s5 = s4.intern();          // returns pool reference for "world"
        String s6 = "world";             // pool reference

        System.out.println("s4 = new String(\"world\")");
        System.out.println("s5 = s4.intern()");
        System.out.println("s6 = \"world\"");
        System.out.println();
        System.out.println("s4 == s5:  " + (s4 == s5));  // false — s4 is heap, s5 is pool
        System.out.println("s5 == s6:  " + (s5 == s6));  // true  — both are pool references
        System.out.println("s4 == s6:  " + (s4 == s6));  // false — s4 is heap, s6 is pool
        System.out.println();

        // --- 3. String concatenation and pool ---
        System.out.println("=== 3. Concatenation Behavior ===");
        String a = "hel" + "lo";         // compile-time constant folding -> pool "hello"
        String b = "hello";
        System.out.println("\"hel\" + \"lo\" == \"hello\": " + (a == b));  // true — compiler optimizes

        String c = "hel";
        String d = c + "lo";             // runtime concatenation -> new object on heap
        System.out.println("variable + \"lo\" == \"hello\": " + (d == b));  // false — runtime concat

        final String e = "hel";          // final -> compile-time constant
        String f = e + "lo";             // compile-time constant folding
        System.out.println("final var + \"lo\" == \"hello\": " + (f == b));  // true — final = constant
        System.out.println();

        // --- 4. How many objects? ---
        System.out.println("=== 4. Object Count Quiz ===");
        System.out.println("Q: String s = new String(\"abc\"); — how many objects?");
        System.out.println("A: Up to 2: \"abc\" in pool (if not present) + new String on heap.\n");

        System.out.println("Q: String s = \"abc\" + \"def\"; — how many objects?");
        System.out.println("A: 1 object: \"abcdef\" in pool. Compiler folds \"abc\"+\"def\" at compile time.\n");

        System.out.println("Q: String s1 = \"abc\"; String s2 = s1 + \"def\"; — how many objects?");
        System.out.println("A: 3 objects: \"abc\" in pool, \"def\" in pool, \"abcdef\" on heap (runtime concat).\n");

        // --- 5. String immutability proof ---
        System.out.println("=== 5. Immutability Demo ===");
        String original = "Hello";
        String modified = original.concat(" World");
        System.out.println("original: \"" + original + "\" (unchanged)");
        System.out.println("modified: \"" + modified + "\" (new object)");
        System.out.println("original == modified: " + (original == modified));
        System.out.println();

        // --- 6. hashCode caching ---
        System.out.println("=== 6. hashCode Caching ===");
        String str = "performance";
        long start = System.nanoTime();
        int hash1 = str.hashCode();  // computed once
        long first = System.nanoTime() - start;

        start = System.nanoTime();
        int hash2 = str.hashCode();  // cached, no recomputation
        long second = System.nanoTime() - start;

        System.out.println("First hashCode() call:  " + hash1 + " (" + first + " ns)");
        System.out.println("Second hashCode() call: " + hash2 + " (" + second + " ns)");
        System.out.println("hashCode is cached internally after first computation.\n");

        // --- 7. StringBuilder vs StringBuffer performance ---
        System.out.println("=== 7. StringBuilder vs StringBuffer Performance ===");
        int iterations = 100_000;

        // StringBuilder (not synchronized)
        long sbStart = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append("a");
        }
        long sbTime = System.nanoTime() - sbStart;

        // StringBuffer (synchronized)
        long bufStart = System.nanoTime();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < iterations; i++) {
            buf.append("a");
        }
        long bufTime = System.nanoTime() - bufStart;

        // String concatenation (worst — creates new object each time)
        long concatStart = System.nanoTime();
        String concat = "";
        for (int i = 0; i < 10_000; i++) {  // only 10K, concat is very slow
            concat += "a";
        }
        long concatTime = System.nanoTime() - concatStart;

        System.out.printf("StringBuilder (%d iterations): %d ms%n", iterations, sbTime / 1_000_000);
        System.out.printf("StringBuffer  (%d iterations): %d ms%n", iterations, bufTime / 1_000_000);
        System.out.printf("String +=     (%d iterations):  %d ms (O(n^2) due to copying!)%n", 10_000, concatTime / 1_000_000);
        System.out.println();

        // --- 8. Useful String methods ---
        System.out.println("=== 8. Key String Methods ===");
        String test = "  Hello, World!  ";
        System.out.println("trim():        \"" + test.trim() + "\"");
        System.out.println("strip():       \"" + test.strip() + "\"");        // Java 11 — handles Unicode whitespace
        System.out.println("stripLeading():\"" + test.stripLeading() + "\"");
        System.out.println("isBlank():     " + "   ".isBlank());              // Java 11
        System.out.println("repeat(3):     " + "ab".repeat(3));               // Java 11
        System.out.println("chars():       count=" + "hello".chars().count()); // Java 9 stream of chars

        // --- 9. Key takeaways ---
        System.out.println("\n=== 9. Key Takeaways ===");
        System.out.println("1. Use .equals() for content comparison, NEVER == (unless you know both are interned)");
        System.out.println("2. String literals are automatically interned (pool)");
        System.out.println("3. new String() always creates a heap object (avoid it)");
        System.out.println("4. Use StringBuilder for concatenation in loops");
        System.out.println("5. String is immutable: every modification creates a new object");
        System.out.println("6. final String variables are compile-time constants (constant folding applies)");
    }
}
