package interview.level4_java9to17.optional_enhancements;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Optional Enhancements (Java 9-11)
// =============================================================================
//
// Q: "Should Optional be used as a method parameter?"
// A: "No. Optional was designed for return types to signal 'might be absent.'
//     Using it as a parameter forces callers to wrap values unnecessarily and
//     introduces ambiguity (what if someone passes null for the Optional?).
//     Use method overloading or @Nullable instead."
//
// Q: "Should you wrap a Collection in Optional?"
// A: "No. Return an empty collection instead of Optional<List<T>>. An empty
//     list already means 'no results.' Wrapping it in Optional adds a second
//     level of 'emptiness' with no benefit."
//
// Q: "What's the difference between Optional.or() and Optional.orElse()?"
// A: "orElse(value) returns the value directly (unwraps the Optional).
//     or(supplier) returns another Optional — it chains Optionals without
//     unwrapping. or() is for fallback lookups where each source might also
//     be absent."
//
// Q: "When was Optional.isEmpty() added and why?"
// A: "Java 11. It's the complement of isPresent(). Before Java 11, you had to
//     write !opt.isPresent(), which is less readable, especially in stream
//     filters."
//
// =============================================================================

import java.util.*;
import java.util.stream.Stream;

public class OptionalEnhancements {

    // Simulated data sources
    private static final Map<String, String> PRIMARY_DB = Map.of(
            "alice", "alice@primary.com",
            "bob", "bob@primary.com"
    );
    private static final Map<String, String> BACKUP_DB = Map.of(
            "charlie", "charlie@backup.com",
            "bob", "bob@backup.com"
    );
    private static final Map<String, String> CACHE = Map.of(
            "alice", "alice@cache.com"
    );

    // -------------------------------------------------------------------------
    // 1. ifPresentOrElse (Java 9) — handle both cases
    // -------------------------------------------------------------------------
    static void ifPresentOrElse() {
        System.out.println("=== 1. ifPresentOrElse (Java 9) ===");

        // BEFORE Java 9: manual if-else
        Optional<String> name = Optional.of("Alice");
        if (name.isPresent()) {
            System.out.println("  [Before] Found: " + name.get());
        } else {
            System.out.println("  [Before] Not found");
        }

        // AFTER: ifPresentOrElse — cleaner, no get()
        name.ifPresentOrElse(
                n -> System.out.println("  [After]  Found: " + n),
                () -> System.out.println("  [After]  Not found")
        );

        // Empty case
        Optional<String> empty = Optional.empty();
        empty.ifPresentOrElse(
                n -> System.out.println("  Present: " + n),
                () -> System.out.println("  Empty -> running fallback action")
        );

        // Real use case: logging
        Optional<String> userId = Optional.of("U123");
        userId.ifPresentOrElse(
                id -> System.out.println("  Processing user: " + id),
                () -> System.out.println("  WARNING: No user ID provided")
        );
    }

    // -------------------------------------------------------------------------
    // 2. Optional.or (Java 9) — fallback to another Optional
    // -------------------------------------------------------------------------
    static void optionalOr() {
        System.out.println("\n=== 2. Optional.or (Java 9) ===");

        // BEFORE: manual chaining
        String username = "charlie";
        Optional<String> email = Optional.ofNullable(PRIMARY_DB.get(username));
        if (email.isEmpty()) {
            email = Optional.ofNullable(BACKUP_DB.get(username));
        }
        System.out.println("  [Before] " + username + " -> " + email.orElse("not found"));

        // AFTER: or() chains Optional suppliers
        String user1 = "charlie";
        Optional<String> result1 = Optional.ofNullable(CACHE.get(user1))
                .or(() -> Optional.ofNullable(PRIMARY_DB.get(user1)))
                .or(() -> Optional.ofNullable(BACKUP_DB.get(user1)));
        System.out.println("  [After]  " + user1 + " -> " + result1.orElse("not found"));

        // Chain: cache -> primary -> backup -> default
        for (String user : List.of("alice", "bob", "charlie", "unknown")) {
            String found = Optional.ofNullable(CACHE.get(user))
                    .or(() -> Optional.ofNullable(PRIMARY_DB.get(user)))
                    .or(() -> Optional.ofNullable(BACKUP_DB.get(user)))
                    .orElse("no-email@default.com");
            System.out.println("  " + user + " -> " + found);
        }

        // Key difference: or() vs orElse()
        System.out.println("\n  or() returns Optional<T>  — stays in Optional chain");
        System.out.println("  orElse() returns T        — unwraps the Optional");
    }

    // -------------------------------------------------------------------------
    // 3. Optional.stream (Java 9) — bridge Optional and Stream
    // -------------------------------------------------------------------------
    static void optionalStream() {
        System.out.println("\n=== 3. Optional.stream (Java 9) ===");

        // BEFORE: flatMap with manual conversion
        List<Optional<String>> optionals = List.of(
                Optional.of("Alice"),
                Optional.empty(),
                Optional.of("Bob"),
                Optional.empty(),
                Optional.of("Charlie")
        );

        // Old way
        List<String> oldWay = optionals.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        System.out.println("  [Before] filter+get: " + oldWay);

        // AFTER: Optional.stream() converts Optional to 0-or-1 element stream
        List<String> newWay = optionals.stream()
                .flatMap(Optional::stream)  // empty -> 0 elements, present -> 1 element
                .toList();
        System.out.println("  [After]  flatMap+stream: " + newWay);

        // Real use case: map lookup with stream
        List<String> users = List.of("alice", "unknown1", "bob", "unknown2", "charlie");

        List<String> emails = users.stream()
                .map(u -> Optional.ofNullable(PRIMARY_DB.get(u))
                        .or(() -> Optional.ofNullable(BACKUP_DB.get(u))))
                .flatMap(Optional::stream)
                .toList();
        System.out.println("  Resolved emails: " + emails);
    }

    // -------------------------------------------------------------------------
    // 4. Optional.isEmpty (Java 11)
    // -------------------------------------------------------------------------
    static void optionalIsEmpty() {
        System.out.println("\n=== 4. Optional.isEmpty (Java 11) ===");

        Optional<String> present = Optional.of("Hello");
        Optional<String> absent = Optional.empty();

        // BEFORE: !isPresent()
        System.out.println("  [Before] !present.isPresent(): " + !present.isPresent());
        System.out.println("  [Before] !absent.isPresent():  " + !absent.isPresent());

        // AFTER: isEmpty()
        System.out.println("  [After]  present.isEmpty(): " + present.isEmpty());
        System.out.println("  [After]  absent.isEmpty():  " + absent.isEmpty());

        // Real use case: guard clauses
        Optional<String> config = Optional.ofNullable(System.getenv("MY_CONFIG"));
        if (config.isEmpty()) {
            System.out.println("  Config not set — using defaults");
        }

        // In stream filters
        List<Optional<String>> opts = List.of(
                Optional.of("A"), Optional.empty(), Optional.of("B"), Optional.empty()
        );
        long emptyCount = opts.stream().filter(Optional::isEmpty).count();
        System.out.println("  Empty optionals: " + emptyCount);
    }

    // -------------------------------------------------------------------------
    // 5. Anti-patterns — what NOT to do with Optional
    // -------------------------------------------------------------------------
    static void antiPatterns() {
        System.out.println("\n=== 5. Optional Anti-Patterns ===");

        System.out.println("  ANTI-PATTERN 1: Optional as method parameter");
        System.out.println("  BAD:  void process(Optional<String> name) { ... }");
        System.out.println("  GOOD: void process(String name) { ... }");
        System.out.println("        void process() { ... }  // overload for absent case");
        System.out.println();

        System.out.println("  ANTI-PATTERN 2: Optional of collection");
        System.out.println("  BAD:  Optional<List<String>> getNames() { ... }");
        System.out.println("  GOOD: List<String> getNames() { return List.of(); }");
        System.out.println();

        System.out.println("  ANTI-PATTERN 3: Optional as class field");
        System.out.println("  BAD:  private Optional<String> middleName;");
        System.out.println("  GOOD: private String middleName; // nullable");
        System.out.println("  WHY:  Optional is not Serializable, adds overhead");
        System.out.println();

        System.out.println("  ANTI-PATTERN 4: isPresent() + get()");
        // BAD
        Optional<String> opt = Optional.of("value");
        if (opt.isPresent()) {
            String val = opt.get(); // never do this
            System.out.println("  BAD:  if (opt.isPresent()) opt.get() -> " + val);
        }
        // GOOD
        opt.ifPresent(val -> System.out.println("  GOOD: opt.ifPresent(v -> ...) -> " + val));
        String result = opt.orElse("default");
        System.out.println("  GOOD: opt.orElse(\"default\") -> " + result);

        System.out.println();
        System.out.println("  ANTI-PATTERN 5: Optional.of(nullable)");
        System.out.println("  BAD:  Optional.of(possiblyNull)   -> NPE if null");
        System.out.println("  GOOD: Optional.ofNullable(value)  -> handles null safely");

        System.out.println();
        System.out.println("  ANTI-PATTERN 6: Creating Optional just to chain");
        System.out.println("  BAD:  Optional.ofNullable(x).orElse(y)");
        System.out.println("  GOOD: x != null ? x : y   (or Objects.requireNonNullElse)");
    }

    // -------------------------------------------------------------------------
    // 6. Correct Optional usage patterns
    // -------------------------------------------------------------------------
    static void correctPatterns() {
        System.out.println("\n=== 6. Correct Optional Patterns ===");

        // Pattern 1: Method return type
        System.out.println("  Pattern 1: Optional as return type");
        Optional<String> found = findUserEmail("alice");
        found.ifPresentOrElse(
                email -> System.out.println("    Found: " + email),
                () -> System.out.println("    Not found")
        );

        // Pattern 2: map/flatMap chain
        System.out.println("\n  Pattern 2: map/flatMap chain");
        Optional<String> domain = findUserEmail("alice")
                .map(email -> email.split("@")[1])
                .map(String::toUpperCase);
        System.out.println("    Domain: " + domain.orElse("unknown"));

        // Pattern 3: orElseGet with lazy default
        System.out.println("\n  Pattern 3: orElseGet (lazy)");
        String email = findUserEmail("unknown")
                .orElseGet(() -> {
                    System.out.println("    Computing default email...");
                    return "default@example.com";
                });
        System.out.println("    Email: " + email);

        // Pattern 4: orElseThrow
        System.out.println("\n  Pattern 4: orElseThrow");
        try {
            String required = findUserEmail("unknown")
                    .orElseThrow(() -> new IllegalStateException("Email is required"));
        } catch (IllegalStateException e) {
            System.out.println("    Caught: " + e.getMessage());
        }

        // Pattern 5: filter + map
        System.out.println("\n  Pattern 5: filter + map");
        Optional<String> validEmail = findUserEmail("alice")
                .filter(e -> e.contains("@"))
                .map(String::toLowerCase);
        System.out.println("    Valid email: " + validEmail.orElse("invalid"));
    }

    private static Optional<String> findUserEmail(String username) {
        return Optional.ofNullable(PRIMARY_DB.get(username));
    }

    // -------------------------------------------------------------------------
    // 7. Complete API reference table
    // -------------------------------------------------------------------------
    static void apiReference() {
        System.out.println("\n=== 7. Optional API by Java Version ===");
        System.out.println("  ┌──────────────────────────┬─────────┬───────────────────────────────┐");
        System.out.println("  │ Method                   │ Version │ Purpose                       │");
        System.out.println("  ├──────────────────────────┼─────────┼───────────────────────────────┤");
        System.out.println("  │ of / ofNullable / empty  │ Java 8  │ Create Optional               │");
        System.out.println("  │ isPresent / get          │ Java 8  │ Check / unwrap                │");
        System.out.println("  │ ifPresent                │ Java 8  │ Execute if present            │");
        System.out.println("  │ map / flatMap / filter   │ Java 8  │ Transform                     │");
        System.out.println("  │ orElse / orElseGet       │ Java 8  │ Default value                 │");
        System.out.println("  │ orElseThrow              │ Java 8  │ Throw if absent               │");
        System.out.println("  ├──────────────────────────┼─────────┼───────────────────────────────┤");
        System.out.println("  │ ifPresentOrElse          │ Java 9  │ Handle both cases             │");
        System.out.println("  │ or                       │ Java 9  │ Fallback Optional supplier    │");
        System.out.println("  │ stream                   │ Java 9  │ Convert to 0/1 element stream │");
        System.out.println("  ├──────────────────────────┼─────────┼───────────────────────────────┤");
        System.out.println("  │ orElseThrow (no-arg)     │ Java 10 │ Throws NoSuchElementException │");
        System.out.println("  ├──────────────────────────┼─────────┼───────────────────────────────┤");
        System.out.println("  │ isEmpty                  │ Java 11 │ Complement of isPresent       │");
        System.out.println("  └──────────────────────────┴─────────┴───────────────────────────────┘");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Optional Enhancements: Java 9-11              ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        ifPresentOrElse();
        optionalOr();
        optionalStream();
        optionalIsEmpty();
        antiPatterns();
        correctPatterns();
        apiReference();
    }
}
