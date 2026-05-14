package interview.level5_java17to21.pattern_matching_switch;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: Pattern Matching for switch — Java 21
// =====================================================================
//
// Q: "How does pattern matching for switch work with sealed classes?"
// A: "Sealed classes define a closed set of subtypes. The compiler knows
//     all possible cases, so it enforces exhaustiveness — every subtype
//     must be handled. No default branch is needed if all permitted
//     subtypes are covered. This makes the code safer: adding a new
//     subtype causes a compile error in every switch that doesn't handle
//     it."
//
// Q: "What are guarded patterns (when clause)?"
// A: "A guarded pattern adds a boolean condition after the pattern:
//       case Circle c when c.radius() > 10 -> ...
//     The case matches only if the type matches AND the guard is true.
//     Guards replace the old pattern of type-check + if-else chains."
//
// Q: "How does null handling work in switch?"
// A: "Before Java 21, passing null to switch threw NullPointerException.
//     Now you can add 'case null' explicitly:
//       case null -> handleNull();
//     Or combine it: 'case null, default -> ...'
//     This eliminates the need for a null check before the switch."
//
// Q: "What is the difference between switch expressions and switch
//     statements with pattern matching?"
// A: "Switch expressions return a value (var x = switch(y) { ... }).
//     They require exhaustiveness. Switch statements don't return a value
//     and don't require exhaustiveness (but the compiler warns). Pattern
//     matching works in both, but switch expressions are preferred."
//
// COMPILE: javac PatternMatchingSwitchDemo.java
// RUN:     java PatternMatchingSwitchDemo
// =====================================================================

public class PatternMatchingSwitchDemo {

    // ---------------------------------------------------------------
    // Sealed hierarchy for exhaustive switch
    // ---------------------------------------------------------------
    sealed interface Shape permits Circle, Rectangle, Triangle {}

    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    // ---------------------------------------------------------------
    // Another sealed hierarchy — Payment types
    // ---------------------------------------------------------------
    sealed interface Payment permits CreditCard, BankTransfer, Crypto {}

    record CreditCard(String number, double amount) implements Payment {}
    record BankTransfer(String iban, double amount) implements Payment {}
    record Crypto(String wallet, String coin, double amount) implements Payment {}

    // ---------------------------------------------------------------
    // 1. Basic pattern matching in switch
    // ---------------------------------------------------------------
    static void basicPatternMatching() {
        System.out.println("=== 1. Basic Pattern Matching in switch ===\n");

        Object[] values = {"Hello", 42, 3.14, true, null, new int[]{1, 2, 3}};

        for (Object obj : values) {
            String result = switch (obj) {
                case null         -> "null value";
                case Integer i    -> "Integer: " + i;
                case String s     -> "String of length " + s.length() + ": " + s;
                case Double d     -> "Double: " + d;
                case Boolean b    -> "Boolean: " + b;
                case int[] arr    -> "int[] of length " + arr.length;
                default           -> "Unknown: " + obj.getClass().getSimpleName();
            };
            System.out.println("  " + result);
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 2. Sealed classes + exhaustive switch
    // ---------------------------------------------------------------
    static double area(Shape shape) {
        // No default needed — compiler knows all subtypes of sealed Shape
        return switch (shape) {
            case Circle c     -> Math.PI * c.radius() * c.radius();
            case Rectangle r  -> r.width() * r.height();
            case Triangle t   -> 0.5 * t.base() * t.height();
        };
    }

    static void sealedClassSwitch() {
        System.out.println("=== 2. Sealed Classes + Exhaustive switch ===\n");

        Shape[] shapes = {
                new Circle(5),
                new Rectangle(4, 6),
                new Triangle(3, 8)
        };

        for (Shape s : shapes) {
            System.out.printf("  %-30s area = %.2f%n", s, area(s));
        }
        System.out.println();
        System.out.println("  Key point: If you add a new Shape subtype, every switch");
        System.out.println("  that doesn't handle it will fail to compile.\n");
    }

    // ---------------------------------------------------------------
    // 3. Guarded patterns (when clause)
    // ---------------------------------------------------------------
    static String classifyShape(Shape shape) {
        return switch (shape) {
            case Circle c when c.radius() > 100    -> "Huge circle (r=" + c.radius() + ")";
            case Circle c when c.radius() > 10     -> "Large circle (r=" + c.radius() + ")";
            case Circle c                          -> "Small circle (r=" + c.radius() + ")";
            case Rectangle r when r.width() == r.height() -> "Square (" + r.width() + "x" + r.height() + ")";
            case Rectangle r                       -> "Rectangle (" + r.width() + "x" + r.height() + ")";
            case Triangle t when t.base() == t.height()   -> "Isoceles-ish triangle";
            case Triangle t                        -> "Triangle (b=" + t.base() + ", h=" + t.height() + ")";
        };
    }

    static void guardedPatterns() {
        System.out.println("=== 3. Guarded Patterns (when clause) ===\n");

        Shape[] shapes = {
                new Circle(3),
                new Circle(25),
                new Circle(200),
                new Rectangle(5, 5),
                new Rectangle(4, 6),
                new Triangle(7, 7),
                new Triangle(3, 8)
        };

        for (Shape s : shapes) {
            System.out.println("  " + classifyShape(s));
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 4. Null handling in switch
    // ---------------------------------------------------------------
    static void nullHandling() {
        System.out.println("=== 4. Null Handling in switch ===\n");

        // BEFORE Java 21: had to null-check before switch
        String beforeStyle = nullCheckBefore(null);
        System.out.println("  Before (manual null check): " + beforeStyle);

        // AFTER Java 21: null is a valid case label
        String afterStyle = nullCheckInSwitch(null);
        System.out.println("  After  (case null):         " + afterStyle);

        // Combined null + default
        String combined = nullPlusDefault("hello");
        System.out.println("  Combined (null, default):   " + combined);
        combined = nullPlusDefault(null);
        System.out.println("  Combined (null, default):   " + combined);
        System.out.println();
    }

    // Old way
    static String nullCheckBefore(Object obj) {
        if (obj == null) return "was null";
        return switch (obj) {
            case String s  -> "String: " + s;
            case Integer i -> "Integer: " + i;
            default        -> "other";
        };
    }

    // New way: case null
    static String nullCheckInSwitch(Object obj) {
        return switch (obj) {
            case null      -> "was null (handled in switch!)";
            case String s  -> "String: " + s;
            case Integer i -> "Integer: " + i;
            default        -> "other";
        };
    }

    // Combined null + default
    static String nullPlusDefault(Object obj) {
        return switch (obj) {
            case String s  -> "String: " + s;
            case Integer i -> "Integer: " + i;
            case null, default -> "null or unrecognized";
        };
    }

    // ---------------------------------------------------------------
    // 5. Complex real-world example — Payment processing
    // ---------------------------------------------------------------
    static String processPayment(Payment payment) {
        return switch (payment) {
            case CreditCard cc when cc.amount() > 10_000 ->
                    "CC HIGH-VALUE: $" + cc.amount() + " on card ending " +
                            cc.number().substring(cc.number().length() - 4) +
                            " → requires additional verification";

            case CreditCard cc ->
                    "CC: $" + cc.amount() + " on card ending " +
                            cc.number().substring(cc.number().length() - 4);

            case BankTransfer bt when bt.amount() > 50_000 ->
                    "WIRE HIGH-VALUE: $" + bt.amount() + " to " + bt.iban() +
                            " → compliance review required";

            case BankTransfer bt ->
                    "WIRE: $" + bt.amount() + " to " + bt.iban();

            case Crypto cr when cr.coin().equals("BTC") && cr.amount() > 1 ->
                    "CRYPTO WHALE: " + cr.amount() + " " + cr.coin() +
                            " to " + cr.wallet().substring(0, 8) + "...";

            case Crypto cr ->
                    "CRYPTO: " + cr.amount() + " " + cr.coin() +
                            " to " + cr.wallet().substring(0, 8) + "...";
        };
    }

    static void paymentProcessing() {
        System.out.println("=== 5. Real-World Example: Payment Processing ===\n");

        Payment[] payments = {
                new CreditCard("4111111111111111", 250.00),
                new CreditCard("5500000000000004", 15_000.00),
                new BankTransfer("DE89370400440532013000", 1_000.00),
                new BankTransfer("GB29NWBK60161331926819", 75_000.00),
                new Crypto("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", "BTC", 0.5),
                new Crypto("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", "BTC", 2.5),
        };

        for (Payment p : payments) {
            System.out.println("  " + processPayment(p));
        }
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 6. Before vs After comparison
    // ---------------------------------------------------------------
    static void beforeAfterComparison() {
        System.out.println("=== 6. Before vs After Comparison ===\n");

        System.out.println("  BEFORE (Java 11 style):");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println("  if (obj instanceof String) {");
        System.out.println("      String s = (String) obj;");
        System.out.println("      return s.length();");
        System.out.println("  } else if (obj instanceof Integer) {");
        System.out.println("      Integer i = (Integer) obj;");
        System.out.println("      if (i > 0) return \"positive\";");
        System.out.println("      else return \"non-positive\";");
        System.out.println("  } else if (obj == null) {");
        System.out.println("      return \"null\";");
        System.out.println("  } else {");
        System.out.println("      return \"unknown\";");
        System.out.println("  }");
        System.out.println();
        System.out.println("  AFTER (Java 21 style):");
        System.out.println("  ─────────────────────────────────────────────");
        System.out.println("  return switch (obj) {");
        System.out.println("      case String s             -> s.length();");
        System.out.println("      case Integer i when i > 0 -> \"positive\";");
        System.out.println("      case Integer i             -> \"non-positive\";");
        System.out.println("      case null                  -> \"null\";");
        System.out.println("      default                    -> \"unknown\";");
        System.out.println("  };");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   Pattern Matching for switch — Java 21             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        basicPatternMatching();
        sealedClassSwitch();
        guardedPatterns();
        nullHandling();
        paymentProcessing();
        beforeAfterComparison();

        System.out.println("=== Done ===");
    }
}
