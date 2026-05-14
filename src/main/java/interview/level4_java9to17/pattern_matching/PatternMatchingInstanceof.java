package interview.level4_java9to17.pattern_matching;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 16 Pattern Matching for instanceof
// =============================================================================
//
// Q: "How does pattern matching improve instanceof?"
// A: "It eliminates the separate cast after an instanceof check. The pattern
//     variable is declared directly in the instanceof expression and is
//     automatically cast to the target type. This removes boilerplate, reduces
//     errors (no wrong-type casts), and makes code more readable."
//
// Q: "What are the scope rules for pattern variables?"
// A: "The pattern variable is only in scope where the compiler can prove the
//     instanceof check succeeded. In an if-block, the variable is available
//     inside the true branch. With && (short-circuit), it's available in
//     subsequent conditions. With || or negation, scope is different — the
//     variable is available in the else branch after a negated check."
//
// Q: "Can pattern variables shadow instance fields?"
// A: "Yes, a pattern variable can shadow a field with the same name. This is
//     a common source of confusion and should be avoided for readability."
//
// Q: "How does this relate to pattern matching in switch?"
// A: "instanceof pattern matching (Java 16) was the first step. Java 21 added
//     pattern matching in switch with guards (when clauses), record patterns,
//     and exhaustiveness for sealed types. It's the same concept extended."
//
// =============================================================================

import java.util.List;
import java.util.Objects;

public class PatternMatchingInstanceof {

    // -------------------------------------------------------------------------
    // 1. Before vs After — the classic improvement
    // -------------------------------------------------------------------------
    static void beforeVsAfter() {
        System.out.println("=== 1. Before vs After ===");

        Object obj = "Hello, Pattern Matching!";

        // BEFORE Java 16: instanceof + explicit cast
        if (obj instanceof String) {
            String s = (String) obj;  // redundant cast
            System.out.println("  [Before] Length: " + s.length());
        }

        // AFTER Java 16: pattern variable eliminates the cast
        if (obj instanceof String s) {
            System.out.println("  [After]  Length: " + s.length());
        }

        // More complex before/after
        Object number = 42;

        // Before: verbose and error-prone
        if (number instanceof Integer) {
            Integer i = (Integer) number;
            System.out.println("  [Before] Doubled: " + (i * 2));
        }

        // After: clean
        if (number instanceof Integer i) {
            System.out.println("  [After]  Doubled: " + (i * 2));
        }
    }

    // -------------------------------------------------------------------------
    // 2. Scope rules — where pattern variables are available
    // -------------------------------------------------------------------------
    static void scopeRules() {
        System.out.println("\n=== 2. Scope Rules ===");

        Object obj = "Hello";

        // Pattern variable available in true branch
        if (obj instanceof String s) {
            System.out.println("  In true branch: " + s.toUpperCase());
        }
        // s is NOT available here

        // With && — pattern variable available in subsequent conditions
        if (obj instanceof String s && s.length() > 3) {
            System.out.println("  With &&: long string = " + s);
        }

        // With negation — pattern variable available in else branch
        if (!(obj instanceof String s)) {
            System.out.println("  Not a string");
        } else {
            // s IS available here — compiler knows instanceof succeeded
            System.out.println("  In else after negation: " + s);
        }

        // WARNING: || does NOT work with pattern variables
        // if (obj instanceof String s || s.length() > 0) // COMPILE ERROR
        // because if the left side is false, s is not defined
    }

    // -------------------------------------------------------------------------
    // 3. Guard conditions with &&
    // -------------------------------------------------------------------------
    static void guardConditions() {
        System.out.println("\n=== 3. Guard Conditions ===");

        List<Object> items = List.of("Hello", 42, "", "World", -5, 3.14, "Hi");

        for (Object item : items) {
            if (item instanceof String s && !s.isEmpty()) {
                System.out.println("  Non-empty string: \"" + s + "\"");
            } else if (item instanceof Integer i && i > 0) {
                System.out.println("  Positive integer: " + i);
            } else if (item instanceof Double d) {
                System.out.println("  Double: " + d);
            } else {
                System.out.println("  Other: " + item);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 4. Real-world: equals() method improvement
    // -------------------------------------------------------------------------
    static class Point {
        final int x, y;

        Point(int x, int y) { this.x = x; this.y = y; }

        // BEFORE: traditional equals
        // @Override
        // public boolean equals(Object obj) {
        //     if (this == obj) return true;
        //     if (!(obj instanceof Point)) return false;
        //     Point other = (Point) obj;  // explicit cast
        //     return x == other.x && y == other.y;
        // }

        // AFTER: pattern matching equals — concise and safe
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Point p && x == p.x && y == p.y;
        }

        @Override
        public int hashCode() { return Objects.hash(x, y); }

        @Override
        public String toString() { return "Point(%d, %d)".formatted(x, y); }
    }

    static void improvedEquals() {
        System.out.println("\n=== 4. Improved equals() Method ===");

        var p1 = new Point(3, 4);
        var p2 = new Point(3, 4);
        var p3 = new Point(5, 6);

        System.out.println("  %s.equals(%s) = %s".formatted(p1, p2, p1.equals(p2)));
        System.out.println("  %s.equals(%s) = %s".formatted(p1, p3, p1.equals(p3)));
        System.out.println("  %s.equals(null) = %s".formatted(p1, p1.equals(null)));
        System.out.println("  %s.equals(\"string\") = %s".formatted(p1, p1.equals("string")));
    }

    // -------------------------------------------------------------------------
    // 5. Replacing Visitor pattern and type-checking cascades
    // -------------------------------------------------------------------------
    sealed interface Shape permits CircleShape, RectangleShape, TriangleShape {}
    record CircleShape(double radius) implements Shape {}
    record RectangleShape(double width, double height) implements Shape {}
    record TriangleShape(double base, double height) implements Shape {}

    // Before: Visitor pattern or ugly instanceof chains
    // After: clean pattern matching
    static double calculateArea(Shape shape) {
        if (shape instanceof CircleShape c) {
            return Math.PI * c.radius() * c.radius();
        } else if (shape instanceof RectangleShape r) {
            return r.width() * r.height();
        } else if (shape instanceof TriangleShape t) {
            return 0.5 * t.base() * t.height();
        }
        throw new IllegalArgumentException("Unknown shape");
    }

    static void visitorReplacement() {
        System.out.println("\n=== 5. Replacing Visitor Pattern ===");

        List<Shape> shapes = List.of(
                new CircleShape(5),
                new RectangleShape(4, 6),
                new TriangleShape(3, 8)
        );

        for (var shape : shapes) {
            System.out.println("  %s -> area=%.2f".formatted(shape, calculateArea(shape)));
        }
    }

    // -------------------------------------------------------------------------
    // 6. Pattern matching in expressions and returns
    // -------------------------------------------------------------------------
    static void inExpressionsAndReturns() {
        System.out.println("\n=== 6. In Expressions and Returns ===");

        // In ternary-like expressions (using if-else returning)
        Object obj = List.of(1, 2, 3);

        // Pattern variable used directly in expression
        int size = obj instanceof List<?> list ? list.size() : -1;
        System.out.println("  Size: " + size);

        // In method calls
        Object name = "Alice";
        System.out.println("  Upper: " + (name instanceof String s ? s.toUpperCase() : "N/A"));

        // Chained checks
        Object data = 42;
        String result;
        if (data instanceof Integer i && i > 0 && i < 100) {
            result = "Small positive int: " + i;
        } else if (data instanceof Integer i) {
            result = "Other int: " + i;
        } else if (data instanceof String s && s.matches("\\d+")) {
            result = "Numeric string: " + s;
        } else {
            result = "Unknown: " + data;
        }
        System.out.println("  " + result);
    }

    // -------------------------------------------------------------------------
    // 7. Common mistakes and gotchas
    // -------------------------------------------------------------------------
    static void gotchas() {
        System.out.println("\n=== 7. Gotchas ===");

        // Gotcha 1: || does NOT work (pattern var might not be assigned)
        System.out.println("  1. (obj instanceof String s || s.isEmpty()) -> COMPILE ERROR");
        System.out.println("     Because if left is false, s is uninitialized");

        // Gotcha 2: final types — pointless pattern matching
        String definitelyString = "hello";
        // if (definitelyString instanceof String s) — works but pointless
        // The variable type is already known
        System.out.println("  2. Don't use pattern matching when the type is already known");

        // Gotcha 3: variable shadowing
        System.out.println("  3. Pattern variables can shadow fields — avoid for clarity");

        // Gotcha 4: generics are erased
        Object list = List.of(1, 2, 3);
        if (list instanceof List<?> l) { // OK: wildcard
            System.out.println("  4. Generic pattern: List<?> works: size=" + l.size());
        }
        // if (list instanceof List<String> l) // COMPILE ERROR: cannot check generic
        System.out.println("     List<String> pattern -> COMPILE ERROR (type erasure)");
    }

    // -------------------------------------------------------------------------
    // 8. Progression: instanceof -> switch pattern matching (Java 21)
    // -------------------------------------------------------------------------
    static void progression() {
        System.out.println("\n=== 8. Evolution of Pattern Matching ===");
        System.out.println("  Java 16: instanceof pattern matching (this file)");
        System.out.println("           obj instanceof String s");
        System.out.println();
        System.out.println("  Java 21: switch pattern matching");
        System.out.println("           switch (obj) { case String s -> ... }");
        System.out.println();
        System.out.println("  Java 21: record patterns (deconstruction)");
        System.out.println("           case Point(var x, var y) -> x + y");
        System.out.println();
        System.out.println("  Java 21: guarded patterns");
        System.out.println("           case String s when s.length() > 5 -> ...");
        System.out.println();
        System.out.println("  Future:  unnamed patterns, primitive patterns");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 16: Pattern Matching for instanceof      ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        beforeVsAfter();
        scopeRules();
        guardConditions();
        improvedEquals();
        visitorReplacement();
        inExpressionsAndReturns();
        gotchas();
        progression();
    }
}
