package interview.level1_core.strings;

/**
 * Q5. What is the difference between StringBuilder and StringBuffer?
 *
 * | Feature       | StringBuilder         | StringBuffer          |
 * |--------------|----------------------|----------------------|
 * | Thread Safety | NOT thread-safe       | Thread-safe (synchronized) |
 * | Performance  | Faster                | Slower (sync overhead) |
 * | Introduced   | Java 5                | Java 1.0              |
 * | When to use  | Single-threaded       | Multi-threaded         |
 *
 * Both are MUTABLE (unlike String which is immutable).
 * Both use a resizable char[]/byte[] internally.
 * Default capacity: 16 characters.
 * When capacity exceeded: new capacity = (old * 2) + 2
 */
public class StringBuilderVsBuffer {

    public static void main(String[] args) {

        // === StringBuilder (single-threaded, faster) ===
        System.out.println("=== StringBuilder ===");
        StringBuilder sb = new StringBuilder("Hello");
        sb.append(" World");
        sb.insert(5, ",");
        sb.reverse();
        System.out.println("Reversed: " + sb);
        sb.reverse();
        sb.delete(5, 6);  // remove comma
        System.out.println("Final: " + sb);
        System.out.println("Capacity: " + sb.capacity());

        // === StringBuffer (thread-safe, slower) ===
        System.out.println("\n=== StringBuffer (synchronized methods) ===");
        StringBuffer buf = new StringBuffer("Thread");
        buf.append("-Safe");
        System.out.println("StringBuffer: " + buf);

        // === Performance comparison ===
        System.out.println("\n=== Performance ===");
        int iterations = 500_000;

        long start = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < iterations; i++) builder.append("a");
        long builderTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < iterations; i++) buffer.append("a");
        long bufferTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        String str = "";
        for (int i = 0; i < 50_000; i++) str += "a";  // creates new object each time!
        long stringTime = System.currentTimeMillis() - start;

        System.out.println("StringBuilder: " + builderTime + "ms");
        System.out.println("StringBuffer:  " + bufferTime + "ms");
        System.out.println("String concat: " + stringTime + "ms (only 50K iterations, still slowest!)");

        System.out.println("\n=== Rule: Always use StringBuilder for string concatenation in loops ===");
    }
}
