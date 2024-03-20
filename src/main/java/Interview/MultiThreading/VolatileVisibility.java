package Interview.MultiThreading;

public class VolatileVisibility {

    boolean flag = true;

    public void writerThread() {
        System.out.println("Writer thread: " + Thread.currentThread().getName());
        flag = false;
    }

    public void readerThread(){
        System.out.println("Reader thread: " + Thread.currentThread().getName());
        while(flag){
            System.out.println("inside while");
        }
    }

    public static void main(String[] args) {
        VolatileVisibility volatileVisibility = new VolatileVisibility();
        new Thread(() -> volatileVisibility.readerThread(), "ReaderThread").start();
        System.out.println();
        new Thread(() -> volatileVisibility.writerThread(), "WriterThread").start();
    }

}
