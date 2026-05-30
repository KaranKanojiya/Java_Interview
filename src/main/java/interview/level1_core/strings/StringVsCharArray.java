package interview.level1_core.strings;

import java.util.Arrays;

/**
 * Q19. Why is char[] preferred over String for passwords?
 *
 * 1. Immutability risk:
 *    String is immutable → password stays in memory until GC collects it
 *    char[] is mutable → you can overwrite it with zeros when done
 *
 * 2. String pool risk:
 *    String literals go to the String pool → may persist for entire JVM lifetime
 *    char[] is never pooled
 *
 * 3. Logging/toString risk:
 *    String can accidentally appear in logs, toString(), stack traces
 *    char[] prints as [C@hashcode (memory address), not the actual content
 *
 * 4. Heap dump risk:
 *    String persists in memory → visible in heap dumps
 *    char[] can be zeroed out → reduced exposure window
 *
 * This is why JPasswordField.getPassword() returns char[], not String.
 */
public class StringVsCharArray {

    public static void main(String[] args) {

        // === Problem with String passwords ===
        System.out.println("=== String password (BAD) ===");
        String passwordStr = "MySecret123";
        System.out.println("String in log: " + passwordStr);  // password visible!
        // passwordStr is now in the String pool — can't erase it
        // Even setting passwordStr = null doesn't erase "MySecret123" from pool

        // === char[] password (GOOD) ===
        System.out.println("\n=== char[] password (GOOD) ===");
        char[] passwordArr = {'M', 'y', 'S', 'e', 'c', 'r', 'e', 't', '1', '2', '3'};
        System.out.println("char[] in log: " + passwordArr);  // prints [C@hash, NOT content
        System.out.println("Explicit print: " + new String(passwordArr));

        // Clear password when done
        Arrays.fill(passwordArr, '\0');
        System.out.println("After clearing: " + new String(passwordArr));  // empty

        // === Demo: toString() difference ===
        System.out.println("\n=== toString() behavior ===");
        String str = "password123";
        char[] arr = "password123".toCharArray();
        System.out.println("String.toString(): " + str.toString());   // prints password
        System.out.println("char[].toString(): " + arr.toString());   // prints [C@hashcode

        // Clean up
        Arrays.fill(arr, '\0');

        // === Summary ===
        System.out.println("\n=== Why char[] for passwords ===");
        System.out.println("1. Can be explicitly cleared (Arrays.fill with 0)");
        System.out.println("2. Not stored in String pool");
        System.out.println("3. Doesn't leak in logs/toString()");
        System.out.println("4. Shorter exposure window in heap dumps");
        System.out.println("\nThat's why JPasswordField returns char[], not String!");
    }
}
