package interview.level2_java8.streams;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Stream_QA {

    public static void main(String[] args) {

/*
 `map`: This method transforms each element in a stream into another element
 `flatMap`: This method transforms each element in a stream into a stream of elements,
     and then merges those streams into a single stream
 */

// In this example:
// * `map(User::getPhone)` extracts the phone number from each user and returns a stream of phone numbers.
// * `map(User::getEmail)` extracts the email address list from each user and returns a stream of lists.
// * `flatMap(user -> user.getEmail().stream())` extracts the email address list from each user,
//      then flattens each list into a stream of individual email addresses, and merges them into a single stream.
        List<User> users = Stream.of(
                new User("user1", "1234567890", Arrays.asList("abc@gmail.com", "def@gmail.com")),
                new User("user2", "1234567891", Arrays.asList("ghi@gmail.com", "jkl@gmail.com")))
                .collect(Collectors.toList());

        List<String> phoneNumbers = users.stream().map(User::getPhone).collect(Collectors.toList());
        System.out.println(phoneNumbers+"\n");

        List<List<String>> withoutFlatMapListOfEmail = users.stream().map(User::getEmail).collect(Collectors.toList());
        System.out.println(withoutFlatMapListOfEmail+"\n");

        List<String> withFlatMapListOfEmail = users.stream().flatMap(user -> user.getEmail().stream()).collect(Collectors.toList());
        System.out.println(withFlatMapListOfEmail+"\n");



        /*---------------------------------------- Stream Interview Question ------------------------------------*/


        // WAP using stream to find frequence of each  character in a given string

        String input = "karan";

        Map<Character, Long> freq = input.chars().mapToObj(ch -> (char) ch).filter(Character::isLetter)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<Character, Long> frequencyMap = input.chars().mapToObj(ch -> (char) ch).filter(Character::isLetter)
                .collect(Collectors.groupingBy(ch -> ch, Collectors.counting()));


        HashSet<Character> set=new HashSet<>();

        char findFirstRepeatingChar=input.chars().mapToObj(ch->(char)ch)
                .filter(c->!set.add(c)).findFirst().orElse('\0');

        System.out.println("findFirstRepeatingChar "+findFirstRepeatingChar);


        Map<Character, Integer> charCountMap = new LinkedHashMap<>();

        // Count occurrences of each character in the string
        input.chars().forEach(ch -> charCountMap.put((char) ch, charCountMap.getOrDefault((char) ch, 0) + 1));

        // Find the first character with count 1
        char findFirstUniqueChar = charCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse('\0');

        System.out.println("findFirstUniqueChar "+findFirstUniqueChar);

        freq.forEach((k, v) -> System.out.println(k + "->" + v));

        System.out.println("frequencyMap -> " + frequencyMap+"\n");

        // Assume you have list of employees in various department, WAP to find highest paid employee of each department

        List<Employee> employeeList = Stream.of(
                new Employee(111, "Jiya Brein", 32, "Female", "HR", 2011, 25000.0),
                new Employee(122, "Paul Niksui", 25, "Male", "Sales And Marketing", 2015, 13500.0),
                new Employee(133, "Martin Theron", 29, "Male", "Infrastructure", 2012, 18000.0),
                new Employee(144, "Murali Gowda", 28, "Male", "Product Development", 2014, 32500.0),
                new Employee(155, "Nima Roy", 27, "Female", "HR", 2013, 22700.0),
                new Employee(166, "Iqbal Hussain", 43, "Male", "Security And Transport", 2016, 10500.0),
                new Employee(177, "Manu Sharma", 35, "Male", "Account And Finance", 2010, 27000.0),
                new Employee(188, "Wang Liu", 31, "Male", "Product Development", 2015, 34500.0),
                new Employee(199, "Amelia Zoe", 24, "Female", "Sales And Marketing", 2016, 11500.0),
                new Employee(200, "Jaden Dough", 38, "Male", "Security And Transport", 2015, 11000.5),
                new Employee(211, "Jasna Kaur", 27, "Female", "Infrastructure", 2014, 15700.0),
                new Employee(222, "Nitin Joshi", 25, "Male", "Product Development", 2016, 28200.0),
                new Employee(233, "Jyothi Reddy", 27, "Female", "Account And Finance", 2013, 21300.0),
                new Employee(244, "Nicolus Den", 24, "Male", "Sales And Marketing", 2017, 10700.5),
                new Employee(255, "Ali Baig", 23, "Male", "Infrastructure", 2018, 12700.0),
                new Employee(266, "Sanvi Pandey", 26, "Female", "Product Development", 2015, 28900.0),
                new Employee(277, "Anuj Chettiar", 31, "Male", "Product Development", 2012, 35700.0)
        ).collect(Collectors.toList());


        // Approach 1 :
        Comparator<Employee> compareBySalary = Comparator.comparing(Employee::getSalary);


        Map<String, Optional<Employee>> employeeMap1 = employeeList.stream()
                .collect(
                        Collectors.groupingBy(Employee::getDepartment,
                                Collectors.reducing(BinaryOperator.maxBy(compareBySalary))));

        System.out.println(employeeMap1+"\n");


        // Approach 2:


        Map<String, Employee> employeeMap2 = employeeList.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment,
                        Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)), Optional::get)));

        System.out.println(employeeMap2+"\n");


        List<Student> studentList = Stream.of(
                new Student(1, "Rohit", 30, "Male", "Mechanical Engineering", "Mumbai", 122, Arrays.asList("+912632632782", "+1673434729929")),
                new Student(2, "Pulkit", 56, "Male", "Computer Engineering", "Delhi", 67, Arrays.asList("+912632632762", "+1673434723929")),
                new Student(3, "Ankit", 25, "Female", "Mechanical Engineering", "Kerala", 164, Arrays.asList("+912632633882", "+1673434709929")),
                new Student(4, "Satish Ray", 30, "Male", "Mechanical Engineering", "Kerala", 26, Arrays.asList("+9126325832782", "+1671434729929")),
                new Student(5, "Roshan", 23, "Male", "Biotech Engineering", "Mumbai", 12, Arrays.asList("+012632632782")),
                new Student(6, "Chetan", 24, "Male", "Mechanical Engineering", "Karnataka", 90, Arrays.asList("+9126254632782", "+16736784729929")),
                new Student(7, "Arun", 26, "Male", "Electronics Engineering", "Karnataka", 324, Arrays.asList("+912632632782", "+1671234729929")),
                new Student(8, "Nam", 31, "Male", "Computer Engineering", "Karnataka", 433, Arrays.asList("+9126326355782", "+1673434729929")),
                new Student(9, "Sonu", 27, "Female", "Computer Engineering", "Karnataka", 7, Arrays.asList("+9126398932782", "+16563434729929", "+5673434729929")),
                new Student(10, "Shubham", 26, "Male", "Instrumentation Engineering", "Mumbai", 98, Arrays.asList("+912632646482", "+16734323229929")))
                .collect(Collectors.toList());

        // 1. Find the list of students whose rank is in between 50 and 100
        List<Student> list = studentList.stream()
                .filter(s -> s.getRank() >= 50 && s.getRank() <= 100)
                .collect(Collectors.toList());
        System.out.println("students whose rank is in between 50 and 100 " + list+"\n");

// 3. Find all departments names
        List<String> deptNames = studentList.stream()
                .map(Student::getDept)
                .distinct()
                .collect(Collectors.toList());
        System.out.println("All department names: " + deptNames+"\n");

//4. Find all the contact numbers
        List<String> contacts = studentList.stream()
                .flatMap(s->s.getContacts().stream()).distinct()
                .collect(Collectors.toList());
        System.out.println("All contact numbers: " + contacts);

//5. Group The Student By Department Names
        Map<String, List<Student>> studentMap = studentList.stream()
                .collect(Collectors.groupingBy(s->s.getDept()));
        System.out.println("Students grouped by department: " + studentMap+"\n");

//6. Find the department who is having maximum number of students
        Map.Entry<String, Long> results = studentList.stream()
                .collect(Collectors.groupingBy(Student::getDept, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).get();

        System.out.println("Department with the maximum number of students: " + results+"\n");

//7. Find the average age of male and female students
        Map<String, Double> avgStudents = studentList.stream()
                .collect(Collectors.groupingBy(Student::getGender,
                        Collectors.averagingInt(Student::getAge)));
        System.out.println("Average age of male and female students: " + avgStudents+"\n");

//8. Find the highest rank in each department
        Map<String, Optional<Student>> stdMap = studentList.stream()
                .collect(Collectors.groupingBy(Student::getDept,
                        Collectors.minBy(Comparator.comparing(Student::getRank))));
        System.out.println("Highest rank in each department: " + stdMap+"\n");

//9 .Find the student who has second rank
        Student secondHighestRankStudent = studentList.stream()
                .sorted(Comparator.comparing(Student::getRank))
                .skip(1)
                .findFirst().get();
        System.out.println("SecondHighest rank Student: " + secondHighestRankStudent+"\n");

        // Stream vs Parallel Stream :
        System.out.println("------------------ Normal Stream ---------------------------");

        IntStream.range(1, 10).forEach(t -> System.out.println(Thread.currentThread().getName() + t));

        System.out.println("------------------ Parallel Stream ---------------------------");

        IntStream.range(1, 10).parallel().forEach(t -> System.out.println(Thread.currentThread().getName() + t));


    }
}


