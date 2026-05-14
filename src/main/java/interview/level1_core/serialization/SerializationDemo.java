package interview.level1_core.serialization;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: What is serialization in Java?
// A: Serialization is converting an object's state to a byte stream (for storage or
//    network transfer). Deserialization is the reverse. Implement java.io.Serializable
//    (a marker interface with no methods).
//
// Q: What is serialVersionUID and why is it important?
// A: It's a version identifier for the serialized class. During deserialization, the JVM
//    checks if the serialVersionUID of the serialized object matches the loaded class.
//    If they don't match, InvalidClassException is thrown. Always declare it explicitly
//    to avoid issues when the class evolves.
//
// Q: What does the transient keyword do?
// A: Fields marked transient are excluded from serialization. Use it for sensitive data
//    (passwords), derived/computed fields, or non-serializable fields.
//
// Q: What is the difference between Serializable and Externalizable?
// A: Serializable: Marker interface, JVM handles serialization automatically.
//    Externalizable: Extends Serializable, requires implementing writeExternal() and
//    readExternal() methods. Gives full control over what gets serialized.
//    Externalizable requires a public no-arg constructor.
//
// Q: What is the Serialization Proxy Pattern?
// A: A technique from Effective Java: instead of serializing the object directly,
//    serialize a private inner class (proxy) that represents the object's state.
//    On deserialization, the proxy creates a new instance using the public API.
//    This prevents many serialization attacks and invariant violations.
//
// Q: What are the security risks of serialization?
// A: Deserialization can execute arbitrary code if the class has readObject() or if
//    the classpath contains "gadget" classes. This is the basis of many Java exploits
//    (e.g., Apache Commons Collections). Prefer JSON/Protobuf over Java serialization.
//
// Q: Can you serialize a static field?
// A: No. Static fields belong to the class, not the object instance. They are NOT serialized.
// ========================================================

import java.io.*;
import java.util.Base64;

public class SerializationDemo {

    // --- 1. Basic Serializable class ---
    static class Employee implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private int age;
        private transient String password;  // NOT serialized
        private transient double bonus;     // NOT serialized

        public Employee(String name, int age, String password, double bonus) {
            this.name = name;
            this.age = age;
            this.password = password;
            this.bonus = bonus;
        }

        @Override
        public String toString() {
            return "Employee{name='" + name + "', age=" + age +
                    ", password='" + password + "', bonus=" + bonus + "}";
        }
    }

    // --- 2. Custom readObject/writeObject ---
    static class Account implements Serializable {
        private static final long serialVersionUID = 2L;

        private String accountId;
        private transient String encryptedPin;  // custom handling
        private double balance;

        public Account(String accountId, String pin, double balance) {
            this.accountId = accountId;
            this.encryptedPin = encrypt(pin);
            this.balance = balance;
        }

        // Custom serialization: encrypt sensitive data
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();  // serialize non-transient fields
            // Manually serialize the encrypted pin
            out.writeUTF(encryptedPin != null ? encryptedPin : "");
        }

        // Custom deserialization: decrypt sensitive data
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();  // deserialize non-transient fields
            // Manually deserialize the encrypted pin
            this.encryptedPin = in.readUTF();
        }

        private static String encrypt(String pin) {
            return Base64.getEncoder().encodeToString(pin.getBytes());
        }

        @Override
        public String toString() {
            return "Account{id='" + accountId + "', encPin='" + encryptedPin +
                    "', balance=" + balance + "}";
        }
    }

    // --- 3. Externalizable (full control) ---
    static class Product implements Externalizable {
        // Externalizable REQUIRES public no-arg constructor
        private String name;
        private double price;
        private String internalCode;  // won't be serialized

        public Product() {}  // required for Externalizable

        public Product(String name, double price, String internalCode) {
            this.name = name;
            this.price = price;
            this.internalCode = internalCode;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            // Explicitly choose what to serialize
            out.writeUTF(name);
            out.writeDouble(price);
            // internalCode intentionally omitted
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            // Must read in the SAME ORDER as writeExternal
            name = in.readUTF();
            price = in.readDouble();
            internalCode = "RESTORED_DEFAULT";
        }

        @Override
        public String toString() {
            return "Product{name='" + name + "', price=" + price +
                    ", internalCode='" + internalCode + "'}";
        }
    }

    // --- 4. Serialization Proxy Pattern (Effective Java) ---
    static class ImmutablePeriod implements Serializable {
        private static final long serialVersionUID = 4L;

        private final long start;
        private final long end;

        public ImmutablePeriod(long start, long end) {
            if (start > end) throw new IllegalArgumentException("start > end");
            this.start = start;
            this.end = end;
        }

        // Instead of serializing this object, serialize a proxy
        private Object writeReplace() {
            return new SerializationProxy(this);
        }

        // Prevent direct deserialization (attacker can't bypass the proxy)
        private void readObject(ObjectInputStream in) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required!");
        }

        @Override
        public String toString() {
            return "Period[" + start + " -> " + end + "]";
        }

        // The proxy: simple, can't be tampered with
        private static class SerializationProxy implements Serializable {
            private static final long serialVersionUID = 41L;
            private final long start;
            private final long end;

            SerializationProxy(ImmutablePeriod p) {
                this.start = p.start;
                this.end = p.end;
            }

            // On deserialization, create a new ImmutablePeriod via constructor
            // This ensures validation (start <= end) is always enforced
            private Object readResolve() {
                return new ImmutablePeriod(start, end);
            }
        }
    }

    // --- Helper: serialize to byte array ---
    static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    // --- Helper: deserialize from byte array ---
    @SuppressWarnings("unchecked")
    static <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("  Serialization Deep Dive");
        System.out.println("========================================\n");

        // --- 1. Basic serialization with transient ---
        System.out.println("=== 1. Basic Serialization (transient keyword) ===");
        Employee emp = new Employee("Karan", 30, "secret123", 5000.0);
        System.out.println("Before:  " + emp);

        byte[] data = serialize(emp);
        System.out.println("Serialized size: " + data.length + " bytes");

        Employee deserialized = deserialize(data);
        System.out.println("After:   " + deserialized);
        System.out.println("Notice: password=null, bonus=0.0 (transient fields reset to defaults)\n");

        // --- 2. Custom readObject/writeObject ---
        System.out.println("=== 2. Custom Serialization (writeObject/readObject) ===");
        Account account = new Account("ACC-001", "1234", 10000.0);
        System.out.println("Before:  " + account);

        byte[] accData = serialize(account);
        Account accDeserialized = deserialize(accData);
        System.out.println("After:   " + accDeserialized);
        System.out.println("Notice: encryptedPin survived (custom writeObject serialized it)\n");

        // --- 3. Externalizable ---
        System.out.println("=== 3. Externalizable (Full Control) ===");
        Product product = new Product("Laptop", 999.99, "INT-CODE-XYZ");
        System.out.println("Before:  " + product);

        byte[] prodData = serialize(product);
        Product prodDeserialized = deserialize(prodData);
        System.out.println("After:   " + prodDeserialized);
        System.out.println("Notice: internalCode is 'RESTORED_DEFAULT' (not serialized)\n");

        // --- 4. Serialization Proxy Pattern ---
        System.out.println("=== 4. Serialization Proxy Pattern ===");
        ImmutablePeriod period = new ImmutablePeriod(100, 200);
        System.out.println("Before:  " + period);

        byte[] periodData = serialize(period);
        ImmutablePeriod periodDeserialized = deserialize(periodData);
        System.out.println("After:   " + periodDeserialized);
        System.out.println("Proxy ensures constructor validation always runs on deserialization.\n");

        // --- 5. serialVersionUID mismatch demo ---
        System.out.println("=== 5. serialVersionUID ===");
        System.out.println("If serialVersionUID in serialized data != class's serialVersionUID:");
        System.out.println("  -> InvalidClassException is thrown during deserialization");
        System.out.println("Always declare serialVersionUID explicitly to control versioning.\n");

        // --- 6. Inheritance and serialization ---
        System.out.println("=== 6. Key Rules ===");
        System.out.println("1. If parent is NOT Serializable, its fields are NOT serialized.");
        System.out.println("   Parent must have a no-arg constructor (called during deserialization).");
        System.out.println("2. If parent IS Serializable, child is automatically Serializable.");
        System.out.println("3. Static fields are NEVER serialized (they belong to the class).");
        System.out.println("4. transient fields get default values after deserialization.");
        System.out.println("5. Use readResolve() for singleton preservation during deserialization.");
        System.out.println("6. Prefer JSON/Protobuf over Java serialization in production (security).");
    }
}
