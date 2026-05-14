package interview.level1_core.immutability;

// LEVEL: Core Java (Mid-Level)
//
// ==================== INTERVIEW Q&A ====================
// Q: How do you create an immutable class in Java?
// A: Follow these 5 rules:
//    1. Make the class final (prevent subclassing).
//    2. Make all fields private and final.
//    3. Don't provide setter methods.
//    4. Make defensive copies of mutable fields in the constructor.
//    5. Return defensive copies from getter methods for mutable fields.
//
// Q: Why is immutability important for concurrency?
// A: Immutable objects are inherently thread-safe — no synchronization needed.
//    Their state cannot change after construction, so multiple threads can share them
//    freely without locks, volatile, or synchronized blocks.
//
// Q: What is a defensive copy and why is it needed?
// A: A defensive copy is creating a new copy of a mutable object rather than storing/
//    returning the original reference. Without it, callers can modify the internal state
//    of your "immutable" object through the original reference.
//    Example: If you store a Date field without copying, the caller can call setTime() on it.
//
// Q: Name some immutable classes in the JDK.
// A: String, Integer (all wrapper classes), BigInteger, BigDecimal, LocalDate,
//    LocalTime, LocalDateTime, Duration, Period, Optional, Path.
//
// Q: Can an immutable class have mutable fields?
// A: Yes, but you MUST make defensive copies in both the constructor and getters.
//    Or use unmodifiable wrappers (Collections.unmodifiableList, List.copyOf in Java 10+).
//
// Q: What is the difference between unmodifiable and immutable?
// A: Collections.unmodifiableList() creates a VIEW — if the original list changes, the view
//    changes too. List.copyOf() (Java 10+) creates a true immutable copy.
// ========================================================

import java.util.*;

public class ImmutableClassRecipe {

    // ===================================================
    // STEP-BY-STEP: Building an Immutable Class
    // ===================================================

    // Step 1: Make the class FINAL (prevent subclassing)
    static final class ImmutableEmployee {
        // Step 2: All fields PRIVATE and FINAL
        private final String name;
        private final int age;
        private final List<String> skills;       // mutable field!
        private final Date joinDate;             // mutable field!
        private final Address address;           // mutable field!

        // Step 3: Constructor with defensive copies of mutable parameters
        public ImmutableEmployee(String name, int age, List<String> skills,
                                 Date joinDate, Address address) {
            this.name = name;  // String is already immutable — no copy needed
            this.age = age;    // primitives are inherently immutable

            // DEFENSIVE COPY: create a new ArrayList, don't store the original reference
            // If we stored the original, caller could modify it after construction
            this.skills = new ArrayList<>(skills);

            // DEFENSIVE COPY: Date is mutable, so copy it
            this.joinDate = new Date(joinDate.getTime());

            // DEFENSIVE COPY: Address is mutable, so deep copy it
            this.address = new Address(address.getCity(), address.getState());
        }

        // Step 4: NO setter methods — fields are final and cannot be reassigned

        // Step 5: Getters return DEFENSIVE COPIES of mutable fields
        public String getName() {
            return name;  // String is immutable — safe to return directly
        }

        public int getAge() {
            return age;  // primitive — safe
        }

        public List<String> getSkills() {
            // Option A: Return unmodifiable view
            // return Collections.unmodifiableList(skills);

            // Option B: Return a new copy (truly defensive)
            return new ArrayList<>(skills);
        }

        public Date getJoinDate() {
            // Return a copy — caller can't modify our internal Date
            return new Date(joinDate.getTime());
        }

        public Address getAddress() {
            // Return a copy — caller can't modify our internal Address
            return new Address(address.getCity(), address.getState());
        }

        @Override
        public String toString() {
            return "ImmutableEmployee{name='" + name + "', age=" + age +
                    ", skills=" + skills + ", joinDate=" + joinDate +
                    ", address=" + address + "}";
        }
    }

    // Mutable class used as a field in ImmutableEmployee
    static class Address {
        private String city;
        private String state;

        public Address(String city, String state) {
            this.city = city;
            this.state = state;
        }

        public String getCity() { return city; }
        public String getState() { return state; }

        public void setCity(String city) { this.city = city; }  // mutable!

        @Override
        public String toString() {
            return city + ", " + state;
        }
    }

    // ===================================================
    // MODERN APPROACH: Using Java Records (Java 16+)
    // ===================================================
    // Records are implicitly final with private final fields.
    // But they DON'T make defensive copies automatically — you must do it yourself.
    // record ImmutablePoint(int x, int y) {}  // truly immutable (only primitives)

    // ===================================================
    // USING Collections.unmodifiableList vs List.copyOf
    // ===================================================
    static void unmodifiableVsCopyOf() {
        System.out.println("=== Unmodifiable vs CopyOf ===");
        List<String> original = new ArrayList<>(Arrays.asList("A", "B", "C"));

        // unmodifiableList: creates a VIEW of the original
        List<String> unmodifiable = Collections.unmodifiableList(original);

        // List.copyOf (Java 10+): creates a true COPY
        List<String> copied = List.copyOf(original);

        // Modify original
        original.add("D");

        System.out.println("Original (after add D): " + original);
        System.out.println("Unmodifiable VIEW:      " + unmodifiable);  // also shows D!
        System.out.println("List.copyOf:            " + copied);        // still A,B,C

        // Try to modify the unmodifiable list
        try {
            unmodifiable.add("E");
        } catch (UnsupportedOperationException e) {
            System.out.println("unmodifiable.add() -> UnsupportedOperationException");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Immutable Class Recipe");
        System.out.println("========================================\n");

        // --- 1. Create an immutable employee ---
        System.out.println("=== 1. Creating Immutable Object ===");
        List<String> skills = new ArrayList<>(Arrays.asList("Java", "Spring"));
        Date joinDate = new Date();
        Address address = new Address("San Francisco", "CA");

        ImmutableEmployee emp = new ImmutableEmployee("Karan", 30, skills, joinDate, address);
        System.out.println("Created: " + emp);
        System.out.println();

        // --- 2. Prove that modifying original references doesn't affect the object ---
        System.out.println("=== 2. Defensive Copy in Constructor ===");
        System.out.println("Modifying original list, date, and address...");
        skills.add("Python");                       // modify original list
        joinDate.setTime(0);                        // modify original date
        address.setCity("New York");                // modify original address

        System.out.println("Original skills: " + skills);
        System.out.println("Original date: " + joinDate);
        System.out.println("Original address: " + address);
        System.out.println("Employee skills: " + emp.getSkills() + " (UNCHANGED!)");
        System.out.println("Employee date: " + emp.getJoinDate() + " (UNCHANGED!)");
        System.out.println("Employee address: " + emp.getAddress() + " (UNCHANGED!)");
        System.out.println();

        // --- 3. Prove that modifying returned values doesn't affect the object ---
        System.out.println("=== 3. Defensive Copy in Getter ===");
        List<String> returnedSkills = emp.getSkills();
        returnedSkills.add("Hacking!");  // try to modify via getter

        Date returnedDate = emp.getJoinDate();
        returnedDate.setTime(0);         // try to modify via getter

        Address returnedAddr = emp.getAddress();
        returnedAddr.setCity("Hacked!");  // try to modify via getter

        System.out.println("Modified returned skills: " + returnedSkills);
        System.out.println("Employee's actual skills: " + emp.getSkills() + " (SAFE!)");
        System.out.println("Modified returned date: " + returnedDate);
        System.out.println("Employee's actual date: " + emp.getJoinDate() + " (SAFE!)");
        System.out.println("Modified returned address: " + returnedAddr);
        System.out.println("Employee's actual address: " + emp.getAddress() + " (SAFE!)");
        System.out.println();

        // --- 4. Unmodifiable vs CopyOf ---
        unmodifiableVsCopyOf();

        // --- 5. Common mistakes ---
        System.out.println("=== 5. Common Mistakes to Avoid ===");
        System.out.println("MISTAKE 1: Storing mutable arguments directly");
        System.out.println("  BAD:  this.list = list;");
        System.out.println("  GOOD: this.list = new ArrayList<>(list);");
        System.out.println();
        System.out.println("MISTAKE 2: Returning mutable fields directly");
        System.out.println("  BAD:  return this.list;");
        System.out.println("  GOOD: return new ArrayList<>(this.list);");
        System.out.println("  GOOD: return Collections.unmodifiableList(this.list);");
        System.out.println();
        System.out.println("MISTAKE 3: Forgetting to make the class final");
        System.out.println("  A subclass could add mutable state or override methods.");
        System.out.println();
        System.out.println("MISTAKE 4: Using unmodifiableList() without copying first");
        System.out.println("  It's a VIEW — original list changes are reflected!");
        System.out.println();

        // --- 6. Benefits summary ---
        System.out.println("=== 6. Why Immutability Matters ===");
        System.out.println("1. Thread safety: No synchronization needed");
        System.out.println("2. HashMap keys: Safe to use as keys (hashCode won't change)");
        System.out.println("3. Caching: Can be freely shared and cached");
        System.out.println("4. Simplicity: No defensive programming for callers");
        System.out.println("5. Security: Can't be tampered with after creation");
    }
}
