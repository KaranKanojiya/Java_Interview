# Java Interview Prep -- From Core to Cutting Edge

**Complete Java interview preparation repository — organized by experience level, from fundamentals to JVM internals.**

> **140 Java files** | **7 progressive levels** | **Java 8 through Java 25/26** | **130+ interview questions with code**

```
Level 1  [==========]  Core Java           Mid-Level       30 questions
Level 2  [========  ]  Java 8 Features     Mid/Senior      20 questions
Level 3  [========  ]  Multithreading      Senior          25 questions
Level 4  [======    ]  Java 9-17           Senior/Staff    15 questions
Level 5  [======    ]  Java 17-21          Staff           15 questions
Level 6  [====      ]  JVM Internals       Staff/Principal 15 questions
Level 7  [==        ]  Java 25/26          Principal       10 questions
```

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Repository Structure](#repository-structure)
3. [Level 1: Core Java (Mid-Level) -- 30 Questions](#level-1-core-java-mid-level----30-questions)
4. [Level 2: Java 8 Features (Mid/Senior) -- 20 Questions](#level-2-java-8-features-midsenior----20-questions)
5. [Level 3: Multithreading & Concurrency (Senior) -- 25 Questions](#level-3-multithreading--concurrency-senior----25-questions)
6. [Level 4: Java 9-17 (Senior/Staff) -- 15 Questions](#level-4-java-9-17-seniorstaff----15-questions)
7. [Level 5: Java 17-21 (Staff) -- 15 Questions](#level-5-java-17-21-staff----15-questions)
8. [Level 6: JVM Internals (Staff/Principal) -- 15 Questions](#level-6-jvm-internals-staffprincipal----15-questions)
9. [Level 7: Java 25/26 (Principal) -- 10 Questions](#level-7-java-2526-principal----10-questions)
10. [Recently Asked in FAANG / Big Tech (2024-2025)](#recently-asked-in-faang--big-tech-2024-2025)
11. [Quick Reference Tables](#quick-reference-tables)
12. [How to Use This Repo -- 6-Week Study Plan](#how-to-use-this-repo----6-week-study-plan)

---

## Quick Start

### Prerequisites

- **JDK 21+** (preview features enabled)
- **Maven 3.9+**

### Clone & Build

```bash
git clone <repo-url>
cd Java_Interview

# Compile all 140 files
mvn compile

# Run any demo (example)
java --enable-preview -cp target/classes interview.level1_core.collections.HashMapInternals
java --enable-preview -cp target/classes interview.level5_java17to21.virtual_threads.VirtualThreadsDemo
```

### Project Configuration

The project uses Maven with Java 21 and `--enable-preview` enabled:

```xml
<maven.compiler.source>21</maven.compiler.source>
<maven.compiler.target>21</maven.compiler.target>
<compilerArgs>
    <arg>--enable-preview</arg>
</compilerArgs>
```

---

## Repository Structure

```
src/main/java/interview/
|
|-- level1_core/                          # Core Java -- Mid-Level
|   |-- oop/                              # OOP pillars, inheritance, polymorphism
|   |-- collections/                      # HashMap internals, ConcurrentHashMap, TreeMap, PriorityQueue
|   |-- strings/                          # String pool, intern(), StringBuilder vs StringBuffer
|   |-- exceptions/                       # Exception hierarchy, custom exceptions, try-catch-finally
|   |-- generics/                         # Type erasure, PECS, bounded types, generic methods
|   |-- immutability/                     # Immutable class recipe, defensive copies
|   |-- serialization/                    # Serializable, transient, serialization proxy
|   |-- enums/                            # Advanced enums, EnumSet, EnumMap, state machine
|   |-- hashcode_equals/                  # Contract, HashMap interaction
|   |-- sorting/                          # Comparable vs Comparator
|
|-- level2_java8/                         # Java 8 Features -- Mid/Senior
|   |-- streams/                          # Stream API, exercises, map/flatMap/reduce/groupingBy
|   |-- lambda/                           # Lambda expressions, functional interfaces
|   |-- functional_interfaces/            # UPI payment example, Predicate/Function/Consumer/Supplier
|   |-- optional/                         # Optional best practices, anti-patterns
|   |-- datetime/                         # Java 8 DateTime API
|   |-- method_references/                # Static, instance, constructor references
|
|-- level3_multithreading/                # Concurrency -- Senior
|   |-- basics/                           # Thread creation, Runnable, Callable, volatile, ThreadLocal
|   |-- executors/                        # ThreadPool types, rejection policies, lifecycle
|   |-- completable_future/              # CompletableFuture chaining, combining, exception handling
|   |-- locks/                            # ReentrantLock, ReadWriteLock, Condition
|   |-- synchronizers/                    # CountDownLatch, CyclicBarrier, Semaphore, Phaser
|   |-- fork_join/                        # ForkJoinPool, RecursiveTask, work-stealing
|   |-- patterns/                         # Producer-Consumer, Deadlock detection/prevention
|
|-- level4_java9to17/                     # Modern Java -- Senior/Staff
|   |-- var_type/                         # Local variable type inference
|   |-- switch_expressions/               # Arrow syntax, yield, exhaustiveness
|   |-- text_blocks/                      # Multi-line strings, indentation
|   |-- records/                          # Record classes, compact constructors
|   |-- sealed_classes/                   # Sealed types, algebraic data types
|   |-- pattern_matching/                 # Pattern matching for instanceof
|   |-- stream_enhancements/              # takeWhile, dropWhile, toList, mapMulti, teeing
|   |-- optional_enhancements/            # ifPresentOrElse, or, stream, isEmpty
|   |-- http_client/                      # Java 11 HttpClient, async requests
|   |-- modules/                          # JPMS overview
|
|-- level5_java17to21/                    # Cutting Edge -- Staff
|   |-- virtual_threads/                  # Project Loom, 100K thread demo, pinning
|   |-- structured_concurrency/           # StructuredTaskScope, ShutdownOnFailure
|   |-- scoped_values/                    # ScopedValue vs ThreadLocal
|   |-- pattern_matching_switch/          # Guarded patterns, null handling, sealed exhaustiveness
|   |-- record_patterns/                  # Destructuring, nested patterns
|   |-- sequenced_collections/            # getFirst/getLast, reversed views
|   |-- string_templates/                 # Template processors, alternatives
|
|-- level6_jvm_internals/                 # JVM Deep Dive -- Staff/Principal
|   |-- memory_model/                     # Heap, Metaspace, Stack, thread memory
|   |-- gc/                               # G1, ZGC, Shenandoah, GC tuning flags
|   |-- classloading/                     # Class loader hierarchy, custom class loader
|   |-- jit/                              # JIT compilation, escape analysis, inlining
|   |-- memory_leaks/                     # 7 common leak patterns
|   |-- profiling/                        # JFR, jcmd, jmap, jstack, heap dumps
|
|-- level7_java25_26/                     # Future Awareness -- Principal
    |-- stream_gatherers/                 # Custom intermediate operations
    |-- flexible_constructors/            # Statements before super()
    |-- value_types/                      # Project Valhalla, identity-free objects
    |-- primitive_generics/               # List<int>, no autoboxing
    |-- compact_source/                   # Unnamed classes, instance main
```

---

## Level 1: Core Java (Mid-Level) -- 30 Questions

> These are the bread-and-butter questions. If you cannot answer these confidently, stop here and study before moving on. Every single Java interview starts at this level.

---

### Q1. How does HashMap internally work? (THE #1 most-asked question)

**Answer:** HashMap uses an array of buckets (default size 16). Each bucket is a linked list (or balanced tree after threshold).

1. `map.put(key, value)` computes `hashCode()` of the key
2. Bucket index = `hash & (n - 1)` where n is array length
3. If no collision, insert as first node
4. If collision, check `equals()` -- if key exists, replace value; otherwise append to list
5. **Java 8 improvement:** When a bucket exceeds 8 nodes (treeify threshold), the linked list converts to a red-black tree for O(log n) lookup instead of O(n)

Each node stores: `key`, `value`, `hash`, `next`

**Code:**
- [`level1_core/collections/HashMapInternals.java`](src/main/java/interview/level1_core/collections/HashMapInternals.java)

---

### Q2. What is the equals() and hashCode() contract?

**Answer:**
- If two objects are **equal** (`equals()` returns true), they **must** have the same `hashCode()`
- If two objects have the same `hashCode()`, they are **not necessarily** equal
- If you override `equals()`, you **must** override `hashCode()`
- Violating this contract causes HashMap/HashSet to malfunction (duplicate entries, lost lookups)

**Code:**
- [`level1_core/hashcode_equals/Employee.java`](src/main/java/interview/level1_core/hashcode_equals/Employee.java)

---

### Q3. Why is String immutable in Java?

**Answer:**
1. **String pool optimization** -- Strings can be shared/reused safely in the pool
2. **Security** -- Strings used for DB connections, file paths, network URLs cannot be tampered
3. **Thread safety** -- Immutable objects are inherently thread-safe
4. **Hashcode caching** -- Since String is immutable, hashCode is computed once and cached

**Code:**
- [`level1_core/strings/StringObject.java`](src/main/java/interview/level1_core/strings/StringObject.java)
- [`level1_core/strings/StringPoolAndIntern.java`](src/main/java/interview/level1_core/strings/StringPoolAndIntern.java)

---

### Q4. How many ways can you create a String object?

**Answer:**
1. **String literal:** `String s = "hello";` -- created in the String Constant Pool (SCP)
2. **new keyword:** `String s = new String("hello");` -- creates object in heap + pool entry if not present
3. `"hello" + "world"` -- compile-time concatenation creates one pool entry
4. `intern()` -- moves/references a string in the pool

`String s = new String("hello");` creates **2 objects** if "hello" is not already in the pool (one in heap, one in SCP).

**Code:**
- [`level1_core/strings/StringPoolAndIntern.java`](src/main/java/interview/level1_core/strings/StringPoolAndIntern.java)

---

### Q5. What is the difference between StringBuilder and StringBuffer?

**Answer:**

| Feature | StringBuilder | StringBuffer |
|---------|--------------|-------------|
| Thread Safety | Not thread-safe | Thread-safe (synchronized) |
| Performance | Faster | Slower |
| When to use | Single-threaded string manipulation | Multi-threaded string manipulation |

Both are mutable, unlike String.

---

### Q6. Explain the Exception hierarchy in Java

**Answer:**
```
Throwable
|-- Error (JVM errors -- OutOfMemoryError, StackOverflowError)
|-- Exception
    |-- Checked Exceptions (compile-time: IOException, SQLException)
    |-- RuntimeException (unchecked: NullPointerException, ArrayIndexOutOfBoundsException)
```

- **Checked exceptions:** Must be caught or declared in `throws`
- **Unchecked exceptions:** Extend `RuntimeException`, no compile-time check
- **Errors:** Should never be caught (represent JVM-level failures)

**Code:**
- [`level1_core/exceptions/ExceptionOrder.java`](src/main/java/interview/level1_core/exceptions/ExceptionOrder.java)
- [`level1_core/exceptions/TryCatchFinallyReturnFlow.java`](src/main/java/interview/level1_core/exceptions/TryCatchFinallyReturnFlow.java)

---

### Q7. What is the difference between throw and throws?

**Answer:**
- **`throw`** -- Used to explicitly throw an exception: `throw new RuntimeException("error");`
- **`throws`** -- Used in method signature to declare what exceptions a method might throw: `void read() throws IOException`

---

### Q8. How to write a custom exception?

**Answer:** Extend `Exception` (checked) or `RuntimeException` (unchecked):
```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
```

**Code:**
- [`level1_core/exceptions/OrderNotFoundException.java`](src/main/java/interview/level1_core/exceptions/OrderNotFoundException.java)

---

### Q9. Is the finally block always executed?

**Answer:** Yes, except when:
1. `System.exit()` is called in try/catch
2. JVM crashes
3. Thread is killed

Important: If both `catch` and `finally` have return statements, the **finally block's return value wins**.

**Code:**
- [`level1_core/exceptions/TryCatchFinallyReturnFlow.java`](src/main/java/interview/level1_core/exceptions/TryCatchFinallyReturnFlow.java)

---

### Q10. What is the exception ordering rule in catch blocks?

**Answer:** In multiple catch blocks, **child exceptions must be caught before parent exceptions**. Otherwise, the compiler throws an error because the child catch block would be unreachable.

**Code:**
- [`level1_core/exceptions/ExceptionOrder.java`](src/main/java/interview/level1_core/exceptions/ExceptionOrder.java)

---

### Q11. What is final, finally, and finalize?

**Answer:**

| Keyword | Type | Purpose |
|---------|------|---------|
| `final` | Keyword | Variable: cannot reassign. Method: cannot override. Class: cannot extend |
| `finally` | Block | Always executes after try/catch (cleanup: close DB connections, streams) |
| `finalize` | Method | Called by GC before reclaiming object memory (deprecated since Java 9) |

---

### Q12. What are the four pillars of OOP?

**Answer:**
1. **Inheritance** -- One class acquires properties of another (`extends`). Use for "is-a" relationships
2. **Encapsulation** -- Binding data + methods together; private fields + public getters/setters
3. **Abstraction** -- Hiding implementation details. Achieved via abstract classes (0-100%) and interfaces (100%)
4. **Polymorphism** -- One interface, multiple implementations. Compile-time (overloading) and runtime (overriding)

**Code:**
- [`level1_core/oop/Parent.java`](src/main/java/interview/level1_core/oop/Parent.java)
- [`level1_core/oop/Child.java`](src/main/java/interview/level1_core/oop/Child.java)
- [`level1_core/oop/Example.java`](src/main/java/interview/level1_core/oop/Example.java)

---

### Q13. Can you override private and static methods?

**Answer:**
- **Private methods:** No. They are not visible to subclasses
- **Static methods:** No. They are bound to the class, not the instance. You can **hide** a static method (method hiding), but it is not true overriding

---

### Q14. What changed in interfaces from Java 7 to Java 8?

**Answer:**
- **Java 7:** Only abstract methods allowed
- **Java 8:** Added `default` methods (optional to override) and `static` methods (utility methods)
- **Java 9:** Added `private` methods in interfaces

**Code:**
- [`level1_core/oop/DaoFramework.java`](src/main/java/interview/level1_core/oop/DaoFramework.java)
- [`level1_core/oop/Entity.java`](src/main/java/interview/level1_core/oop/Entity.java)
- [`level1_core/oop/Deletable.java`](src/main/java/interview/level1_core/oop/Deletable.java)

---

### Q15. What is a marker interface? Can you create your own?

**Answer:** A marker interface is an **empty interface** with no methods. It serves as a "tag" to indicate a capability:
- `Serializable` -- Object can be serialized
- `Cloneable` -- Object can be cloned
- `Remote` -- Object can be used in RMI

Yes, you can create your own. However, modern Java prefers **annotations** over marker interfaces.

---

### Q16. What is type erasure in Generics?

**Answer:** Java generics are implemented using **type erasure** -- the compiler removes all generic type information at compile time and inserts casts where needed. At runtime, `List<String>` and `List<Integer>` are both just `List`.

Consequences:
- Cannot use `instanceof` with generic types: `obj instanceof List<String>` is illegal
- Cannot create generic arrays: `new T[]` is illegal
- Cannot use primitives: `List<int>` is illegal (must use `List<Integer>`)

**Code:**
- [`level1_core/generics/GenericsDeepDive.java`](src/main/java/interview/level1_core/generics/GenericsDeepDive.java)
- [`level1_core/generics/GenericMethodsAndBounds.java`](src/main/java/interview/level1_core/generics/GenericMethodsAndBounds.java)

---

### Q17. What is PECS (Producer Extends, Consumer Super)?

**Answer:**
- **`? extends T`** -- Producer: reads items from a collection (upper bound). Use when you only GET values.
- **`? super T`** -- Consumer: writes items to a collection (lower bound). Use when you only PUT values.

```java
// Producer -- reads from source
void copy(List<? extends Number> source) { ... }

// Consumer -- writes to destination
void addAll(List<? super Integer> dest) { ... }
```

**Code:**
- [`level1_core/generics/GenericsDeepDive.java`](src/main/java/interview/level1_core/generics/GenericsDeepDive.java)

---

### Q18. How to create an immutable class in Java?

**Answer:** The 5-step recipe:
1. Make the class `final` (prevent subclassing)
2. Declare all fields `private final`
3. No setter methods
4. Initialize all fields via constructor
5. Return **defensive copies** of mutable fields from getters

**Code:**
- [`level1_core/immutability/Employee.java`](src/main/java/interview/level1_core/immutability/Employee.java)
- [`level1_core/immutability/Address.java`](src/main/java/interview/level1_core/immutability/Address.java)
- [`level1_core/immutability/ImmutableClassRecipe.java`](src/main/java/interview/level1_core/immutability/ImmutableClassRecipe.java)

---

### Q19. String vs char[] for storing passwords?

**Answer:** Use `char[]` because:
1. Strings are **immutable** -- password stays in memory as plain text until GC
2. Strings may be stored in the **String Pool**, persisting even longer
3. char[] can be **explicitly zeroed out** after use: `Arrays.fill(password, '0')`
4. `toString()` of char[] does not reveal contents (prints hash, not characters)

---

### Q20. What collections did you use in your project?

**Answer:**
- **List:** `ArrayList` (random access), `LinkedList` (frequent insert/delete)
- **Set:** `HashSet` (unordered unique), `LinkedHashSet` (insertion-order unique), `TreeSet` (sorted unique)
- **Map:** `HashMap` (unordered key-value), `LinkedHashMap` (insertion-order), `TreeMap` (sorted keys)

---

### Q21. ArrayList vs LinkedList?

**Answer:**

| Feature | ArrayList | LinkedList |
|---------|-----------|------------|
| Internal structure | Dynamic array | Doubly linked list |
| Random access `get(i)` | O(1) | O(n) |
| Insert/Delete at middle | O(n) -- shift elements | O(1) -- change pointers |
| Memory | Less (no pointers) | More (prev/next pointers per node) |
| Best for | Read-heavy operations | Write-heavy operations |

---

### Q22. Why does Set not allow duplicates?

**Answer:** `HashSet.add(e)` internally calls `HashMap.put(e, PRESENT)` where the element becomes the **key** of the map. Since HashMap keys are unique (checked via `hashCode()` + `equals()`), duplicates are automatically rejected.

---

### Q23. What happens when you declare a List as final?

**Answer:** `final` prevents **reassignment** of the reference, not modification of contents:
```java
final List<Integer> list = new ArrayList<>();
list.add(1);                    // ALLOWED -- modifying contents
list = new ArrayList<>();       // COMPILE ERROR -- reassigning reference
```

For truly unmodifiable lists, use `Collections.unmodifiableList()` or `List.of()`.

---

### Q24. Comparable vs Comparator?

**Answer:**

| Feature | Comparable | Comparator |
|---------|-----------|------------|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T o)` | `compare(T o1, T o2)` |
| Location | Inside the class being compared | External class |
| Sort orders | Single natural ordering | Multiple custom orderings |
| Usage | `Collections.sort(list)` | `Collections.sort(list, comparator)` |

**Code:**
- [`level1_core/sorting/Student.java`](src/main/java/interview/level1_core/sorting/Student.java)
- [`level1_core/sorting/AgeComparator.java`](src/main/java/interview/level1_core/sorting/AgeComparator.java)

---

### Q25. What is fail-fast vs fail-safe?

**Answer:**

| Feature | Fail-Fast | Fail-Safe |
|---------|-----------|-----------|
| Behavior | Throws `ConcurrentModificationException` if modified during iteration | Allows modification during iteration |
| Mechanism | Checks `modCount` on each iteration | Works on a **copy** of the collection |
| Memory | No extra memory | Extra memory for the copy |
| Examples | `ArrayList`, `HashMap`, `HashSet` | `ConcurrentHashMap`, `CopyOnWriteArrayList` |

**Code:**
- [`level1_core/collections/FailFastVsFailSafe.java`](src/main/java/interview/level1_core/collections/FailFastVsFailSafe.java)
- [`level1_core/collections/FailFast.java`](src/main/java/interview/level1_core/collections/FailFast.java)
- [`level1_core/collections/FailSafe.java`](src/main/java/interview/level1_core/collections/FailSafe.java)

---

### Q26. HashMap vs Hashtable vs ConcurrentHashMap?

**Answer:**

| Feature | HashMap | Hashtable | ConcurrentHashMap |
|---------|---------|-----------|-------------------|
| Thread-safe | No | Yes (entire table locked) | Yes (segment-level locking) |
| Null keys/values | 1 null key, many null values | No nulls | No nulls |
| Performance | Fastest (no sync) | Slowest (full sync) | Fast (fine-grained locks) |
| Iterator | Fail-fast | Fail-safe | Fail-safe |
| When to use | Single-threaded | Legacy (avoid) | Multi-threaded |

**Code:**
- [`level1_core/collections/ConcurrentHashMapInternals.java`](src/main/java/interview/level1_core/collections/ConcurrentHashMapInternals.java)

---

### Q27. How does ConcurrentHashMap achieve thread safety?

**Answer:** Unlike Hashtable which locks the **entire table**, ConcurrentHashMap uses:
- **Java 7:** Segment-level locking (16 segments by default)
- **Java 8+:** Node-level locking using `synchronized` + CAS operations on individual buckets

This allows multiple threads to read/write different buckets simultaneously.

**Code:**
- [`level1_core/collections/ConcurrentHashMapInternals.java`](src/main/java/interview/level1_core/collections/ConcurrentHashMapInternals.java)

---

### Q28. What is Serialization? What is the transient keyword?

**Answer:**
- **Serialization:** Converting an object to a byte stream for storage/transmission. Class must implement `Serializable`.
- **Deserialization:** Reconstructing the object from the byte stream.
- **`transient`:** Fields marked transient are **excluded** from serialization (e.g., passwords, cached values).
- **`serialVersionUID`:** Version control for serialized objects. If not declared, JVM generates one -- and deserialization fails if the class changes.

**Code:**
- [`level1_core/serialization/SerializationDemo.java`](src/main/java/interview/level1_core/serialization/SerializationDemo.java)

---

### Q29. What are Enums in Java? Can they have methods?

**Answer:** Enums are special classes that represent a fixed set of constants. They can have:
- Fields, constructors, methods
- Abstract methods (each constant provides implementation)
- Implement interfaces

Advanced usage: EnumSet (bit-vector backed, fastest Set), EnumMap (array-backed, fastest Map for enum keys), state machines.

**Code:**
- [`level1_core/enums/EnumAdvanced.java`](src/main/java/interview/level1_core/enums/EnumAdvanced.java)

---

### Q30. TreeMap and PriorityQueue -- when to use each?

**Answer:**
- **TreeMap:** Sorted key-value pairs (red-black tree). O(log n) for get/put. Use when you need sorted keys or range queries (`subMap`, `headMap`, `tailMap`).
- **PriorityQueue:** Min-heap by default. O(log n) insert, O(1) peek. Use when you need the smallest/largest element quickly (e.g., top-K problems, task scheduling).

**Code:**
- [`level1_core/collections/TreeMapAndPriorityQueue.java`](src/main/java/interview/level1_core/collections/TreeMapAndPriorityQueue.java)

---

## Level 2: Java 8 Features (Mid/Senior) -- 20 Questions

> Java 8 is the most heavily tested version in interviews. Every single company asks Stream API questions. Know these cold.

---

### Q1. What are the key features introduced in Java 8?

**Answer:**
1. **Lambda Expressions** -- Concise way to represent anonymous functions
2. **Stream API** -- Functional-style operations on collections
3. **Functional Interfaces** -- `@FunctionalInterface` with exactly one abstract method
4. **Optional** -- Container to avoid NullPointerException
5. **Default & Static methods in interfaces**
6. **Method References** -- Shorthand for lambdas
7. **New Date/Time API** (`java.time` package)
8. **CompletableFuture** -- Async programming

**Code:**
- [`level2_java8/Java8.java`](src/main/java/interview/level2_java8/Java8.java)

---

### Q2. What is a Functional Interface?

**Answer:** An interface with **exactly one abstract method** (can have multiple default/static methods). Annotated with `@FunctionalInterface`. Can be implemented using lambda expressions.

Pre-Java 8 functional interfaces: `Runnable`, `Callable`, `Comparator`

Java 8 introduced: `Predicate`, `Function`, `Consumer`, `Supplier`

---

### Q3. What are the four core functional interfaces in Java 8?

**Answer:**

| Interface | Input | Output | Method | Example Use Case |
|-----------|-------|--------|--------|-----------------|
| `Predicate<T>` | T | boolean | `test(T)` | Filtering: `filter(x -> x > 5)` |
| `Function<T,R>` | T | R | `apply(T)` | Transformation: `map(String::toUpperCase)` |
| `Consumer<T>` | T | void | `accept(T)` | Side effects: `forEach(System.out::println)` |
| `Supplier<T>` | none | T | `get()` | Factory: `orElseGet(() -> new User())` |

**Code:**
- [`level2_java8/functional_interfaces/UPIPayment.java`](src/main/java/interview/level2_java8/functional_interfaces/UPIPayment.java)
- [`level2_java8/functional_interfaces/AmazonPay.java`](src/main/java/interview/level2_java8/functional_interfaces/AmazonPay.java)
- [`level2_java8/functional_interfaces/Paytm.java`](src/main/java/interview/level2_java8/functional_interfaces/Paytm.java)

---

### Q4. What is a Lambda Expression?

**Answer:** A lambda is a concise representation of an anonymous function that can be passed around. Syntax: `(parameters) -> expression` or `(parameters) -> { statements; }`

```java
// Before Java 8
Runnable r = new Runnable() {
    @Override public void run() { System.out.println("Hello"); }
};

// Java 8 Lambda
Runnable r = () -> System.out.println("Hello");
```

Lambdas can only be used where a **functional interface** is expected.

**Code:**
- [`level2_java8/lambda/LambdaExpression_Example.java`](src/main/java/interview/level2_java8/lambda/LambdaExpression_Example.java)
- [`level2_java8/lambda/MyFunction.java`](src/main/java/interview/level2_java8/lambda/MyFunction.java)

---

### Q5. What is the Stream API? What is a Stream?

**Answer:**
- **Stream API** processes collections in a functional style using lambda expressions
- A **Stream** is a sequence of elements that supports aggregate operations
- Streams are **lazy** (intermediate operations are not executed until a terminal operation is called)
- Streams are **not data structures** -- they do not store data
- Streams **cannot be reused** -- once consumed, create a new one

**Code:**
- [`level2_java8/streams/Stream_QA.java`](src/main/java/interview/level2_java8/streams/Stream_QA.java)
- [`level2_java8/streams/Stream_Excercise.java`](src/main/java/interview/level2_java8/streams/Stream_Excercise.java)

---

### Q6. What are the key Stream operations?

**Answer:**

**Intermediate Operations** (lazy, return a Stream):
| Operation | Description | Example |
|-----------|-------------|---------|
| `filter` | Select elements matching predicate | `stream.filter(x -> x > 5)` |
| `map` | Transform each element | `stream.map(String::toUpperCase)` |
| `flatMap` | Flatten nested streams | `stream.flatMap(list -> list.stream())` |
| `sorted` | Sort elements | `stream.sorted(Comparator.reverseOrder())` |
| `distinct` | Remove duplicates | `stream.distinct()` |
| `peek` | Debug/inspect (side effect) | `stream.peek(System.out::println)` |

**Terminal Operations** (eager, produce a result):
| Operation | Description | Example |
|-----------|-------------|---------|
| `collect` | Accumulate into collection | `stream.collect(Collectors.toList())` |
| `forEach` | Perform action on each element | `stream.forEach(System.out::println)` |
| `reduce` | Combine all elements into one | `stream.reduce(0, Integer::sum)` |
| `count` | Count elements | `stream.count()` |
| `findFirst` | Get first element | `stream.findFirst()` |
| `anyMatch` | Check if any element matches | `stream.anyMatch(x -> x > 5)` |

**Code:**
- [`level2_java8/streams/Stream_QA.java`](src/main/java/interview/level2_java8/streams/Stream_QA.java)

---

### Q7. When to use map vs flatMap?

**Answer:**
- **`map`**: One-to-one transformation. Each input produces exactly one output.
  - `users.stream().map(User::getName)` -- each User maps to one String
- **`flatMap`**: One-to-many transformation. Each input produces zero or more outputs. Flattens nested structures.
  - `users.stream().flatMap(u -> u.getEmails().stream())` -- each User has multiple emails

Rule of thumb: If you get `Stream<Stream<T>>` with `map`, you need `flatMap` instead.

---

### Q8. What is the difference between Stream and Parallel Stream?

**Answer:**
- **Stream:** Sequential, single-threaded, processes elements one by one
- **Parallel Stream:** Uses `ForkJoinPool.commonPool()`, splits work across multiple cores

When to use Parallel Stream:
- Large datasets (10K+ elements)
- Stateless, non-interfering operations
- CPU-intensive operations (not I/O)

When NOT to use:
- Small datasets (overhead exceeds benefit)
- Operations with shared mutable state
- Order-dependent operations
- I/O-bound operations (use async instead)

---

### Q9. What is the groupingBy collector?

**Answer:** `Collectors.groupingBy()` groups stream elements by a classifier function, returning a `Map`:

```java
// Group employees by department
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Count employees per department
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
```

**Code:**
- [`level2_java8/streams/Stream_Excercise.java`](src/main/java/interview/level2_java8/streams/Stream_Excercise.java)

---

### Q10. What is reduce() in Streams?

**Answer:** `reduce()` combines all elements into a single result using an associative accumulation function:

```java
// Sum of all numbers
int sum = numbers.stream().reduce(0, Integer::sum);

// Longest string
Optional<String> longest = strings.stream()
    .reduce((s1, s2) -> s1.length() >= s2.length() ? s1 : s2);
```

Three forms: `reduce(identity, accumulator)`, `reduce(accumulator)`, `reduce(identity, accumulator, combiner)`.

---

### Q11. What is Optional and why was it introduced?

**Answer:** `Optional<T>` is a container that may or may not contain a non-null value. Introduced to eliminate `NullPointerException` and make APIs self-documenting.

**Best practices:**
- Use as return type, never as method parameter or field
- Use `orElse`, `orElseGet`, `map`, `flatMap` instead of `get()`/`isPresent()` checks
- Never use `Optional.of(null)` -- use `Optional.ofNullable()`

**Anti-patterns:**
```java
// BAD -- Optional used as if-else
if (opt.isPresent()) return opt.get();

// GOOD -- Functional style
return opt.orElse(defaultValue);
return opt.map(User::getName).orElse("Unknown");
```

---

### Q12. What are Method References?

**Answer:** A shorthand for lambdas that call a single existing method:

| Type | Syntax | Lambda Equivalent |
|------|--------|-------------------|
| Static method | `Math::max` | `(a, b) -> Math.max(a, b)` |
| Instance method (bound) | `str::toUpperCase` | `() -> str.toUpperCase()` |
| Instance method (unbound) | `String::toUpperCase` | `s -> s.toUpperCase()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

**Code:**
- [`level2_java8/method_references/MethodReference.java`](src/main/java/interview/level2_java8/method_references/MethodReference.java)

---

### Q13. What is the difference between findFirst() and findAny()?

**Answer:**
- **`findFirst()`**: Returns the **first** element in encounter order. Deterministic.
- **`findAny()`**: Returns **any** element. Non-deterministic in parallel streams (faster).

Use `findAny()` with parallel streams when you do not care about which element is returned.

---

### Q14. What is the difference between Collection.stream() and Stream.of()?

**Answer:**
- `collection.stream()` -- Creates a stream from an existing collection
- `Stream.of(1, 2, 3)` -- Creates a stream from individual elements or an array
- `Arrays.stream(array)` -- Creates a stream from an array

---

### Q15. Can streams be reused?

**Answer:** No. A stream can only be consumed **once**. After a terminal operation, the stream is closed. Attempting to reuse throws `IllegalStateException`.

```java
Stream<String> stream = names.stream();
stream.forEach(System.out::println);  // OK
stream.count();  // IllegalStateException: stream has already been operated upon
```

---

### Q16. What is the peek() operation used for?

**Answer:** `peek()` is an intermediate operation that performs an action on each element without consuming the stream. Intended for **debugging**:

```java
list.stream()
    .filter(x -> x > 5)
    .peek(x -> System.out.println("Filtered: " + x))
    .map(x -> x * 2)
    .collect(Collectors.toList());
```

Never use `peek()` for business logic -- side effects make code harder to reason about.

---

### Q17. How does the Java 8 DateTime API differ from java.util.Date?

**Answer:**
- **Immutable and thread-safe** (unlike Date/Calendar which are mutable)
- Clear separation: `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`
- No ambiguous month indexing (January = 1, not 0)
- Fluent API: `date.plusDays(5).minusMonths(1)`
- Built-in formatting: `DateTimeFormatter`

---

### Q18. Explain Collectors.toUnmodifiableList() vs Collectors.toList()

**Answer:**
- `Collectors.toList()` -- Returns a **mutable** ArrayList
- `Collectors.toUnmodifiableList()` -- Returns an **unmodifiable** list (Java 10+)
- `Stream.toList()` -- Returns an **unmodifiable** list (Java 16+, preferred)

---

### Q19. What is the difference between Predicate.and() vs Predicate.or()?

**Answer:** Predicate composition for complex filtering:
```java
Predicate<Employee> senior = e -> e.getAge() > 30;
Predicate<Employee> highPaid = e -> e.getSalary() > 100000;

// AND -- both conditions must be true
employees.stream().filter(senior.and(highPaid));

// OR -- at least one condition must be true
employees.stream().filter(senior.or(highPaid));

// NEGATE -- reverse the condition
employees.stream().filter(senior.negate());
```

---

### Q20. What is mapToObj() and when do you use it?

**Answer:** Converts a primitive stream (`IntStream`, `LongStream`, `DoubleStream`) to a `Stream<Object>`:

```java
String str = "Hello";
Stream<Character> charStream = str.chars()        // IntStream
    .mapToObj(ch -> (char) ch);                    // Stream<Character>
```

Used when you need to convert primitive stream elements to objects for further operations.

---

## Level 3: Multithreading & Concurrency (Senior) -- 25 Questions

> Concurrency is the dividing line between mid-level and senior. If you can explain these topics clearly AND write the code, you are ready for senior roles.

---

### Q1. What are the ways to create a thread in Java?

**Answer:**
1. **Extend `Thread` class:** Override `run()` method
2. **Implement `Runnable` interface:** Pass to `Thread` constructor (preferred -- separates task from thread)
3. **Implement `Callable<V>` interface:** Returns a result, can throw checked exceptions. Used with `ExecutorService`
4. **Use `ExecutorService`:** Pool-based thread management (production-grade approach)

**Code:**
- [`level3_multithreading/basics/Thread_Creation.java`](src/main/java/interview/level3_multithreading/basics/Thread_Creation.java)
- [`level3_multithreading/basics/Thread_Example.java`](src/main/java/interview/level3_multithreading/basics/Thread_Example.java)
- [`level3_multithreading/basics/Task.java`](src/main/java/interview/level3_multithreading/basics/Task.java)

---

### Q2. What is the difference between Runnable and Callable?

**Answer:**

| Feature | Runnable | Callable |
|---------|----------|----------|
| Return value | `void` | `V` (generic return type) |
| Exceptions | Cannot throw checked exceptions | Can throw checked exceptions |
| Method | `run()` | `call()` |
| Used with | `Thread`, `ExecutorService` | `ExecutorService` only |
| Result handling | None | Returns `Future<V>` |

---

### Q3. What is the volatile keyword?

**Answer:** `volatile` ensures **visibility** of changes across threads. When a variable is volatile:
- Every read comes from **main memory** (not CPU cache)
- Every write goes to **main memory** immediately
- Prevents instruction reordering around the variable

`volatile` does NOT provide **atomicity** -- `count++` on a volatile int is still not thread-safe (use `AtomicInteger`).

**Code:**
- [`level3_multithreading/basics/VolatileVisibility.java`](src/main/java/interview/level3_multithreading/basics/VolatileVisibility.java)

---

### Q4. What is ThreadLocal?

**Answer:** `ThreadLocal<T>` provides **thread-confined** variables -- each thread has its own independent copy. No synchronization needed.

Common use cases:
- User context in web applications (userId, tenantId)
- Database connections per thread
- SimpleDateFormat (not thread-safe, so one per thread)

Danger: **Memory leaks** in thread pools -- always call `threadLocal.remove()` after use.

**Code:**
- [`level3_multithreading/basics/UserContextHolder.java`](src/main/java/interview/level3_multithreading/basics/UserContextHolder.java)

---

### Q5. What is the difference between Concurrency and Parallelism?

**Answer:**
- **Concurrency:** Multiple tasks making progress (may be interleaved on a single core). Think: one cook switching between multiple dishes.
- **Parallelism:** Multiple tasks running simultaneously on multiple cores. Think: multiple cooks each making a dish.

**Code:**
- [`level3_multithreading/basics/ConcurrencyExample.java`](src/main/java/interview/level3_multithreading/basics/ConcurrencyExample.java)
- [`level3_multithreading/basics/ParallelismExample.java`](src/main/java/interview/level3_multithreading/basics/ParallelismExample.java)

---

### Q6. What is synchronized vs Lock (ReentrantLock)?

**Answer:**

| Feature | synchronized | ReentrantLock |
|---------|-------------|---------------|
| Lock acquisition | Implicit (block entry) | Explicit (`lock()` / `unlock()`) |
| Fairness | Not guaranteed | Configurable (`new ReentrantLock(true)`) |
| Try-lock | Not possible | `tryLock()` with timeout |
| Interruptible | No | `lockInterruptibly()` |
| Condition variables | One (`wait/notify`) | Multiple `Condition` objects |
| Must unlock in finally | Not needed | **Required** -- must call `unlock()` in finally |

**Code:**
- [`level3_multithreading/locks/ReentrantLock_API.java`](src/main/java/interview/level3_multithreading/locks/ReentrantLock_API.java)
- [`level3_multithreading/locks/ReentrantLock_ReadWrite_API.java`](src/main/java/interview/level3_multithreading/locks/ReentrantLock_ReadWrite_API.java)
- [`level3_multithreading/locks/LockCondition.java`](src/main/java/interview/level3_multithreading/locks/LockCondition.java)

---

### Q7. What is ReadWriteLock?

**Answer:** `ReadWriteLock` allows **concurrent reads** but **exclusive writes**:
- Multiple threads can hold the **read lock** simultaneously
- Only one thread can hold the **write lock**, and no reads are allowed during a write

Use when reads vastly outnumber writes (e.g., caching, configuration).

**Code:**
- [`level3_multithreading/locks/ReentrantLock_ReadWrite_API.java`](src/main/java/interview/level3_multithreading/locks/ReentrantLock_ReadWrite_API.java)

---

### Q8. What are the types of thread pools in ExecutorService?

**Answer:**

| Pool Type | Description | Use Case |
|-----------|-------------|----------|
| `newFixedThreadPool(n)` | Fixed number of threads | Known, steady workload |
| `newCachedThreadPool()` | Creates threads as needed, reuses idle | Bursty, short-lived tasks |
| `newSingleThreadExecutor()` | Single thread, tasks queued | Sequential execution guarantee |
| `newScheduledThreadPool(n)` | Fixed pool with scheduling | Periodic/delayed tasks |
| `newWorkStealingPool()` | ForkJoinPool-based (Java 8+) | CPU-intensive, recursive tasks |

**Code:**
- [`level3_multithreading/executors/TypesOfPool.java`](src/main/java/interview/level3_multithreading/executors/TypesOfPool.java)
- [`level3_multithreading/executors/ThreadPoolLifeCycle.java`](src/main/java/interview/level3_multithreading/executors/ThreadPoolLifeCycle.java)

---

### Q9. What are the rejection policies when a thread pool queue is full?

**Answer:**

| Policy | Behavior |
|--------|----------|
| `AbortPolicy` (default) | Throws `RejectedExecutionException` |
| `CallerRunsPolicy` | Submitting thread runs the task itself (backpressure) |
| `DiscardPolicy` | Silently discards the task |
| `DiscardOldestPolicy` | Discards oldest queued task, retries submission |

**Code:**
- [`level3_multithreading/executors/RejectionPolicy.java`](src/main/java/interview/level3_multithreading/executors/RejectionPolicy.java)

---

### Q10. What is CompletableFuture?

**Answer:** `CompletableFuture` enables **asynchronous, non-blocking** programming with a fluent API for chaining, combining, and error handling:

```java
CompletableFuture.supplyAsync(() -> fetchUser(id))    // async supplier
    .thenApply(user -> user.getName())                 // transform
    .thenCompose(name -> fetchOrders(name))             // chain another async
    .thenAccept(orders -> display(orders))              // consume result
    .exceptionally(ex -> handleError(ex));              // error handling
```

Key methods: `supplyAsync`, `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `anyOf`, `exceptionally`, `handle`

**Code:**
- [`level3_multithreading/completable_future/Completable.java`](src/main/java/interview/level3_multithreading/completable_future/Completable.java)

---

### Q11. thenApply vs thenCompose vs thenCombine?

**Answer:**
- **`thenApply(Function)`**: Synchronous transformation. Like `map` for futures.
- **`thenCompose(Function)`**: Asynchronous chaining. Like `flatMap` for futures. Returns `CompletableFuture<T>` not `CompletableFuture<CompletableFuture<T>>`.
- **`thenCombine(Future, BiFunction)`**: Combines results of two independent futures.

---

### Q12. What is CountDownLatch vs CyclicBarrier?

**Answer:**

| Feature | CountDownLatch | CyclicBarrier |
|---------|---------------|---------------|
| Reusable | No (one-time use) | Yes (resets after all threads arrive) |
| Count direction | Counts **down** to zero | Counts **up** to parties |
| Threads waiting | One thread waits for N events | N threads wait for each other |
| Use case | Main thread waits for workers to finish | Workers synchronize at a common point |

**Code:**
- [`level3_multithreading/synchronizers/CountDownLatchDemo.java`](src/main/java/interview/level3_multithreading/synchronizers/CountDownLatchDemo.java)
- [`level3_multithreading/synchronizers/CyclicBarrierDemo.java`](src/main/java/interview/level3_multithreading/synchronizers/CyclicBarrierDemo.java)

---

### Q13. What is Semaphore?

**Answer:** Controls access to a shared resource by maintaining a set of **permits**:
- `acquire()` -- Takes a permit (blocks if none available)
- `release()` -- Returns a permit

Use case: Rate limiting (e.g., max 10 concurrent DB connections).

**Code:**
- [`level3_multithreading/synchronizers/SemaphoreDemo.java`](src/main/java/interview/level3_multithreading/synchronizers/SemaphoreDemo.java)

---

### Q14. What is Phaser?

**Answer:** A flexible synchronizer that supports **dynamic registration** and **multiple phases**. Combines features of CountDownLatch and CyclicBarrier:
- Threads can register/deregister dynamically
- Reusable across multiple phases
- Each phase has a number, and threads advance together

Use case: Multi-phase computations where the number of participants changes.

**Code:**
- [`level3_multithreading/synchronizers/PhaserDemo.java`](src/main/java/interview/level3_multithreading/synchronizers/PhaserDemo.java)

---

### Q15. What is ForkJoinPool and work-stealing?

**Answer:**
- **ForkJoinPool:** Thread pool designed for **divide-and-conquer** algorithms. Uses `RecursiveTask<V>` (returns result) or `RecursiveAction` (no result).
- **Work-stealing:** Idle threads "steal" tasks from busy threads' deques, maximizing CPU utilization.
- Used internally by parallel streams and `CompletableFuture.supplyAsync()`.

**Code:**
- [`level3_multithreading/fork_join/ForkJoinPoolDemo.java`](src/main/java/interview/level3_multithreading/fork_join/ForkJoinPoolDemo.java)

---

### Q16. Explain the Producer-Consumer pattern

**Answer:** A classic concurrency pattern where:
- **Producer** generates data and puts it in a shared buffer
- **Consumer** takes data from the buffer and processes it
- **Buffer** (BlockingQueue) handles synchronization

Implementations: `ArrayBlockingQueue`, `LinkedBlockingQueue`, `wait/notify`

**Code:**
- [`level3_multithreading/patterns/ProducerConsumerPattern.java`](src/main/java/interview/level3_multithreading/patterns/ProducerConsumerPattern.java)

---

### Q17. What is a Deadlock? How to prevent it?

**Answer:** Deadlock occurs when two or more threads are waiting for each other's locks, creating a circular wait.

**Four conditions for deadlock (all must be true):**
1. Mutual exclusion
2. Hold and wait
3. No preemption
4. Circular wait

**Prevention strategies:**
1. **Lock ordering** -- Always acquire locks in the same order
2. **Timeout** -- Use `tryLock(timeout)` instead of `lock()`
3. **Avoid nested locks** -- Minimize lock scope
4. **Use higher-level constructs** -- `java.util.concurrent` classes

**Code:**
- [`level3_multithreading/patterns/DeadlockDemo.java`](src/main/java/interview/level3_multithreading/patterns/DeadlockDemo.java)

---

### Q18. What is the difference between wait/notify and Lock/Condition?

**Answer:**
- `wait()`/`notify()` -- Must be called inside `synchronized` block. Only one wait-set per object.
- `Lock`/`Condition` -- More flexible. Multiple conditions per lock. Support `await(timeout)`, `signalAll()`.

```java
// Old way
synchronized(lock) { lock.wait(); lock.notify(); }

// New way
Condition condition = reentrantLock.newCondition();
reentrantLock.lock();
try { condition.await(); condition.signal(); }
finally { reentrantLock.unlock(); }
```

---

### Q19. What is the thread lifecycle?

**Answer:**
```
NEW  -->  RUNNABLE  -->  RUNNING  -->  TERMINATED
              |             |
              |             +--> BLOCKED (waiting for lock)
              |             +--> WAITING (wait/join/park)
              |             +--> TIMED_WAITING (sleep/wait(timeout))
              |             |
              +<------------+
```

States: `NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`

---

### Q20. What is the difference between sleep() and wait()?

**Answer:**

| Feature | `Thread.sleep()` | `Object.wait()` |
|---------|-----------------|-----------------|
| Lock release | Does NOT release lock | Releases the lock |
| Called on | Thread class (static) | Object instance |
| Context | Anywhere | Inside `synchronized` block only |
| Wake up by | Timeout or `interrupt()` | `notify()`, `notifyAll()`, or timeout |

---

### Q21. What is an AtomicInteger? When to use it?

**Answer:** `AtomicInteger` provides **lock-free, thread-safe** operations on an int using CAS (Compare-And-Swap):
- `incrementAndGet()`, `decrementAndGet()`
- `compareAndSet(expected, update)`
- `getAndAdd(delta)`

Use instead of `synchronized` for simple counters -- much faster under low-to-moderate contention.

---

### Q22. What is the difference between submit() and execute()?

**Answer:**
- `execute(Runnable)` -- Fires and forgets. No return value. Unchecked exceptions propagate to UncaughtExceptionHandler.
- `submit(Callable/Runnable)` -- Returns a `Future`. Exceptions are captured in the Future -- retrieve via `future.get()`.

---

### Q23. How to properly shut down an ExecutorService?

**Answer:**
```java
executor.shutdown();                    // Stop accepting new tasks
executor.awaitTermination(60, SECONDS); // Wait for running tasks
if (!executor.isTerminated()) {
    executor.shutdownNow();             // Force stop -- interrupts running tasks
}
```

**Code:**
- [`level3_multithreading/executors/ThreadPoolLifeCycle.java`](src/main/java/interview/level3_multithreading/executors/ThreadPoolLifeCycle.java)

---

### Q24. What is the happens-before relationship?

**Answer:** The Java Memory Model defines ordering guarantees:
1. **Program order** -- Actions in a thread happen-before subsequent actions in the same thread
2. **Monitor lock** -- Unlock happens-before subsequent lock of the same monitor
3. **Volatile** -- Write to volatile happens-before subsequent read of the same volatile
4. **Thread start** -- `thread.start()` happens-before any action in the started thread
5. **Thread join** -- All actions in thread happen-before `join()` returns

---

### Q25. What is the difference between Callable and Future?

**Answer:**
- **`Callable<V>`** -- Represents a task that returns a result (like Runnable but with return value)
- **`Future<V>`** -- Represents the **result** of an async computation. Provides `get()` (blocking), `isDone()`, `cancel()`

```java
Callable<String> task = () -> "result";
Future<String> future = executor.submit(task);
String result = future.get(); // blocks until done
```

---

## Level 4: Java 9-17 (Senior/Staff) -- 15 Questions

> Modern Java features that show you keep current. Companies like Amazon and Google actively look for candidates who know Java 11+ features.

---

### Q1. What is var (local variable type inference)?

**Answer:** Introduced in Java 10. `var` lets the compiler infer the type from the initializer. It is **NOT dynamic typing** -- Java is still statically typed. The type is determined at **compile time**.

```java
var list = new ArrayList<String>();  // Inferred as ArrayList<String>
var map = Map.of("key", "value");    // Inferred as Map<String, String>
```

**Restrictions:** Cannot use with method parameters, return types, or fields. Cannot use without initializer.

**Code:**
- [`level4_java9to17/var_type/VarLocalTypeInference.java`](src/main/java/interview/level4_java9to17/var_type/VarLocalTypeInference.java)

---

### Q2. What are Records?

**Answer:** Records (Java 14+) are **immutable data carriers** that auto-generate `equals()`, `hashCode()`, `toString()`, accessors, and constructor:

```java
public record Point(int x, int y) { }
// That is it -- no boilerplate
```

- Fields are `private final` (immutable)
- Supports compact constructors for validation
- Cannot extend other classes (implicitly extend `Record`)
- Can implement interfaces

**Code:**
- [`level4_java9to17/records/RecordsDeepDive.java`](src/main/java/interview/level4_java9to17/records/RecordsDeepDive.java)

---

### Q3. Records vs POJOs vs Lombok?

**Answer:**

| Feature | POJO | Lombok | Record |
|---------|------|--------|--------|
| Boilerplate | High (manual) | Low (@Data) | None |
| Mutability | Mutable | Configurable | Immutable |
| Inheritance | Yes | Yes | No (final) |
| Customization | Full | Full | Limited |
| Standard Java | Yes | Third-party | Yes (14+) |
| Best for | Mutable entities | Reducing boilerplate | DTOs, value objects |

---

### Q4. What are Sealed Classes?

**Answer:** Sealed classes (Java 17) restrict which classes can extend them, creating **closed hierarchies**:

```java
public sealed class Shape permits Circle, Rectangle, Triangle { }
public final class Circle extends Shape { }
public non-sealed class Rectangle extends Shape { } // open for further extension
public sealed class Triangle extends Shape permits Equilateral { }
```

Benefits:
- Compiler knows all subtypes -- enables exhaustive `switch` without `default`
- Models algebraic data types
- Better domain modeling

**Code:**
- [`level4_java9to17/sealed_classes/SealedClassesDemo.java`](src/main/java/interview/level4_java9to17/sealed_classes/SealedClassesDemo.java)

---

### Q5. What is Pattern Matching for instanceof?

**Answer:** Eliminates the cast-after-instanceof boilerplate (Java 16):

```java
// Before
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}

// After -- pattern variable 's' is automatically cast and scoped
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

The pattern variable is scoped to the flow where the pattern matched.

**Code:**
- [`level4_java9to17/pattern_matching/PatternMatchingInstanceof.java`](src/main/java/interview/level4_java9to17/pattern_matching/PatternMatchingInstanceof.java)

---

### Q6. What are Switch Expressions?

**Answer:** Java 14 enhanced switch with:
- **Arrow syntax** (`->`) -- no fall-through, no break needed
- **`yield`** -- returns a value from a switch block
- **Exhaustiveness check** -- compiler ensures all cases are covered for enums/sealed classes

```java
String result = switch (day) {
    case MONDAY, FRIDAY    -> "Work hard";
    case TUESDAY           -> "Meeting day";
    case SATURDAY, SUNDAY  -> "Weekend";
    default -> {
        yield "Regular day";
    }
};
```

**Code:**
- [`level4_java9to17/switch_expressions/SwitchExpressions.java`](src/main/java/interview/level4_java9to17/switch_expressions/SwitchExpressions.java)

---

### Q7. What are Text Blocks?

**Answer:** Multi-line string literals (Java 15) using `"""` delimiter:

```java
String json = """
        {
            "name": "Karan",
            "role": "SDE"
        }
        """;
```

- Leading whitespace is automatically stripped (based on closing `"""` position)
- Supports escape sequences: `\s` (preserved space), `\` (line continuation)

**Code:**
- [`level4_java9to17/text_blocks/TextBlocks.java`](src/main/java/interview/level4_java9to17/text_blocks/TextBlocks.java)

---

### Q8. What are the Stream enhancements from Java 9 to 17?

**Answer:**

| Feature | Java Version | Description |
|---------|-------------|-------------|
| `takeWhile(predicate)` | 9 | Take elements while predicate is true, then stop |
| `dropWhile(predicate)` | 9 | Skip elements while predicate is true, then take rest |
| `Stream.ofNullable(x)` | 9 | Empty stream if null, singleton stream otherwise |
| `Collectors.teeing()` | 12 | Apply two collectors and merge results |
| `mapMulti()` | 16 | Imperative alternative to flatMap |
| `stream.toList()` | 16 | Shorter than `.collect(Collectors.toList())` (unmodifiable) |

**Code:**
- [`level4_java9to17/stream_enhancements/StreamJava9To17.java`](src/main/java/interview/level4_java9to17/stream_enhancements/StreamJava9To17.java)

---

### Q9. What are the Optional enhancements from Java 9 to 11?

**Answer:**

| Method | Java Version | Description |
|--------|-------------|-------------|
| `ifPresentOrElse(action, emptyAction)` | 9 | Execute action if present, otherwise execute empty action |
| `or(() -> Optional.of(default))` | 9 | Return alternative Optional if empty |
| `stream()` | 9 | Convert Optional to a Stream (0 or 1 elements) |
| `isEmpty()` | 11 | Opposite of `isPresent()` |

**Code:**
- [`level4_java9to17/optional_enhancements/OptionalEnhancements.java`](src/main/java/interview/level4_java9to17/optional_enhancements/OptionalEnhancements.java)

---

### Q10. What is the Java 11 HttpClient?

**Answer:** Replaced the old `HttpURLConnection` with a modern, fluent API supporting HTTP/1.1 and HTTP/2:

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .GET()
    .build();

// Synchronous
HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

// Asynchronous
CompletableFuture<HttpResponse<String>> future =
    client.sendAsync(request, BodyHandlers.ofString());
```

**Code:**
- [`level4_java9to17/http_client/HttpClientDemo.java`](src/main/java/interview/level4_java9to17/http_client/HttpClientDemo.java)

---

### Q11. What is JPMS (Java Platform Module System)?

**Answer:** Introduced in Java 9 (Project Jigsaw). Modules encapsulate packages and declare explicit dependencies:

```java
module com.myapp {
    requires java.sql;
    exports com.myapp.api;
}
```

Benefits: Strong encapsulation, reliable configuration, smaller runtime images (`jlink`).

**Code:**
- [`level4_java9to17/modules/ModulesOverview.java`](src/main/java/interview/level4_java9to17/modules/ModulesOverview.java)

---

### Q12. What are the new factory methods for collections (Java 9)?

**Answer:**
```java
List<String> list = List.of("a", "b", "c");           // Immutable List
Set<String> set = Set.of("a", "b", "c");               // Immutable Set
Map<String, Integer> map = Map.of("a", 1, "b", 2);    // Immutable Map
Map<String, Integer> map2 = Map.ofEntries(
    Map.entry("a", 1),
    Map.entry("b", 2)
);
```

These are unmodifiable. Attempting `add()` or `put()` throws `UnsupportedOperationException`.

---

### Q13. What is the new switch expression exhaustiveness check?

**Answer:** When using switch as an expression (returning a value), the compiler enforces that all possible values are covered. For enums and sealed classes, this means either covering every case or adding a `default`. This is compile-time safety that prevents bugs.

---

### Q14. What are private methods in interfaces (Java 9)?

**Answer:** Interfaces can now have `private` methods for code reuse within default methods:

```java
public interface Logger {
    default void logInfo(String msg)  { log("INFO", msg); }
    default void logError(String msg) { log("ERROR", msg); }
    private void log(String level, String msg) {
        System.out.println(level + ": " + msg);
    }
}
```

---

### Q15. What is the Collectors.teeing() collector?

**Answer:** Applies two collectors simultaneously and merges their results (Java 12):

```java
// Calculate average -- collect both sum and count, then merge
double average = Stream.of(1, 2, 3, 4, 5)
    .collect(Collectors.teeing(
        Collectors.summingDouble(i -> i),
        Collectors.counting(),
        (sum, count) -> sum / count
    ));
```

---

## Level 5: Java 17-21 (Staff) -- 15 Questions

> These features represent the future of Java. Virtual threads alone are a game-changer for server-side development. Staff-level candidates should know these.

---

### Q1. What are Virtual Threads (Project Loom)?

**Answer:** Lightweight threads managed by the JVM (not the OS). You can create **millions** of virtual threads:

```java
// Create 100,000 virtual threads
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return "done";
        });
    }
}
```

- **Platform threads:** 1:1 mapping to OS threads, ~2MB stack, limited to thousands
- **Virtual threads:** M:N mapping (many virtual to few carrier threads), ~few KB stack, millions possible

Best for: I/O-bound workloads (web servers, microservices)
NOT for: CPU-bound workloads (use parallel streams or ForkJoinPool)

**Code:**
- [`level5_java17to21/virtual_threads/VirtualThreadsDemo.java`](src/main/java/interview/level5_java17to21/virtual_threads/VirtualThreadsDemo.java)
- [`level5_java17to21/virtual_threads/VirtualVsPlatformComparison.java`](src/main/java/interview/level5_java17to21/virtual_threads/VirtualVsPlatformComparison.java)

---

### Q2. Virtual threads vs Platform threads -- when to use which?

**Answer:**

| Factor | Virtual Threads | Platform Threads |
|--------|----------------|-----------------|
| Workload type | I/O-bound | CPU-bound |
| Count | Millions | Hundreds |
| Stack size | ~few KB | ~1-2 MB |
| Scheduling | JVM (cooperative) | OS (preemptive) |
| Pooling needed? | No -- create per task | Yes -- pool and reuse |
| Synchronized blocks | Avoid (causes pinning) | Fine to use |
| ThreadLocal | Use ScopedValue instead | OK but be cautious |

**Pinning:** Virtual threads get "pinned" to carrier threads inside `synchronized` blocks. Use `ReentrantLock` instead.

---

### Q3. What is Structured Concurrency?

**Answer:** Treats concurrent tasks as a **single unit of work** with clear lifecycle management (preview in Java 21):

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<String> user  = scope.fork(() -> fetchUser());
    Subtask<String> order = scope.fork(() -> fetchOrder());

    scope.join();           // Wait for both
    scope.throwIfFailed();  // Propagate first failure

    return new Response(user.get(), order.get());
}
// If fetchUser() fails, fetchOrder() is automatically cancelled
```

Benefits: No thread leaks, automatic cancellation, clear parent-child relationship.

**Code:**
- [`level5_java17to21/structured_concurrency/StructuredConcurrencyDemo.java`](src/main/java/interview/level5_java17to21/structured_concurrency/StructuredConcurrencyDemo.java)

---

### Q4. What is ScopedValue? How does it differ from ThreadLocal?

**Answer:**

| Feature | ThreadLocal | ScopedValue |
|---------|------------|-------------|
| Mutability | Mutable (set/get anytime) | Immutable within scope |
| Lifecycle | Explicit `remove()` required | Automatic (scope-based) |
| Memory leaks | Common in thread pools | Not possible |
| Virtual threads | Problematic | Designed for |
| Inheritance | InheritableThreadLocal | Automatically inherited by child scopes |

```java
private static final ScopedValue<String> USER = ScopedValue.newInstance();

ScopedValue.runWhere(USER, "karan", () -> {
    System.out.println(USER.get()); // "karan"
    // Automatically cleaned up when scope exits
});
```

**Code:**
- [`level5_java17to21/scoped_values/ScopedValuesDemo.java`](src/main/java/interview/level5_java17to21/scoped_values/ScopedValuesDemo.java)

---

### Q5. What is Pattern Matching for switch?

**Answer:** Extends switch to match on **types and conditions** (Java 21):

```java
String describe(Object obj) {
    return switch (obj) {
        case Integer i when i > 0 -> "Positive integer: " + i;
        case Integer i            -> "Non-positive integer: " + i;
        case String s             -> "String of length " + s.length();
        case null                 -> "null value";
        default                   -> "Something else";
    };
}
```

Features:
- **Guarded patterns:** `case Type t when condition`
- **Null handling:** `case null` instead of NPE
- **Exhaustiveness:** Compiler ensures all sealed subtypes are covered
- **Dominance checking:** More specific patterns must come before general ones

**Code:**
- [`level5_java17to21/pattern_matching_switch/PatternMatchingSwitchDemo.java`](src/main/java/interview/level5_java17to21/pattern_matching_switch/PatternMatchingSwitchDemo.java)

---

### Q6. What are Record Patterns?

**Answer:** Destructure records directly in patterns (Java 21):

```java
record Point(int x, int y) { }
record Line(Point start, Point end) { }

// Destructure in instanceof
if (obj instanceof Point(int x, int y)) {
    System.out.println("x=" + x + ", y=" + y);
}

// Nested destructuring in switch
switch (shape) {
    case Line(Point(var x1, var y1), Point(var x2, var y2)) ->
        System.out.println("Line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
}
```

**Code:**
- [`level5_java17to21/record_patterns/RecordPatternsDemo.java`](src/main/java/interview/level5_java17to21/record_patterns/RecordPatternsDemo.java)

---

### Q7. What are Sequenced Collections?

**Answer:** Java 21 introduced new interfaces for collections with a defined encounter order:

```
SequencedCollection (addFirst, addLast, getFirst, getLast, reversed)
|-- SequencedSet
|   |-- SequencedSortedSet
|-- SequencedDeque
SequencedMap (firstEntry, lastEntry, pollFirstEntry, putFirst, reversed)
```

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
list.getFirst();    // "a"
list.getLast();     // "c"
list.reversed();   // Reversed view: ["c", "b", "a"]
```

**Code:**
- [`level5_java17to21/sequenced_collections/SequencedCollectionsDemo.java`](src/main/java/interview/level5_java17to21/sequenced_collections/SequencedCollectionsDemo.java)

---

### Q8. What are String Templates?

**Answer:** String templates (preview, explored in Java 21) allow embedded expressions in strings:

```java
String name = "Karan";
int age = 30;
String message = STR."Hello \{name}, you are \{age} years old";
```

Note: String Templates were removed/reworked after Java 21 preview. The concept is evolving. Current alternative: `String.format()` or `MessageFormat`.

**Code:**
- [`level5_java17to21/string_templates/StringTemplatesDemo.java`](src/main/java/interview/level5_java17to21/string_templates/StringTemplatesDemo.java)

---

### Q9. How do virtual threads change application architecture?

**Answer:**
- **Before:** Thread-per-request limited by OS thread count (~200-500). Led to reactive/async frameworks (WebFlux, Vert.x).
- **After:** Virtual thread-per-request with millions of threads. Simple blocking code performs like async code.

Architectural shift:
1. Remove thread pools for I/O tasks -- create a new virtual thread per request
2. Replace `synchronized` with `ReentrantLock` (avoid pinning)
3. Replace `ThreadLocal` with `ScopedValue`
4. Keep thread pools for CPU-bound tasks only

---

### Q10. Can you use synchronized with virtual threads?

**Answer:** You can, but **avoid it**. `synchronized` blocks cause **pinning** -- the virtual thread monopolizes its carrier thread, negating the benefit. Use `ReentrantLock` instead:

```java
// BAD -- pins virtual thread
synchronized(lock) { doIO(); }

// GOOD -- virtual thread can unmount during lock wait
reentrantLock.lock();
try { doIO(); }
finally { reentrantLock.unlock(); }
```

---

### Q11. What is the difference between ShutdownOnFailure and ShutdownOnSuccess?

**Answer:**
- **`ShutdownOnFailure`**: Cancels all subtasks if **any** fails. Use when you need **all** results.
- **`ShutdownOnSuccess`**: Cancels remaining subtasks when **first** succeeds. Use when you need **any one** result (e.g., fastest response from multiple services).

---

### Q12. What are Unnamed Patterns and Variables (Java 21)?

**Answer:** Use `_` for unused variables to improve readability:

```java
// Unnamed variable in catch
try { ... } catch (Exception _) { log("error occurred"); }

// Unnamed pattern in switch
case Point(int x, _) -> "x is " + x;

// Unnamed in enhanced for
for (var _ : collection) { count++; }
```

---

### Q13. What are the new methods added to Map in Java 9+?

**Answer:**
```java
map.getOrDefault(key, defaultValue);
map.putIfAbsent(key, value);
map.computeIfAbsent(key, k -> new ArrayList<>());
map.computeIfPresent(key, (k, v) -> v + 1);
map.merge(key, value, (old, new_) -> old + new_);
map.forEach((k, v) -> System.out.println(k + "=" + v));
map.replaceAll((k, v) -> v.toUpperCase());
```

---

### Q14. How does Java 21 improve null handling in switch?

**Answer:** Before Java 21, passing `null` to a switch threw `NullPointerException`. Now:

```java
switch (obj) {
    case null -> System.out.println("Null!");
    case String s -> System.out.println("String: " + s);
    default -> System.out.println("Other: " + obj);
}
```

---

### Q15. What is the benefit of combining sealed classes with pattern matching switch?

**Answer:** The compiler guarantees **exhaustiveness** -- you handle every possible subtype without a `default`:

```java
sealed interface Shape permits Circle, Rectangle, Triangle {}

double area(Shape shape) {
    return switch (shape) {
        case Circle c    -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
        case Triangle t  -> 0.5 * t.base() * t.height();
        // No default needed -- compiler knows all subtypes
    };
}
```

If a new subtype is added, the compiler forces you to handle it.

---

## Level 6: JVM Internals (Staff/Principal) -- 15 Questions

> If you can explain JVM memory, GC algorithms, and class loading, you demonstrate depth that separates staff engineers from senior engineers.

---

### Q1. What are the JVM memory areas?

**Answer:**
```
+------------------------------------------+
|              JVM Memory                  |
|                                          |
|  +--------+  +--------+  +-----------+  |
|  |  Heap  |  | Stack  |  | Metaspace |  |
|  | (shared)|  |(per    |  | (class    |  |
|  |        |  | thread)|  |  metadata) |  |
|  +--------+  +--------+  +-----------+  |
|                                          |
|  +-----+  +-----------+  +-----------+  |
|  | PC   |  | Native    |  | Code      |  |
|  |Register| | Method   |  | Cache     |  |
|  |      |  | Stack     |  | (JIT)     |  |
|  +-----+  +-----------+  +-----------+  |
+------------------------------------------+
```

| Area | Shared? | Contents |
|------|---------|----------|
| Heap | Yes (all threads) | Objects, arrays, instance variables |
| Stack | No (per thread) | Local variables, method call frames, partial results |
| Metaspace | Yes | Class metadata, method data (replaced PermGen in Java 8) |
| PC Register | No (per thread) | Address of current instruction |
| Native Method Stack | No (per thread) | Native (JNI) method calls |
| Code Cache | Yes | JIT-compiled native code |

**Code:**
- [`level6_jvm_internals/memory_model/JvmMemoryModelDemo.java`](src/main/java/interview/level6_jvm_internals/memory_model/JvmMemoryModelDemo.java)

---

### Q2. What is the difference between Stack and Heap?

**Answer:**

| Feature | Stack | Heap |
|---------|-------|------|
| Stores | Local variables, method frames | Objects, instance variables |
| Access | LIFO (per thread) | Global (shared) |
| Speed | Faster (contiguous memory) | Slower (fragmented) |
| Size | Small (~512KB-1MB per thread) | Large (configurable: -Xmx) |
| Errors | StackOverflowError | OutOfMemoryError |
| GC | Not garbage collected | Garbage collected |
| Thread-safe | Yes (thread-confined) | No (shared, needs sync) |

---

### Q3. What are the main GC algorithms?

**Answer:**

| GC | Java Version | Pause Goal | Best For |
|----|-------------|------------|----------|
| Serial GC | All | Long pauses OK | Small apps, single-core |
| Parallel GC | All | Throughput | Batch processing |
| G1 GC | 7+ (default 9+) | < 200ms pauses | General purpose (default) |
| ZGC | 11+ | < 10ms pauses | Large heaps, low latency |
| Shenandoah | 12+ | < 10ms pauses | Low latency (RedHat) |

**G1 GC:** Divides heap into equal-sized regions. Prioritizes collecting regions with most garbage first ("Garbage First").

**ZGC:** Concurrent, non-generational (until Java 21), uses colored pointers and load barriers. Handles terabyte-sized heaps with sub-10ms pauses.

**Code:**
- [`level6_jvm_internals/gc/GarbageCollectionDemo.java`](src/main/java/interview/level6_jvm_internals/gc/GarbageCollectionDemo.java)
- [`level6_jvm_internals/gc/GCTuningDemo.java`](src/main/java/interview/level6_jvm_internals/gc/GCTuningDemo.java)

---

### Q4. What are the key GC tuning flags?

**Answer:**
```bash
# Heap sizing
-Xms512m                    # Initial heap size
-Xmx4g                      # Maximum heap size
-XX:NewRatio=2               # Old:Young generation ratio

# GC algorithm selection
-XX:+UseG1GC                 # Use G1 (default since Java 9)
-XX:+UseZGC                  # Use ZGC
-XX:+UseShenandoahGC         # Use Shenandoah

# G1-specific
-XX:MaxGCPauseMillis=200     # Target max pause time
-XX:G1HeapRegionSize=16m     # Region size

# Logging
-Xlog:gc*                    # GC logging (Java 9+)
-verbose:gc                  # Simple GC output

# Metaspace
-XX:MetaspaceSize=256m       # Initial metaspace
-XX:MaxMetaspaceSize=512m    # Max metaspace
```

**Code:**
- [`level6_jvm_internals/gc/GCTuningDemo.java`](src/main/java/interview/level6_jvm_internals/gc/GCTuningDemo.java)

---

### Q5. How does the Class Loading mechanism work?

**Answer:** Three-phase process with delegation hierarchy:

**Class Loaders (parent-first delegation):**
1. **Bootstrap ClassLoader** -- Loads `java.base` module (rt.jar). Written in native code.
2. **Platform ClassLoader** (formerly Extension) -- Loads platform modules.
3. **Application ClassLoader** -- Loads application classpath classes.

**Loading Phases:**
1. **Loading** -- Read `.class` bytecode into memory
2. **Linking**
   - Verification -- Validate bytecode
   - Preparation -- Allocate memory for static fields
   - Resolution -- Resolve symbolic references
3. **Initialization** -- Execute static initializers and `<clinit>`

**Code:**
- [`level6_jvm_internals/classloading/ClassLoadingDemo.java`](src/main/java/interview/level6_jvm_internals/classloading/ClassLoadingDemo.java)

---

### Q6. What is JIT compilation?

**Answer:** The JVM interprets bytecode initially, then the **Just-In-Time compiler** compiles frequently-executed methods ("hot spots") to native machine code:

- **C1 (Client):** Quick compilation, basic optimizations
- **C2 (Server):** Slow compilation, aggressive optimizations
- **Tiered compilation (default):** Start with C1, promote hot methods to C2

Key optimizations:
- **Inlining** -- Replaces method call with method body
- **Escape analysis** -- If an object does not escape a method, allocates on stack (no GC needed)
- **Loop unrolling** -- Reduces loop overhead
- **Dead code elimination** -- Removes unreachable code

**Code:**
- [`level6_jvm_internals/jit/JitCompilationDemo.java`](src/main/java/interview/level6_jvm_internals/jit/JitCompilationDemo.java)

---

### Q7. What is Escape Analysis?

**Answer:** The JIT compiler analyzes whether an object "escapes" its creation scope:
- **No escape:** Object used only within the method -- allocated on **stack** (no GC)
- **Thread-local escape:** Object passed to another method but stays in the thread -- **lock elision** (remove unnecessary synchronization)
- **Global escape:** Object is visible to other threads -- allocated on **heap** (normal)

```java
// Object does NOT escape -- stack-allocated
void calculate() {
    Point p = new Point(1, 2);  // May be stack-allocated
    return p.x + p.y;
}
```

---

### Q8. What are the 7 common memory leak patterns in Java?

**Answer:**
1. **Static collections** -- Objects added to `static List/Map` and never removed
2. **Unclosed resources** -- Streams, connections, readers not closed (use try-with-resources)
3. **Listeners/callbacks** -- Registered but never unregistered
4. **Inner class references** -- Non-static inner class holds reference to outer class
5. **ThreadLocal not cleaned** -- In thread pools, values persist across requests
6. **Cache without eviction** -- Unbounded cache (use `WeakHashMap` or size-limited cache)
7. **String.intern() abuse** -- Interning large/dynamic strings fills the string pool

**Code:**
- [`level6_jvm_internals/memory_leaks/MemoryLeakPatterns.java`](src/main/java/interview/level6_jvm_internals/memory_leaks/MemoryLeakPatterns.java)

---

### Q9. What profiling tools does the JVM provide?

**Answer:**

| Tool | Purpose |
|------|---------|
| `jps` | List running JVM processes |
| `jstack <pid>` | Thread dump -- diagnose deadlocks, thread states |
| `jmap -heap <pid>` | Heap summary -- memory usage by generation |
| `jmap -dump:file=heap.hprof <pid>` | Heap dump for offline analysis |
| `jcmd <pid> VM.flags` | View JVM flags |
| `jcmd <pid> GC.run` | Trigger GC |
| **JFR** (Java Flight Recorder) | Low-overhead profiling (CPU, allocations, locks, I/O) |
| **JMC** (Java Mission Control) | GUI for analyzing JFR recordings |
| `jstat -gcutil <pid>` | GC statistics in real-time |

**Code:**
- [`level6_jvm_internals/profiling/ProfilingToolsOverview.java`](src/main/java/interview/level6_jvm_internals/profiling/ProfilingToolsOverview.java)

---

### Q10. What is Metaspace? How is it different from PermGen?

**Answer:**

| Feature | PermGen (Java 7-) | Metaspace (Java 8+) |
|---------|-------------------|---------------------|
| Location | Fixed-size heap region | Native memory (OS) |
| Default size | 64MB (fixed) | Unlimited (auto-grows) |
| Tuning | `-XX:MaxPermSize` | `-XX:MaxMetaspaceSize` |
| GC | Rarely collected | Collected when threshold reached |
| Error | `OutOfMemoryError: PermGen` | `OutOfMemoryError: Metaspace` |

Metaspace stores class metadata, method data, and constant pool. It grows automatically but can be limited with `-XX:MaxMetaspaceSize`.

---

### Q11. How do you diagnose an OutOfMemoryError?

**Answer:**
1. **Add JVM flags:** `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof`
2. **Analyze heap dump** with Eclipse MAT, VisualVM, or JProfiler
3. **Look for:** Dominator tree (which objects hold the most memory), leak suspects, histogram of object counts
4. **Check GC logs:** `-Xlog:gc*:file=gc.log` -- look for frequent Full GCs with little reclaimed memory
5. **Types of OOM:**
   - `Java heap space` -- Heap full (increase `-Xmx` or fix leak)
   - `Metaspace` -- Too many classes loaded (check classloader leaks)
   - `Unable to create native thread` -- OS thread limit reached
   - `GC overhead limit exceeded` -- >98% time in GC, <2% heap recovered

---

### Q12. What is a thread dump and how do you read it?

**Answer:** A thread dump shows the state and stack trace of every thread:

```
"http-nio-8080-exec-1" #25 daemon prio=5 RUNNABLE
    at com.myapp.Service.process(Service.java:42)
    at com.myapp.Controller.handle(Controller.java:18)

"pool-1-thread-3" #30 BLOCKED on java.util.HashMap@3f45a
    waiting to lock <0x000000076f2a8> (a java.util.HashMap)
    at com.myapp.Cache.get(Cache.java:15)
```

**Key states:** RUNNABLE, BLOCKED (waiting for lock), WAITING (wait/park), TIMED_WAITING (sleep)

**Get a thread dump:**
- `jstack <pid>`
- `kill -3 <pid>` (Unix)
- `jcmd <pid> Thread.print`

---

### Q13. What is the difference between Young and Old generation?

**Answer:**
```
Heap
|-- Young Generation (short-lived objects, Minor GC)
|   |-- Eden Space (new objects allocated here)
|   |-- Survivor Space S0
|   |-- Survivor Space S1
|-- Old Generation (long-lived objects, Major/Full GC)
```

- Objects are created in **Eden**
- Surviving objects move to **Survivor** spaces (S0/S1 alternate)
- After N GC cycles (default 15), objects are **tenured** to Old Generation
- **Minor GC:** Collects Young Gen (fast, frequent)
- **Major/Full GC:** Collects Old Gen (slow, infrequent)

---

### Q14. What is String Deduplication in G1 GC?

**Answer:** G1 can detect duplicate String values on the heap and make them point to the same `char[]` array. Enable with `-XX:+UseStringDeduplication`. Useful when many strings have the same content (e.g., "status", "ACTIVE").

---

### Q15. What is the impact of creating too many threads?

**Answer:** Each platform thread consumes:
- ~1-2 MB of stack memory
- OS scheduling overhead
- Context-switching cost

1000 threads = ~1-2 GB just for stacks. This is why:
- Thread pools limit thread count
- Virtual threads (Java 21) solve the problem by using minimal stack space
- `-Xss` flag controls stack size per thread

---

## Level 7: Java 25/26 (Principal) -- 10 Questions

> These features are preview or upcoming. Knowing them signals you follow the Java roadmap and think about the future of the platform.

---

### Q1. What are Stream Gatherers?

**Answer:** Stream Gatherers (Java 24) allow creating **custom intermediate operations** for streams -- something previously impossible without the full collector pattern:

```java
// Example: sliding window of size 3
Stream.of(1, 2, 3, 4, 5)
    .gather(Gatherers.windowSliding(3))
    .forEach(System.out::println);
// [1, 2, 3], [2, 3, 4], [3, 4, 5]
```

Built-in gatherers: `windowFixed`, `windowSliding`, `fold`, `scan`, `mapConcurrent`

Custom gatherers implement `Gatherer<T, A, R>` with `initializer`, `integrator`, `combiner`, `finisher`.

**Code:**
- [`level7_java25_26/stream_gatherers/StreamGatherersDemo.java`](src/main/java/interview/level7_java25_26/stream_gatherers/StreamGatherersDemo.java)

---

### Q2. What are Flexible Constructors (Statements Before super())?

**Answer:** Java 25 (preview) allows statements before `super()` or `this()` calls:

```java
public class Validated extends Base {
    public Validated(String input) {
        // Validation BEFORE super() -- previously illegal
        if (input == null) throw new IllegalArgumentException("null input");
        var processed = input.trim().toLowerCase();
        super(processed);
    }
}
```

Previously, `super()` had to be the first statement, forcing awkward workarounds like static helper methods.

**Code:**
- [`level7_java25_26/flexible_constructors/FlexibleConstructorsDemo.java`](src/main/java/interview/level7_java25_26/flexible_constructors/FlexibleConstructorsDemo.java)

---

### Q3. What is Project Valhalla (Value Types)?

**Answer:** Value types are **identity-free objects** -- they have no object header, no identity, and can be flattened into arrays:

```java
value class Complex {
    double real;
    double imaginary;
}
```

Benefits:
- **No identity** -- compared by value, not reference
- **No object header** -- 16 bytes savings per object
- **Flat arrays** -- `Complex[]` stores values contiguously (like C structs) instead of an array of pointers
- **No null** -- value types cannot be null (use `Complex?` for nullable)

Impact: Massive performance improvement for numerics, geometry, financial calculations.

**Code:**
- [`level7_java25_26/value_types/ValueTypesOverview.java`](src/main/java/interview/level7_java25_26/value_types/ValueTypesOverview.java)

---

### Q4. What are Primitive Generics?

**Answer:** Part of Project Valhalla. Currently `List<int>` is illegal -- you must use `List<Integer>` (autoboxing overhead). Primitive generics will allow:

```java
List<int> numbers = new ArrayList<>();     // No boxing
Map<int, double> coordinates = new HashMap<>(); // No wrapper objects
```

Impact: Eliminates billions of unnecessary Integer/Double wrapper objects in typical applications.

**Code:**
- [`level7_java25_26/primitive_generics/PrimitiveGenericsDemo.java`](src/main/java/interview/level7_java25_26/primitive_generics/PrimitiveGenericsDemo.java)

---

### Q5. What is Compact Source (Unnamed Classes)?

**Answer:** Simplifies small programs by removing class/method ceremony (Java 21+):

```java
// Before -- HelloWorld.java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}

// After -- HelloWorld.java (compact source)
void main() {
    println("Hello, World!");
}
```

- No class declaration needed
- `main()` can be an instance method without `String[] args`
- `println()` available without `System.out`

Best for: Scripts, teaching, prototyping.

**Code:**
- [`level7_java25_26/compact_source/CompactSourceDemo.java`](src/main/java/interview/level7_java25_26/compact_source/CompactSourceDemo.java)

---

### Q6. What is the difference between identity objects and value objects?

**Answer:**
- **Identity objects:** Every instance has a unique identity (`==` checks reference). Two objects with same data are still different objects. This is how Java works today.
- **Value objects:** No identity. Two instances with same data are the same object. Like `int` -- `5 == 5` regardless of where the 5 came from.

Value types cannot be: locked on (`synchronized`), used with identity-sensitive operations (`==`, `System.identityHashCode`, `wait/notify`).

---

### Q7. How will Valhalla affect performance of collections?

**Answer:**
```
Current: ArrayList<Integer>
Heap:  [ptr][ptr][ptr][ptr][ptr]  --> each points to Integer object
                                      (16-byte header + 4-byte int = 20 bytes each)
                                      Cache misses on every access

Future: ArrayList<int>
Heap:  [4][4][4][4][4]           --> values stored inline
                                    (4 bytes each, contiguous memory)
                                    Cache-friendly sequential access
```

For 1M integers: Current ~20MB with pointer chasing. Future: ~4MB contiguous. Approximately 5x memory reduction with dramatically better cache performance.

---

### Q8. What new methods does Gatherer interface provide?

**Answer:** The `Gatherer<T, A, R>` interface has four components:

| Component | Type | Purpose |
|-----------|------|---------|
| `initializer()` | `Supplier<A>` | Create initial state |
| `integrator()` | `Integrator<A, T, R>` | Process each element, emit downstream |
| `combiner()` | `BinaryOperator<A>` | Combine states (for parallel) |
| `finisher()` | `BiConsumer<A, Downstream<R>>` | Final processing after all elements |

This mirrors the Collector API but for **intermediate** operations rather than terminal ones.

---

### Q9. How do Stream Gatherers compare to Collectors?

**Answer:**

| Feature | Collector | Gatherer |
|---------|-----------|----------|
| Position in pipeline | Terminal only | Intermediate |
| Output | Single result | Stream of elements |
| Short-circuit | No | Yes (can stop early) |
| Examples | `toList()`, `groupingBy()` | `windowSliding()`, `scan()` |
| Introduced | Java 8 | Java 24 |

---

### Q10. What is the roadmap for Java beyond 25?

**Answer:** Key projects in progress:
- **Valhalla:** Value types + primitive generics (ongoing)
- **Panama:** Foreign function & memory API (stable in Java 22)
- **Amber:** Pattern matching, data-oriented programming (ongoing)
- **Loom:** Virtual threads (stable in Java 21), structured concurrency (preview)
- **Leyden:** Startup/warmup improvements (ahead-of-time compilation)

Java continues the 6-month release cadence with LTS every 2 years (Java 21 LTS, Java 25 LTS next).

---

## Recently Asked in FAANG / Big Tech (2024-2025)

> Based on interview reports from Amazon, Google, Meta, Microsoft, Netflix, Uber, and Stripe. These are the actual questions candidates reported being asked in Java backend/platform roles.

---

### Top 20 Most-Asked Questions

| # | Question | Company | Level | Code Reference |
|---|----------|---------|-------|---------------|
| 1 | How does HashMap internally work? Explain put/get, collision handling, treeification | Amazon, Google, Microsoft | L1 | `level1_core/collections/HashMapInternals.java` |
| 2 | Design a thread-safe cache. What concurrency constructs would you use? | Amazon, Netflix | L3 | `level1_core/collections/ConcurrentHashMapInternals.java` |
| 3 | Explain CompletableFuture. How would you chain multiple API calls? | Amazon, Uber, Stripe | L3 | `level3_multithreading/completable_future/Completable.java` |
| 4 | What are Virtual Threads? When would you use them vs platform threads? | Netflix, Google, Stripe | L5 | `level5_java17to21/virtual_threads/VirtualThreadsDemo.java` |
| 5 | Write a stream pipeline: group employees by dept, sort by salary, get top 3 | Amazon, Microsoft, Meta | L2 | `level2_java8/streams/Stream_Excercise.java` |
| 6 | How would you diagnose a memory leak in production? Walk through the tools | Amazon, Netflix, Google | L6 | `level6_jvm_internals/memory_leaks/MemoryLeakPatterns.java` |
| 7 | Explain G1 vs ZGC. When would you choose one over the other? | Google, Netflix, Uber | L6 | `level6_jvm_internals/gc/GarbageCollectionDemo.java` |
| 8 | Implement the Producer-Consumer pattern using BlockingQueue | Amazon, Microsoft | L3 | `level3_multithreading/patterns/ProducerConsumerPattern.java` |
| 9 | What is the equals/hashCode contract? What breaks if you violate it? | Amazon, Google, Microsoft | L1 | `level1_core/hashcode_equals/Employee.java` |
| 10 | How does ConcurrentHashMap differ from Collections.synchronizedMap()? | Amazon, Stripe | L1 | `level1_core/collections/ConcurrentHashMapInternals.java` |
| 11 | Explain Sealed Classes + Pattern Matching switch. How do they work together? | Google, Stripe | L4/L5 | `level4_java9to17/sealed_classes/SealedClassesDemo.java` |
| 12 | What is Structured Concurrency? How does it prevent thread leaks? | Netflix, Google | L5 | `level5_java17to21/structured_concurrency/StructuredConcurrencyDemo.java` |
| 13 | Write an immutable class with mutable fields. Explain defensive copying | Amazon, Microsoft | L1 | `level1_core/immutability/Employee.java` |
| 14 | How does the class loading mechanism work? Can you write a custom class loader? | Google, Amazon | L6 | `level6_jvm_internals/classloading/ClassLoadingDemo.java` |
| 15 | Explain the difference between map, flatMap, and reduce with examples | Amazon, Meta, Uber | L2 | `level2_java8/streams/Stream_QA.java` |
| 16 | What causes thread deadlock? How do you detect and prevent it? | Amazon, Microsoft, Google | L3 | `level3_multithreading/patterns/DeadlockDemo.java` |
| 17 | Records vs POJOs -- when would you use each? | Stripe, Netflix | L4 | `level4_java9to17/records/RecordsDeepDive.java` |
| 18 | ScopedValue vs ThreadLocal -- why was ScopedValue introduced? | Netflix, Google | L5 | `level5_java17to21/scoped_values/ScopedValuesDemo.java` |
| 19 | Walk through JVM memory areas. What goes where? | Amazon, Google | L6 | `level6_jvm_internals/memory_model/JvmMemoryModelDemo.java` |
| 20 | Explain type erasure. What are its limitations? How does PECS help? | Amazon, Google, Meta | L1 | `level1_core/generics/GenericsDeepDive.java` |

### Common Follow-Up Patterns

**Amazon** loves to go deep on HashMap, then pivot to "how would you make it thread-safe?" (ConcurrentHashMap, then segments, then CAS).

**Google** focuses on JVM internals and modern Java features. Expect questions on GC tuning, class loading, and pattern matching.

**Netflix** heavily tests virtual threads, structured concurrency, and production observability (JFR, metrics, GC logs).

**Stripe** asks about immutability, functional programming patterns, and type safety (sealed classes, records).

**Meta** focuses on Stream API fluency -- expect live coding with complex stream pipelines.

---

## Quick Reference Tables

### Java Version Feature Matrix

| Feature | Java 8 | Java 9 | Java 10 | Java 11 | Java 14 | Java 16 | Java 17 | Java 21 | Java 25 |
|---------|--------|--------|---------|---------|---------|---------|---------|---------|---------|
| Lambda Expressions | X | | | | | | | | |
| Stream API | X | | | | | | | | |
| Optional | X | | | | | | | | |
| Default Methods | X | | | | | | | | |
| CompletableFuture | X | | | | | | | | |
| JPMS (Modules) | | X | | | | | | | |
| `List.of()` / `Set.of()` | | X | | | | | | | |
| Private interface methods | | X | | | | | | | |
| `var` keyword | | | X | | | | | | |
| HttpClient | | | | X | | | | | |
| `String.isBlank()` / `strip()` | | | | X | | | | | |
| Switch Expressions | | | | | X | | | | |
| Records | | | | | X | | | | |
| Pattern Matching instanceof | | | | | | X | | | |
| `Stream.toList()` | | | | | | X | | | |
| Sealed Classes | | | | | | | X | | |
| Text Blocks | | | | | X | | X | | |
| Virtual Threads | | | | | | | | X | |
| Pattern Matching switch | | | | | | | | X | |
| Record Patterns | | | | | | | | X | |
| Sequenced Collections | | | | | | | | X | |
| Structured Concurrency | | | | | | | | P | P |
| ScopedValues | | | | | | | | P | P |
| Stream Gatherers | | | | | | | | | X |
| Flexible Constructors | | | | | | | | | P |

`X` = Final/Stable | `P` = Preview

### LTS (Long-Term Support) Releases

| Version | Release Date | Support Until |
|---------|-------------|--------------|
| Java 8 | Mar 2014 | Dec 2030+ |
| Java 11 | Sep 2018 | Sep 2026 |
| Java 17 | Sep 2021 | Sep 2029 |
| Java 21 | Sep 2023 | Sep 2031 |
| Java 25 | Sep 2025 | Sep 2033 |

---

### Collections Cheat Sheet

```
Do you need key-value pairs?
|
+-- YES --> Is ordering important?
|           |
|           +-- No order needed ---------> HashMap
|           +-- Insertion order ----------> LinkedHashMap
|           +-- Sorted by key ------------> TreeMap
|           +-- Thread-safe --------------> ConcurrentHashMap
|
+-- NO --> Do you need unique elements?
           |
           +-- YES --> Is ordering important?
           |           |
           |           +-- No order needed ------> HashSet
           |           +-- Insertion order -------> LinkedHashSet
           |           +-- Sorted ----------------> TreeSet
           |
           +-- NO --> Do you need a queue/deque?
                      |
                      +-- YES --> FIFO queue -----> ArrayDeque / LinkedList
                      |     +--> Priority --------> PriorityQueue
                      |     +--> Blocking --------> ArrayBlockingQueue
                      |     +--> Concurrent ------> ConcurrentLinkedQueue
                      |
                      +-- NO --> Random access? --> ArrayList
                            +--> Frequent insert --> LinkedList
                            +--> Thread-safe -----> CopyOnWriteArrayList
```

| Collection | Null Keys | Null Values | Thread-Safe | Ordered | Sorted |
|-----------|-----------|-------------|-------------|---------|--------|
| ArrayList | N/A | Yes | No | Yes (index) | No |
| LinkedList | N/A | Yes | No | Yes (index) | No |
| HashSet | N/A | One null | No | No | No |
| TreeSet | N/A | No | No | Yes | Yes |
| HashMap | One null | Yes | No | No | No |
| LinkedHashMap | One null | Yes | No | Yes (insertion) | No |
| TreeMap | No | Yes | No | Yes | Yes |
| ConcurrentHashMap | No | No | Yes | No | No |
| CopyOnWriteArrayList | N/A | Yes | Yes | Yes | No |

---

### Concurrency Cheat Sheet

```
What kind of concurrent work?
|
+-- Simple thread safety
|   +-- Atomic variable (counter) -----> AtomicInteger / AtomicLong
|   +-- Synchronized access ------------> ReentrantLock or synchronized
|   +-- Read-heavy, write-rare ---------> ReadWriteLock
|
+-- Task execution
|   +-- Fixed workload -----------------> newFixedThreadPool(n)
|   +-- Bursty short tasks -------------> newCachedThreadPool()
|   +-- Scheduled/periodic -------------> newScheduledThreadPool(n)
|   +-- I/O-bound (Java 21+) ----------> newVirtualThreadPerTaskExecutor()
|   +-- CPU-bound recursive ------------> ForkJoinPool
|
+-- Coordination
|   +-- Wait for N tasks to complete ---> CountDownLatch
|   +-- N threads meet at a barrier ----> CyclicBarrier
|   +-- Limit concurrent access --------> Semaphore
|   +-- Multi-phase sync ----------------> Phaser
|
+-- Async composition
|   +-- Chain async operations ----------> CompletableFuture
|   +-- Structured task groups ----------> StructuredTaskScope (Java 21+)
|
+-- Thread-safe collections
    +-- Map -----------------------------> ConcurrentHashMap
    +-- List ----------------------------> CopyOnWriteArrayList
    +-- Queue ---------------------------> ConcurrentLinkedQueue
    +-- Blocking Queue ------------------> ArrayBlockingQueue / LinkedBlockingQueue
```

| Construct | Use Case | Key Method |
|-----------|----------|-----------|
| `synchronized` | Simple mutual exclusion | Block-level |
| `ReentrantLock` | Advanced locking (tryLock, fairness) | `lock()` / `unlock()` |
| `ReadWriteLock` | Read-heavy workloads | `readLock()` / `writeLock()` |
| `AtomicInteger` | Lock-free counters | `incrementAndGet()` |
| `CountDownLatch` | Wait for N events | `await()` / `countDown()` |
| `CyclicBarrier` | Sync N threads at a point | `await()` |
| `Semaphore` | Limit concurrent access | `acquire()` / `release()` |
| `CompletableFuture` | Async pipelines | `thenApply()` / `thenCompose()` |
| `VirtualThread` | Lightweight I/O threads | `Thread.ofVirtual().start()` |

---

### GC Algorithm Comparison

| Feature | Serial | Parallel | G1 | ZGC | Shenandoah |
|---------|--------|----------|-----|-----|------------|
| Threads | Single | Multi | Multi | Multi | Multi |
| Generational | Yes | Yes | Yes | Yes (21+) | Yes (21+) |
| Pause target | None | None | Configurable | < 10ms | < 10ms |
| Max heap | Small | Medium | Large | Terabytes | Terabytes |
| Default since | - | Java 8 | Java 9 | - | - |
| Best for | Clients | Batch/throughput | General purpose | Low latency | Low latency |
| Flag | `-XX:+UseSerialGC` | `-XX:+UseParallelGC` | `-XX:+UseG1GC` | `-XX:+UseZGC` | `-XX:+UseShenandoahGC` |
| Compaction | Full STW | Full STW | Incremental | Concurrent | Concurrent |

---

### Stream Operations Cheat Sheet

**Intermediate Operations (Lazy -- return Stream)**

| Operation | Input | Output | Description |
|-----------|-------|--------|-------------|
| `filter(Predicate)` | `Stream<T>` | `Stream<T>` | Keep elements matching predicate |
| `map(Function)` | `Stream<T>` | `Stream<R>` | Transform each element |
| `flatMap(Function)` | `Stream<T>` | `Stream<R>` | Flatten nested streams |
| `distinct()` | `Stream<T>` | `Stream<T>` | Remove duplicates |
| `sorted()` | `Stream<T>` | `Stream<T>` | Sort (natural or comparator) |
| `peek(Consumer)` | `Stream<T>` | `Stream<T>` | Debug/inspect elements |
| `limit(long)` | `Stream<T>` | `Stream<T>` | Truncate to N elements |
| `skip(long)` | `Stream<T>` | `Stream<T>` | Skip first N elements |
| `takeWhile(Predicate)` | `Stream<T>` | `Stream<T>` | Take while predicate true (Java 9) |
| `dropWhile(Predicate)` | `Stream<T>` | `Stream<T>` | Drop while predicate true (Java 9) |
| `mapMulti(BiConsumer)` | `Stream<T>` | `Stream<R>` | Imperative flatMap alternative (Java 16) |

**Terminal Operations (Eager -- produce result)**

| Operation | Return Type | Description |
|-----------|------------|-------------|
| `forEach(Consumer)` | `void` | Perform action on each element |
| `collect(Collector)` | `R` | Accumulate into collection/result |
| `toList()` | `List<T>` | Collect to unmodifiable list (Java 16) |
| `reduce(BinaryOperator)` | `Optional<T>` | Combine all elements |
| `count()` | `long` | Count elements |
| `findFirst()` | `Optional<T>` | First element |
| `findAny()` | `Optional<T>` | Any element (non-deterministic in parallel) |
| `anyMatch(Predicate)` | `boolean` | Any element matches? |
| `allMatch(Predicate)` | `boolean` | All elements match? |
| `noneMatch(Predicate)` | `boolean` | No element matches? |
| `min(Comparator)` | `Optional<T>` | Minimum element |
| `max(Comparator)` | `Optional<T>` | Maximum element |
| `toArray()` | `Object[]` | Convert to array |

**Common Collectors**

| Collector | Result | Example |
|-----------|--------|---------|
| `toList()` | `List<T>` | `stream.collect(toList())` |
| `toSet()` | `Set<T>` | `stream.collect(toSet())` |
| `toMap(keyFn, valFn)` | `Map<K,V>` | `stream.collect(toMap(User::getId, Function.identity()))` |
| `joining(delimiter)` | `String` | `stream.collect(joining(", "))` |
| `groupingBy(classifier)` | `Map<K, List<T>>` | `stream.collect(groupingBy(User::getDept))` |
| `partitioningBy(pred)` | `Map<Boolean, List<T>>` | `stream.collect(partitioningBy(x -> x > 5))` |
| `counting()` | `Long` | `groupingBy(User::getDept, counting())` |
| `summingInt(fn)` | `Integer` | `stream.collect(summingInt(User::getAge))` |
| `averagingDouble(fn)` | `Double` | `stream.collect(averagingDouble(User::getSalary))` |
| `teeing(c1, c2, merger)` | `R` | Combine two collectors (Java 12) |

---

## How to Use This Repo -- 6-Week Study Plan

### Week 1: Core Java Foundation (Level 1)

**Goal:** Answer any core Java question without hesitation

| Day | Topic | Files to Study |
|-----|-------|---------------|
| Mon | HashMap internals + hashCode/equals | `collections/HashMapInternals.java`, `hashcode_equals/Employee.java` |
| Tue | String pool + immutability | `strings/`, `immutability/` |
| Wed | Exception handling | `exceptions/` (all files) |
| Thu | Generics + type erasure | `generics/` (both files) |
| Fri | Collections framework + fail-fast/safe | `collections/` (all files) |
| Sat | OOP pillars + interfaces + sorting | `oop/`, `sorting/` |
| Sun | Serialization + enums + review | `serialization/`, `enums/`, review weak areas |

---

### Week 2: Java 8 Mastery (Level 2)

**Goal:** Write any stream pipeline from scratch in an interview

| Day | Topic | Files to Study |
|-----|-------|---------------|
| Mon | Lambda expressions + functional interfaces | `lambda/`, `functional_interfaces/` |
| Tue | Stream API operations (filter, map, reduce) | `streams/Stream_QA.java` |
| Wed | Stream exercises (groupingBy, sorting, complex) | `streams/Stream_Excercise.java` |
| Thu | flatMap + Collectors deep dive | Practice 10 stream problems |
| Fri | Optional + method references | `method_references/`, practice Optional |
| Sat | DateTime API + practice | `datetime/`, `Java8.java` |
| Sun | Timed mock: solve 5 stream problems in 30 min | Self-test |

---

### Week 3: Concurrency Deep Dive (Level 3)

**Goal:** Explain any concurrency concept and write the code

| Day | Topic | Files to Study |
|-----|-------|---------------|
| Mon | Thread basics + volatile + ThreadLocal | `basics/` (all files) |
| Tue | ExecutorService + thread pool types | `executors/` (all files) |
| Wed | CompletableFuture | `completable_future/Completable.java` |
| Thu | Locks: ReentrantLock, ReadWriteLock, Condition | `locks/` (all files) |
| Fri | Synchronizers: Latch, Barrier, Semaphore, Phaser | `synchronizers/` (all files) |
| Sat | ForkJoinPool + Producer-Consumer + Deadlock | `fork_join/`, `patterns/` |
| Sun | Review: draw diagrams for each pattern | Whiteboard practice |

---

### Week 4: Modern Java (Levels 4 + 5)

**Goal:** Demonstrate that you keep current with Java evolution

| Day | Topic | Files to Study |
|-----|-------|---------------|
| Mon | var + records + sealed classes | `var_type/`, `records/`, `sealed_classes/` |
| Tue | Pattern matching (instanceof + switch) | `pattern_matching/`, `pattern_matching_switch/` |
| Wed | Switch expressions + text blocks + streams 9-17 | `switch_expressions/`, `text_blocks/`, `stream_enhancements/` |
| Thu | Virtual threads + comparison demo | `virtual_threads/` (both files) |
| Fri | Structured concurrency + ScopedValue | `structured_concurrency/`, `scoped_values/` |
| Sat | Record patterns + sequenced collections | `record_patterns/`, `sequenced_collections/` |
| Sun | Review all modern features, write code from memory | Self-test |

---

### Week 5: JVM Internals (Level 6)

**Goal:** Explain what happens under the hood

| Day | Topic | Files to Study |
|-----|-------|---------------|
| Mon | JVM memory model (heap, stack, metaspace) | `memory_model/JvmMemoryModelDemo.java` |
| Tue | GC algorithms (G1, ZGC, Shenandoah) | `gc/GarbageCollectionDemo.java` |
| Wed | GC tuning + flags | `gc/GCTuningDemo.java` |
| Thu | Class loading + JIT compilation | `classloading/`, `jit/` |
| Fri | Memory leak patterns | `memory_leaks/MemoryLeakPatterns.java` |
| Sat | Profiling tools (JFR, jcmd, jstack, jmap) | `profiling/ProfilingToolsOverview.java` |
| Sun | Practice: tune GC for a sample app | Hands-on |

---

### Week 6: Future Java + Mock Interviews (Level 7)

**Goal:** Stand out with forward-looking knowledge

| Day | Topic | Files to Study |
|-----|-------|---------------|
| Mon | Stream Gatherers + flexible constructors | `stream_gatherers/`, `flexible_constructors/` |
| Tue | Value types + primitive generics | `value_types/`, `primitive_generics/` |
| Wed | FAANG top 20 questions (timed practice) | See FAANG section above |
| Thu | Full mock interview: Level 1-3 (60 min) | Random selection, explain + code |
| Fri | Full mock interview: Level 4-6 (60 min) | Random selection, explain + code |
| Sat | Weak area review + edge cases | Focus on questions you got wrong |
| Sun | Final review + confidence builder | Skim all levels, ready to go |

---

### Tips for Interview Day

1. **Start with the what, then the why, then the how.** "HashMap uses an array of buckets [what]. This gives O(1) average lookup [why]. Here is how put() works... [how]"

2. **Draw diagrams** for: HashMap buckets, thread lifecycle, JVM memory areas, GC generations

3. **Always mention tradeoffs.** "ArrayList gives O(1) random access but O(n) insertion. LinkedList is the opposite."

4. **Show awareness of versions.** "In Java 8, HashMap buckets treeify when..." shows depth

5. **Connect concepts.** "This is why ConcurrentHashMap is better than Hashtable -- it uses segment-level locking instead of table-level, which relates to how ReadWriteLock improves over synchronized..."

---

## License

This repository is for educational purposes -- personal interview preparation.

---

> **140 files. 130+ questions. 7 levels. Java 8 to Java 26. Good luck.**
