package Interview.MultiThreading;


import java.util.concurrent.atomic.AtomicInteger;

public class Thread_Example {


    // volatile example :
    volatile boolean flag=true;

    public static void main(String[] args) {

        // AtomicInteger

        AtomicInteger atomicInteger=new AtomicInteger(0);

        System.out.println(atomicInteger.get());

        int var=atomicInteger.getAndAdd(7);

        // Prints the updated value
        System.out.println("Previous value: "+ var);
        System.out.println("Current value: "+ atomicInteger);


    }




}

