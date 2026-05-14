package interview.level5_java17to21.string_templates;

// LEVEL: Staff
//
// =====================================================================
// INTERVIEW Q&A: String Templates — Java 21 (Preview, later removed)
// =====================================================================
//
// Q: "What are string template processors?"
// A: "String templates (JEP 430, preview in Java 21) allow embedding
//     expressions directly in strings using a template processor:
//       STR.\"Hello, \{name}! You are \{age} years old.\"
//     The STR processor performs simple interpolation. FMT adds
//     format specifiers. Custom processors can do validation, SQL
//     injection prevention, etc."
//
// Q: "Why were string templates proposed?"
// A: "Java's string handling was verbose compared to other languages.
//     Options were: concatenation (+), String.format(), StringBuilder,
//     MessageFormat. All are error-prone (wrong argument order) or
//     verbose. Templates provide type-safe, readable interpolation."
//
// Q: "What happened to string templates?"
// A: "String templates were preview in Java 21 (JEP 430) and Java 22
//     (JEP 459, second preview). They were WITHDRAWN/removed in Java 23
//     because the design was deemed too complex. The feature may return
//     in a future release with a simpler design. For now, use
//     String.formatted() or String.format() as the best alternatives."
//
// Q: "What are the current best practices for string formatting?"
// A: "1) String.formatted() — instance method on String (Java 15+):
//        'Hello, %s! Age: %d'.formatted(name, age)
//     2) String.format() — static method:
//        String.format('Hello, %s! Age: %d', name, age)
//     3) StringBuilder — for loops/performance
//     4) MessageFormat — for i18n with numbered args
//     5) Text blocks (Java 15+) — for multi-line strings"
//
// COMPILE: javac StringTemplatesDemo.java
// RUN:     java StringTemplatesDemo
//
// NOTE: This demo shows the CONCEPT of string templates and provides
//       working alternatives. The actual STR/FMT syntax requires
//       --enable-preview on Java 21/22, and may not be available on
//       later versions.
// =====================================================================

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StringTemplatesDemo {

    // ---------------------------------------------------------------
    // 1. The problem — verbose string building in Java
    // ---------------------------------------------------------------
    static void theProblem() {
        System.out.println("=== 1. The Problem: Verbose String Building ===\n");

        String name = "Karan";
        int age = 30;
        double salary = 150_000.50;

        // Way 1: Concatenation (ugly, error-prone with types)
        String s1 = "Hello, " + name + "! Age: " + age + ", Salary: $" + salary;
        System.out.println("  [concat]         " + s1);

        // Way 2: String.format (printf-style, wrong arg order = silent bug)
        String s2 = String.format("Hello, %s! Age: %d, Salary: $%.2f", name, age, salary);
        System.out.println("  [String.format]  " + s2);

        // Way 3: StringBuilder (verbose but efficient in loops)
        String s3 = new StringBuilder()
                .append("Hello, ").append(name)
                .append("! Age: ").append(age)
                .append(", Salary: $").append(salary)
                .toString();
        System.out.println("  [StringBuilder]  " + s3);

        // Way 4: MessageFormat (for i18n, numbered args)
        String s4 = MessageFormat.format("Hello, {0}! Age: {1}, Salary: ${2}",
                name, age, salary);
        System.out.println("  [MessageFormat]  " + s4);

        // Way 5: String.formatted (Java 15+, cleanest current option)
        String s5 = "Hello, %s! Age: %d, Salary: $%.2f".formatted(name, age, salary);
        System.out.println("  [.formatted()]   " + s5);

        System.out.println();
    }

    // ---------------------------------------------------------------
    // 2. What string templates WOULD look like (JEP 430 syntax)
    // ---------------------------------------------------------------
    static void stringTemplateSyntax() {
        System.out.println("=== 2. String Template Syntax (JEP 430 — Preview/Withdrawn) ===\n");

        System.out.println("  The proposed syntax (Java 21 preview):");
        System.out.println();
        System.out.println("    // STR processor — simple interpolation");
        System.out.println("    String name = \"Karan\";");
        System.out.println("    int age = 30;");
        System.out.println("    String msg = STR.\"Hello, \\{name}! Age: \\{age}\";");
        System.out.println("    // → \"Hello, Karan! Age: 30\"");
        System.out.println();
        System.out.println("    // Expressions inside \\{...}");
        System.out.println("    String calc = STR.\"2 + 3 = \\{2 + 3}\";");
        System.out.println("    // → \"2 + 3 = 5\"");
        System.out.println();
        System.out.println("    // Method calls");
        System.out.println("    String upper = STR.\"Name: \\{name.toUpperCase()}\";");
        System.out.println("    // → \"Name: KARAN\"");
        System.out.println();
        System.out.println("    // FMT processor — with format specifiers");
        System.out.println("    double salary = 150000.50;");
        System.out.println("    String fmt = FMT.\"Salary: $%.2f\\{salary}\";");
        System.out.println("    // → \"Salary: $150000.50\"");
        System.out.println();
        System.out.println("    // Multi-line with text blocks");
        System.out.println("    String json = STR.\"\"\"");
        System.out.println("        {");
        System.out.println("            \"name\": \"\\{name}\",");
        System.out.println("            \"age\": \\{age}");
        System.out.println("        }\"\"\";");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 3. Custom template processor concept
    // ---------------------------------------------------------------
    static void customProcessorConcept() {
        System.out.println("=== 3. Custom Template Processor Concept ===\n");

        System.out.println("  The most powerful idea was CUSTOM PROCESSORS:");
        System.out.println();
        System.out.println("    // SQL processor — prevents SQL injection!");
        System.out.println("    String userInput = \"Robert'; DROP TABLE users;--\";");
        System.out.println("    PreparedStatement ps = SQL.\"SELECT * FROM users WHERE name = \\{userInput}\";");
        System.out.println("    // → Produces a PreparedStatement with ? parameter");
        System.out.println("    // → userInput is NEVER concatenated into SQL string");
        System.out.println();
        System.out.println("    // JSON processor — validates JSON structure");
        System.out.println("    JsonObject json = JSON.\"{ \\\"name\\\": \\{name}, \\\"age\\\": \\{age} }\";");
        System.out.println();
        System.out.println("    // HTML processor — escapes XSS");
        System.out.println("    String html = HTML.\"<p>Hello, \\{userInput}</p>\";");
        System.out.println("    // → <p>Hello, Robert&#39;; DROP TABLE users;--</p>");
        System.out.println();
        System.out.println("  This is why templates were more than just interpolation —");
        System.out.println("  they could enforce safety at compile time.\n");
    }

    // ---------------------------------------------------------------
    // 4. Working alternatives — best practices TODAY
    // ---------------------------------------------------------------

    /**
     * Simulates a type-safe template processor using varargs.
     * This is the closest you can get without the actual API.
     */
    @FunctionalInterface
    interface TemplateProcessor<R> {
        R process(String template, Object... args);
    }

    static void workingAlternatives() {
        System.out.println("=== 4. Working Alternatives (Java 15-21+) ===\n");

        String name = "Karan";
        int age = 30;
        double salary = 150_000.50;
        LocalDate today = LocalDate.now();

        // Best alternative 1: String.formatted() (Java 15+)
        System.out.println("  --- String.formatted() (RECOMMENDED) ---");
        String msg1 = "Hello, %s! Age: %d, Salary: $%,.2f".formatted(name, age, salary);
        System.out.println("  " + msg1);

        // Multi-line with text blocks + formatted
        String json = """
                {
                    "name": "%s",
                    "age": %d,
                    "salary": %.2f,
                    "date": "%s"
                }""".formatted(name, age, salary, today);
        System.out.println("  JSON:\n" + json.indent(2));

        // Best alternative 2: Custom helper for named templates
        System.out.println("  --- Custom Named Template Helper ---");
        String result = namedTemplate(
                "Hello, ${name}! You joined on ${date}.",
                Map.of("name", name, "date", today.toString())
        );
        System.out.println("  " + result);
        System.out.println();

        // Best alternative 3: Simulated processor pattern
        System.out.println("  --- Simulated Template Processor ---");

        // SQL-safe processor simulation
        TemplateProcessor<String> SQL_SAFE = (template, args) -> {
            String safe = template;
            for (int i = 0; i < args.length; i++) {
                String sanitized = args[i].toString()
                        .replace("'", "''")   // escape single quotes
                        .replace(";", "");     // strip semicolons
                safe = safe.replaceFirst("\\?", sanitized);
            }
            return safe;
        };

        String malicious = "Robert'; DROP TABLE users;--";
        String safeSql = SQL_SAFE.process(
                "SELECT * FROM users WHERE name = '?'", malicious);
        System.out.println("  Input:  " + malicious);
        System.out.println("  SQL:    " + safeSql);
        System.out.println("  → Injection prevented!\n");
    }

    /**
     * Named template implementation — replaces ${key} with values.
     * A practical alternative to string templates.
     */
    static String namedTemplate(String template, Map<String, String> values) {
        String result = template;
        for (var entry : values.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    // ---------------------------------------------------------------
    // 5. Performance comparison of string building approaches
    // ---------------------------------------------------------------
    static void performanceComparison() {
        System.out.println("=== 5. Performance Characteristics ===\n");

        int iterations = 100_000;
        String name = "Karan";
        int age = 30;

        // Concatenation
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String s = "Hello, " + name + "! Age: " + age + ". Iteration: " + i;
        }
        long concatNs = System.nanoTime() - start;

        // String.format
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String s = String.format("Hello, %s! Age: %d. Iteration: %d", name, age, i);
        }
        long formatNs = System.nanoTime() - start;

        // String.formatted
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String s = "Hello, %s! Age: %d. Iteration: %d".formatted(name, age, i);
        }
        long formattedNs = System.nanoTime() - start;

        // StringBuilder
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String s = new StringBuilder()
                    .append("Hello, ").append(name)
                    .append("! Age: ").append(age)
                    .append(". Iteration: ").append(i)
                    .toString();
        }
        long builderNs = System.nanoTime() - start;

        System.out.println("  " + iterations + " iterations:");
        System.out.printf("    Concatenation:     %6d ms%n", concatNs / 1_000_000);
        System.out.printf("    String.format():   %6d ms%n", formatNs / 1_000_000);
        System.out.printf("    .formatted():      %6d ms%n", formattedNs / 1_000_000);
        System.out.printf("    StringBuilder:     %6d ms%n", builderNs / 1_000_000);
        System.out.println();
        System.out.println("  Notes:");
        System.out.println("    - Concatenation is optimized by javac to StringBuilder (simple cases)");
        System.out.println("    - String.format() is slowest due to parsing format string each time");
        System.out.println("    - StringBuilder is fastest for complex/loop scenarios");
        System.out.println("    - String templates (when available) will compile to optimal bytecode");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // 6. Text blocks refresher (Java 15+, essential companion)
    // ---------------------------------------------------------------
    static void textBlocksRefresher() {
        System.out.println("=== 6. Text Blocks — Essential Companion Feature ===\n");

        String name = "Karan";
        int age = 30;

        // Multi-line strings made easy
        String html = """
                <html>
                    <body>
                        <h1>Welcome, %s!</h1>
                        <p>Age: %d</p>
                    </body>
                </html>
                """.formatted(name, age);
        System.out.println("  HTML:\n" + html.indent(2));

        // SQL
        String sql = """
                SELECT u.name, u.age, o.total
                FROM users u
                JOIN orders o ON u.id = o.user_id
                WHERE u.name = '%s'
                  AND u.age > %d
                ORDER BY o.total DESC
                LIMIT 10
                """.formatted(name, age);
        System.out.println("  SQL:\n" + sql.indent(2));

        // JSON with text block
        List<String> skills = List.of("Java", "Spring", "Kubernetes");
        String skillsJson = skills.stream()
                .map(s -> "        \"%s\"".formatted(s))
                .reduce((a, b) -> a + ",\n" + b)
                .orElse("");
        String jsonDoc = """
                {
                    "name": "%s",
                    "age": %d,
                    "skills": [
                %s
                    ]
                }
                """.formatted(name, age, skillsJson);
        System.out.println("  JSON:\n" + jsonDoc.indent(2));
    }

    // ---------------------------------------------------------------
    // 7. Timeline and status
    // ---------------------------------------------------------------
    static void timelineAndStatus() {
        System.out.println("=== 7. String Templates Timeline ===\n");
        System.out.println("  Java 21 (Sep 2023): JEP 430 — First Preview");
        System.out.println("    → STR, FMT, RAW processors, StringTemplate API");
        System.out.println();
        System.out.println("  Java 22 (Mar 2024): JEP 459 — Second Preview");
        System.out.println("    → Simplified API, removed RAW processor");
        System.out.println();
        System.out.println("  Java 23 (Sep 2024): WITHDRAWN");
        System.out.println("    → Design deemed too complex");
        System.out.println("    → May return in simpler form in future JDK");
        System.out.println();
        System.out.println("  Current best practice:");
        System.out.println("    → Use String.formatted() + text blocks");
        System.out.println("    → Use MessageFormat for i18n");
        System.out.println("    → Use StringBuilder for performance-critical paths");
        System.out.println();
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   String Templates Demo — Java 21 (Preview)         ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        theProblem();
        stringTemplateSyntax();
        customProcessorConcept();
        workingAlternatives();
        performanceComparison();
        textBlocksRefresher();
        timelineAndStatus();

        System.out.println("=== Done ===");
    }
}
