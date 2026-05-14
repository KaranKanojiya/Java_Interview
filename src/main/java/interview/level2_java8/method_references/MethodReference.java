package interview.level2_java8.method_references;

import java.util.Arrays;
import java.util.List;

public class MethodReference {

    // Main entry point of the program
    public static void main(String[] args) {

        // Create a list of integers
        List<Integer> numbers = Arrays.asList(1, 10, 6, 9, 4, 3, 2, 5, 7);

        // Print sorted elements using a static method reference:
        System.out.println("Printing using static method reference:");
        numbers.stream()
                .sorted()  // Sort the elements in ascending order
                .forEach(MethodReference::printElement); // Call the static printElement method

        // Print sorted elements using an instance method reference:
        System.out.println("\nPrinting using instance method reference:");
        MethodReference instance = new MethodReference(); // Create an instance of the class
        numbers.stream()
                .sorted()  // Sort the elements again
                .forEach(instance::printElement1); // Call the instance method printElement1
    }

    // Static method to print an integer element
    private static void printElement(int number) {
        System.out.println(number);
    }

    // Instance method to print an integer element
    private void printElement1(int number) {
        System.out.println(number);
    }
}


