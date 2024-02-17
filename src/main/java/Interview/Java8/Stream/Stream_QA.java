package Interview.Java8.Stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Stream_QA {

    public static void main(String[] args) {

        List<User> users= Stream.of(
                new User("user1","1234567890", Arrays.asList("abc@gmail.com","def@gmail.com")),
                new User("user2","1234567891", Arrays.asList("ghi@gmail.com","jkl@gmail.com")))
                .collect(Collectors.toList());

        List<String> phoneNumbers=users.stream().map(User::getPhone).collect(Collectors.toList());
        System.out.println(phoneNumbers);

        List<List<String>> withoutFlatMapListOfEmail=users.stream().map(User::getEmail).collect(Collectors.toList());
        System.out.println(withoutFlatMapListOfEmail);

        List<String> withFlatMapListOfEmail=users.stream().flatMap(user -> user.getEmail().stream()).collect(Collectors.toList());
        System.out.println(withFlatMapListOfEmail);

    }



}
