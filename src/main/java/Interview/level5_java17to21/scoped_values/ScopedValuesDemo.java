package interview.level5_java17to21.scoped_values;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: Scoped Values — Java 21 (Preview)
// =====================================================================
//
// Q: "What are ScopedValues and why do we need them?"
// A: "ScopedValue is a replacement for ThreadLocal that is designed for
//     virtual threads. ThreadLocal stores a mutable copy per thread.
//     With millions of virtual threads, that means millions of copies →
//     OOM. ScopedValue is immutable, bounded to a scope (method call
//     tree), and automatically cleaned up when the scope exits."
//
// Q: "How do ScopedValues differ from ThreadLocal?"
// A: "1) Immutable — set once per scope, no set() method after binding.
//     2) Bounded lifetime — automatically removed when scope exits.
//     3) Inherited efficiently — child virtual threads see the parent's
//        scoped value without copying (zero-cost inheritance).
//     4) No memory leak risk — no forgotten remove() calls."
//
// Q: "What does ScopedValue.where(KEY, value).run(() -> ...) do?"
// A: "It binds KEY to value for the duration of the Runnable. Any code
//     in the call stack can read KEY.get(). When run() returns, the
//     binding is removed. Nested where() calls can rebind (shadow) the
//     value for an inner scope."
//
// Q: "Can ScopedValues be used with StructuredTaskScope?"
// A: "Yes. Forked subtasks in a StructuredTaskScope automatically inherit
//     the parent's ScopedValue bindings. This is the intended use case."
//
// COMPILE: javac --enable-preview --source 21 ScopedValuesDemo.java
// RUN:     java --enable-preview ScopedValuesDemo
//
// NOTE: ScopedValue is a preview API in Java 21. If not available,
//       the simulation section shows the concept using ThreadLocal.
// =====================================================================

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScopedValuesDemo {

    // ---------------------------------------------------------------
    // 1. ScopedValue usage (Preview API)
    // ---------------------------------------------------------------

    // ScopedValue is declared as a static final field (like ThreadLocal)
    // Using fully qualified name to avoid import issues with preview API
    // private static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
    // private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    static void scopedValueDemo() {
        System.out.println("=== 1. ScopedValue API (Preview) ===\n");

        try {
            scopedValueInternal();
        } catch (NoClassDefFoundError | UnsupportedOperationException e) {
            System.out.println("  ScopedValue not available in this runtime.");
            System.out.println("  See simulation in section 3.\n");
        }
    }

    @SuppressWarnings("preview")
    private static void scopedValueInternal() {
        // Declare ScopedValues
        final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
        final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

        // Bind and run — value is available in the entire call tree
        ScopedValue.where(CURRENT_USER, "Karan")
                .where(REQUEST_ID, "REQ-001")
                .run(() -> {
                    System.out.println("  User:      " + CURRENT_USER.get());
                    System.out.println("  RequestId: " + REQUEST_ID.get());

                    // Called method can read the scoped value
                    handleRequest(CURRENT_USER, REQUEST_ID);

                    // Nested scope — shadows the outer binding
                    ScopedValue.where(CURRENT_USER, "Admin")
                            .run(() -> {
                                System.out.println("  [Inner scope] User: " + CURRENT_USER.get());
                                System.out.println("  [Inner scope] RequestId: " + REQUEST_ID.get()
                                        + " (inherited from outer)");
                            });

                    // After inner scope exits, outer binding is restored
                    System.out.println("  [Outer scope] User: " + CURRENT_USER.get());
                });

        // Outside the scope — value is not bound
        System.out.println("  isBound() outside scope: " + CURRENT_USER.isBound());
        System.out.println();
    }

    @SuppressWarnings("preview")
    private static void handleRequest(ScopedValue<String> currentUser,
                                       ScopedValue<String> requestId) {
        // Deep in the call stack — no parameter passing needed
        System.out.println("  [handleRequest] Processing for user: " + currentUser.get()
                + ", request: " + requestId.get());
    }

    // ---------------------------------------------------------------
    // 2. ThreadLocal problems with virtual threads
    // ---------------------------------------------------------------
    static void threadLocalProblems() {
        System.out.println("=== 2. Why ThreadLocal is Problematic with Virtual Threads ===\n");

        // Problem 1: Memory — each of 1M virtual threads gets its own copy
        ThreadLocal<byte[]> heavyState = ThreadLocal.withInitial(() -> new byte[1024]);

        System.out.println("  Problem 1: MEMORY");
        System.out.println("    ThreadLocal<byte[1024]> with 1M virtual threads");
        System.out.println("    = 1M * 1KB = ~1GB just for ThreadLocal state!\n");

        // Problem 2: Lifecycle — must call remove() or leak
        ThreadLocal<String> leakyLocal = new ThreadLocal<>();
        leakyLocal.set("I will leak if you forget remove()");
        // leakyLocal.remove(); // easy to forget!

        System.out.println("  Problem 2: LIFECYCLE");
        System.out.println("    Must call remove() explicitly or state leaks.");
        System.out.println("    Easy to forget, especially in exception paths.\n");

        // Problem 3: Mutability — anyone can call set() at any time
        ThreadLocal<String> mutableLocal = new ThreadLocal<>();
        mutableLocal.set("original");
        mutableLocal.set("mutated"); // no compile-time safety

        System.out.println("  Problem 3: MUTABILITY");
        System.out.println("    ThreadLocal allows set() at any point.");
        System.out.println("    Hard to reason about what value a method sees.\n");

        // Problem 4: InheritableThreadLocal doesn't work well with pools
        System.out.println("  Problem 4: INHERITANCE");
        System.out.println("    InheritableThreadLocal copies value at thread creation.");
        System.out.println("    With thread pools, the 'parent' is unpredictable.\n");

        // Cleanup
        heavyState.remove();
        leakyLocal.remove();
        mutableLocal.remove();
    }

    // ---------------------------------------------------------------
    // 3. Simulation — ScopedValue concept using ThreadLocal
    // ---------------------------------------------------------------
    static void simulatedScopedValue() throws Exception {
        System.out.println("=== 3. Simulated ScopedValue Using ThreadLocal ===\n");

        System.out.println("  This simulates the ScopedValue concept for runtimes");
        System.out.println("  where the preview API is not available.\n");

        // Simulated scoped value using ThreadLocal + try-finally
        var simulatedScope = new SimulatedScopedValue<String>();

        simulatedScope.where("Karan", () -> {
            System.out.println("  [Outer] User: " + simulatedScope.get());

            // Nested rebinding (shadowing)
            simulatedScope.where("Admin", () -> {
                System.out.println("  [Inner] User: " + simulatedScope.get());
            });

            // Outer value restored
            System.out.println("  [Outer] User restored: " + simulatedScope.get());
        });

        System.out.println("  [Outside] isBound: " + simulatedScope.isBound());
        System.out.println();
    }

    /**
     * Minimal simulation of ScopedValue semantics using ThreadLocal.
     * Shows the core idea: bounded, immutable-per-scope binding.
     */
    static class SimulatedScopedValue<T> {
        private final ThreadLocal<T> holder = new ThreadLocal<>();

        public T get() {
            T value = holder.get();
            if (value == null) throw new IllegalStateException("Not bound");
            return value;
        }

        public boolean isBound() {
            return holder.get() != null;
        }

        public void where(T value, Runnable action) {
            T previous = holder.get();
            holder.set(value);
            try {
                action.run();
            } finally {
                // Restore previous value (or remove if none)
                if (previous == null) holder.remove();
                else holder.set(previous);
            }
        }
    }

    // ---------------------------------------------------------------
    // 4. ScopedValue with virtual threads
    // ---------------------------------------------------------------
    static void scopedValueWithVirtualThreads() throws Exception {
        System.out.println("=== 4. ScopedValue with Virtual Threads ===\n");

        try {
            scopedValueWithVTInternal();
        } catch (NoClassDefFoundError | UnsupportedOperationException e) {
            System.out.println("  (Preview not available — conceptual explanation)");
            System.out.println();
            System.out.println("  When using ScopedValue with virtual threads:");
            System.out.println("    ScopedValue.where(USER, \"Karan\").run(() -> {");
            System.out.println("        // Fork virtual threads — they inherit USER automatically");
            System.out.println("        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {");
            System.out.println("            scope.fork(() -> processOrder(USER.get()));");
            System.out.println("            scope.fork(() -> sendEmail(USER.get()));");
            System.out.println("            scope.join();");
            System.out.println("        }");
            System.out.println("    });");
            System.out.println();
            System.out.println("  Key benefit: zero-cost inheritance, no per-thread copy.\n");
        }
    }

    @SuppressWarnings("preview")
    private static void scopedValueWithVTInternal() throws Exception {
        final ScopedValue<String> USER = ScopedValue.newInstance();

        ScopedValue.where(USER, "Karan").run(() -> {
            try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
                // Virtual threads inherit scoped values from their parent
                for (int i = 0; i < 5; i++) {
                    final int taskId = i;
                    exec.submit(() -> {
                        System.out.println("  VT-" + taskId + " sees USER=" + USER.get()
                                + " (thread: " + Thread.currentThread() + ")");
                    });
                }
            }
        });
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 5. Comparison table
    // ---------------------------------------------------------------
    static void comparisonTable() {
        System.out.println("=== 5. ThreadLocal vs ScopedValue — Comparison ===\n");
        System.out.println("  ┌────────────────────┬──────────────────┬──────────────────┐");
        System.out.println("  │ Feature            │ ThreadLocal      │ ScopedValue      │");
        System.out.println("  ├────────────────────┼──────────────────┼──────────────────┤");
        System.out.println("  │ Mutability         │ Mutable (set())  │ Immutable/scope  │");
        System.out.println("  │ Lifetime           │ Manual remove()  │ Auto (scope end) │");
        System.out.println("  │ Memory per thread  │ Full copy        │ Shared reference │");
        System.out.println("  │ Inheritance        │ Copy on create   │ Zero-cost        │");
        System.out.println("  │ Virtual-thread fit │ Poor (OOM risk)  │ Excellent        │");
        System.out.println("  │ Rebinding          │ set() anytime    │ Nested where()   │");
        System.out.println("  │ Thread safety      │ Per-thread copy  │ Scope-bound      │");
        System.out.println("  └────────────────────┴──────────────────┴──────────────────┘");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   Scoped Values Demo — Java 21 (Preview)            ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        scopedValueDemo();
        threadLocalProblems();
        simulatedScopedValue();
        scopedValueWithVirtualThreads();
        comparisonTable();

        System.out.println("=== Done ===");
    }
}
