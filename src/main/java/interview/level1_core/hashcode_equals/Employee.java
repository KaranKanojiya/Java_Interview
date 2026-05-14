package interview.level1_core.hashcode_equals;

import java.util.HashSet;
import java.util.Objects;

public class Employee {

    private int id;
    private String name;

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return getId() == employee.getId() && Objects.equals(getName(), employee.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public static void main(String[] args) {

        Employee employee1=new Employee(1,"karan");
        Employee employee2=new Employee(1,"karan");

        HashSet<Employee> set=new HashSet<>();
        set.add(employee1);
        set.add(employee2);
        System.out.println(set); // Will add both emp without overriding hashcode and equals method
    }

}
