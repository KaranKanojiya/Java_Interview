package interview.level1_core.enums;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: Can enums have constructors, fields, and methods?
// A: Yes. Enum constants are instances of the enum class. Each constant can pass arguments
//    to the constructor. The constructor must be private (or package-private).
//
// Q: Can an enum implement an interface?
// A: Yes. Each enum constant can even provide its own implementation (constant-specific
//    method implementation). Enums cannot extend other classes because they implicitly
//    extend java.lang.Enum.
//
// Q: Why is enum the best way to implement singleton in Java?
// A: 1. Thread-safe by JVM guarantee (class loading is synchronized).
//    2. Serialization-safe (JVM handles it — no extra readResolve() needed).
//    3. Reflection-safe (can't create new instances via reflection).
//    4. Simple and concise.
//    Joshua Bloch (Effective Java): "A single-element enum is the best way to implement a singleton."
//
// Q: What are EnumSet and EnumMap?
// A: EnumSet: A Set implementation optimized for enums. Uses a bit vector internally.
//    Extremely fast and compact. Use instead of HashSet for enum types.
//    EnumMap: A Map implementation with enum keys. Uses an array internally (ordinal as index).
//    Both are much faster and more memory-efficient than their Hash* counterparts.
//
// Q: What is the ordinal() method and why should you avoid it?
// A: ordinal() returns the position of the enum constant (0-based). Avoid using it for
//    logic because it changes if constants are reordered. Use fields instead.
// ========================================================

import java.util.EnumMap;
import java.util.EnumSet;

public class EnumAdvanced {

    // --- 1. Enum with fields, constructor, and methods ---
    enum Planet {
        MERCURY(3.303e+23, 2.4397e6),
        VENUS(4.869e+24, 6.0518e6),
        EARTH(5.976e+24, 6.37814e6),
        MARS(6.421e+23, 3.3972e6);

        private final double mass;    // in kilograms
        private final double radius;  // in meters

        // Constructor must be private (or package-private)
        Planet(double mass, double radius) {
            this.mass = mass;
            this.radius = radius;
        }

        // Method on enum
        double surfaceGravity() {
            final double G = 6.67300E-11;
            return G * mass / (radius * radius);
        }

        double surfaceWeight(double otherMass) {
            return otherMass * surfaceGravity();
        }
    }

    // --- 2. Enum implementing interface (Strategy pattern) ---
    interface MathOperation {
        double apply(double a, double b);
    }

    enum Operation implements MathOperation {
        ADD("+") {
            @Override
            public double apply(double a, double b) { return a + b; }
        },
        SUBTRACT("-") {
            @Override
            public double apply(double a, double b) { return a - b; }
        },
        MULTIPLY("*") {
            @Override
            public double apply(double a, double b) { return a * b; }
        },
        DIVIDE("/") {
            @Override
            public double apply(double a, double b) {
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            }
        };

        private final String symbol;

        Operation(String symbol) { this.symbol = symbol; }

        @Override
        public String toString() { return symbol; }
    }

    // --- 3. Enum Singleton pattern ---
    enum DatabaseConnection {
        INSTANCE;

        private String url = "jdbc:mysql://localhost:3306/mydb";

        public void connect() {
            System.out.println("  Connected to: " + url);
        }

        public void query(String sql) {
            System.out.println("  Executing: " + sql);
        }
    }

    // --- 4. Enum State Machine ---
    enum TrafficLight {
        RED {
            @Override
            public TrafficLight next() { return GREEN; }

            @Override
            public String action() { return "STOP"; }
        },
        GREEN {
            @Override
            public TrafficLight next() { return YELLOW; }

            @Override
            public String action() { return "GO"; }
        },
        YELLOW {
            @Override
            public TrafficLight next() { return RED; }

            @Override
            public String action() { return "SLOW DOWN"; }
        };

        public abstract TrafficLight next();
        public abstract String action();
    }

    // --- 5. Enum with abstract method (constant-specific behavior) ---
    enum HttpStatus {
        OK(200, "Success"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_ERROR(500, "Internal Server Error");

        private final int code;
        private final String message;

        HttpStatus(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() { return code; }
        public String getMessage() { return message; }

        // Reverse lookup by code
        public static HttpStatus fromCode(int code) {
            for (HttpStatus status : values()) {
                if (status.code == code) return status;
            }
            throw new IllegalArgumentException("Unknown status code: " + code);
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Advanced Enums Deep Dive");
        System.out.println("========================================\n");

        // --- 1. Enum with fields and methods ---
        System.out.println("=== 1. Enum with Fields/Methods ===");
        double earthWeight = 75.0;  // kg
        double mass = earthWeight / Planet.EARTH.surfaceGravity();
        for (Planet p : Planet.values()) {
            System.out.printf("  Weight on %s: %.2f N%n", p, p.surfaceWeight(mass));
        }
        System.out.println();

        // --- 2. Enum implementing interface ---
        System.out.println("=== 2. Enum Implementing Interface (Strategy Pattern) ===");
        double a = 10, b = 3;
        for (Operation op : Operation.values()) {
            System.out.printf("  %.0f %s %.0f = %.2f%n", a, op, b, op.apply(a, b));
        }
        System.out.println();

        // --- 3. Enum Singleton ---
        System.out.println("=== 3. Enum Singleton ===");
        DatabaseConnection db = DatabaseConnection.INSTANCE;
        db.connect();
        db.query("SELECT * FROM users");
        System.out.println("  Same instance? " + (db == DatabaseConnection.INSTANCE));
        System.out.println();

        // --- 4. Enum State Machine ---
        System.out.println("=== 4. Enum State Machine (Traffic Light) ===");
        TrafficLight light = TrafficLight.RED;
        for (int i = 0; i < 6; i++) {
            System.out.println("  " + light + " -> " + light.action());
            light = light.next();
        }
        System.out.println();

        // --- 5. Enum reverse lookup ---
        System.out.println("=== 5. Reverse Lookup ===");
        HttpStatus status = HttpStatus.fromCode(404);
        System.out.println("  Code 404 -> " + status + ": " + status.getMessage());
        System.out.println();

        // --- 6. EnumSet ---
        System.out.println("=== 6. EnumSet (Bit-Vector Based, Super Fast) ===");
        // Create EnumSet
        EnumSet<Planet> innerPlanets = EnumSet.of(Planet.MERCURY, Planet.VENUS, Planet.EARTH, Planet.MARS);
        System.out.println("  Inner planets: " + innerPlanets);

        EnumSet<Planet> allPlanets = EnumSet.allOf(Planet.class);
        System.out.println("  All planets: " + allPlanets);

        EnumSet<Planet> noPlanets = EnumSet.noneOf(Planet.class);
        System.out.println("  None: " + noPlanets);

        // Range
        EnumSet<Operation> mathOps = EnumSet.range(Operation.ADD, Operation.MULTIPLY);
        System.out.println("  Range ADD..MULTIPLY: " + mathOps);

        // Complement
        EnumSet<Operation> notAdd = EnumSet.complementOf(EnumSet.of(Operation.ADD));
        System.out.println("  Complement of ADD: " + notAdd);
        System.out.println();

        // --- 7. EnumMap ---
        System.out.println("=== 7. EnumMap (Array-Based, Ordered by Ordinal) ===");
        EnumMap<TrafficLight, Integer> durations = new EnumMap<>(TrafficLight.class);
        durations.put(TrafficLight.RED, 30);
        durations.put(TrafficLight.GREEN, 45);
        durations.put(TrafficLight.YELLOW, 5);
        System.out.println("  Traffic light durations: " + durations);
        durations.forEach((k, v) -> System.out.println("    " + k + " lasts " + v + " seconds"));
        System.out.println();

        // --- 8. Common enum methods ---
        System.out.println("=== 8. Built-in Enum Methods ===");
        System.out.println("  name():    " + Planet.EARTH.name());
        System.out.println("  ordinal(): " + Planet.EARTH.ordinal());
        System.out.println("  valueOf(): " + Planet.valueOf("EARTH"));
        System.out.println("  values():  " + java.util.Arrays.toString(Planet.values()));
        System.out.println();

        // --- 9. Interview tip ---
        System.out.println("=== 9. Key Takeaways ===");
        System.out.println("  - Enums are full classes, not just constants");
        System.out.println("  - Use EnumSet instead of HashSet for enum types (64x faster for <= 64 constants)");
        System.out.println("  - Use EnumMap instead of HashMap for enum keys (array-based, no hashing)");
        System.out.println("  - Enum singleton = thread-safe + serialization-safe + reflection-safe");
        System.out.println("  - Avoid ordinal() for logic — use fields instead");
    }
}
