package interview.level1_core.oop;

/**
 * Q11. What is the difference between final, finally, and finalize?
 *
 * final (keyword):
 *   - Variable: cannot be reassigned after initialization
 *   - Method: cannot be overridden by subclasses
 *   - Class: cannot be extended (e.g., String, Integer)
 *
 * finally (block):
 *   - Always executes after try/catch (cleanup code)
 *   - Used for: closing resources, releasing locks
 *   - Skipped only on: System.exit(), JVM crash, thread kill
 *
 * finalize (method):
 *   - Called by GC before reclaiming object memory
 *   - DEPRECATED since Java 9 — do NOT use
 *   - Problems: unpredictable timing, performance overhead, may never run
 *   - Use Cleaner or try-with-resources instead
 */
public class FinalFinallyFinalize {

    // final class — cannot be extended
    static final class Constants {
        static final double PI = 3.14159;  // final variable — cannot change
    }

    static class Parent {
        // final method — cannot be overridden
        final void doWork() {
            System.out.println("  Parent.doWork() — final, cannot override");
        }
    }

    static class Child extends Parent {
        // This would NOT compile:
        // void doWork() { } // error: cannot override final method
    }

    public static void main(String[] args) {

        // === final variable ===
        System.out.println("=== final ===");
        final int x = 10;
        // x = 20;  // Compile error: cannot assign to final variable

        final StringBuilder sb = new StringBuilder("Hello");
        sb.append(" World");  // OK! final means reference can't change, object CAN be mutated
        // sb = new StringBuilder();  // Compile error: can't reassign reference
        System.out.println("final StringBuilder (mutable content): " + sb);
        System.out.println("final constant PI: " + Constants.PI);

        new Parent().doWork();

        // === finally ===
        System.out.println("\n=== finally ===");
        try {
            System.out.println("  try block");
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("  catch block: " + e.getMessage());
        } finally {
            System.out.println("  finally block — ALWAYS runs");
        }

        // finally with return — finally wins
        System.out.println("Method with return in finally: " + methodWithFinally());

        // === finalize (DEPRECATED) ===
        System.out.println("\n=== finalize (DEPRECATED since Java 9) ===");
        System.out.println("Don't use finalize()! Use try-with-resources or Cleaner instead.");
        System.out.println("Problems:");
        System.out.println("  1. May never be called (GC is unpredictable)");
        System.out.println("  2. Delays garbage collection (object survives extra GC cycle)");
        System.out.println("  3. No ordering guarantees between finalizers");
        System.out.println("  4. Exceptions in finalize() are silently ignored");
    }

    static String methodWithFinally() {
        try {
            return "from try";
        } finally {
            return "from finally";  // this WINS — finally's return overrides try's return
        }
    }
}
