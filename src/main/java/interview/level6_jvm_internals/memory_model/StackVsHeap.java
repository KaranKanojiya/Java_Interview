package interview.level6_jvm_internals.memory_model;

import java.util.ArrayList;
import java.util.List;

/**
 * Q2. What is the difference between Stack and Heap memory?
 *
 * | Feature          | Stack                         | Heap                            |
 * |-----------------|-------------------------------|----------------------------------|
 * | Stores          | Primitives, local variables,  | Objects, instance variables      |
 * |                 | method frames, references     |                                  |
 * | Scope           | Per thread (thread-private)    | Shared across all threads        |
 * | Lifetime        | Method entry → method exit    | GC decides when to reclaim       |
 * | Size            | Small (default ~512KB-1MB)    | Large (can be GBs)               |
 * | Speed           | Very fast (LIFO push/pop)     | Slower (GC overhead, allocation) |
 * | Error           | StackOverflowError            | OutOfMemoryError: Java heap      |
 * | Thread safety   | Inherently safe (private)     | Must synchronize shared access   |
 * | Configure       | -Xss (stack size per thread)  | -Xms, -Xmx (heap min/max)       |
 *
 * Stack frame contents (per method call):
 *   - Local variables (primitives stored by value, objects stored as references)
 *   - Operand stack (intermediate computation results)
 *   - Return address (where to go after method returns)
 *   - Frame data (exception table, constant pool reference)
 *
 * Key insight for interviews:
 *   - "Object obj = new Object();" → reference 'obj' on STACK, actual Object on HEAP
 *   - Primitives inside methods → STACK
 *   - Primitives inside objects → HEAP (as part of the object)
 *   - String literals → String Pool (special area in Heap, since Java 7 in old gen)
 */
public class StackVsHeap {

    // Instance variable → stored on HEAP (part of the StackVsHeap object)
    private int instanceVar = 10;

    // Static variable → stored in Metaspace (not heap, not stack)
    private static int staticVar = 20;

    public static void main(String[] args) {

        // === Stack memory demo ===
        System.out.println("=== Stack Memory ===");

        int localPrimitive = 42;  // primitive → STACK
        String localRef;          // reference → STACK, no object yet

        StackVsHeap obj = new StackVsHeap();
        // 'obj' reference → STACK
        // actual StackVsHeap object → HEAP
        // obj.instanceVar (10) → HEAP (inside the object)

        System.out.println("localPrimitive (stack): " + localPrimitive);
        System.out.println("obj.instanceVar (heap): " + obj.instanceVar);
        System.out.println("staticVar (metaspace):  " + staticVar);

        // === Each thread gets its own stack ===
        System.out.println("\n=== Each thread has its own stack ===");
        Thread t1 = new Thread(() -> methodA("Thread-1"));
        Thread t2 = new Thread(() -> methodA("Thread-2"));
        t1.start();
        t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // === StackOverflowError — stack exhaustion ===
        System.out.println("\n=== StackOverflowError ===");
        try {
            infiniteRecursion(0);
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError caught! (too many frames on stack)");
        }

        // === OutOfMemoryError: heap exhaustion ===
        System.out.println("\n=== OutOfMemoryError (heap) ===");
        // Uncomment to actually trigger (will crash JVM with small -Xmx):
        // List<byte[]> leak = new ArrayList<>();
        // while (true) { leak.add(new byte[1024 * 1024]); }

        // === Where things live ===
        System.out.println("\n=== Memory location summary ===");
        System.out.println("Local primitives (int, boolean):  STACK");
        System.out.println("Local object references:          STACK (ref only)");
        System.out.println("Object instances (new):           HEAP");
        System.out.println("Instance variables:               HEAP (inside object)");
        System.out.println("Static variables:                 METASPACE");
        System.out.println("String literals:                  STRING POOL (heap)");
        System.out.println("Class metadata:                   METASPACE");
        System.out.println("Method bytecode:                  METASPACE");

        System.out.println("\n=== JVM flags ===");
        System.out.println("-Xss512k          → stack size per thread (default ~512KB-1MB)");
        System.out.println("-Xms256m -Xmx4g   → heap min/max");
        System.out.println("-XX:MetaspaceSize  → initial metaspace size");
    }

    static void methodA(String threadName) {
        int x = 10;  // x is on THIS thread's stack
        methodB(threadName, x);
    }

    static void methodB(String threadName, int val) {
        int y = val + 5;  // y is on THIS thread's stack
        System.out.println("  " + threadName + " → methodB: y = " + y);
    }

    static void infiniteRecursion(int depth) {
        infiniteRecursion(depth + 1);  // each call adds a frame to the stack
    }
}
