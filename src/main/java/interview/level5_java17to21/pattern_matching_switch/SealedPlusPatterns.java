package interview.level5_java17to21.pattern_matching_switch;

/**
 * Q15. How do sealed classes combine with pattern matching?
 *
 * Sealed classes + pattern matching switch = exhaustive, type-safe branching
 * without instanceof chains or visitor pattern.
 *
 * The compiler knows ALL possible subtypes of a sealed type.
 * Pattern matching allows destructuring records in switch.
 * Together: every case is covered, no default needed, adding a subtype forces handling.
 *
 * This is Java's answer to algebraic data types (like Rust enums, Kotlin sealed classes).
 *
 * Pattern: sealed interface + record implementations + switch expression
 */
public class SealedPlusPatterns {

    // === Domain model: Payment methods ===
    sealed interface Payment permits CreditCard, DebitCard, UPI, Cash {}
    record CreditCard(String number, String holder, int cvv) implements Payment {}
    record DebitCard(String number, String holder, String pin) implements Payment {}
    record UPI(String upiId, String provider) implements Payment {}
    record Cash(double amount) implements Payment {}

    // === Nested sealed hierarchy ===
    sealed interface Expr permits Num, Add, Mul, Neg {}
    record Num(double value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}
    record Neg(Expr operand) implements Expr {}

    // === Process payment — exhaustive, no default ===
    static String processPayment(Payment payment) {
        return switch (payment) {
            case CreditCard(var num, var holder, _) ->
                    "Charging credit card " + mask(num) + " of " + holder;
            case DebitCard(var num, _, _) ->
                    "Debiting card " + mask(num);
            case UPI(var id, var provider) ->
                    "UPI transfer to " + id + " via " + provider;
            case Cash(var amount) ->
                    String.format("Cash payment of $%.2f", amount);
            // No default needed! Compiler knows all subtypes
        };
    }

    // === Evaluate expression — recursive pattern matching ===
    static double evaluate(Expr expr) {
        return switch (expr) {
            case Num(var v) -> v;
            case Add(var l, var r) -> evaluate(l) + evaluate(r);
            case Mul(var l, var r) -> evaluate(l) * evaluate(r);
            case Neg(var e) -> -evaluate(e);
        };
    }

    // === Guarded patterns — additional conditions ===
    static String validatePayment(Payment payment) {
        return switch (payment) {
            case CreditCard c when c.number().length() != 16 -> "Invalid card number";
            case CreditCard c when c.cvv() < 100 || c.cvv() > 999 -> "Invalid CVV";
            case CreditCard c -> "Valid credit card";
            case UPI u when !u.upiId().contains("@") -> "Invalid UPI ID";
            case UPI u -> "Valid UPI";
            case Cash c when c.amount() <= 0 -> "Invalid cash amount";
            case Cash c -> "Valid cash";
            case DebitCard d -> "Valid debit card";
        };
    }

    public static void main(String[] args) {

        // === Process different payment types ===
        System.out.println("=== Exhaustive pattern matching ===");
        Payment[] payments = {
                new CreditCard("4111222233334444", "Karan", 123),
                new DebitCard("9876543210123456", "John", "1234"),
                new UPI("karan@upi", "GooglePay"),
                new Cash(500.00)
        };
        for (Payment p : payments) {
            System.out.println("  " + processPayment(p));
        }

        // === Expression evaluation (recursive) ===
        System.out.println("\n=== Recursive pattern matching (expression tree) ===");
        // Expression: -(3 + 4) * 2 = -14
        Expr expr = new Mul(new Neg(new Add(new Num(3), new Num(4))), new Num(2));
        System.out.println("  -(3 + 4) * 2 = " + evaluate(expr));

        // === Guarded patterns ===
        System.out.println("\n=== Guarded patterns (validation) ===");
        System.out.println("  " + validatePayment(new CreditCard("411", "Test", 123)));
        System.out.println("  " + validatePayment(new CreditCard("4111222233334444", "Test", 123)));
        System.out.println("  " + validatePayment(new UPI("invalid", "GPay")));
        System.out.println("  " + validatePayment(new Cash(-50)));

        // === Key benefits ===
        System.out.println("\n=== Benefits ===");
        System.out.println("1. Exhaustive — compiler ensures all subtypes handled");
        System.out.println("2. Type-safe — no ClassCastException possible");
        System.out.println("3. Concise — destructuring in switch cases");
        System.out.println("4. Maintainable — adding new subtype = compile error in all switches");
        System.out.println("5. No visitor pattern needed!");
    }

    static String mask(String cardNumber) {
        if (cardNumber.length() < 4) return "****";
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
}
