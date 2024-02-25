package Interview.CoreJava.Exception;

public class TryCatchFinallyReturnFlow {

    public static int m1(){
        try {
            return 1/0;
        }catch (Exception e){
            System.out.println("hello");
        }finally {
            return 3;
        }
    }

    public static void main(String[] args) {

        System.out.println(TryCatchFinallyReturnFlow.m1());
    }
}
