package interview.level1_core.strings;

public class StringObject {

    public static void main(String[] args) {

        String s1=new String("karan");

        String s2="karan";

        //intern() method used to get reference from SCP
        System.out.println(s1.intern().hashCode()==s2.hashCode());
    }
}
