package interview.level1_core.exceptions;

public class Test {

    public static void main(String[] args) throws OrderNotFoundException {

        Test test=new Test();
        test.getOrder(101);

    }

    public void getOrder(int orderId) throws OrderNotFoundException {

        if(orderId==101) throw new OrderNotFoundException("Order not found with orderId:"+orderId);
        else System.out.println("Order found with orderId:"+orderId);
    }
}
