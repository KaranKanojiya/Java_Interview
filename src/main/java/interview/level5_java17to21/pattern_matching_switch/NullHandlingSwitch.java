package interview.level5_java17to21.pattern_matching_switch;

/**
 * Q14. How does null handling work in switch (Java 21)?
 *
 * Before Java 21:
 *   switch(obj) always threw NullPointerException if obj was null
 *   → Required null check BEFORE switch
 *
 * Java 21:
 *   - case null → explicitly handle null in switch
 *   - case null, default → combine null with default
 *   - No more NPE from switch!
 *
 * This works with both switch expressions and switch statements.
 */
public class NullHandlingSwitch {

    sealed interface Animal permits Dog, Cat, Bird {}
    record Dog(String name) implements Animal {}
    record Cat(String name) implements Animal {}
    record Bird(String name) implements Animal {}

    public static void main(String[] args) {

        // === Before Java 21: NPE danger ===
        System.out.println("=== Before Java 21 ===");
        String value = null;
        // Old way: must check null before switch
        if (value != null) {
            // switch (value) { ... }
        }
        System.out.println("Had to check null BEFORE switch to avoid NPE");

        // === Java 21: case null ===
        System.out.println("\n=== Java 21: case null ===");
        String input = null;
        String result = switch (input) {
            case null -> "Input was null!";
            case "hello" -> "Hello!";
            case "bye" -> "Goodbye!";
            default -> "Unknown: " + input;
        };
        System.out.println("Null input: " + result);

        // Non-null input
        input = "hello";
        result = switch (input) {
            case null -> "null";
            case "hello" -> "Hello!";
            default -> "other";
        };
        System.out.println("Non-null input: " + result);

        // === case null, default → combined ===
        System.out.println("\n=== case null, default (combined) ===");
        String status = null;
        String message = switch (status) {
            case "active" -> "User is active";
            case "inactive" -> "User is inactive";
            case null, default -> "Unknown or null status";
        };
        System.out.println("Combined null+default: " + message);

        // === Null with pattern matching ===
        System.out.println("\n=== Null with sealed types ===");
        testAnimal(new Dog("Rex"));
        testAnimal(new Cat("Whiskers"));
        testAnimal(null);

        // === Null in guarded patterns ===
        System.out.println("\n=== Null with guards ===");
        Object obj = null;
        String desc = switch (obj) {
            case null -> "null object";
            case Integer i when i > 0 -> "positive: " + i;
            case Integer i -> "non-positive: " + i;
            case String s when s.length() > 5 -> "long string: " + s;
            case String s -> "short string: " + s;
            default -> "other: " + obj;
        };
        System.out.println("Result: " + desc);
    }

    static void testAnimal(Animal animal) {
        String sound = switch (animal) {
            case null -> "No animal (null)";
            case Dog d -> d.name() + " says Woof!";
            case Cat c -> c.name() + " says Meow!";
            case Bird b -> b.name() + " says Tweet!";
        };
        System.out.println("  " + sound);
    }
}
