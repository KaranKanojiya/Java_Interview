package interview.level2_java8.lambda;

import java.util.function.Function;

public class LambdaExpression_Example {



    // Anonymous Class
    public static void main(String[] args) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello");
            }
        };
        new Thread(runnable).start();

        // Lambda Expression

        Runnable runnable1 = () -> System.out.println("Hello Lambda");
        new Thread(runnable1).start();


        // MyFunction Add Lambda Expression

        MyFunction addFunction = (a, b) -> a + b;
        System.out.println(addFunction.add(2,2));


        // Use Java @Function Interface apply method

        Function<Integer,String> function=(t)-> "output:"+t;
        System.out.println(function.apply(27));

    }
}
