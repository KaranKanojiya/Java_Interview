package Interview.Java8.Functional_Interface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public interface UPIPayment {

    String doPayment(String source,String dest);

    // It' not compulsory to override it
    default double getScratchCard(){
        return new Random().nextDouble();
    }

    // Act as a Utility Class
    static String datePatterns(String patterns){
        SimpleDateFormat dateFormat=new SimpleDateFormat(patterns);
        return dateFormat.format(new Date());
    }
}
