package interview.level7_java25_26.flexible_constructors;

/**
 * ============================================================================
 * FLEXIBLE CONSTRUCTOR BODIES — Java 22+ (JEP 447, finalized Java 25)
 * Level: 7 — Java 25/26 Awareness
 * Status: PREVIEW in Java 22-24, FINALIZED Java 25
 * ============================================================================
 *
 * WHAT CHANGED?
 * ─────────────
 * Before Java 22, the FIRST statement in a constructor MUST be:
 *   - super(args)   — explicit superclass constructor call, OR
 *   - this(args)    — delegate to another constructor
 *
 * You could NOT put any statements before super()/this().
 * This was annoying when you needed to:
 *   1. Validate arguments before passing them to super()
 *   2. Compute a value to pass to super()
 *   3. Share code between constructors
 *
 * WORKAROUND (Pre-Java 22): static factory methods or static helper methods
 *
 * NOW (Java 22+): You CAN place statements before super()/this(), with one
 * rule — you cannot access 'this' (the instance) before super() completes.
 *
 * ============================================================================
 * INTERVIEW Q&A
 * ============================================================================
 *
 * Q: "What are Flexible Constructor Bodies (JEP 447)?"
 * A: "Java 22+ allows statements before super() or this() calls in
 *     constructors. Previously, super()/this() had to be the very first
 *     statement. Now you can validate arguments, compute derived values,
 *     or perform logging before delegating to super(). The restriction is
 *     that you cannot read or write instance fields or call instance methods
 *     before super() completes — only static members and the parameters."
 *
 * Q: "Why was this restriction there in the first place?"
 * A: "The JVM requires the superclass to be initialized before subclass
 *     fields exist. Allowing arbitrary code before super() could lead to
 *     accessing uninitialized state. JEP 447 relaxes the syntax rule while
 *     keeping the safety guarantee — you still can't touch 'this' before
 *     super() runs."
 *
 * Q: "What can you do before super() now?"
 * A: "Validate parameters (throw early), compute values to pass to super(),
 *     log constructor entry, perform static method calls. Essentially
 *     anything that doesn't require the instance (this)."
 */
public class FlexibleConstructorsDemo {

    public static void main(String[] args) {
        System.out.println("=== Flexible Constructor Bodies (JEP 447) ===\n");

        demoBeforeJava22();
        demoAfterJava22();
        demoComputeBeforeSuper();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BEFORE Java 22: Validation required awkward workarounds
    // ─────────────────────────────────────────────────────────────────────────
    static void demoBeforeJava22() {
        System.out.println("── BEFORE Java 22: Workarounds for pre-super validation ──\n");

        // Problem: PositiveInteger must validate that value > 0 before calling super
        // But super() MUST be the first statement!

        /*
         * class NumberWrapper {
         *     final int value;
         *     NumberWrapper(int value) { this.value = value; }
         * }
         *
         * // DOES NOT COMPILE (pre-Java 22):
         * class PositiveInteger extends NumberWrapper {
         *     PositiveInteger(int value) {
         *         if (value <= 0) throw new IllegalArgumentException("Must be positive");
         *         super(value);  // ERROR: super() must be first statement!
         *     }
         * }
         *
         * // WORKAROUND 1: Static factory method
         * class PositiveInteger extends NumberWrapper {
         *     private PositiveInteger(int value) {
         *         super(value);  // super() is first — compiles
         *     }
         *     static PositiveInteger of(int value) {
         *         if (value <= 0) throw new IllegalArgumentException("Must be positive");
         *         return new PositiveInteger(value);
         *     }
         * }
         *
         * // WORKAROUND 2: Static helper in super() call
         * class PositiveInteger extends NumberWrapper {
         *     PositiveInteger(int value) {
         *         super(validate(value));  // Sneaky validation inside super()
         *     }
         *     private static int validate(int value) {
         *         if (value <= 0) throw new IllegalArgumentException("Must be positive");
         *         return value;
         *     }
         * }
         */

        // Demonstrate workaround 2 (compiles on all Java versions)
        try {
            NumberWrapper good = new PositiveIntegerOld(42);
            System.out.println("Created PositiveIntegerOld(42): value = " + good.value);

            NumberWrapper bad = new PositiveIntegerOld(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("PositiveIntegerOld(-5) threw: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AFTER Java 22+: Clean validation before super()
    // ─────────────────────────────────────────────────────────────────────────
    static void demoAfterJava22() {
        System.out.println("── AFTER Java 22+: Statements before super() ──\n");

        /*
         * // NOW COMPILES (Java 22+):
         * class PositiveInteger extends NumberWrapper {
         *     PositiveInteger(int value) {
         *         if (value <= 0) {
         *             throw new IllegalArgumentException("Must be positive: " + value);
         *         }
         *         System.out.println("Validation passed for: " + value);
         *         super(value);  // super() no longer needs to be first!
         *     }
         * }
         *
         * RULES:
         *   - You CAN: validate parameters, call static methods, create local
         *     variables, log, throw exceptions
         *   - You CANNOT: access 'this', read/write instance fields, call
         *     instance methods — until after super() completes
         *
         * // This is STILL ILLEGAL (even in Java 22+):
         * class Bad extends NumberWrapper {
         *     int extra;
         *     Bad(int value) {
         *         this.extra = 10;   // ERROR: can't access 'this' before super()
         *         super(value);
         *     }
         * }
         */

        // Simulating the new behavior
        try {
            PositiveIntegerNew good = new PositiveIntegerNew(42);
            System.out.println("Created PositiveIntegerNew(42): value = " + good.value);

            PositiveIntegerNew bad = new PositiveIntegerNew(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("PositiveIntegerNew(-5) threw: " + e.getMessage());
        }
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATTERN: Compute a derived value to pass to super()
    // ─────────────────────────────────────────────────────────────────────────
    static void demoComputeBeforeSuper() {
        System.out.println("── Pattern: Compute before super() ──\n");

        /*
         * BEFORE Java 22 — had to use static helper:
         *
         *   class UpperName extends Named {
         *       UpperName(String name) {
         *           super(name.toUpperCase());  // Must inline or use static helper
         *       }
         *   }
         *
         * AFTER Java 22 — can use local variables:
         *
         *   class UpperName extends Named {
         *       UpperName(String name) {
         *           Objects.requireNonNull(name, "name must not be null");
         *           String upper = name.strip().toUpperCase();
         *           if (upper.isEmpty()) throw new IllegalArgumentException("blank name");
         *           super(upper);  // Clean!
         *       }
         *   }
         *
         * RECORD EXAMPLE (also benefits):
         *
         *   record Range(int lo, int hi) {
         *       Range(int lo, int hi) {
         *           if (lo > hi) {
         *               // Swap to ensure lo <= hi
         *               int temp = lo;
         *               lo = hi;
         *               hi = temp;
         *           }
         *           this(lo, hi);  // Not yet supported — records may benefit later
         *       }
         *   }
         *
         * ANOTHER REAL-WORLD EXAMPLE:
         *
         *   class DatabaseConnection extends Connection {
         *       DatabaseConnection(String url) {
         *           // Validate URL format before expensive super() initialization
         *           Objects.requireNonNull(url);
         *           if (!url.startsWith("jdbc:")) {
         *               throw new IllegalArgumentException("Invalid JDBC URL: " + url);
         *           }
         *           String sanitizedUrl = url.strip();
         *           logger.info("Creating connection to: " + sanitizedUrl);
         *           super(sanitizedUrl);
         *       }
         *   }
         */

        UpperName name = new UpperName("  hello world  ");
        System.out.println("UpperName('  hello world  '): " + name.value);
        System.out.println();

        System.out.println("── Summary ──");
        System.out.println("1. Pre-Java 22: super()/this() must be first statement");
        System.out.println("2. Java 22+: statements allowed before super()/this()");
        System.out.println("3. Restriction: cannot access 'this' before super()");
        System.out.println("4. Benefit: cleaner validation, no static helper hacks");
    }

    // ── Supporting classes ────────────────────────────────────────────────────

    static class NumberWrapper {
        final int value;
        NumberWrapper(int value) {
            this.value = value;
        }
    }

    // OLD WAY: static helper method to validate before super()
    static class PositiveIntegerOld extends NumberWrapper {
        PositiveIntegerOld(int value) {
            super(validatePositive(value)); // static helper squished into super()
        }
        private static int validatePositive(int value) {
            if (value <= 0) throw new IllegalArgumentException("Must be positive: " + value);
            return value;
        }
    }

    // NEW WAY (Java 22+ syntax — shown here using the old-style workaround
    // so this file compiles on Java 21, but annotated with the new syntax)
    static class PositiveIntegerNew extends NumberWrapper {
        /*
         * JAVA 22+ SYNTAX (what you would actually write):
         *
         *   PositiveIntegerNew(int value) {
         *       if (value <= 0) {
         *           throw new IllegalArgumentException("Must be positive: " + value);
         *       }
         *       super(value);
         *   }
         */
        PositiveIntegerNew(int value) {
            // Compiled with Java 21 fallback: validation in static helper
            super(requirePositive(value));
        }
        private static int requirePositive(int value) {
            if (value <= 0) throw new IllegalArgumentException("Must be positive: " + value);
            return value;
        }
    }

    static class Named {
        final String value;
        Named(String value) { this.value = value; }
    }

    static class UpperName extends Named {
        /*
         * JAVA 22+ SYNTAX:
         *
         *   UpperName(String name) {
         *       Objects.requireNonNull(name, "name must not be null");
         *       String upper = name.strip().toUpperCase();
         *       if (upper.isEmpty()) throw new IllegalArgumentException("blank name");
         *       super(upper);
         *   }
         */
        UpperName(String name) {
            super(name.strip().toUpperCase());
        }
    }
}
