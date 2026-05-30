package interview.level1_core.exceptions;

import java.io.FileNotFoundException;

/**
 * Q7. What is the difference between throw and throws?
 *
 * throw:
 *   - Used INSIDE a method body to explicitly throw an exception
 *   - Followed by an exception OBJECT: throw new RuntimeException("error")
 *   - Can throw only ONE exception at a time
 *
 * throws:
 *   - Used in METHOD SIGNATURE to declare what exceptions the method might throw
 *   - Followed by exception CLASS name(s): void read() throws IOException, SQLException
 *   - Caller must handle (catch) or propagate (throws) the declared exceptions
 *   - Only needed for CHECKED exceptions (RuntimeException doesn't need throws)
 */
public class ThrowVsThrows {

    // throws — declares this method MIGHT throw FileNotFoundException
    static String readFile(String path) throws FileNotFoundException {
        if (path == null) {
            // throw — actually throws the exception
            throw new FileNotFoundException("Path cannot be null!");
        }
        return "file contents";
    }

    // RuntimeException doesn't require throws declaration
    static int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");  // no throws needed
        }
        return a / b;
    }

    // Multiple exceptions in throws
    static void riskyMethod() throws FileNotFoundException, InterruptedException {
        throw new FileNotFoundException("demo");
    }

    public static void main(String[] args) {

        // Must handle checked exception (FileNotFoundException)
        System.out.println("=== throw and throws ===");
        try {
            readFile(null);
        } catch (FileNotFoundException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Runtime exception — no try-catch required (but recommended)
        try {
            divide(10, 0);
        } catch (ArithmeticException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        System.out.println("\n=== Summary ===");
        System.out.println("throw:  used to throw an exception object (inside method)");
        System.out.println("throws: used to declare exceptions (in method signature)");
    }
}
