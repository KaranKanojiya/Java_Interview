package interview.level2_java8.functional_interfaces;

/**
 * Q2. What is a Functional Interface?
 *
 * A functional interface has EXACTLY ONE abstract method.
 * It can have any number of default/static methods.
 * @FunctionalInterface annotation is optional but recommended (compile-time safety).
 *
 * Key built-in functional interfaces:
 *   Predicate<T>    → boolean test(T t)
 *   Function<T,R>   → R apply(T t)
 *   Consumer<T>     → void accept(T t)
 *   Supplier<T>     → T get()
 *   BiFunction<T,U,R> → R apply(T t, U u)
 *   UnaryOperator<T>  → T apply(T t)   (extends Function<T,T>)
 *   BinaryOperator<T> → T apply(T t1, T t2) (extends BiFunction<T,T,T>)
 */
public class FunctionalInterfaceDefinition {

    // Custom functional interface
    @FunctionalInterface
    interface MathOperation {
        int operate(int a, int b);

        // Allowed: default methods
        default MathOperation andThen(MathOperation after) {
            return (a, b) -> after.operate(this.operate(a, b), 0);
        }

        // Allowed: static methods
        static MathOperation add() {
            return (a, b) -> a + b;
        }
    }

    // This will NOT compile if uncommented — two abstract methods
    // @FunctionalInterface
    // interface InvalidFunctional {
    //     void method1();
    //     void method2();
    // }

    // Functional interface via inheritance — still valid if only ONE abstract method total
    @FunctionalInterface
    interface StringProcessor extends java.util.function.Function<String, String> {
        // Inherits: R apply(T t) — that's the single abstract method
        // Can add defaults:
        default StringProcessor compose(StringProcessor before) {
            return s -> this.apply(before.apply(s));
        }
    }

    public static void main(String[] args) {

        // 1. Lambda assigned to custom functional interface
        MathOperation add = (a, b) -> a + b;
        MathOperation multiply = (a, b) -> a * b;

        System.out.println("=== Custom Functional Interface ===");
        System.out.println("5 + 3 = " + add.operate(5, 3));
        System.out.println("5 * 3 = " + multiply.operate(5, 3));

        // 2. Using static factory method
        MathOperation adder = MathOperation.add();
        System.out.println("Static factory: 10 + 20 = " + adder.operate(10, 20));

        // 3. StringProcessor — functional interface via inheritance
        StringProcessor toUpper = String::toUpperCase;
        StringProcessor addBrackets = s -> "[" + s + "]";
        StringProcessor combined = addBrackets.compose(toUpper);

        System.out.println("\n=== Inherited Functional Interface ===");
        System.out.println("Process 'hello': " + combined.apply("hello"));  // [HELLO]

        // 4. Runnable and Comparable are functional interfaces too
        System.out.println("\n=== JDK Functional Interfaces ===");
        Runnable r = () -> System.out.println("Runnable is a functional interface!");
        r.run();

        java.util.Comparator<String> byLength = (s1, s2) -> Integer.compare(s1.length(), s2.length());
        System.out.println("Comparator 'hi' vs 'hello': " + byLength.compare("hi", "hello"));
    }
}
