package interview.level3_multithreading.locks;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantLock_ReadWrite_API {

    ReentrantReadWriteLock lock=new ReentrantReadWriteLock();

    ReentrantReadWriteLock.ReadLock readLock=lock.readLock();
    ReentrantReadWriteLock.WriteLock writeLock=lock.writeLock();

    public void readResource() {
        readLock.lock();
        System.out.println("view Resource");
        readLock.unlock();
    }

    public void writeResource(){
        writeLock.lock();
        System.out.println("write Resource");
        writeLock.unlock();
    }

    public static void main(String[] args) {

        Thread t1=new Thread(()-> new ReentrantLock_ReadWrite_API().readResource());t1.start();
        Thread t2=new Thread(()->new ReentrantLock_ReadWrite_API().readResource());t2.start();
        Thread t3=new Thread(()->new ReentrantLock_ReadWrite_API().writeResource());t3.start();
        Thread t4=new Thread(()->new ReentrantLock_ReadWrite_API().writeResource());t4.start();

    }



}
