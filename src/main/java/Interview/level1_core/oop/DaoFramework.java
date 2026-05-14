package interview.level1_core.oop;

public class DaoFramework {

    public void delete(Object object){
        if(object instanceof Deletable){
            System.out.println("Allow Delete");
        }else{
            System.out.println("Do not allow to delete");
        }
    }
}
