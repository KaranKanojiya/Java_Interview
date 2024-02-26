package Interview.CoreJava.Immutable;

public class Address {

    private String name;
    private int    pinCode;

    public Address(String name, int pinCode) {
        this.name = name;
        this.pinCode = pinCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", pinCode=" + pinCode +
                '}';
    }
}
