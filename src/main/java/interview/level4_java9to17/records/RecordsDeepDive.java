package interview.level4_java9to17.records;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 16 Records
// =============================================================================
//
// Q: "When would you NOT use a record?"
// A: "When you need: (1) mutability — records are immutable, (2) inheritance —
//     records are implicitly final and extend java.lang.Record, (3) JPA
//     entities — Hibernate requires no-arg constructors and mutable setters,
//     (4) classes that need to hide their internal state behind a different
//     API, (5) lazy initialization or caching patterns."
//
// Q: "What do you get automatically with a record?"
// A: "The compiler generates: a canonical constructor, private final fields,
//     accessor methods (named like the components, not getXxx), equals(),
//     hashCode(), and toString(). All based on the record components."
//
// Q: "Can a record implement interfaces?"
// A: "Yes. Records can implement any number of interfaces. They just cannot
//     extend other classes (they implicitly extend java.lang.Record)."
//
// Q: "What is a compact canonical constructor?"
// A: "A constructor that omits the parameter list — the parameters are
//     implicit. It's used for validation and normalization. The assignment
//     to fields (this.x = x) happens automatically after the compact
//     constructor body executes."
//
// Q: "Are records just Lombok @Value?"
// A: "Similar concept but records are a language feature, not annotation
//     processing. Records have stronger guarantees: they're truly immutable,
//     the component names ARE the API, and they work with pattern matching
//     and sealed classes."
//
// =============================================================================

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordsDeepDive {

    // -------------------------------------------------------------------------
    // 1. Basic record — auto-generated constructor, accessors, equals, hashCode, toString
    // -------------------------------------------------------------------------
    record Point(int x, int y) {}

    static void basicRecord() {
        System.out.println("=== 1. Basic Record ===");

        var p1 = new Point(3, 4);
        var p2 = new Point(3, 4);
        var p3 = new Point(5, 6);

        // Accessor methods — named after components (NOT getX())
        System.out.println("p1.x() = " + p1.x());
        System.out.println("p1.y() = " + p1.y());

        // Auto-generated toString
        System.out.println("toString: " + p1);

        // Auto-generated equals and hashCode (value-based)
        System.out.println("p1.equals(p2): " + p1.equals(p2)); // true
        System.out.println("p1.equals(p3): " + p1.equals(p3)); // false
        System.out.println("p1.hashCode() == p2.hashCode(): " + (p1.hashCode() == p2.hashCode()));
    }

    // -------------------------------------------------------------------------
    // 2. Compact canonical constructor — validation & normalization
    // -------------------------------------------------------------------------
    record Range(int min, int max) {
        // Compact constructor — no parameter list, auto-assigns fields after body
        Range {
            if (min > max) {
                throw new IllegalArgumentException(
                        "min (%d) must be <= max (%d)".formatted(min, max));
            }
        }
    }

    record Email(String address) {
        // Compact constructor for normalization
        Email {
            if (address == null || address.isBlank()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }
            address = address.toLowerCase().strip(); // normalize
        }
    }

    static void compactConstructor() {
        System.out.println("\n=== 2. Compact Canonical Constructor ===");

        var range = new Range(1, 10);
        System.out.println("Valid range: " + range);

        try {
            new Range(10, 1); // throws
        } catch (IllegalArgumentException e) {
            System.out.println("Validation caught: " + e.getMessage());
        }

        var email = new Email("  ALICE@Example.COM  ");
        System.out.println("Normalized email: " + email.address()); // alice@example.com
    }

    // -------------------------------------------------------------------------
    // 3. Custom constructor (non-canonical)
    // -------------------------------------------------------------------------
    record Color(int r, int g, int b) {
        // Compact constructor for validation
        Color {
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                throw new IllegalArgumentException("RGB values must be 0-255");
            }
        }

        // Custom constructor MUST delegate to canonical constructor
        Color(String hex) {
            this(
                    Integer.parseInt(hex.substring(1, 3), 16),
                    Integer.parseInt(hex.substring(3, 5), 16),
                    Integer.parseInt(hex.substring(5, 7), 16)
            );
        }
    }

    static void customConstructor() {
        System.out.println("\n=== 3. Custom Constructor ===");

        var c1 = new Color(255, 128, 0);
        var c2 = new Color("#FF8000");

        System.out.println("From RGB: " + c1);
        System.out.println("From hex: " + c2);
        System.out.println("Equal? " + c1.equals(c2)); // true
    }

    // -------------------------------------------------------------------------
    // 4. Static methods and instance methods
    // -------------------------------------------------------------------------
    record Money(double amount, String currency) {
        // Static factory method
        static Money usd(double amount) {
            return new Money(amount, "USD");
        }

        static Money eur(double amount) {
            return new Money(amount, "EUR");
        }

        // Instance method
        Money add(Money other) {
            if (!this.currency.equals(other.currency)) {
                throw new IllegalArgumentException("Currency mismatch");
            }
            return new Money(this.amount + other.amount, this.currency);
        }

        // Custom accessor (computed property)
        String display() {
            return "%s %.2f".formatted(currency, amount);
        }
    }

    static void staticAndInstanceMethods() {
        System.out.println("\n=== 4. Static & Instance Methods ===");

        var price = Money.usd(29.99);
        var tax = Money.usd(2.40);
        var total = price.add(tax);

        System.out.println("Price: " + price.display());
        System.out.println("Tax:   " + tax.display());
        System.out.println("Total: " + total.display());
    }

    // -------------------------------------------------------------------------
    // 5. Records implementing interfaces
    // -------------------------------------------------------------------------
    interface Measurable {
        double measure();
    }

    interface Printable {
        void prettyPrint();
    }

    record Circle(double radius) implements Measurable, Printable {
        @Override
        public double measure() {
            return Math.PI * radius * radius;
        }

        @Override
        public void prettyPrint() {
            System.out.println("  Circle(r=%.2f, area=%.2f)".formatted(radius, measure()));
        }
    }

    record Rectangle(double width, double height) implements Measurable, Printable {
        @Override
        public double measure() {
            return width * height;
        }

        @Override
        public void prettyPrint() {
            System.out.println("  Rectangle(%.2f x %.2f, area=%.2f)".formatted(width, height, measure()));
        }
    }

    static void recordsWithInterfaces() {
        System.out.println("\n=== 5. Records Implementing Interfaces ===");

        List<Printable> shapes = List.of(
                new Circle(5.0),
                new Rectangle(3.0, 4.0),
                new Circle(2.5)
        );
        shapes.forEach(Printable::prettyPrint);

        // Records work beautifully with Comparable
        record Student(String name, double gpa) implements Comparable<Student> {
            @Override
            public int compareTo(Student other) {
                return Double.compare(other.gpa, this.gpa); // descending
            }
        }

        var students = List.of(
                new Student("Alice", 3.9),
                new Student("Bob", 3.5),
                new Student("Charlie", 3.8)
        );
        students.stream().sorted().forEach(s ->
                System.out.println("  " + s.name() + ": " + s.gpa()));
    }

    // -------------------------------------------------------------------------
    // 6. Local records (inside methods)
    // -------------------------------------------------------------------------
    static void localRecords() {
        System.out.println("\n=== 6. Local Records ===");

        // Records can be declared inside methods — great for intermediate data
        record NameAge(String name, int age) {}

        var people = List.of(
                new NameAge("Alice", 30),
                new NameAge("Bob", 25),
                new NameAge("Charlie", 35)
        );

        // Local record as stream intermediate type
        record AgeGroup(String group, List<NameAge> members) {}

        var groups = people.stream()
                .collect(Collectors.groupingBy(p -> p.age() >= 30 ? "Senior" : "Junior"))
                .entrySet().stream()
                .map(e -> new AgeGroup(e.getKey(), e.getValue()))
                .toList();

        groups.forEach(g -> System.out.println("  " + g.group() + ": " + g.members()));
    }

    // -------------------------------------------------------------------------
    // 7. Records as DTOs (Data Transfer Objects)
    // -------------------------------------------------------------------------
    // Records are ideal DTOs — immutable, value-based, concise
    record UserDTO(long id, String name, String email) {}
    record OrderDTO(long orderId, long userId, double total, LocalDate date) {}

    record ApiResponse<T>(int status, String message, T data) {
        // Generic records work great for API responses
        static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(200, "OK", data);
        }

        static <T> ApiResponse<T> error(int status, String message) {
            return new ApiResponse<>(status, message, null);
        }
    }

    static void recordsAsDTOs() {
        System.out.println("\n=== 7. Records as DTOs ===");

        var user = new UserDTO(1, "Alice", "alice@example.com");
        var order = new OrderDTO(101, 1, 99.99, LocalDate.of(2024, 1, 15));

        var response = ApiResponse.success(user);
        System.out.println("Response: " + response);

        var errorResp = ApiResponse.error(404, "User not found");
        System.out.println("Error: " + errorResp);

        // Records in collections — value semantics make them great map keys
        var userOrders = Map.of(user, List.of(order));
        System.out.println("Orders: " + userOrders);
    }

    // -------------------------------------------------------------------------
    // 8. Records with pattern matching (Java 21)
    // -------------------------------------------------------------------------
    sealed interface Shape permits CircleShape, RectShape {}
    record CircleShape(double radius) implements Shape {}
    record RectShape(double w, double h) implements Shape {}

    static String describeShape(Shape shape) {
        // Record pattern — deconstruct directly in switch
        return switch (shape) {
            case CircleShape(var r) -> "Circle with radius %.2f, area=%.2f".formatted(r, Math.PI * r * r);
            case RectShape(var w, var h) -> "Rectangle %s%.2f x %.2f, area=%.2f".formatted("", w, h, w * h);
        };
    }

    static void recordPatternMatching() {
        System.out.println("\n=== 8. Records with Pattern Matching (Java 21) ===");

        List<Shape> shapes = List.of(
                new CircleShape(5.0),
                new RectShape(3.0, 4.0),
                new CircleShape(2.5)
        );

        shapes.forEach(s -> System.out.println("  " + describeShape(s)));
    }

    // -------------------------------------------------------------------------
    // 9. What records CANNOT do
    // -------------------------------------------------------------------------
    static void limitations() {
        System.out.println("\n=== 9. Record Limitations ===");

        System.out.println("Records CANNOT:");
        System.out.println("  1. Extend other classes (implicitly extend Record)");
        System.out.println("  2. Be mutable (fields are final)");
        System.out.println("  3. Have additional instance fields (only components)");
        System.out.println("  4. Be abstract");
        System.out.println("  5. Use native methods");
        System.out.println();
        System.out.println("Records CAN:");
        System.out.println("  1. Implement interfaces");
        System.out.println("  2. Have static fields and methods");
        System.out.println("  3. Have instance methods");
        System.out.println("  4. Have custom constructors (must delegate to canonical)");
        System.out.println("  5. Override accessors, equals, hashCode, toString");
        System.out.println("  6. Be generic");
        System.out.println("  7. Be serializable (components are serialized)");
        System.out.println("  8. Be declared locally (inside methods)");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 16: Records Deep Dive                    ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        basicRecord();
        compactConstructor();
        customConstructor();
        staticAndInstanceMethods();
        recordsWithInterfaces();
        localRecords();
        recordsAsDTOs();
        recordPatternMatching();
        limitations();
    }
}
