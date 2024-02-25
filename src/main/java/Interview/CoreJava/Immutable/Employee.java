package Interview.CoreJava.Immutable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public  final class Employee {

    private final String name;
    private final Date doj;
    private final List<String> mobile;

    public Employee(String name, Date doj, List<String> mobile) {
        this.name = name;
        this.doj = doj;
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public Date getDoj() {
        return (Date) doj.clone();
    }

    public List<String> getMobile() {
        return Collections.unmodifiableList(mobile);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", doj=" + doj +
                ", mobile=" + mobile +
                '}';
    }

    public static void main(String[] args) {

        Employee employee=new Employee("karan",new Date(),Arrays.stream(new String[]{"8212345678"}).collect(Collectors.toList()));
        employee.getDoj().setDate(10);
        employee.getMobile().add("100");

        System.out.println(employee);


    }
}
