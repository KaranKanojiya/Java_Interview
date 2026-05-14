package interview.level4_java9to17.switch_expressions;

import java.util.List;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 14 Switch Expressions & Pattern Matching Preview
// =============================================================================
//
// Q: "What's the difference between a switch statement and a switch expression?"
// A: "A switch statement executes code blocks and uses break. A switch expression
//     returns a value, uses arrow (->) syntax, doesn't fall through, and the
//     compiler enforces exhaustiveness. If you assign the result to a variable,
//     the switch MUST cover all possible values."
//
// Q: "What is the yield keyword?"
// A: "yield is used inside a switch expression when a case arm uses a block
//     (curly braces) instead of a single expression. It returns a value from
//     that block. Think of it as 'return' for switch expressions."
//
// Q: "What does exhaustiveness mean in switch expressions?"
// A: "The compiler verifies that every possible input value is handled. For enums,
//     all constants must be covered (or use default). For sealed classes, all
//     permitted subtypes must be covered. This turns runtime errors into
//     compile-time errors."
//
// Q: "Can you mix arrow syntax and colon syntax in the same switch?"
// A: "No. A single switch must use either all arrow cases (->) or all colon
//     cases (:). Mixing is a compile error."
//
// =============================================================================

public class SwitchExpressions {

    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    enum Shape { CIRCLE, RECTANGLE, TRIANGLE }

    // -------------------------------------------------------------------------
    // 1. Old switch statement — fall-through, verbose, error-prone
    // -------------------------------------------------------------------------
    static void oldSwitchStatement() {
        System.out.println("=== 1. Old Switch Statement (Pre-Java 14) ===");

        Day day = Day.WED;
        String type;

        // Classic switch — requires break, falls through without it
        switch (day) {
            case MON:
            case TUE:
            case WED:
            case THU:
            case FRI:
                type = "Weekday";
                break;
            case SAT:
            case SUN:
                type = "Weekend";
                break;
            default:
                type = "Unknown";
        }
        System.out.println(day + " is a " + type);

        // Problem: forgot break? Silent fall-through bug.
    }

    // -------------------------------------------------------------------------
    // 2. New switch expression — arrow syntax, no fall-through
    // -------------------------------------------------------------------------
    static void newSwitchExpression() {
        System.out.println("\n=== 2. New Switch Expression (Java 14) ===");

        Day day = Day.SAT;

        // Switch expression returns a value directly
        String type = switch (day) {
            case MON, TUE, WED, THU, FRI -> "Weekday";
            case SAT, SUN -> "Weekend";
        };
        System.out.println(day + " is a " + type);

        // Multiple values per case, no fall-through, exhaustive
        int numLetters = switch (day) {
            case MON, FRI, SUN -> 3;
            case TUE -> 3;
            case WED, THU, SAT -> 3;
        };
        System.out.println(day + " name has " + numLetters + " letters");
    }

    // -------------------------------------------------------------------------
    // 3. yield keyword — for multi-statement case arms
    // -------------------------------------------------------------------------
    static void yieldKeyword() {
        System.out.println("\n=== 3. yield Keyword ===");

        Day day = Day.FRI;

        // When a case needs a block, use yield to return the value
        String mood = switch (day) {
            case MON -> "Terrible";
            case TUE, WED, THU -> "Okay";
            case FRI -> {
                System.out.println("  (Calculating Friday mood...)");
                var base = "Good";
                yield base + " — TGIF!";  // yield returns from the block
            }
            case SAT, SUN -> "Excellent";
        };
        System.out.println("Mood on " + day + ": " + mood);
    }

    // -------------------------------------------------------------------------
    // 4. Exhaustiveness checking
    // -------------------------------------------------------------------------
    static void exhaustivenessChecking() {
        System.out.println("\n=== 4. Exhaustiveness Checking ===");

        Shape shape = Shape.CIRCLE;

        // Compiler enforces: all enum values must be covered
        double area = switch (shape) {
            case CIRCLE    -> Math.PI * 5 * 5;
            case RECTANGLE -> 4.0 * 6.0;
            case TRIANGLE  -> 0.5 * 3.0 * 4.0;
            // If you remove any case, compiler error!
            // "the switch expression does not cover all possible input values"
        };
        System.out.println(shape + " area: " + String.format("%.2f", area));

        // For non-enum types (int, String), default is required
        int code = 200;
        String status = switch (code) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 500 -> "Server Error";
            default  -> "Unknown (" + code + ")";
        };
        System.out.println("HTTP " + code + ": " + status);
    }

    // -------------------------------------------------------------------------
    // 5. Switch expression with return type inference
    // -------------------------------------------------------------------------
    static void switchReturnTypeInference() {
        System.out.println("\n=== 5. Return Type Inference ===");

        Day day = Day.SAT;

        // Compiler infers common type across all branches
        // All branches return the same type (or compatible types)
        Number value = switch (day) {
            case MON -> 1;        // int, autoboxed to Integer
            case TUE -> 2.0;     // double, autoboxed to Double
            case WED -> 3L;      // long, autoboxed to Long
            default  -> 0;
        };
        System.out.println("Value: " + value + " (type: " + value.getClass().getSimpleName() + ")");
    }

    // -------------------------------------------------------------------------
    // 6. Arrow syntax as statement (not expression) — for side effects
    // -------------------------------------------------------------------------
    static void arrowSyntaxAsStatement() {
        System.out.println("\n=== 6. Arrow Syntax as Statement ===");

        Day day = Day.TUE;

        // Arrow syntax can also be used in switch statements (not just expressions)
        // No fall-through, but no return value either
        switch (day) {
            case MON       -> System.out.println("  Start of work week");
            case TUE, WED  -> System.out.println("  Midweek grind");
            case THU       -> System.out.println("  Almost Friday");
            case FRI       -> System.out.println("  TGIF!");
            case SAT, SUN  -> System.out.println("  Weekend vibes");
        }
    }

    // -------------------------------------------------------------------------
    // 7. Pattern matching in switch (Java 17 preview, standard Java 21)
    // -------------------------------------------------------------------------
    static void patternMatchingInSwitch() {
        System.out.println("\n=== 7. Pattern Matching in Switch (Java 21) ===");

        // Pattern matching for type checking in switch
        Object obj = "Hello, World!";

        String result = switch (obj) {
            case Integer i -> "Integer: " + i;
            case Long l    -> "Long: " + l;
            case Double d  -> "Double: " + String.format("%.2f", d);
            case String s when s.length() > 10 -> "Long string: \"" + s + "\"";
            case String s  -> "Short string: \"" + s + "\"";
            case null      -> "null value";
            default        -> "Other: " + obj.getClass().getSimpleName();
        };
        System.out.println(result);

        // Demonstrate null handling (Java 21)
        printType(42);
        printType("test");
        printType(3.14);
        printType(null);
        printType(List.of(1, 2, 3));
    }

    private static void printType(Object obj) {
        String desc = switch (obj) {
            case Integer i when i > 0  -> "positive int: " + i;
            case Integer i             -> "non-positive int: " + i;
            case String s              -> "string of length " + s.length();
            case Double d              -> "double: " + d;
            case null                  -> "null!";
            default                    -> "other: " + obj.getClass().getSimpleName();
        };
        System.out.println("  " + desc);
    }

    // -------------------------------------------------------------------------
    // 8. Sealed classes + switch = exhaustive type matching
    // -------------------------------------------------------------------------
    sealed interface Expr permits Num, Add, Mul {}
    record Num(int value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}

    static int evaluate(Expr expr) {
        return switch (expr) {
            case Num n   -> n.value();
            case Add a   -> evaluate(a.left()) + evaluate(a.right());
            case Mul m   -> evaluate(m.left()) * evaluate(m.right());
            // No default needed — sealed interface is exhaustive!
        };
    }

    static void sealedWithSwitch() {
        System.out.println("\n=== 8. Sealed Classes + Switch (Exhaustive) ===");

        // (2 + 3) * 4
        Expr expr = new Mul(new Add(new Num(2), new Num(3)), new Num(4));
        System.out.println("(2 + 3) * 4 = " + evaluate(expr));

        // 1 + (2 * 3)
        Expr expr2 = new Add(new Num(1), new Mul(new Num(2), new Num(3)));
        System.out.println("1 + (2 * 3) = " + evaluate(expr2));
    }

    // -------------------------------------------------------------------------
    // 9. Comparison summary
    // -------------------------------------------------------------------------
    static void comparisonSummary() {
        System.out.println("\n=== 9. Old vs New Switch Comparison ===");
        System.out.println("┌─────────────────────┬──────────────────┬──────────────────┐");
        System.out.println("│ Feature             │ Old (statement)  │ New (expression) │");
        System.out.println("├─────────────────────┼──────────────────┼──────────────────┤");
        System.out.println("│ Syntax              │ case X:          │ case X ->        │");
        System.out.println("│ Fall-through         │ Yes (default)    │ No               │");
        System.out.println("│ Returns value?       │ No               │ Yes              │");
        System.out.println("│ Exhaustive?          │ No               │ Yes (required)   │");
        System.out.println("│ Multiple labels      │ Stacked cases    │ case A, B ->     │");
        System.out.println("│ Block return         │ break            │ yield            │");
        System.out.println("│ Null handling         │ NPE              │ case null ->     │");
        System.out.println("└─────────────────────┴──────────────────┴──────────────────┘");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 14-21: Switch Expressions & Patterns     ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        oldSwitchStatement();
        newSwitchExpression();
        yieldKeyword();
        exhaustivenessChecking();
        switchReturnTypeInference();
        arrowSyntaxAsStatement();
        patternMatchingInSwitch();
        sealedWithSwitch();
        comparisonSummary();
    }
}
