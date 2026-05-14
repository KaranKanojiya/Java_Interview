package interview.level3_multithreading.basics;

import interview.level2_java8.streams.User;

public class UserContextHolder {

    public static ThreadLocal<User> holder = new ThreadLocal<>();

    static class Service1 implements Runnable {
        @Override
        public void run() {
            // Set user for Service1 thread
            User user = new User();
            user.setName("Service1User");
            UserContextHolder.holder.set(user);
            // Call service3 from Service1 thread
            Service3 service3 = new Service3();
            service3.process();
        }
    }

    static class Service2 implements Runnable {
        @Override
        public void run() {
            // Set user for Service2 thread
            User user = new User();
            user.setName("Service2User");
            UserContextHolder.holder.set(user);
            // Call service3 from Service2 thread
            Service3 service3 = new Service3();
            service3.process();
        }
    }

    static class Service3 {
        public void process() {
            // Get user from ThreadLocal
            User user = UserContextHolder.holder.get();
            System.out.println("Service3 thread: " + Thread.currentThread().getName() + ", User: " + user);
        }
    }

    public static void main(String[] args) {
        // Create and start two threads for Service1 and Service2
        Thread service1Thread = new Thread(new Service1(), "Service1Thread");
        Thread service2Thread = new Thread(new Service2(), "Service2Thread");
        service1Thread.start();
        service2Thread.start();
    }
}
