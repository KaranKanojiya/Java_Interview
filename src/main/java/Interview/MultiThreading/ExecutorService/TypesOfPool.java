package Interview.MultiThreading.ExecutorService;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class TypesOfPool implements Runnable {
    @Override
    public void run() {
        System.out.println("Thread Name " + Thread.currentThread().getName());
        System.out.println("Current  Time " + Calendar.getInstance().getTime());


    }

    public static void main(String[] args) {

        // -------------------------  Types of Thread Pools : --------------------------------------------------

        // ----- Fixed Thread Pool

        ExecutorService excecutorService= Executors.newFixedThreadPool(10);
        //excecutorService.execute(new TypesOfPool());


        // ----- Cached Thread Pool

        ExecutorService cachedThreadPool= Executors.newCachedThreadPool();
        //cachedThreadPool.execute(new TypesOfPool());


        // ------ ScheduledExecutorService
        ScheduledExecutorService service=Executors.newScheduledThreadPool(10);

        // task to run after 10 second delay:
        service.schedule(new TypesOfPool(),10, SECONDS);

        // task to run repeatedly every 10 seconds
        service.scheduleAtFixedRate(new TypesOfPool(),15,10, SECONDS);

        // task to run repeatedly  10 seconds after previous task completes
        service.scheduleWithFixedDelay(new TypesOfPool(),15,10, SECONDS);

        // ------ Single Threaded Executor:
        ExecutorService singleThreadedExecutor= Executors.newSingleThreadExecutor();
        singleThreadedExecutor.execute(new TypesOfPool());

        ScheduledExecutorService singleScheduledThreadedExecutor= Executors.newSingleThreadScheduledExecutor();
        singleScheduledThreadedExecutor.scheduleAtFixedRate(new Task(),15,10,SECONDS);

        // LifeCycle method of threadPool




    }

}
