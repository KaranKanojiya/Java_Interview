package interview.level3_multithreading.executors;

import java.util.concurrent.*;

public class RejectionPolicy {

    public static void main(String[] args) {
        ExecutorService service = new ThreadPoolExecutor(
                10,100,
                120,
                TimeUnit.SECONDS,new ArrayBlockingQueue<>(300));
        try {
            service.execute(new Task());
        }catch (RejectedExecutionException e){
            System.err.println("Task Rejected"+e.getMessage());
        }
    }
}
