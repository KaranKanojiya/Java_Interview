package interview.level1_core.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FailSafe {

    public static void main(String[] args) {

        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3, 4));

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            System.out.println(iterator.next());

            // This will not print,
            // Since it has created separate copy
            list.add(9);
        }

        // Creating a ConcurrentHashMap

        ConcurrentHashMap<String, Integer> map
                = new ConcurrentHashMap<String, Integer>();

        map.put("ONE", 1);
        map.put("TWO", 2);
        map.put("THREE", 3);
        map.put("FOUR", 4);

        // Getting an Iterator from map
        Iterator it = map.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            System.out.println(key + " : " + map.get(key));

            // This will reflect in iterator.
            // Hence, it has not created separate copy
            map.put("SEVEN", 7);
        }

    }
}
