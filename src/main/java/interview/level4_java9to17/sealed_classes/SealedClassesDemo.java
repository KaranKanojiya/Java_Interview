package interview.level4_java9to17.sealed_classes;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 17 Sealed Classes & Interfaces
// =============================================================================
//
// Q: "What problem do sealed classes solve?"
// A: "Sealed classes give you controlled inheritance — you define exactly which
//     classes can extend/implement your type. This enables: (1) exhaustive
//     pattern matching in switch (compiler knows all subtypes), (2) algebraic
//     data types (sum types), (3) a middle ground between final (no extension)
//     and open (anyone can extend). Before sealed, you had to use package-private
//     constructors as a workaround."
//
// Q: "What are the rules for permitted subclasses?"
// A: "Each permitted subclass MUST: (1) be in the same module (or same package
//     if unnamed module), (2) directly extend the sealed class, (3) choose one
//     modifier: final (no further extension), sealed (controlled extension),
//     or non-sealed (opens up extension again)."
//
// Q: "How do sealed classes relate to pattern matching?"
// A: "The compiler knows all permitted subtypes, so a switch on a sealed type
//     can be exhaustive without a default branch. If a new subtype is added,
//     existing switches won't compile — forcing you to handle the new case.
//     This turns runtime ClassCastException into compile-time errors."
//
// Q: "Can a sealed interface permit records and enums?"
// A: "Yes. Records are implicitly final, so they satisfy the 'must be final,
//     sealed, or non-sealed' requirement. Enums are also implicitly final.
//     This makes sealed interfaces + records perfect for algebraic data types."
//
// =============================================================================

import java.util.List;

public class SealedClassesDemo {

    // -------------------------------------------------------------------------
    // 1. Basic sealed class — permits clause
    // -------------------------------------------------------------------------
    sealed static class Animal permits Dog, Cat, Bird {
        String name;
        Animal(String name) { this.name = name; }
    }

    // Each permitted subclass must be final, sealed, or non-sealed
    static final class Dog extends Animal {
        String breed;
        Dog(String name, String breed) { super(name); this.breed = breed; }
        @Override public String toString() { return "Dog(%s, %s)".formatted(name, breed); }
    }

    static final class Cat extends Animal {
        boolean indoor;
        Cat(String name, boolean indoor) { super(name); this.indoor = indoor; }
        @Override public String toString() { return "Cat(%s, indoor=%s)".formatted(name, indoor); }
    }

    static final class Bird extends Animal {
        boolean canFly;
        Bird(String name, boolean canFly) { super(name); this.canFly = canFly; }
        @Override public String toString() { return "Bird(%s, canFly=%s)".formatted(name, canFly); }
    }

    // This would NOT compile — Fish is not in the permits list:
    // class Fish extends Animal { Fish() { super("Fish"); } }

    void basicSealed() {
        System.out.println("=== 1. Basic Sealed Class ===");

        List<Animal> animals = List.of(
                new Dog("Rex", "Shepherd"),
                new Cat("Whiskers", true),
                new Bird("Eagle", true)
        );

        for (var animal : animals) {
            // Exhaustive pattern matching — all permitted subtypes covered
            String desc = switch (animal) {
                case Dog d  -> d.name + " is a " + d.breed + " dog";
                case Cat c  -> c.name + " is " + (c.indoor ? "an indoor" : "an outdoor") + " cat";
                case Bird b -> b.name + (b.canFly ? " can fly" : " cannot fly");
                // Note: sealed records don't need default (see section 8).
                // Sealed classes may need default due to separate compilation.
                default -> throw new AssertionError("Unreachable");
            };
            System.out.println("  " + desc);
        }
    }

    // -------------------------------------------------------------------------
    // 2. Sealed interface with records — algebraic data types
    // -------------------------------------------------------------------------
    sealed interface Expr permits Literal, BinOp, UnaryOp, Var {
        // Algebraic data type: an expression is exactly one of these
    }

    record Literal(double value) implements Expr {}
    record Var(String name) implements Expr {}
    record BinOp(String op, Expr left, Expr right) implements Expr {}
    record UnaryOp(String op, Expr operand) implements Expr {}

    static String prettyPrint(Expr expr) {
        return switch (expr) {
            case Literal l  -> String.valueOf(l.value());
            case Var v      -> v.name();
            case BinOp b    -> "(%s %s %s)".formatted(
                    prettyPrint(b.left()), b.op(), prettyPrint(b.right()));
            case UnaryOp u  -> "%s(%s)".formatted(u.op(), prettyPrint(u.operand()));
            // No default — sealed interface is exhaustive!
        };
    }

    static void algebraicDataType() {
        System.out.println("\n=== 2. Algebraic Data Types (Sealed + Records) ===");

        // Expression: -(x + 2.0) * y
        Expr expr = new BinOp("*",
                new UnaryOp("-", new BinOp("+", new Var("x"), new Literal(2.0))),
                new Var("y")
        );

        System.out.println("  Expression: " + prettyPrint(expr));
    }

    // -------------------------------------------------------------------------
    // 3. Sealed with non-sealed — reopening extension
    // -------------------------------------------------------------------------
    sealed interface Transport permits Car, Train, OpenVehicle {}

    record Car(String model, int doors) implements Transport {}
    record Train(String line, int cars) implements Transport {}

    // non-sealed reopens the hierarchy — anyone can extend this
    non-sealed interface OpenVehicle extends Transport {}

    // Now anyone can implement OpenVehicle
    record Bicycle(String brand) implements OpenVehicle {}
    record Scooter(String brand, boolean electric) implements OpenVehicle {}

    static void nonSealedDemo() {
        System.out.println("\n=== 3. non-sealed Reopens Extension ===");

        List<Transport> vehicles = List.of(
                new Car("Tesla", 4),
                new Train("Metro Line 1", 8),
                new Bicycle("Trek"),
                new Scooter("Vespa", true)
        );

        for (var v : vehicles) {
            String desc = switch (v) {
                case Car c         -> "Car: " + c.model() + " (" + c.doors() + " doors)";
                case Train t       -> "Train: " + t.line() + " (" + t.cars() + " cars)";
                case OpenVehicle o -> "Open vehicle: " + o; // catches Bicycle, Scooter, and future types
            };
            System.out.println("  " + desc);
        }
    }

    // -------------------------------------------------------------------------
    // 4. Sealed class hierarchy — multi-level sealing
    // -------------------------------------------------------------------------
    sealed interface Shape permits Circle, Polygon {}

    record Circle(double radius) implements Shape {}

    // Sealed subinterface — further restricts hierarchy
    sealed interface Polygon extends Shape permits Triangle, Quadrilateral {}

    record Triangle(double a, double b, double c) implements Polygon {}

    sealed interface Quadrilateral extends Polygon permits RectangleShape, Square {}

    record RectangleShape(double width, double height) implements Quadrilateral {}
    record Square(double side) implements Quadrilateral {}

    static double area(Shape shape) {
        return switch (shape) {
            case Circle c       -> Math.PI * c.radius() * c.radius();
            case Triangle t     -> {
                // Heron's formula
                double s = (t.a() + t.b() + t.c()) / 2;
                yield Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
            }
            case RectangleShape r -> r.width() * r.height();
            case Square sq      -> sq.side() * sq.side();
        };
    }

    static void multiLevelSealed() {
        System.out.println("\n=== 4. Multi-Level Sealed Hierarchy ===");

        List<Shape> shapes = List.of(
                new Circle(5),
                new Triangle(3, 4, 5),
                new RectangleShape(6, 8),
                new Square(4)
        );

        for (var shape : shapes) {
            System.out.println("  %s -> area=%.2f".formatted(shape, area(shape)));
        }
    }

    // -------------------------------------------------------------------------
    // 5. Real-world example: Result type (like Rust's Result)
    // -------------------------------------------------------------------------
    sealed interface Result<T> permits Success, Failure {
        default boolean isSuccess() { return this instanceof Success; }
    }

    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error, Exception cause) implements Result<T> {
        Failure(String error) { this(error, null); }
    }

    static Result<Integer> divide(int a, int b) {
        if (b == 0) return new Failure<>("Division by zero");
        return new Success<>(a / b);
    }

    static void resultTypeDemo() {
        System.out.println("\n=== 5. Result Type (Sealed + Generics) ===");

        var results = List.of(divide(10, 3), divide(42, 0), divide(100, 5));

        for (var result : results) {
            String msg = switch (result) {
                case Success<Integer> s -> "OK: " + s.value();
                case Failure<Integer> f -> "ERROR: " + f.error();
            };
            System.out.println("  " + msg);
        }
    }

    // -------------------------------------------------------------------------
    // 6. Sealed + guards in pattern matching (Java 21)
    // -------------------------------------------------------------------------
    sealed interface HttpStatus permits Ok, Redirect, ClientError, ServerError {}
    record Ok(int code) implements HttpStatus {}
    record Redirect(int code, String location) implements HttpStatus {}
    record ClientError(int code, String message) implements HttpStatus {}
    record ServerError(int code, String message) implements HttpStatus {}

    static String handleStatus(HttpStatus status) {
        return switch (status) {
            case Ok o                                -> "Success: " + o.code();
            case Redirect r when r.code() == 301     -> "Permanent redirect to " + r.location();
            case Redirect r                          -> "Temporary redirect (" + r.code() + ") to " + r.location();
            case ClientError e when e.code() == 404  -> "Not Found: " + e.message();
            case ClientError e when e.code() == 403  -> "Forbidden: " + e.message();
            case ClientError e                       -> "Client error " + e.code() + ": " + e.message();
            case ServerError e                       -> "Server error " + e.code() + ": " + e.message();
        };
    }

    static void guardsDemo() {
        System.out.println("\n=== 6. Sealed + Guards in Switch ===");

        var statuses = List.of(
                new Ok(200),
                new Redirect(301, "/new-path"),
                new Redirect(302, "/temp"),
                new ClientError(404, "Page not found"),
                new ClientError(403, "Access denied"),
                new ServerError(500, "Internal error")
        );

        statuses.forEach(s -> System.out.println("  " + handleStatus(s)));
    }

    // -------------------------------------------------------------------------
    // 7. Comparison: before vs after sealed classes
    // -------------------------------------------------------------------------
    static void comparison() {
        System.out.println("\n=== 7. Before vs After Sealed Classes ===");

        System.out.println("BEFORE sealed classes:");
        System.out.println("  - Use abstract class + package-private constructor");
        System.out.println("  - Or use enum (but enums can't have different fields per constant)");
        System.out.println("  - Or use Visitor pattern (verbose)");
        System.out.println("  - switch needs default branch (not exhaustive)");
        System.out.println("  - New subtypes: no compile-time safety\n");

        System.out.println("AFTER sealed classes:");
        System.out.println("  - sealed keyword + permits clause");
        System.out.println("  - Compiler knows ALL subtypes");
        System.out.println("  - switch is exhaustive — no default needed");
        System.out.println("  - New subtype added? All switches fail to compile");
        System.out.println("  - Algebraic data types = sealed interface + records");
        System.out.println("  - Replaces Visitor pattern in many cases");

        System.out.println("\n  Modifier rules for permitted subclasses:");
        System.out.println("  ┌─────────────┬────────────────────────────────────┐");
        System.out.println("  │ Modifier    │ Meaning                            │");
        System.out.println("  ├─────────────┼────────────────────────────────────┤");
        System.out.println("  │ final       │ No further subclassing             │");
        System.out.println("  │ sealed      │ Controlled subclassing (permits)   │");
        System.out.println("  │ non-sealed  │ Reopens hierarchy to all           │");
        System.out.println("  └─────────────┴────────────────────────────────────┘");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 17: Sealed Classes & Interfaces          ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        var demo = new SealedClassesDemo();
        demo.basicSealed();
        algebraicDataType();
        nonSealedDemo();
        multiLevelSealed();
        resultTypeDemo();
        guardsDemo();
        comparison();
    }
}
