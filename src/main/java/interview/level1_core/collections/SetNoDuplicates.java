package interview.level1_core.collections;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Q22. How does a Set ensure no duplicates?
 *
 * HashSet uses HashMap internally:
 *   - Each element is stored as a KEY in the HashMap (value is a dummy constant)
 *   - When you add(element):
 *     1. Computes hashCode() → determines bucket
 *     2. Checks equals() against existing keys in that bucket
 *     3. If equals() returns true → element already exists, not added
 *     4. If no match → added to the set
 *
 * Therefore: equals() and hashCode() contract is CRITICAL for Sets.
 *   - If you override equals(), you MUST override hashCode()
 *   - Violating this → Set may allow duplicates!
 *
 * TreeSet:
 *   - Uses compareTo() (or Comparator) instead of hashCode()/equals()
 *   - Maintains sorted order using a Red-Black tree
 *
 * LinkedHashSet:
 *   - Like HashSet + maintains insertion order via linked list
 */
public class SetNoDuplicates {

    // Class WITHOUT proper equals/hashCode
    static class BadEmployee {
        String name;
        int id;
        BadEmployee(int id, String name) { this.id = id; this.name = name; }
        public String toString() { return "BadEmployee{" + id + ", " + name + "}"; }
    }

    // Class WITH proper equals/hashCode
    static class GoodEmployee {
        String name;
        int id;
        GoodEmployee(int id, String name) { this.id = id; this.name = name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GoodEmployee that = (GoodEmployee) o;
            return id == that.id && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }

        public String toString() { return "GoodEmployee{" + id + ", " + name + "}"; }
    }

    public static void main(String[] args) {

        // === Basic dedup with primitives/String ===
        System.out.println("=== Set with String (works correctly) ===");
        Set<String> names = new HashSet<>();
        names.add("Karan");
        names.add("John");
        names.add("Karan");  // duplicate — ignored
        System.out.println("Set: " + names);  // [Karan, John]
        System.out.println("Size: " + names.size());  // 2

        // === Without equals/hashCode — duplicates allowed! ===
        System.out.println("\n=== BAD: Without equals/hashCode ===");
        Set<BadEmployee> badSet = new HashSet<>();
        badSet.add(new BadEmployee(1, "Karan"));
        badSet.add(new BadEmployee(1, "Karan"));  // SAME data, but different object!
        System.out.println("Size (expected 1, got): " + badSet.size());  // 2!
        System.out.println("Set: " + badSet);

        // === With equals/hashCode — dedup works ===
        System.out.println("\n=== GOOD: With equals/hashCode ===");
        Set<GoodEmployee> goodSet = new HashSet<>();
        goodSet.add(new GoodEmployee(1, "Karan"));
        goodSet.add(new GoodEmployee(1, "Karan"));  // duplicate detected!
        System.out.println("Size (expected 1, got): " + goodSet.size());  // 1
        System.out.println("Set: " + goodSet);

        // === How it works internally ===
        System.out.println("\n=== Internals ===");
        GoodEmployee e1 = new GoodEmployee(1, "Karan");
        GoodEmployee e2 = new GoodEmployee(1, "Karan");
        System.out.println("e1.hashCode(): " + e1.hashCode());
        System.out.println("e2.hashCode(): " + e2.hashCode());
        System.out.println("Same hash? " + (e1.hashCode() == e2.hashCode()));
        System.out.println("equals? " + e1.equals(e2));
        System.out.println("→ HashSet recognizes them as duplicates");

        // === contains() also uses hashCode + equals ===
        System.out.println("\n=== contains() ===");
        GoodEmployee search = new GoodEmployee(1, "Karan");
        System.out.println("Set contains new GoodEmployee(1, Karan): " + goodSet.contains(search));  // true
    }
}
