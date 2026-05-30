package interview.level4_java9to17.switch_expressions;

/**
 * Q13. What is the switch exhaustiveness check?
 *
 * In switch EXPRESSIONS (not statements), the compiler requires ALL possible values
 * to be covered. This is called exhaustiveness checking.
 *
 * When is it required?
 *   - Switch expressions (that return a value): must cover all cases or have default
 *   - Pattern matching switch on sealed types: must cover all permitted subtypes
 *
 * Benefits:
 *   - Compile-time safety: can't forget a case
 *   - No silent bugs from missing cases
 *   - Adding a new enum constant → compile error in all switches (forces handling)
 */
public class SwitchExhaustivenessCheck {

    enum Season { SPRING, SUMMER, FALL, WINTER }

    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double w, double h) implements Shape {}
    record Triangle(double base, double h) implements Shape {}

    public static void main(String[] args) {

        // === Enum exhaustiveness ===
        System.out.println("=== Enum switch expression (must be exhaustive) ===");
        Season season = Season.SUMMER;

        // Switch EXPRESSION — must cover all enum values or have default
        String activity = switch (season) {
            case SPRING -> "Gardening";
            case SUMMER -> "Swimming";
            case FALL -> "Hiking";
            case WINTER -> "Skiing";
            // If you remove any case → COMPILE ERROR (not exhaustive)
        };
        System.out.println(season + " → " + activity);

        // === Sealed type exhaustiveness ===
        System.out.println("\n=== Sealed type switch (must cover all subtypes) ===");
        Shape shape = new Circle(5.0);

        double area = switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.w() * r.h();
            case Triangle t -> 0.5 * t.base() * t.h();
            // No default needed! Compiler knows Circle, Rectangle, Triangle are ALL subtypes
            // Adding a new Shape subtype → compile error here (forces handling)
        };
        System.out.printf("Area of %s: %.2f%n", shape, area);

        // === Switch statement (NOT exhaustive by default) ===
        System.out.println("\n=== Switch STATEMENT (not exhaustive) ===");
        switch (season) {
            case SPRING -> System.out.println("Spring");
            // Missing cases are OK in statements — no compile error
        }

        // === Exhaustiveness with yield ===
        System.out.println("\n=== yield in switch expression ===");
        int numericSeason = switch (season) {
            case SPRING -> 1;
            case SUMMER -> { System.out.println("  (computing...)"); yield 2; }
            case FALL -> 3;
            case WINTER -> 4;
        };
        System.out.println("Season number: " + numericSeason);

        System.out.println("\n=== Summary ===");
        System.out.println("Switch EXPRESSION: must be exhaustive (all cases or default)");
        System.out.println("Switch STATEMENT:  not required to be exhaustive");
        System.out.println("Sealed types:      compiler knows all subtypes → no default needed");
        System.out.println("Enum:              compiler knows all constants → no default needed");
    }
}
