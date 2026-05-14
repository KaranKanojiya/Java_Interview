package interview.level1_core.generics;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: Can a generic type have multiple bounds?
// A: Yes. Use & syntax: <T extends Comparable<T> & Serializable>.
//    The first bound can be a class or interface; subsequent bounds must be interfaces.
//    Example: <T extends Number & Comparable<T>> — T must extend Number AND implement Comparable.
//
// Q: What is a recursive type bound?
// A: A type parameter that refers to itself in its bound. Classic example:
//    <T extends Comparable<T>> — T must be comparable to itself.
//    This is how Enum is defined: Enum<E extends Enum<E>>.
//
// Q: What is a type token and why is it useful?
// A: A type token is a Class<T> object used to pass runtime type information.
//    Since generics are erased, Class<T> preserves the type at runtime.
//    Used in frameworks like Jackson (TypeReference), GSON, and dependency injection.
//
// Q: What is the difference between <T> and <?>?
// A: <T> declares a type variable that can be referenced elsewhere in the method/class.
//    <?> is a wildcard — it means "some unknown type" and cannot be referenced by name.
//    Use <T> when you need to refer to the type; use <?> when you don't.
//
// Q: Can generic methods be static?
// A: Yes. Static methods cannot use class-level type parameters, but they CAN declare
//    their own: static <T> T method(T arg). The <T> here is independent of any class <T>.
// ========================================================

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenericMethodsAndBounds {

    // --- 1. Generic method (standalone, not tied to class-level generics) ---
    public static <T> void printArray(T[] array) {
        System.out.print("[");
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            if (i < array.length - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    // --- 2. Multiple bounds: T must be Number AND Comparable ---
    public static <T extends Number & Comparable<T>> T findMin(List<T> list) {
        T min = list.get(0);
        for (T item : list) {
            if (item.compareTo(min) < 0) {
                min = item;
            }
        }
        return min;
    }

    // --- 3. Multiple bounds with Serializable ---
    public static <T extends Comparable<T> & Serializable> List<T> sortAndReturn(List<T> list) {
        List<T> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        return sorted;
    }

    // --- 4. Recursive type bound: <T extends Comparable<T>> ---
    // This ensures T can compare itself to other T instances
    static class SortableBox<T extends Comparable<T>> implements Comparable<SortableBox<T>> {
        private final T value;

        SortableBox(T value) { this.value = value; }

        public T getValue() { return value; }

        @Override
        public int compareTo(SortableBox<T> other) {
            return this.value.compareTo(other.value);
        }

        @Override
        public String toString() { return "Box[" + value + "]"; }
    }

    // --- 5. Type token pattern: using Class<T> for runtime type info ---
    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + clazz.getName(), e);
        }
    }

    // --- 6. Type-safe heterogeneous container (Joshua Bloch, Effective Java) ---
    static class TypeSafeMap {
        private final java.util.Map<Class<?>, Object> map = new java.util.HashMap<>();

        public <T> void put(Class<T> type, T value) {
            map.put(type, type.cast(value));  // runtime type check
        }

        public <T> T get(Class<T> type) {
            return type.cast(map.get(type));  // safe cast using type token
        }
    }

    // --- 7. Generic method returning different types based on input ---
    @SuppressWarnings("unchecked")
    public static <T> T convertValue(Object value, Class<T> targetType) {
        if (targetType == String.class) {
            return (T) String.valueOf(value);
        } else if (targetType == Integer.class) {
            return (T) Integer.valueOf(value.toString());
        } else if (targetType == Double.class) {
            return (T) Double.valueOf(value.toString());
        }
        throw new IllegalArgumentException("Unsupported type: " + targetType);
    }

    // --- 8. Bounded wildcard in method signature ---
    // Copy elements from a producer (extends) to a consumer (super)
    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (T item : src) {
            dest.add(item);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Generic Method ===");
        Integer[] intArr = {1, 2, 3, 4, 5};
        String[] strArr = {"Hello", "World"};
        printArray(intArr);
        printArray(strArr);

        System.out.println("\n=== 2. Multiple Bounds (Number & Comparable) ===");
        List<Integer> ints = Arrays.asList(5, 2, 8, 1, 9);
        System.out.println("Min: " + findMin(ints));
        List<Double> doubles = Arrays.asList(3.14, 1.41, 2.72);
        System.out.println("Min double: " + findMin(doubles));

        System.out.println("\n=== 3. Multiple Bounds (Comparable & Serializable) ===");
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        System.out.println("Sorted: " + sortAndReturn(names));

        System.out.println("\n=== 4. Recursive Type Bound ===");
        List<SortableBox<Integer>> boxes = new ArrayList<>();
        boxes.add(new SortableBox<>(30));
        boxes.add(new SortableBox<>(10));
        boxes.add(new SortableBox<>(20));
        Collections.sort(boxes);
        System.out.println("Sorted boxes: " + boxes);

        System.out.println("\n=== 5. Type Token Pattern ===");
        StringBuilder sb = createInstance(StringBuilder.class);
        sb.append("Created via type token!");
        System.out.println(sb);

        System.out.println("\n=== 6. Type-Safe Heterogeneous Container ===");
        TypeSafeMap tsm = new TypeSafeMap();
        tsm.put(String.class, "Hello");
        tsm.put(Integer.class, 42);
        tsm.put(Double.class, 3.14);
        System.out.println("String: " + tsm.get(String.class));
        System.out.println("Integer: " + tsm.get(Integer.class));
        System.out.println("Double: " + tsm.get(Double.class));

        System.out.println("\n=== 7. Generic Conversion ===");
        String s = convertValue(42, String.class);
        Integer i = convertValue("123", Integer.class);
        System.out.println("42 as String: \"" + s + "\", \"123\" as Integer: " + i);

        System.out.println("\n=== 8. Bounded Wildcard Copy ===");
        List<Number> dest = new ArrayList<>();
        List<Integer> src = Arrays.asList(1, 2, 3);
        copy(dest, src);  // dest is List<? super Integer>, src is List<? extends Integer>
        System.out.println("Copied: " + dest);
    }
}
