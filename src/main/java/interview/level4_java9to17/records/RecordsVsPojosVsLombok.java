package interview.level4_java9to17.records;

/**
 * Q3. What is the difference between Records, POJOs, and Lombok?
 *
 * | Feature           | POJO (manual)      | Lombok              | Record (Java 16+)    |
 * |------------------|--------------------|---------------------|---------------------|
 * | Boilerplate      | High               | Low (@Data)          | None                 |
 * | Mutability       | Mutable            | Mutable              | Immutable            |
 * | equals/hashCode  | Manual             | Generated            | Auto-generated       |
 * | toString         | Manual             | Generated            | Auto-generated       |
 * | Constructor      | Manual             | @AllArgsConstructor  | Canonical constructor|
 * | Getters          | getXxx()           | getXxx()             | xxx() (no get prefix)|
 * | Setters          | setXxx()           | setXxx()             | None (immutable)     |
 * | Inheritance      | Can extend         | Can extend           | Cannot extend        |
 * | Fields           | Any                | Any                  | All final            |
 * | Dependency       | None               | Annotation processor | JDK 16+              |
 *
 * Use Record when: simple immutable data carrier (DTOs, API responses, value objects)
 * Use POJO when: need mutability, inheritance, or JPA entities
 * Use Lombok when: need mutable objects with reduced boilerplate (legacy codebases)
 */
public class RecordsVsPojosVsLombok {

    // === 1. Traditional POJO — lots of boilerplate ===
    static class PersonPojo {
        private String name;
        private int age;

        public PersonPojo(String name, int age) { this.name = name; this.age = age; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PersonPojo p)) return false;
            return age == p.age && java.util.Objects.equals(name, p.name);
        }
        @Override
        public int hashCode() { return java.util.Objects.hash(name, age); }
        @Override
        public String toString() { return "PersonPojo{name='" + name + "', age=" + age + "}"; }
    }
    // That's ~20 lines for 2 fields!

    // === 2. Lombok equivalent (conceptual — requires dependency) ===
    // @Data  // generates getters, setters, equals, hashCode, toString
    // @AllArgsConstructor
    // public class PersonLombok {
    //     private String name;
    //     private int age;
    // }
    // Just 4 lines! But requires Lombok annotation processor.

    // === 3. Record — zero boilerplate ===
    record PersonRecord(String name, int age) {
        // That's it! Auto-generates:
        // - canonical constructor
        // - name() and age() accessor methods (no "get" prefix)
        // - equals(), hashCode(), toString()
        // - All fields are final (immutable)

        // You CAN add custom methods
        String greeting() { return "Hi, I'm " + name + ", age " + age; }

        // Compact constructor for validation
        PersonRecord {
            if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
        }
    }

    public static void main(String[] args) {

        // === POJO — mutable ===
        System.out.println("=== POJO ===");
        PersonPojo pojo = new PersonPojo("Karan", 30);
        pojo.setAge(31);  // mutable!
        System.out.println(pojo);

        // === Record — immutable ===
        System.out.println("\n=== Record ===");
        PersonRecord record = new PersonRecord("Karan", 30);
        // record.age = 31;  // COMPILE ERROR — fields are final
        System.out.println(record);
        System.out.println("Name: " + record.name());    // no "get" prefix
        System.out.println("Greeting: " + record.greeting());

        // Equals — based on all fields
        PersonRecord same = new PersonRecord("Karan", 30);
        System.out.println("Equals: " + record.equals(same));  // true

        // Compact constructor validation
        try {
            new PersonRecord("Test", -1);
        } catch (IllegalArgumentException e) {
            System.out.println("Validation: " + e.getMessage());
        }

        // === Records cannot extend classes ===
        System.out.println("\n=== Limitations of Records ===");
        System.out.println("❌ Cannot extend a class (implicitly extends java.lang.Record)");
        System.out.println("❌ Cannot be abstract");
        System.out.println("❌ All fields are final (immutable)");
        System.out.println("❌ Cannot declare instance fields (only in header)");
        System.out.println("✅ Can implement interfaces");
        System.out.println("✅ Can have static fields and methods");
        System.out.println("✅ Can have custom methods");
        System.out.println("✅ Can have compact constructors for validation");
    }
}
