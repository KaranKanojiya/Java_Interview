package Interview.CoreJava.oop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Example {

    public static void main(String[] args) {

        // List as a final
        final List<Integer> list=new ArrayList<>();

        list.add(1);
        list.add(2);

      //  list= new ArrayList<>();



        // Custom Array List to allow duplicates

        CustomArrayList customArrayList=new CustomArrayList();
        customArrayList.add(1);
        customArrayList.add(2);
        customArrayList.add(1);
        System.out.println("customArrayList:"+customArrayList);


        HashSet<Integer> set=new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(2);

        System.out.println("Set :"+set);

    }
}
