package interview.level4_java9to17.modules;

/**
 * Q14. What are private interface methods (Java 9)?
 *
 * Java 9 added private methods in interfaces to share code between default methods.
 *
 * Evolution of interfaces:
 *   Java 7:  only abstract methods
 *   Java 8:  + default methods, + static methods
 *   Java 9:  + private methods, + private static methods
 *
 * Why?
 *   When multiple default methods share common logic, private methods avoid
 *   code duplication WITHOUT exposing the helper to implementing classes.
 *
 * Rules:
 *   - private methods can be instance or static
 *   - private methods are NOT inherited by implementing classes
 *   - private methods CAN be called by default methods and other private methods
 */
public class PrivateInterfaceMethods {

    interface Logger {
        // Abstract method (implementing class must provide)
        String getSource();

        // Default method — uses private helper
        default void logInfo(String message) {
            log("INFO", message);
        }

        default void logError(String message) {
            log("ERROR", message);
        }

        default void logWarning(String message) {
            log("WARN", message);
        }

        // Private instance method — shared logic, NOT exposed to implementors
        private void log(String level, String message) {
            String formatted = formatMessage(level, getSource(), message);
            System.out.println(formatted);
        }

        // Private static method — utility, no access to instance
        private static String formatMessage(String level, String source, String message) {
            return "[" + level + "] [" + source + "] " + message;
        }

        // Static method (Java 8) — accessible via Interface.method()
        static void logStatic(String message) {
            System.out.println("[STATIC] " + message);
        }
    }

    // Implementing class only sees abstract + default methods
    static class AppLogger implements Logger {
        @Override
        public String getSource() { return "MyApp"; }
        // Cannot access log() or formatMessage() — they're private
    }

    // Another example with validation
    interface Validator {
        default boolean isValidEmail(String email) {
            return isNotEmpty(email) && email.contains("@") && email.contains(".");
        }

        default boolean isValidName(String name) {
            return isNotEmpty(name) && name.length() >= 2;
        }

        // Shared validation — private
        private boolean isNotEmpty(String value) {
            return value != null && !value.isBlank();
        }
    }

    static class UserValidator implements Validator {}

    public static void main(String[] args) {

        // === Logger with private interface methods ===
        System.out.println("=== Private interface methods ===");
        Logger logger = new AppLogger();
        logger.logInfo("Application started");
        logger.logWarning("Low memory");
        logger.logError("Connection failed");

        // Static method
        Logger.logStatic("This is a static log");

        // === Validator ===
        System.out.println("\n=== Validator with shared private method ===");
        Validator validator = new UserValidator();
        System.out.println("Valid email 'k@a.com': " + validator.isValidEmail("k@a.com"));
        System.out.println("Valid email '':         " + validator.isValidEmail(""));
        System.out.println("Valid name 'Karan':     " + validator.isValidName("Karan"));
        System.out.println("Valid name 'K':         " + validator.isValidName("K"));

        // === Summary ===
        System.out.println("\n=== Interface method evolution ===");
        System.out.println("Java 7: abstract methods only");
        System.out.println("Java 8: + default, + static");
        System.out.println("Java 9: + private, + private static");
        System.out.println("\nPrivate methods: share code between defaults without exposing to subclasses");
    }
}
