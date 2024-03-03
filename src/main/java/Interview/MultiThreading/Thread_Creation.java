package Interview.MultiThreading;

import java.util.concurrent.Executors;

public class Thread_Creation {

    public static void main(String[] args) {


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                // code goes here.
            }
        });
        t1.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // code goes here.
            }
        }).start();

        new Thread(() -> {
            // code goes here.
        }).start();


        // --Using ExecutorService:

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //myCustomMethod();
            }
        });

        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //myCustomMethod();
            }
        });






    }
}
