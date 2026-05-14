package interview.level5_java17to21.record_patterns;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: Record Patterns — Java 21
// =====================================================================
//
// Q: "What are record patterns?"
// A: "Record patterns allow you to destructure a record's components
//     directly in instanceof checks and switch expressions. Instead of
//     matching the record and then calling accessor methods, you extract
//     the components inline:
//       if (obj instanceof Point(int x, int y)) { use x, y directly }
//     This eliminates boilerplate and makes the code more readable."
//
// Q: "Can record patterns be nested?"
// A: "Yes. If a record contains another record, you can destructure
//     both in a single pattern:
//       case Line(Point(var x1, var y1), Point(var x2, var y2)) -> ...
//     The compiler recursively matches and extracts components."
//
// Q: "How do record patterns work with sealed classes?"
// A: "Sealed interfaces with record implementations enable exhaustive
//     pattern matching with destructuring. The compiler checks that all
//     permitted subtypes are handled."
//
// Q: "What is the difference between record patterns and deconstruction
//     patterns?"
// A: "They are the same thing. 'Record pattern' is the official JEP
//     term. 'Deconstruction pattern' is the conceptual name — it
//     destructs (decomposes) a record into its components."
//
// COMPILE: javac RecordPatternsDemo.java
// RUN:     java RecordPatternsDemo
// =====================================================================

public class RecordPatternsDemo {

    // ---------------------------------------------------------------
    // Record definitions
    // ---------------------------------------------------------------
    record Point(int x, int y) {}
    record Line(Point start, Point end) {}
    record Circle(Point center, double radius) {}
    record ColoredPoint(Point point, String color) {}
    record Rectangle(Point topLeft, Point bottomRight) {}

    // Sealed hierarchy with records
    sealed interface Expr permits Num, Add, Mul, Neg {}
    record Num(double value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}
    record Neg(Expr operand) implements Expr {}

    // Generic record
    record Pair<A, B>(A first, B second) {}

    // ---------------------------------------------------------------
    // 1. Basic record patterns in instanceof
    // ---------------------------------------------------------------
    static void basicRecordPattern() {
        System.out.println("=== 1. Basic Record Patterns in instanceof ===\n");

        Object obj = new Point(3, 7);

        // BEFORE: match then extract
        System.out.println("  BEFORE (Java 16 pattern matching for instanceof):");
        if (obj instanceof Point p) {
            System.out.println("    Point at (" + p.x() + ", " + p.y() + ")");
        }

        // AFTER: destructure directly
        System.out.println("  AFTER  (Java 21 record patterns):");
        if (obj instanceof Point(int x, int y)) {
            System.out.println("    Point at (" + x + ", " + y + ")");
        }

        // With var inference
        System.out.println("  With var:");
        if (obj instanceof Point(var x, var y)) {
            System.out.println("    Point at (" + x + ", " + y + ")");
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 2. Nested record patterns
    // ---------------------------------------------------------------
    static void nestedRecordPatterns() {
        System.out.println("=== 2. Nested Record Patterns ===\n");

        Object obj = new ColoredPoint(new Point(5, 10), "red");

        // Single-level destructure
        if (obj instanceof ColoredPoint(Point p, String color)) {
            System.out.println("  Single-level: Point=" + p + ", color=" + color);
        }

        // Nested destructure — decompose Point too
        if (obj instanceof ColoredPoint(Point(var x, var y), var color)) {
            System.out.println("  Nested:       x=" + x + ", y=" + y + ", color=" + color);
        }

        // Deep nesting with Line
        var line = new Line(new Point(0, 0), new Point(10, 20));
        if (line instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            System.out.println("  Line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2
                    + ") → length=" + String.format("%.2f", length));
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 3. Record patterns in switch
    // ---------------------------------------------------------------
    static void recordPatternsInSwitch() {
        System.out.println("=== 3. Record Patterns in switch ===\n");

        Object[] shapes = {
                new Point(0, 0),
                new Circle(new Point(5, 5), 3.0),
                new Rectangle(new Point(0, 10), new Point(10, 0)),
                new Line(new Point(1, 1), new Point(4, 5)),
                "not a shape"
        };

        for (Object shape : shapes) {
            String desc = switch (shape) {
                // Destructure Point
                case Point(var x, var y) ->
                        "Point at (" + x + ", " + y + ")";

                // Destructure Circle, including nested Point
                case Circle(Point(var cx, var cy), var r) ->
                        "Circle at (" + cx + ", " + cy + ") with radius " + r;

                // Destructure Rectangle with nested Points
                case Rectangle(Point(var x1, var y1), Point(var x2, var y2)) ->
                        "Rectangle from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2
                                + ") area=" + Math.abs((x2 - x1) * (y1 - y2));

                // Destructure Line
                case Line(Point(var x1, var y1), Point(var x2, var y2)) ->
                        "Line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")";

                default -> "Unknown: " + shape;
            };
            System.out.println("  " + desc);
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 4. Record patterns with guarded patterns (when clause)
    // ---------------------------------------------------------------
    static void guardedRecordPatterns() {
        System.out.println("=== 4. Record Patterns + Guarded Patterns ===\n");

        Point[] points = {
                new Point(0, 0),
                new Point(3, 0),
                new Point(0, 5),
                new Point(4, 7),
                new Point(-2, -3)
        };

        for (Point p : points) {
            String quadrant = switch (p) {
                case Point(var x, var y) when x == 0 && y == 0 -> "Origin";
                case Point(var x, var y) when x == 0           -> "Y-axis";
                case Point(var x, var y) when y == 0           -> "X-axis";
                case Point(var x, var y) when x > 0 && y > 0   -> "Quadrant I";
                case Point(var x, var y) when x < 0 && y > 0   -> "Quadrant II";
                case Point(var x, var y) when x < 0 && y < 0   -> "Quadrant III";
                case Point(var x, var y)                        -> "Quadrant IV";
            };
            System.out.println("  " + p + " → " + quadrant);
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 5. Sealed hierarchy + record patterns — expression evaluator
    // ---------------------------------------------------------------
    static double evaluate(Expr expr) {
        return switch (expr) {
            case Num(var v)                     -> v;
            case Add(var left, var right)        -> evaluate(left) + evaluate(right);
            case Mul(var left, var right)        -> evaluate(left) * evaluate(right);
            case Neg(var operand)                -> -evaluate(operand);
        };
    }

    static String prettyPrint(Expr expr) {
        return switch (expr) {
            case Num(var v)                -> String.valueOf(v);
            case Add(var l, var r)         -> "(" + prettyPrint(l) + " + " + prettyPrint(r) + ")";
            case Mul(var l, var r)         -> "(" + prettyPrint(l) + " * " + prettyPrint(r) + ")";
            case Neg(var op)               -> "-(" + prettyPrint(op) + ")";
        };
    }

    static void expressionEvaluator() {
        System.out.println("=== 5. Sealed Hierarchy + Record Patterns: Expression Evaluator ===\n");

        // (3 + 4) * -(2)
        Expr expr = new Mul(
                new Add(new Num(3), new Num(4)),
                new Neg(new Num(2))
        );

        System.out.println("  Expression: " + prettyPrint(expr));
        System.out.println("  Result:     " + evaluate(expr));

        // Nested: (1 + 2) + (3 * 4)
        Expr expr2 = new Add(
                new Add(new Num(1), new Num(2)),
                new Mul(new Num(3), new Num(4))
        );

        System.out.println("  Expression: " + prettyPrint(expr2));
        System.out.println("  Result:     " + evaluate(expr2));
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 6. Generic record patterns
    // ---------------------------------------------------------------
    static void genericRecordPatterns() {
        System.out.println("=== 6. Generic Record Patterns ===\n");

        Pair<String, Integer> nameAge = new Pair<>("Karan", 30);
        Pair<Point, String> labeledPoint = new Pair<>(new Point(1, 2), "Home");

        // Destructure generic record
        if (nameAge instanceof Pair<String, Integer>(var name, var age)) {
            System.out.println("  Name: " + name + ", Age: " + age);
        }

        // Nested generic + record destructuring
        if (labeledPoint instanceof Pair<Point, String>(Point(var x, var y), var label)) {
            System.out.println("  Label: " + label + " at (" + x + ", " + y + ")");
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 7. Before vs After — full comparison
    // ---------------------------------------------------------------
    static void beforeAfterComparison() {
        System.out.println("=== 7. Before vs After — Code Comparison ===\n");

        System.out.println("  BEFORE (Java 16 — instanceof with binding variable):");
        System.out.println("  ─────────────────────────────────────────────────────");
        System.out.println("  if (obj instanceof Line line) {");
        System.out.println("      Point start = line.start();");
        System.out.println("      Point end = line.end();");
        System.out.println("      int x1 = start.x();");
        System.out.println("      int y1 = start.y();");
        System.out.println("      int x2 = end.x();");
        System.out.println("      int y2 = end.y();");
        System.out.println("      // now use x1, y1, x2, y2");
        System.out.println("  }");
        System.out.println();
        System.out.println("  AFTER (Java 21 — record pattern with nested destructuring):");
        System.out.println("  ─────────────────────────────────────────────────────");
        System.out.println("  if (obj instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {");
        System.out.println("      // x1, y1, x2, y2 available directly — 1 line vs 6!");
        System.out.println("  }");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   Record Patterns Demo — Java 21                    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        basicRecordPattern();
        nestedRecordPatterns();
        recordPatternsInSwitch();
        guardedRecordPatterns();
        expressionEvaluator();
        genericRecordPatterns();
        beforeAfterComparison();

        System.out.println("=== Done ===");
    }
}
