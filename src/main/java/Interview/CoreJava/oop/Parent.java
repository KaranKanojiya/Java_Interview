package Interview.CoreJava.oop;

public class Parent {

    public void m1(){
        System.out.println("Parent :  m1() Method");
    }

    public static void m2(){
        System.out.println("Parent :  m2() Method");
    }


    public static void main(String[] args) {
        Parent parent=new Child();
        parent.m1();  // Will call child method
        parent.m2(); // Will call static method
    }

}
