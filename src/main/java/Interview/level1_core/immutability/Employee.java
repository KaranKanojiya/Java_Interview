package interview.level1_core.immutability;

import java.util.*;
import java.util.stream.Collectors;

public  final class Employee {

    private final String name;
    private final Date doj;
    private final List<String> mobile;
    private final Address address;

    public Employee(String name, Date doj, List<String> mobile, Address address) {
        this.name = name;
        this.doj = doj;
        this.mobile = mobile;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public Date getDoj() {
        return (Date) doj.clone();
    }

    public List<String> getMobile() {
        return new ArrayList<>(mobile);
    }

    public Address getAddress() {
        System.out.println(address.getName());
        return new Address(address.getName(), address.getPinCode());
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", doj=" + doj +
                ", mobile=" + mobile +
                ", address=" + address +
                '}';
    }

    public static void main(String[] args) {

        Employee employee=new Employee("karan",new Date(),
                Arrays.stream(new String[]{"8212345678"}).collect(Collectors.toList()),
                new Address("Bangalore",40067));
        employee.getDoj().setDate(10);
        employee.getMobile().add("100");
        employee.getAddress().setName("Pune");

        System.out.println(employee);


    }
}
