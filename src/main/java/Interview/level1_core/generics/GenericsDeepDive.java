package interview.level1_core.generics;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: What is type erasure in Java?
// A: Type erasure is the process by which the Java compiler removes all generic type
//    information at compile time. Generic types are replaced with their bounds (or Object
//    if unbounded). This means at runtime, List<String> and List<Integer> are both just List.
//    This was done for backward compatibility with pre-generics code.
//
// Q: Why can't you create new T[] in Java?
// A: Because of type erasure. At runtime, T is erased to Object, so new T[] would actually
//    create Object[]. This could violate type safety. Instead, use Array.newInstance() or
//    pass a Class<T> token.
//
// Q: Explain PECS with example.
// A: PECS = Producer Extends, Consumer Super.
//    - Use <? extends T> when you only READ from the collection (it produces T values).
//      e.g., List<? extends Number> — you can read Numbers but can't add to it.
//    - Use <? super T> when you only WRITE to the collection (it consumes T values).
//      e.g., List<? super Integer> — you can add Integers but reads return Object.
//    - Example: Collections.copy(List<? super T> dest, List<? extends T> src)
//
// Q: What is the difference between List<?>, List<Object>, and List<? extends Object>?
// A: List<?> — unbounded wildcard, read-only (can't add anything except null).
//    List<Object> — concrete type, can add Objects, but NOT a supertype of List<String>.
//    List<? extends Object> — effectively same as List<?>.
//
// Q: Can you use generics with primitive types?
// A: No. Generics only work with reference types due to type erasure. Use wrapper classes
//    (Integer, Double, etc.) instead. Project Valhalla may change this in the future.
// ========================================================

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericsDeepDive {

    // --- 1. Generic class with bounded type parameter ---
    static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }
        public V getValue() { return value; }

        @Override
        public String toString() {
            return "(" + key + ", " + value + ")";
        }
    }

    // --- 2. Bounded type parameter: T must be Comparable ---
    static <T extends Comparable<T>> T findMax(List<T> list) {
        T max = list.get(0);
        for (T item : list) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    // --- 3. Generic method with type inference ---
    static <T> List<T> listOf(T... items) {
        List<T> list = new ArrayList<>();
        for (T item : items) {
            list.add(item);
        }
        return list;
    }

    // --- 4. PECS: Producer Extends, Consumer Super ---
    // Producer — reads from source, so use "extends"
    static double sumOfList(List<? extends Number> list) {
        double sum = 0;
        for (Number n : list) {  // safe to read as Number
            sum += n.doubleValue();
        }
        return sum;
    }

    // Consumer — writes to destination, so use "super"
    static void addIntegers(List<? super Integer> list) {
        list.add(1);  // safe to add Integer
        list.add(2);
        list.add(3);
        // Object obj = list.get(0);  // reading returns Object — not very useful
    }

    // --- 5. Wildcard capture helper ---
    static <T> void swapHelper(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    static void swap(List<?> list, int i, int j) {
        // Wildcard capture: delegate to helper method that captures the type
        swapHelper(list, i, j);
    }

    // --- 6. Type erasure demonstration ---
    static void typeErasureDemo() {
        List<String> strings = new ArrayList<>();
        List<Integer> ints = new ArrayList<>();

        // At runtime, both are just ArrayList — type info is erased
        System.out.println("strings class: " + strings.getClass());
        System.out.println("ints class:    " + ints.getClass());
        System.out.println("Same class?    " + (strings.getClass() == ints.getClass()));
        // Output: true — proving type erasure
    }

    // --- 7. Why you can't do new T[] ---
    @SuppressWarnings("unchecked")
    static <T> T[] createArray(Class<T> clazz, int size) {
        // Can't do: return new T[size];  // compile error
        // Workaround using reflection:
        return (T[]) java.lang.reflect.Array.newInstance(clazz, size);
    }

    // --- 8. Diamond operator (Java 7+) ---
    static void diamondOperatorDemo() {
        // Before Java 7:
        // List<Map<String, List<Integer>>> old = new ArrayList<Map<String, List<Integer>>>();

        // With diamond operator — compiler infers the type:
        List<Pair<String, Integer>> pairs = new ArrayList<>();
        pairs.add(new Pair<>("age", 30));
        pairs.add(new Pair<>("score", 95));
        System.out.println("Diamond operator pairs: " + pairs);
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Generic Class (Pair) ===");
        Pair<String, Integer> p = new Pair<>("Karan", 30);
        System.out.println("Pair: " + p);

        System.out.println("\n=== 2. Bounded Type Parameter ===");
        List<Integer> nums = Arrays.asList(3, 7, 1, 9, 4);
        System.out.println("Max: " + findMax(nums));

        System.out.println("\n=== 3. Generic Method with Type Inference ===");
        List<String> names = listOf("Alice", "Bob", "Charlie");
        System.out.println("Names: " + names);

        System.out.println("\n=== 4. PECS Demo ===");
        // Producer Extends — read from List<Integer> as List<? extends Number>
        List<Integer> integers = Arrays.asList(10, 20, 30);
        System.out.println("Sum of integers: " + sumOfList(integers));

        List<Double> doubles = Arrays.asList(1.5, 2.5, 3.5);
        System.out.println("Sum of doubles: " + sumOfList(doubles));

        // Consumer Super — write Integers into List<Number>
        List<Number> numberList = new ArrayList<>();
        addIntegers(numberList);  // List<Number> is a List<? super Integer>
        System.out.println("After addIntegers: " + numberList);

        System.out.println("\n=== 5. Wildcard Capture ===");
        List<String> swapList = new ArrayList<>(Arrays.asList("A", "B", "C"));
        swap(swapList, 0, 2);
        System.out.println("After swap(0,2): " + swapList);

        System.out.println("\n=== 6. Type Erasure ===");
        typeErasureDemo();

        System.out.println("\n=== 7. Array creation with generics ===");
        String[] strArr = createArray(String.class, 3);
        strArr[0] = "Hello";
        System.out.println("Generic array: " + Arrays.toString(strArr));

        System.out.println("\n=== 8. Diamond Operator ===");
        diamondOperatorDemo();
    }
}
