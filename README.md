What are all features of Java 8 did you used ?


1) Function Interfaces ( include default and static method )
2) Lambda Expression
3) Stream
4) CompletableFuture
5) Java DateTime API



📌 Functional Interface :

A interface that contains only one abstract method is known as Functional Interface. It can have any number of default
and static methods.

📌 Can you tell Functional Interface that exist before Java 8

Runnable, Callable and Comparator


📌 default and static method :

default method : Not compulsory to override it
static method : Act as a utility method e.g Date Pattern

📌 What are all Functional interface introduced in Java 8 :

- Function
- Predicate
- Consumer
- Supplier


📌 What is Lambda Expression ?

Lambda Expression basically express instances of functional interfaces, in other words it provide clear and concise
way to represent method of a functional using an expression

📌 What is Stream API ?

Stream API is used to process collection of object with functional style of coding using lambda  expression

📌 What is Stream in Java 8 ?

A stream is a sequence of object that supports various methods which can be pipelined to produced desired results

📌 What is Method Reference ?

Method Reference is a shorthand notation of a lambda expression to call a method

Types : static | instance | constructor



📌 Tell Stream method you used in your project ?

⚙ Filter:

Definition: Selects elements from a stream based on a specified condition.
Example: stream.filter(x -> x % 2 == 0). This filters out odd numbers from a stream.

⚙ forEach:

Definition: Performs an action on each element in a stream.
Example: stream.forEach(System.out::println). This prints each element of the stream to the console.

⚙ sorted:

Definition: Arranges elements in a stream in ascending (default) or descending order.
Example: stream.sorted((a, b) -> b - a). This sorts numbers in descending order.

⚙ map:

Definition: Transforms each element in a stream into a new element.
Example: stream.map(x -> x * 2). This doubles each element in the stream.


⚙ flatMap:

Definition: Combines results from multiple streams into a single stream.
Example: // Using flatMap for transformating and flattening.
List<Integer> listofInts  = listOfListofInts.stream()
.flatMap(list -> list.stream())
.collect(Collectors.toList());

⚙ reduce:

Definition: Combines all elements in a stream into a single value using a provided function.
Example: https://www.geeksforgeeks.org/stream-reduce-java-examples/


⚙ groupingBy:

Definition: Groups elements in a stream based on a key function.
Example: stream.groupingBy(String::toLowerCase). This groups strings by their lowercase representation.

⚙ count:

Definition: Counts the number of elements in a stream.
Example: stream.count(). This counts the number of elements in the stream.

⚙ collect:

Definition: Performs various operations on a stream and collects the results.
Example: stream.collect(Collectors.toList()). This collects elements into a list.

📌 When to use map and flatMap?

map : to fetch single element
flatMap: to fetch list from list or one to many scenario | e.g User having multiple emails
