package interview.level4_java9to17.var_type;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 10 Local Variable Type Inference (var)
// =============================================================================
//
// Q: "Does var make Java dynamically typed?"
// A: "No, Java remains statically typed. var is syntactic sugar — the compiler
//     infers the concrete type at compile time. Once inferred, the type is fixed
//     and cannot change. It's the same as writing the explicit type yourself."
//
// Q: "Where can you use var, and where can't you?"
// A: "var is allowed ONLY for local variables with an initializer. You CANNOT
//     use it for: fields, method parameters, return types, catch parameters,
//     or local variables without an initializer."
//
// Q: "Can you use var in lambda parameters?"
// A: "Yes, since Java 11. This is useful when you need to add annotations to
//     lambda parameters, e.g., (@NonNull var x) -> x.length()."
//
// Q: "What type does var infer for diamond operator or anonymous classes?"
// A: "var list = new ArrayList<>() infers ArrayList<Object> — the diamond
//     operator cannot infer the generic type without a target type. For
//     anonymous classes, var captures the anonymous type, letting you access
//     members declared in the anonymous class body."
//
// =============================================================================

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VarLocalTypeInference {

    // -------------------------------------------------------------------------
    // 1. Basic var usage — type inferred from initializer
    // -------------------------------------------------------------------------
    static void basicVarUsage() {
        System.out.println("=== 1. Basic var Usage ===");

        // Before Java 10:
        String greeting = "Hello, Java 10!";
        ArrayList<String> names = new ArrayList<>();

        // With var (Java 10+):
        var message = "Hello, Java 10!";          // inferred as String
        var nameList = new ArrayList<String>();     // inferred as ArrayList<String>
        var count = 42;                            // inferred as int
        var price = 19.99;                         // inferred as double

        System.out.println("message type: String -> " + message);
        System.out.println("nameList type: ArrayList<String> -> " + nameList.getClass().getSimpleName());
        System.out.println("count type: int -> " + count);
        System.out.println("price type: double -> " + price);

        // var is still statically typed — this would NOT compile:
        // var x = 10;
        // x = "hello"; // ERROR: incompatible types
    }

    // -------------------------------------------------------------------------
    // 2. var with complex types — reduces boilerplate
    // -------------------------------------------------------------------------
    static void varReducesBoilerplate() {
        System.out.println("\n=== 2. var Reduces Boilerplate ===");

        // Before: verbose nested generics
        Map<String, List<String>> oldMap = new HashMap<String, List<String>>();

        // After: cleaner with var
        var cityToPeople = new HashMap<String, List<String>>();
        cityToPeople.put("NYC", List.of("Alice", "Bob"));
        cityToPeople.put("LA", List.of("Charlie"));

        // var with for-each loops
        for (var entry : cityToPeople.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        // var with traditional for loop
        var numbers = List.of(1, 2, 3, 4, 5);
        for (var i = 0; i < numbers.size(); i++) {
            System.out.print(numbers.get(i) + " ");
        }
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // 3. var with try-with-resources
    // -------------------------------------------------------------------------
    static void varWithTryWithResources() {
        System.out.println("\n=== 3. var with Try-With-Resources ===");

        // var works in try-with-resources
        try (var stream = List.of("a", "b", "c").stream()) {
            var result = stream.collect(Collectors.joining(", "));
            System.out.println("Joined: " + result);
        }
    }

    // -------------------------------------------------------------------------
    // 4. var with anonymous classes — captures anonymous type
    // -------------------------------------------------------------------------
    static void varWithAnonymousClass() {
        System.out.println("\n=== 4. var with Anonymous Classes ===");

        // var captures the anonymous type, so you can access extra members
        var point = new Object() {
            int x = 10;
            int y = 20;

            @Override
            public String toString() {
                return "(" + x + ", " + y + ")";
            }
        };

        // These fields are accessible because var inferred the anonymous type
        System.out.println("x=" + point.x + ", y=" + point.y);
        System.out.println("point: " + point);
    }

    // -------------------------------------------------------------------------
    // 5. var in lambda parameters (Java 11)
    // -------------------------------------------------------------------------
    static void varInLambdaParams() {
        System.out.println("\n=== 5. var in Lambda Parameters (Java 11) ===");

        // Java 11 allows var in lambda parameters — useful for annotations
        var names = List.of("Alice", "Bob", "Charlie");

        // Without var:
        names.stream()
                .map((String s) -> s.toUpperCase())
                .forEach(s -> System.out.print(s + " "));
        System.out.println();

        // With var (Java 11) — enables adding annotations like @NonNull:
        names.stream()
                .map((var s) -> s.toLowerCase())
                .forEach(s -> System.out.print(s + " "));
        System.out.println();

        // Note: you can't mix var and explicit types in multi-param lambdas
        // (var a, String b) -> ... // ERROR
        // (var a, var b) -> ...    // OK
    }

    // -------------------------------------------------------------------------
    // 6. Diamond operator gotcha with var
    // -------------------------------------------------------------------------
    static void diamondOperatorGotcha() {
        System.out.println("\n=== 6. Diamond Operator Gotcha ===");

        // PITFALL: var + diamond = Object inference
        var listWithDiamond = new ArrayList<>();  // ArrayList<Object> — NOT what you want
        listWithDiamond.add("Hello");
        listWithDiamond.add(42);  // compiles! It's ArrayList<Object>

        // CORRECT: specify generic type explicitly
        var listExplicit = new ArrayList<String>(); // ArrayList<String>
        listExplicit.add("Hello");
        // listExplicit.add(42); // ERROR: won't compile

        System.out.println("Diamond list (Object): " + listWithDiamond);
        System.out.println("Explicit list (String): " + listExplicit);
    }

    // -------------------------------------------------------------------------
    // 7. WHERE YOU CANNOT USE var — compile errors
    // -------------------------------------------------------------------------
    static void whereVarIsNotAllowed() {
        System.out.println("\n=== 7. Where var Is NOT Allowed ===");

        System.out.println("var CANNOT be used for:");
        System.out.println("  1. Class fields:          private var name = \"x\";  // ERROR");
        System.out.println("  2. Method parameters:     void foo(var x) {}       // ERROR");
        System.out.println("  3. Return types:          var getX() {}            // ERROR");
        System.out.println("  4. Catch parameters:      catch (var e) {}         // ERROR");
        System.out.println("  5. Without initializer:   var x;                   // ERROR");
        System.out.println("  6. With null:              var x = null;            // ERROR");
        System.out.println("  7. Array initializer:      var arr = {1,2,3};      // ERROR");
        System.out.println("  8. Method references:      var ref = String::length; // ERROR (ambiguous)");

        // Correct array usage with var:
        var arr = new int[]{1, 2, 3}; // OK — type is int[]
        System.out.println("Array with var: length=" + arr.length);
    }

    // -------------------------------------------------------------------------
    // 8. Best practices
    // -------------------------------------------------------------------------
    static void bestPractices() {
        System.out.println("\n=== 8. Best Practices ===");

        // GOOD: type is obvious from RHS
        var names = new ArrayList<String>();
        var count = names.size();
        var sb = new StringBuilder();

        // BAD: type is not obvious — prefer explicit type
        // var result = service.process(data); // what type is result?
        // var x = foo();                      // unclear

        // GOOD: use var to reduce verbosity in for-each
        var map = Map.of("a", 1, "b", 2, "c", 3);
        for (var entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        System.out.println("\nGuideline: Use var when the type is obvious from context.");
        System.out.println("Avoid var when it reduces readability.");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 10/11: var Local Variable Type Inference ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        basicVarUsage();
        varReducesBoilerplate();
        varWithTryWithResources();
        varWithAnonymousClass();
        varInLambdaParams();
        diamondOperatorGotcha();
        whereVarIsNotAllowed();
        bestPractices();
    }
}
