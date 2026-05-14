package interview.level2_java8.streams;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Stream_Excercise {


    public static void main(String[] args) {

        // Create a list of integers
        List<Integer> numbers = Arrays.asList(1, 10, 6, 9, 4, 3, 2, 5, 7);

        // Define a predicate to filter numbers greater than 4
        Predicate<Integer> greaterThanFour = (number) -> number > 4;

        // Define a consumer to print elements to the console
        Consumer<Integer> printToConsole = System.out::println;

        // Filter and print sorted numbers using separate predicate and consumer:
        System.out.println("Filtered and sorted numbers (approach 1):");
        numbers.stream()
                .filter(greaterThanFour)  // Apply the predicate to filter numbers
                .sorted()                 // Sort the filtered numbers
                .forEach(printToConsole); // Print each number using the consumer

        // Filter and print sorted numbers using inline lambda expressions:
        System.out.println("\nFiltered and sorted numbers (approach 2):");
        numbers.stream()
                .filter(i -> i > 4)       // Inline predicate for filtering
                .sorted()                 // Sort the filtered numbers
                .forEach(System.out::println); // Print each number directly

    }

}
