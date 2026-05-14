package interview.level1_core.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student implements Comparable<Student>{

    private int id;
    private int rollno;
    private String name;
    private int age;

    public Student(int id,int rollno, String name, int age) {
        this.id = id;
        this.rollno = rollno;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRollno() {
        return rollno;
    }

    public void setRollno(int rollno) {
        this.rollno = rollno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // Implement Comparable :

    @Override
    public int compareTo(Student o) {
        System.out.println("ID of current object: " + getId());
        // Print the ID of the passed object
        System.out.println("ID of passed object: " + o.getId());
        if (getId()==o.getId()) return 0;
        else if (getId()>o.getId()) return 1;
        else return -1;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", rollno=" + rollno +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public static void main(String[] args) {

        List<Student> studentList=new ArrayList<>();
        studentList.add(new Student(1,101,"Vijay",23));
        studentList.add(new Student(2,106,"Ajay",27));
        studentList.add(new Student(3,105,"Jai",21));
        studentList.add(new Student(3,105,"Ash",21));


        /*Comparable Example :


        System.out.println("StudentList comparable :"+studentList);*/

        Collections.sort(studentList);

        // Comparator Example
        Collections.sort(studentList,new AgeComparator());

        System.out.println("StudentList Agecomparator :"+studentList);


    }


}
