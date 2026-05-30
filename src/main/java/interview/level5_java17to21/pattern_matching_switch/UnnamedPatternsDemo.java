package interview.level5_java17to21.pattern_matching_switch;

/**
 * Q12. What are Unnamed Patterns and Variables (Java 21)?
 *
 * Unnamed variables use _ (underscore) to indicate a variable that is intentionally unused.
 *
 * Before Java 21:
 *   catch (Exception e) { log("error"); }    // 'e' is unused
 *   for (var entry : map.entrySet()) { ... }  // sometimes you only need key or value
 *   (String name, int _age) -> name           // '_age' unused but must be named
 *
 * After Java 21:
 *   catch (Exception _) { log("error"); }    // clearly intentional
 *   case Box(RedBall _) -> "red box"          // don't need the ball details
 *
 * Where you can use _:
 *   - catch clauses: catch (Exception _)
 *   - for-each: for (var _ : list) count++
 *   - Lambda parameters: (a, _) -> a
 *   - Pattern matching: case Pair(var x, var _) -> x
 *   - try-with-resources: try (var _ = connection())
 *   - Assignment: var _ = sideEffectMethod()
 *
 * NOT for: local variables you use, method parameters you reference, fields
 */
public class UnnamedPatternsDemo {

    sealed interface Shape permits Circle, Square {}
    record Circle(double radius, String color) implements Shape {}
    record Square(double side, String color) implements Shape {}

    record Pair<A, B>(A first, B second) {}

    public static void main(String[] args) {

        // === Unnamed in catch ===
        System.out.println("=== Unnamed in catch ===");
        try {
            Integer.parseInt("not-a-number");
        } catch (NumberFormatException _) {  // we don't use the exception
            System.out.println("  Invalid number (exception intentionally unused)");
        }

        // === Unnamed in pattern matching ===
        System.out.println("\n=== Unnamed in pattern matching ===");
        Shape shape = new Circle(5.0, "red");

        // Only care about the type, not the fields
        String description = switch (shape) {
            case Circle _ -> "It's a circle";
            case Square _ -> "It's a square";
        };
        System.out.println("  " + description);

        // Care about some fields, not others
        String info = switch (shape) {
            case Circle(var radius, _) -> "Circle with radius " + radius;  // ignore color
            case Square(var side, _) -> "Square with side " + side;
        };
        System.out.println("  " + info);

        // === Unnamed in records destructuring ===
        System.out.println("\n=== Unnamed in record patterns ===");
        Pair<String, Integer> pair = new Pair<>("Karan", 30);

        if (pair instanceof Pair(var name, _)) {  // only need the name
            System.out.println("  Name: " + name);
        }

        // === Unnamed in for-each (counting) ===
        System.out.println("\n=== Unnamed in for-each ===");
        var items = java.util.List.of("a", "b", "c", "d");
        int count = 0;
        for (var _ : items) {  // don't need the element, just counting
            count++;
        }
        System.out.println("  Count: " + count);

        // === Unnamed in lambda ===
        System.out.println("\n=== Unnamed in lambda ===");
        java.util.Map.of("a", 1, "b", 2).forEach((_, value) ->
                System.out.println("  Value: " + value)  // don't need the key
        );

        // === Summary ===
        System.out.println("\n=== Summary ===");
        System.out.println("_ (underscore) = explicitly unused variable");
        System.out.println("Benefits:");
        System.out.println("  1. Clearer intent: reader knows it's intentionally unused");
        System.out.println("  2. No IDE warnings about unused variables");
        System.out.println("  3. Cleaner destructuring in pattern matching");
    }
}
