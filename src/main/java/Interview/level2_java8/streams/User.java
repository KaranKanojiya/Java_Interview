package interview.level2_java8.streams;

import java.util.List;

public class User {

    private String name;
    private String phone;
    private List<String> email;

    public User(String name, String phone, List<String> email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public User() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getEmail() {
        return email;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email=" + email +
                '}';
    }
}
