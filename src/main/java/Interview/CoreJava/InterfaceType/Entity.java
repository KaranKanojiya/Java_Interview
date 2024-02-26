package Interview.CoreJava.InterfaceType;

import Interview.Java8.Stream.Employee;

import java.util.List;

public class Entity implements Deletable{

    public static void main(String[] args) {

        DaoFramework daoFramework=new DaoFramework();
        daoFramework.delete(new Entity());

    }
}
