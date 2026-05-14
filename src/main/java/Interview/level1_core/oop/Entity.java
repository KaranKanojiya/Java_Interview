package interview.level1_core.oop;

import interview.level2_java8.streams.Employee;

import java.util.List;

public class Entity implements Deletable{

    public static void main(String[] args) {

        DaoFramework daoFramework=new DaoFramework();
        daoFramework.delete(new Entity());

    }
}
