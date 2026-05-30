package interview.level1_core.oop;

/**
 * Q13. Can you override private and static methods?
 *
 * Private methods: NO
 *   - Not visible to subclasses → cannot override
 *   - Subclass can define a method with same name, but it's a NEW method (not overriding)
 *
 * Static methods: NO (but you can HIDE them)
 *   - Static methods are bound to the CLASS, not the instance
 *   - Method hiding: subclass defines same static method → hides parent's
 *   - Hiding is resolved at COMPILE time (by reference type)
 *   - Overriding is resolved at RUNTIME (by actual object type)
 *
 * Overriding rules:
 *   ✅ Can override: public, protected, package-private instance methods
 *   ❌ Cannot override: private, static, final methods
 *   ❌ Cannot override: constructors
 */
public class OverridePrivateStatic {

    static class Animal {
        // Private method — not visible to subclass
        private void secret() {
            System.out.println("  Animal.secret() — private");
        }

        // Static method — can be hidden, not overridden
        static void staticMethod() {
            System.out.println("  Animal.staticMethod() — static");
        }

        // Instance method — CAN be overridden
        void instanceMethod() {
            System.out.println("  Animal.instanceMethod()");
        }

        void callSecret() {
            secret();  // calls Animal's private method, even from subclass object
        }
    }

    static class Dog extends Animal {
        // This is NOT overriding — it's a completely new method
        private void secret() {
            System.out.println("  Dog.secret() — new method, NOT override");
        }

        // This is METHOD HIDING, not overriding
        static void staticMethod() {
            System.out.println("  Dog.staticMethod() — hiding Animal's static");
        }

        // This IS overriding
        @Override
        void instanceMethod() {
            System.out.println("  Dog.instanceMethod() — overridden");
        }
    }

    public static void main(String[] args) {

        // === Private method ===
        System.out.println("=== Private methods cannot be overridden ===");
        Animal a = new Dog();
        a.callSecret();  // calls Animal.secret(), NOT Dog.secret()
        // Because private methods are bound at compile time to the declaring class

        // === Static method — hiding vs overriding ===
        System.out.println("\n=== Static methods — HIDING (compile-time binding) ===");
        Animal animal = new Dog();
        animal.staticMethod();  // calls ANIMAL.staticMethod() — based on reference type!
        Dog dog = new Dog();
        dog.staticMethod();     // calls DOG.staticMethod() — based on reference type!

        System.out.println("\n  Key: Static methods use reference TYPE, not object TYPE");

        // === Instance method — true overriding ===
        System.out.println("\n=== Instance methods — OVERRIDING (runtime binding) ===");
        Animal poly = new Dog();
        poly.instanceMethod();  // calls DOG.instanceMethod() — runtime polymorphism!

        System.out.println("\n  Key: Instance methods use actual OBJECT type (polymorphism)");

        // === Summary ===
        System.out.println("\n=== Summary ===");
        System.out.println("Private:  NOT overridden — new independent method");
        System.out.println("Static:   NOT overridden — method HIDING (compile-time)");
        System.out.println("Instance: Overridden — polymorphism (runtime)");
        System.out.println("Final:    CANNOT be overridden (compile error)");
    }
}
