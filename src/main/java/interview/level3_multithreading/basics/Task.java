package interview.level3_multithreading.basics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Task implements Callable {
    @Override
    public Integer call() throws Exception {
        Thread.sleep(1000); // Will throw  java.util.concurrent.TimeoutException
        return new Random().nextInt();

    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {


        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Future<Integer> singleFutureTask=executorService.submit(new Task());

        //Perform some unreleated Operations :

        System.out.println(singleFutureTask.get()); // Will wait till then main thread will be blocked


        //submit the task for execution in List

        List<Future> futureList=new ArrayList<>();
        for(int i=0;i<100;i++){
            Future<Integer> future= executorService.submit(new Task());
            futureList.add(future);

        }
        try {
            for(int i=0;i<10;i++){
                Future<Integer> future=futureList.get(i);
                System.out.println("'Result of future #"+i+ " = "+future.get()); // blocking operation
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Timeout Exception will throw but it will continue process


        Future<Integer> future= executorService.submit(new Task());
        try {
            Integer result=future.get(1,TimeUnit.SECONDS);
            System.out.println("'Result of future #"+future.get()); // blocking operation
        } catch (TimeoutException e) {
            e.printStackTrace();
            System.out.println("Continue the timeout logic");
        }catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Cancel the task

        Future<Integer> futures= executorService.submit(new Task());

        future.cancel(false); //Interrupt is totally diff ball game but if not started no issue

        //Return true if task was cancelled

        System.out.println(futures.isCancelled());

        //Return true if task is completed ( Successfully or return false)
        System.out.println(futures.isDone());
        executorService.shutdown();


    }
}
