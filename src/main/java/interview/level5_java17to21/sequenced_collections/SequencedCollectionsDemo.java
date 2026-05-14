package interview.level5_java17to21.sequenced_collections;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: Sequenced Collections — Java 21
// =====================================================================
//
// Q: "What gap did SequencedCollection fill?"
// A: "Before Java 21, there was no common interface for collections with
//     a defined encounter order. LinkedHashSet, LinkedHashMap, ArrayList,
//     and TreeSet all maintain order, but had no shared API for accessing
//     the first/last element or iterating in reverse. You had to use
//     different methods on different types:
//       - List: list.get(0), list.get(list.size()-1)
//       - SortedSet: set.first(), set.last()
//       - Deque: deque.getFirst(), deque.getLast()
//       - LinkedHashMap: NO direct way to get first/last entry!
//     SequencedCollection unifies this with getFirst(), getLast(), and
//     reversed()."
//
// Q: "What are the three new interfaces?"
// A: "1) SequencedCollection<E> extends Collection<E>
//        → addFirst, addLast, getFirst, getLast, removeFirst, removeLast,
//          reversed()
//     2) SequencedSet<E> extends Set<E>, SequencedCollection<E>
//        → same methods, but addFirst/addLast throw if element exists
//     3) SequencedMap<K,V> extends Map<K,V>
//        → firstEntry, lastEntry, pollFirstEntry, pollLastEntry,
//          putFirst, putLast, reversed(), sequencedKeySet(),
//          sequencedValues(), sequencedEntrySet()"
//
// Q: "Which existing classes now implement these interfaces?"
// A: "ArrayList, LinkedList → SequencedCollection
//     LinkedHashSet, TreeSet → SequencedSet
//     LinkedHashMap, TreeMap → SequencedMap
//     The JDK retrofitted existing classes to implement these interfaces."
//
// Q: "What does reversed() return?"
// A: "A reversed VIEW of the collection (not a copy). Modifications to
//     the reversed view are reflected in the original and vice versa.
//     This is similar to Collections.unmodifiableList but for ordering."
//
// COMPILE: javac SequencedCollectionsDemo.java
// RUN:     java SequencedCollectionsDemo
// =====================================================================

import java.util.*;

public class SequencedCollectionsDemo {

    // ---------------------------------------------------------------
    // 1. SequencedCollection basics (ArrayList, LinkedList)
    // ---------------------------------------------------------------
    static void sequencedCollectionBasics() {
        System.out.println("=== 1. SequencedCollection Basics ===\n");

        // ArrayList implements SequencedCollection
        SequencedCollection<String> list = new ArrayList<>(
                List.of("Alpha", "Beta", "Gamma", "Delta")
        );

        System.out.println("  Collection:   " + list);
        System.out.println("  getFirst():   " + list.getFirst());
        System.out.println("  getLast():    " + list.getLast());

        // addFirst / addLast
        list.addFirst("Zero");
        list.addLast("Omega");
        System.out.println("  After add:    " + list);

        // removeFirst / removeLast
        list.removeFirst();
        list.removeLast();
        System.out.println("  After remove: " + list);

        // reversed() — returns a reversed VIEW
        SequencedCollection<String> reversed = list.reversed();
        System.out.println("  reversed():   " + reversed);
        System.out.println("  Original:     " + list + " (unchanged)");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 2. SequencedSet (LinkedHashSet, TreeSet)
    // ---------------------------------------------------------------
    static void sequencedSetDemo() {
        System.out.println("=== 2. SequencedSet ===\n");

        // LinkedHashSet maintains insertion order
        SequencedSet<String> linkedSet = new LinkedHashSet<>();
        linkedSet.add("Java");
        linkedSet.add("Kotlin");
        linkedSet.add("Scala");
        linkedSet.add("Groovy");

        System.out.println("  LinkedHashSet:  " + linkedSet);
        System.out.println("  getFirst():     " + linkedSet.getFirst());
        System.out.println("  getLast():      " + linkedSet.getLast());
        System.out.println("  reversed():     " + linkedSet.reversed());

        // TreeSet maintains sorted order
        SequencedSet<Integer> treeSet = new TreeSet<>(Set.of(50, 10, 30, 20, 40));

        System.out.println("\n  TreeSet:        " + treeSet);
        System.out.println("  getFirst():     " + treeSet.getFirst() + " (smallest)");
        System.out.println("  getLast():      " + treeSet.getLast() + " (largest)");
        System.out.println("  reversed():     " + treeSet.reversed());
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 3. SequencedMap (LinkedHashMap, TreeMap)
    // ---------------------------------------------------------------
    static void sequencedMapDemo() {
        System.out.println("=== 3. SequencedMap ===\n");

        // LinkedHashMap — BEFORE Java 21, there was NO direct way
        // to get the first or last entry!
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("Alice", 90);
        map.put("Bob", 85);
        map.put("Charlie", 92);
        map.put("Diana", 88);

        System.out.println("  Map:             " + map);
        System.out.println("  firstEntry():    " + map.firstEntry());
        System.out.println("  lastEntry():     " + map.lastEntry());

        // putFirst / putLast — reorder an existing key
        map.putFirst("Diana", 88);
        System.out.println("  After putFirst(Diana): " + map);

        map.putLast("Alice", 90);
        System.out.println("  After putLast(Alice):  " + map);

        // pollFirstEntry / pollLastEntry
        Map.Entry<String, Integer> first = map.pollFirstEntry();
        System.out.println("  pollFirstEntry(): " + first + " → " + map);

        // Sequenced views
        System.out.println("  sequencedKeySet():   " + map.sequencedKeySet());
        System.out.println("  sequencedValues():   " + map.sequencedValues());
        System.out.println("  sequencedEntrySet(): " + map.sequencedEntrySet());

        // Reversed map
        SequencedMap<String, Integer> reversedMap = map.reversed();
        System.out.println("  reversed():          " + reversedMap);
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 4. The problem SequencedCollection solved — BEFORE vs AFTER
    // ---------------------------------------------------------------
    static void beforeAfterComparison() {
        System.out.println("=== 4. Before vs After — The Inconsistency Problem ===\n");

        System.out.println("  Getting the FIRST element:");
        System.out.println("  ┌──────────────────────┬────────────────────────┬───────────────────┐");
        System.out.println("  │ Collection           │ Before Java 21         │ Java 21           │");
        System.out.println("  ├──────────────────────┼────────────────────────┼───────────────────┤");
        System.out.println("  │ List                 │ list.get(0)            │ list.getFirst()   │");
        System.out.println("  │ Deque                │ deque.getFirst()       │ deque.getFirst()  │");
        System.out.println("  │ SortedSet            │ set.first()            │ set.getFirst()    │");
        System.out.println("  │ LinkedHashSet        │ set.iterator().next()  │ set.getFirst()    │");
        System.out.println("  │ LinkedHashMap        │ ??? (no direct way)    │ map.firstEntry()  │");
        System.out.println("  └──────────────────────┴────────────────────────┴───────────────────┘");
        System.out.println();

        System.out.println("  Getting the LAST element:");
        System.out.println("  ┌──────────────────────┬──────────────────────────────┬───────────────────┐");
        System.out.println("  │ Collection           │ Before Java 21               │ Java 21           │");
        System.out.println("  ├──────────────────────┼──────────────────────────────┼───────────────────┤");
        System.out.println("  │ List                 │ list.get(list.size()-1)       │ list.getLast()    │");
        System.out.println("  │ Deque                │ deque.getLast()               │ deque.getLast()   │");
        System.out.println("  │ SortedSet            │ set.last()                   │ set.getLast()     │");
        System.out.println("  │ LinkedHashSet        │ ??? (iterate all!)           │ set.getLast()     │");
        System.out.println("  │ LinkedHashMap        │ ??? (iterate all!)           │ map.lastEntry()   │");
        System.out.println("  └──────────────────────┴──────────────────────────────┴───────────────────┘");
        System.out.println();

        System.out.println("  Reversing iteration:");
        System.out.println("  ┌──────────────────────┬──────────────────────────────┬───────────────────┐");
        System.out.println("  │ Collection           │ Before Java 21               │ Java 21           │");
        System.out.println("  ├──────────────────────┼──────────────────────────────┼───────────────────┤");
        System.out.println("  │ List                 │ listIterator(size)           │ list.reversed()   │");
        System.out.println("  │ Deque                │ descendingIterator()         │ deque.reversed()  │");
        System.out.println("  │ NavigableSet         │ descendingSet()              │ set.reversed()    │");
        System.out.println("  │ LinkedHashSet        │ ??? (no way!)               │ set.reversed()    │");
        System.out.println("  │ LinkedHashMap        │ ??? (no way!)               │ map.reversed()    │");
        System.out.println("  └──────────────────────┴──────────────────────────────┴───────────────────┘");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 5. reversed() is a VIEW, not a copy
    // ---------------------------------------------------------------
    static void reversedViewDemo() {
        System.out.println("=== 5. reversed() Returns a View, Not a Copy ===\n");

        List<String> original = new ArrayList<>(List.of("A", "B", "C"));
        SequencedCollection<String> reversed = original.reversed();

        System.out.println("  Original:  " + original);
        System.out.println("  Reversed:  " + reversed);

        // Modify via reversed view
        reversed.addFirst("Z"); // adds to END of original
        System.out.println("\n  After reversed.addFirst(\"Z\"):");
        System.out.println("  Original:  " + original);
        System.out.println("  Reversed:  " + reversed);

        // Modify original
        original.addFirst("START");
        System.out.println("\n  After original.addFirst(\"START\"):");
        System.out.println("  Original:  " + original);
        System.out.println("  Reversed:  " + reversed);

        System.out.println("\n  → Changes propagate both ways (it's a view).\n");
    }

    // ---------------------------------------------------------------
    // 6. Collections utility methods
    // ---------------------------------------------------------------
    static void collectionsUtilities() {
        System.out.println("=== 6. Unmodifiable Sequenced Wrappers ===\n");

        // New factory methods for unmodifiable sequenced collections
        SequencedCollection<String> seq = new ArrayList<>(List.of("X", "Y", "Z"));
        SequencedCollection<String> unmodifiable = Collections.unmodifiableSequencedCollection(seq);

        System.out.println("  Unmodifiable: " + unmodifiable);
        System.out.println("  getFirst():   " + unmodifiable.getFirst());
        System.out.println("  getLast():    " + unmodifiable.getLast());

        try {
            unmodifiable.addFirst("A");
        } catch (UnsupportedOperationException e) {
            System.out.println("  addFirst():   UnsupportedOperationException (immutable!)");
        }

        // Same for Set and Map
        SequencedSet<String> unmodSet = Collections.unmodifiableSequencedSet(
                new LinkedHashSet<>(List.of("P", "Q", "R")));
        System.out.println("\n  Unmodifiable SequencedSet: " + unmodSet);
        System.out.println("  reversed():               " + unmodSet.reversed());

        SequencedMap<String, Integer> unmodMap = Collections.unmodifiableSequencedMap(
                new LinkedHashMap<>(Map.of("a", 1, "b", 2)));
        System.out.println("\n  Unmodifiable SequencedMap: " + unmodMap);
        System.out.println("  firstEntry():             " + unmodMap.firstEntry());
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 7. Interface hierarchy
    // ---------------------------------------------------------------
    static void interfaceHierarchy() {
        System.out.println("=== 7. New Interface Hierarchy ===\n");
        System.out.println("                    Iterable");
        System.out.println("                       |");
        System.out.println("                   Collection");
        System.out.println("                       |");
        System.out.println("              SequencedCollection  <── NEW");
        System.out.println("               /              \\");
        System.out.println("         List          SequencedSet  <── NEW");
        System.out.println("                       /          \\");
        System.out.println("                   SortedSet     Set");
        System.out.println("                      |");
        System.out.println("                NavigableSet");
        System.out.println();
        System.out.println("                     Map");
        System.out.println("                      |");
        System.out.println("                SequencedMap  <── NEW");
        System.out.println("                      |");
        System.out.println("                  SortedMap");
        System.out.println("                      |");
        System.out.println("                NavigableMap");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   Sequenced Collections Demo — Java 21              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        sequencedCollectionBasics();
        sequencedSetDemo();
        sequencedMapDemo();
        beforeAfterComparison();
        reversedViewDemo();
        collectionsUtilities();
        interfaceHierarchy();

        System.out.println("=== Done ===");
    }
}
