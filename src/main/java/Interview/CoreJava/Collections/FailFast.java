package Interview.CoreJava.Collections;

import Interview.CoreJava.oop.Parent;

import java.util.*;

public class FailFast {
    
    public static void main(String[] args) {



        // List Example:

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);


        Iterator iterator=list.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
            list.remove(2);
        }


        // Map Example :

        Map<Integer,Integer> map=new HashMap<>();

        map.put(1,1);
        map.put(2,2);

        Iterator iterator1=map.entrySet().iterator();

        while (iterator1.hasNext()){
            System.out.println(iterator1.next());
            map.put(3,3);
        }


    }
}
