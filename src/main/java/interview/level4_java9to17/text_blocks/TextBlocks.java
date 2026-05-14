package interview.level4_java9to17.text_blocks;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 15 Text Blocks
// =============================================================================
//
// Q: "How does indentation work in text blocks?"
// A: "The compiler strips 'incidental' whitespace — it finds the leftmost
//     non-whitespace column across all content lines AND the closing \"\"\",
//     then removes that many leading spaces from every line. This is called
//     're-indentation'. The position of the closing \"\"\" on its own line
//     controls the base indentation."
//
// Q: "What new escape sequences did text blocks introduce?"
// A: "Two new ones: \\s (a single space, prevents trailing whitespace stripping)
//     and \\ at end of line (line continuation — joins the next line without
//     a newline). Both work in regular strings too since Java 15."
//
// Q: "Do text blocks always end with a newline?"
// A: "Yes, by default a text block ends with a newline before the closing
//     \"\"\". To suppress the trailing newline, put the closing \"\"\" on the
//     same line as the last content, or use \\ at the end of the last line."
//
// Q: "Are text blocks a new type?"
// A: "No. Text blocks are still java.lang.String. They're a compile-time
//     syntax feature — at runtime there's no difference. You can use them
//     anywhere a String is expected."
//
// =============================================================================

public class TextBlocks {

    // -------------------------------------------------------------------------
    // 1. Basic text block vs traditional string
    // -------------------------------------------------------------------------
    static void basicTextBlock() {
        System.out.println("=== 1. Basic Text Block vs Traditional String ===");

        // Traditional: escaped quotes, explicit \n
        String oldJson = "{\n" +
                "  \"name\": \"Alice\",\n" +
                "  \"age\": 30,\n" +
                "  \"city\": \"NYC\"\n" +
                "}";

        // Text block: natural formatting, no escaping needed
        String newJson = """
                {
                  "name": "Alice",
                  "age": 30,
                  "city": "NYC"
                }""";

        System.out.println("Traditional:\n" + oldJson);
        System.out.println("\nText block:\n" + newJson);
        System.out.println("Equal? " + oldJson.equals(newJson)); // true
    }

    // -------------------------------------------------------------------------
    // 2. Indentation stripping rules
    // -------------------------------------------------------------------------
    static void indentationStripping() {
        System.out.println("\n=== 2. Indentation Stripping ===");

        // The closing """ position controls base indentation
        // Case A: closing """ aligned with content -> no indentation
        String noIndent = """
                Hello
                World""";

        // Case B: closing """ indented less -> content retains relative indent
        String withIndent = """
            Hello
                World
        """;

        System.out.println("No indent:\n[" + noIndent + "]");
        System.out.println("\nWith indent (closing \"\"\" at col 0 equivalent):");
        System.out.println("[" + withIndent + "]");

        // Demonstrate: moving closing """ left adds indentation
        String indentedBlock = """
                Line 1
                Line 2
            """;
        // closing """ is 4 spaces left of content, so content gets 4-space indent
        System.out.println("Indented block:\n[" + indentedBlock + "]");
    }

    // -------------------------------------------------------------------------
    // 3. Escape sequences: \s and line continuation \
    // -------------------------------------------------------------------------
    static void escapeSequences() {
        System.out.println("\n=== 3. New Escape Sequences ===");

        // \s — explicit space, prevents trailing whitespace stripping
        String withTrailingSpace = """
                Name:  Alice\s\s
                Age:   30\s\s\s
                City:  NYC""";
        System.out.println("\\s preserves trailing spaces:");
        for (String line : withTrailingSpace.split("\n")) {
            System.out.println("  [" + line + "] length=" + line.length());
        }

        // \ at end of line — line continuation (no newline inserted)
        String singleLine = """
                This is a very long string that we want to \
                write across multiple lines in source code \
                but should render as a single line.""";
        System.out.println("\nLine continuation (\\):");
        System.out.println("  " + singleLine);
        System.out.println("  Contains newline? " + singleLine.contains("\n")); // false
    }

    // -------------------------------------------------------------------------
    // 4. Practical use case: SQL queries
    // -------------------------------------------------------------------------
    static void sqlExample() {
        System.out.println("\n=== 4. SQL Queries ===");

        // Before text blocks — unreadable
        String oldSql = "SELECT u.name, u.email, o.total\n" +
                "FROM users u\n" +
                "JOIN orders o ON u.id = o.user_id\n" +
                "WHERE o.total > 100\n" +
                "ORDER BY o.total DESC\n" +
                "LIMIT 10";

        // With text blocks — reads like actual SQL
        String newSql = """
                SELECT u.name, u.email, o.total
                FROM users u
                JOIN orders o ON u.id = o.user_id
                WHERE o.total > 100
                ORDER BY o.total DESC
                LIMIT 10""";

        System.out.println("SQL query:\n" + newSql);
        System.out.println("Equal? " + oldSql.equals(newSql));
    }

    // -------------------------------------------------------------------------
    // 5. Practical use case: HTML templates
    // -------------------------------------------------------------------------
    static void htmlExample() {
        System.out.println("\n=== 5. HTML Templates ===");

        String name = "Alice";
        int itemCount = 3;

        // Text blocks with string formatting
        String html = """
                <html>
                    <head><title>Welcome</title></head>
                    <body>
                        <h1>Hello, %s!</h1>
                        <p>You have %d items in your cart.</p>
                    </body>
                </html>
                """.formatted(name, itemCount);

        System.out.println(html);
    }

    // -------------------------------------------------------------------------
    // 6. Practical use case: JSON with formatted()
    // -------------------------------------------------------------------------
    static void jsonWithFormatted() {
        System.out.println("=== 6. JSON with formatted() ===");

        String name = "Bob";
        int age = 25;
        String city = "SF";

        // .formatted() — instance method added in Java 15
        String json = """
                {
                    "name": "%s",
                    "age": %d,
                    "city": "%s",
                    "active": true
                }
                """.formatted(name, age, city);

        System.out.println(json);
    }

    // -------------------------------------------------------------------------
    // 7. Quotes inside text blocks
    // -------------------------------------------------------------------------
    static void quotesInsideTextBlocks() {
        System.out.println("=== 7. Quotes Inside Text Blocks ===");

        // Single and double quotes — no escaping needed (usually)
        String dialogue = """
                She said, "Hello!"
                He replied, "How are you?"
                They shouted, "Let's go!"
                """;
        System.out.println(dialogue);

        // Triple quotes inside — need escaping for at least one
        String meta = """
                A text block starts with \"""
                and ends with \"""
                """;
        System.out.println(meta);
    }

    // -------------------------------------------------------------------------
    // 8. String methods useful with text blocks
    // -------------------------------------------------------------------------
    static void usefulStringMethods() {
        System.out.println("=== 8. Useful String Methods ===");

        String block = """
                  Line 1
                  Line 2
                  Line 3
                """;

        // stripIndent() — applies the same algorithm as text blocks
        System.out.println("stripIndent():");
        System.out.println("  [" + "    hello  \n    world  ".stripIndent() + "]");

        // translateEscapes() — processes escape sequences in runtime strings
        String raw = "Hello\\nWorld\\t!";
        System.out.println("translateEscapes():");
        System.out.println("  Before: " + raw);
        System.out.println("  After:  " + raw.translateEscapes());

        // indent(n) — adds/removes n spaces of indentation
        String text = "Hello\nWorld";
        System.out.println("indent(4):");
        System.out.println(text.indent(4));
    }

    // -------------------------------------------------------------------------
    // 9. Text block gotchas
    // -------------------------------------------------------------------------
    static void gotchas() {
        System.out.println("=== 9. Gotchas & Tips ===");

        // Gotcha 1: Opening """ must be followed by a newline
        // String bad = """content"""; // COMPILE ERROR

        // Gotcha 2: Trailing whitespace is stripped by default
        String stripped = """
                Hello   \s
                World
                """;
        System.out.println("Trailing space preserved with \\s:");
        System.out.println("  First line ends with: [" +
                stripped.split("\n")[0].substring(stripped.split("\n")[0].length() - 4) + "]");

        // Gotcha 3: Windows line endings (\r\n) are normalized to \n
        System.out.println("Line endings are always \\n (LF), never \\r\\n (CRLF)");

        // Gotcha 4: Text blocks ARE compile-time constants (can be used in case labels)
        String value = "hello";
        switch (value) {
            case """
                 hello""" -> System.out.println("Matched text block in case label!");
            default -> System.out.println("No match");
        }
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 15: Text Blocks (\"\"\")                    ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        basicTextBlock();
        indentationStripping();
        escapeSequences();
        sqlExample();
        htmlExample();
        jsonWithFormatted();
        quotesInsideTextBlocks();
        usefulStringMethods();
        gotchas();
    }
}
