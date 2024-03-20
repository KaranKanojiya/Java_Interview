package Interview.MultiThreading.Runnable;

public class Task implements Runnable{

    @Override
    public void run() {
        System.out.println("Thread Name "+Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        Thread thread=new Thread(new Task(),"Thread1");
        thread.start();

        new Thread(() -> new Task().run(), "Thread2").start();
    }
}
