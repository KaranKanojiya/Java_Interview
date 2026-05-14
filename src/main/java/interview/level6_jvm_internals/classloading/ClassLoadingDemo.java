package interview.level6_jvm_internals.classloading;

// LEVEL: Staff / Principal

/*
 * =============================================================================================
 * CLASS LOADING — INTERVIEW DEEP DIVE
 * =============================================================================================
 *
 * Q: "Explain the class loading mechanism."
 * A: Class loading is the process by which the JVM loads .class files (bytecode) into memory,
 *    verifies them, prepares static fields, resolves symbolic references, and initializes
 *    the class. It follows a strict lifecycle:
 *
 *    1. LOADING     — Find the .class file and read its bytes into memory.
 *                     Creates a java.lang.Class object representing the class.
 *
 *    2. LINKING
 *       a. VERIFICATION  — Bytecode verifier checks structural correctness, type safety,
 *                           stack depth, etc. Prevents malicious or corrupt bytecode.
 *       b. PREPARATION    — Allocate memory for static fields and set default values
 *                           (0, null, false). NOT the values from initializers yet.
 *       c. RESOLUTION     — Resolve symbolic references (class names, method names, field
 *                           names) into direct references. Can be lazy (on first use).
 *
 *    3. INITIALIZATION — Execute static initializers (<clinit>) and static field assignments.
 *                         Happens on first active use: new instance, static method call,
 *                         static field access (non-constant), reflection, subclass init.
 *                         The JVM guarantees <clinit> runs exactly once, thread-safely
 *                         (this is why the Initialization-on-demand holder idiom works
 *                         for lazy singletons).
 *
 * =============================================================================================
 * Q: "What is parent delegation (parent-first delegation model)?"
 * A: When a class loader is asked to load a class, it first delegates to its parent.
 *    Only if the parent cannot find the class does the child attempt to load it.
 *
 *    Hierarchy (Java 9+ with modules):
 *
 *    ┌───────────────────────────────────────────────────┐
 *    │           Bootstrap ClassLoader                    │
 *    │  (native code, loads java.base module,             │
 *    │   rt.jar contents, core Java classes)              │
 *    │  Represented as null in Java code.                 │
 *    └──────────────────┬────────────────────────────────┘
 *                       │ delegates up
 *    ┌──────────────────┴────────────────────────────────┐
 *    │        Platform ClassLoader (Java 9+)              │
 *    │  (was "Extension ClassLoader" in Java 8)           │
 *    │  Loads java.* and javax.* modules not in           │
 *    │  java.base, plus other platform modules.           │
 *    └──────────────────┬────────────────────────────────┘
 *                       │ delegates up
 *    ┌──────────────────┴────────────────────────────────┐
 *    │       Application ClassLoader                      │
 *    │  (aka System ClassLoader)                           │
 *    │  Loads classes from classpath (-cp, CLASSPATH)      │
 *    │  Your application classes are loaded here.          │
 *    └──────────────────┬────────────────────────────────┘
 *                       │ delegates up
 *    ┌──────────────────┴────────────────────────────────┐
 *    │       Custom ClassLoader(s)                        │
 *    │  (e.g., Tomcat's WebAppClassLoader,                │
 *    │   OSGi bundle loaders, hot-deploy loaders)         │
 *    └───────────────────────────────────────────────────┘
 *
 *    Benefits of parent delegation:
 *      1. Security: prevents user code from replacing core classes (e.g., java.lang.String)
 *      2. Consistency: ensures a class is loaded only once by a single class loader
 *      3. Visibility: child can see parent's classes, but not vice versa
 *
 *    NOTE: Some frameworks break parent delegation intentionally:
 *      - Tomcat: child-first loading for web apps (so each app can have different library versions)
 *      - OSGi: peer-to-peer class loading between bundles
 *      - Java 9 modules: the module system adds layer-based loading on top of class loaders
 *
 * =============================================================================================
 * Q: "When would you write a custom class loader?"
 * A: Common use cases:
 *      1. Hot deployment — load new versions of classes without restarting the JVM
 *         (app servers like Tomcat do this for web apps)
 *      2. Class isolation — load the same class from different sources (multi-tenant apps)
 *      3. Encryption — load encrypted .class files, decrypt before defining
 *      4. Dynamic code generation — load bytecode generated at runtime (proxies, AOP)
 *      5. Plugin systems — load plugins from external JARs with isolated class spaces
 *      6. Transformation — modify bytecode before defining (Java agents do this with
 *         instrumentation, but custom class loaders can too)
 *
 * =============================================================================================
 * Q: "What is a class loader leak?"
 * A: A class loader leak occurs when a class loader (and all classes it loaded) cannot be
 *    garbage collected because something still holds a reference to it. Common in app servers
 *    during redeployment. The class loader retains references to all its loaded classes, which
 *    retain references to their static fields, which may hold large object graphs.
 *    Symptoms: Metaspace OOM after repeated deployments.
 *    Fix: Ensure no references leak from the web app to the parent class loader (ThreadLocals,
 *    JDBC drivers, shutdown hooks, etc.).
 *
 * =============================================================================================
 * Q: "What is the difference between Class.forName() and ClassLoader.loadClass()?"
 * A:
 *    Class.forName("com.Foo"):
 *      - Loads, links, AND initializes the class (runs static initializers).
 *      - Uses the calling class's class loader by default.
 *      - Class.forName("com.Foo", false, loader) can skip initialization.
 *
 *    classLoader.loadClass("com.Foo"):
 *      - Only loads the class (does not initialize it).
 *      - Initialization is deferred until first active use.
 *      - Follows parent delegation.
 *
 *    This matters for JDBC: Class.forName("com.mysql.cj.jdbc.Driver") triggers
 *    the static initializer that registers the driver with DriverManager. With
 *    loadClass(), it wouldn't register until first use.
 *    (Since JDBC 4.0 / Java 6, drivers auto-register via ServiceLoader, so
 *    Class.forName() is no longer needed for JDBC drivers.)
 *
 * =============================================================================================
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassLoadingDemo {

    // -----------------------------------------------------------------------------------------
    // DEMO 1: Print the class loader hierarchy
    // -----------------------------------------------------------------------------------------
    static void demoClassLoaderHierarchy() {
        System.out.println("=== Class Loader Hierarchy ===\n");

        // Walk up the class loader chain for this class
        ClassLoader cl = ClassLoadingDemo.class.getClassLoader();
        int level = 0;
        while (cl != null) {
            String indent = "  ".repeat(level + 1);
            System.out.println(indent + "Level " + level + ": " + cl.getClass().getName());
            System.out.println(indent + "         toString: " + cl);
            cl = cl.getParent();
            level++;
        }
        System.out.println("  ".repeat(level + 1) + "Level " + level + ": Bootstrap ClassLoader (null)");
        System.out.println("  ".repeat(level + 1) + "         (implemented in native code, loads java.base)");
        System.out.println();

        // Show which class loader loaded common classes
        System.out.println("  Which class loader loaded what:");
        System.out.println("    java.lang.String:     " + String.class.getClassLoader() + " (Bootstrap)");
        System.out.println("    java.sql.Connection:  " + java.sql.Connection.class.getClassLoader());
        System.out.println("    ClassLoadingDemo:     " + ClassLoadingDemo.class.getClassLoader());
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 2: Load a class dynamically with Class.forName()
    // -----------------------------------------------------------------------------------------
    static void demoDynamicLoading() {
        System.out.println("=== Dynamic Class Loading ===\n");

        try {
            // Class.forName — loads AND initializes the class
            System.out.println("  Loading java.util.HashMap with Class.forName()...");
            Class<?> hashMapClass = Class.forName("java.util.HashMap");
            System.out.println("  Loaded: " + hashMapClass.getName());
            System.out.println("  Class loader: " + hashMapClass.getClassLoader() + " (Bootstrap)");
            System.out.println("  Module: " + hashMapClass.getModule().getName());

            // Create an instance reflectively
            Object instance = hashMapClass.getDeclaredConstructor().newInstance();
            System.out.println("  Created instance: " + instance);
            System.out.println();

            // ClassLoader.loadClass — loads but does NOT initialize
            System.out.println("  Loading java.util.TreeMap with ClassLoader.loadClass()...");
            ClassLoader appLoader = ClassLoadingDemo.class.getClassLoader();
            Class<?> treeMapClass = appLoader.loadClass("java.util.TreeMap");
            System.out.println("  Loaded: " + treeMapClass.getName());
            System.out.println("  (Static initializers NOT yet run — deferred until first active use)");
            System.out.println();

        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 3: Custom Class Loader
    //
    // This is a minimal custom class loader that demonstrates the concept.
    // In practice, custom class loaders are used for:
    //   - Hot-deploy (load new versions of classes)
    //   - Isolation (each loader has its own namespace)
    //   - Encryption (decrypt class bytes before defining)
    // -----------------------------------------------------------------------------------------

    /**
     * A simple custom class loader that loads classes from the same classpath
     * but demonstrates the class loading mechanism.
     *
     * Key methods to override:
     *   - findClass(String name): called when parent delegation fails.
     *     Read the class bytes and call defineClass().
     *   - loadClass(String name): override for child-first loading (break delegation).
     *     Usually you should NOT override this; override findClass() instead.
     */
    static class CustomClassLoader extends ClassLoader {

        private final String loaderName;

        CustomClassLoader(String name, ClassLoader parent) {
            super(parent);
            this.loaderName = name;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            System.out.println("    [" + loaderName + "] findClass called for: " + name);

            // Convert class name to file path: com.foo.Bar -> com/foo/Bar.class
            String path = name.replace('.', '/') + ".class";

            // Try to read the class bytes from the classpath
            try (InputStream is = getParent().getResourceAsStream(path)) {
                if (is == null) {
                    throw new ClassNotFoundException("Cannot find: " + name);
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(chunk)) != -1) {
                    buffer.write(chunk, 0, bytesRead);
                }
                byte[] classBytes = buffer.toByteArray();

                System.out.println("    [" + loaderName + "] Read " + classBytes.length +
                        " bytes, calling defineClass...");

                // defineClass() is the magic method — it takes raw bytes and creates a Class
                return defineClass(name, classBytes, 0, classBytes.length);

            } catch (IOException e) {
                throw new ClassNotFoundException("Error reading class: " + name, e);
            }
        }

        @Override
        public String toString() {
            return "CustomClassLoader[" + loaderName + "]";
        }
    }

    static void demoCustomClassLoader() {
        System.out.println("=== Custom Class Loader ===\n");

        try {
            // Create a custom class loader that loads classes from the same classpath
            // but through our custom findClass() method
            CustomClassLoader loader1 = new CustomClassLoader("Loader-A",
                    ClassLoadingDemo.class.getClassLoader());
            CustomClassLoader loader2 = new CustomClassLoader("Loader-B",
                    ClassLoadingDemo.class.getClassLoader());

            // Load java.util.ArrayList — this will delegate to parent (Bootstrap),
            // so our findClass() is NOT called (parent can handle it)
            System.out.println("  Loading java.util.ArrayList (parent will handle it):");
            Class<?> alClass = loader1.loadClass("java.util.ArrayList");
            System.out.println("  Loaded by: " + alClass.getClassLoader() + " (delegated to Bootstrap)");
            System.out.println();

            // KEY INSIGHT: Two classes loaded by different class loaders are DIFFERENT types,
            // even if they have the same name and identical bytecode!
            System.out.println("  Class identity depends on (class name + class loader).");
            System.out.println("  Same .class loaded by two loaders = two DIFFERENT Class objects.");
            System.out.println("  This is how app servers isolate web applications.\n");

            // Demonstrate: same class loaded by system loader vs custom loader
            Class<?> fromSystem = ClassLoadingDemo.class.getClassLoader()
                    .loadClass("java.util.HashMap");
            Class<?> fromCustom = loader1.loadClass("java.util.HashMap");
            System.out.println("  java.util.HashMap from System and Custom loader same? " +
                    (fromSystem == fromCustom)); // true: both delegated to Bootstrap
            System.out.println("  (Both delegated to Bootstrap, so same Class object)\n");

        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 4: Class initialization order
    // -----------------------------------------------------------------------------------------

    // This nested class demonstrates when static initializers run
    static class InitOrderDemo {
        // Static field with initializer — runs during class initialization
        static final String CONSTANT = "compile-time-constant"; // NOT an active use trigger (inlined)
        static final String RUNTIME_CONSTANT = System.getProperty("java.version"); // IS an active use trigger

        static {
            System.out.println("    [InitOrderDemo] Static initializer block executed!");
            System.out.println("    This runs exactly once, and the JVM guarantees thread safety.");
        }
    }

    static void demoClassInitialization() {
        System.out.println("=== Class Initialization Order ===\n");

        // Accessing a compile-time constant does NOT trigger initialization
        // because the value is inlined by the compiler
        System.out.println("  Accessing compile-time constant (no init triggered):");
        System.out.println("    CONSTANT = " + InitOrderDemo.CONSTANT);
        System.out.println("    (Static initializer did NOT run — value was inlined by javac)\n");

        // Accessing a runtime-computed static field DOES trigger initialization
        System.out.println("  Accessing runtime-computed static field (triggers init):");
        System.out.println("    RUNTIME_CONSTANT = " + InitOrderDemo.RUNTIME_CONSTANT);
        System.out.println();

        // Active uses that trigger initialization:
        System.out.println("  Active uses that trigger class initialization:");
        System.out.println("    1. new Foo()            — creating an instance");
        System.out.println("    2. Foo.staticMethod()   — calling a static method");
        System.out.println("    3. Foo.staticField      — reading a non-constant static field");
        System.out.println("    4. Class.forName(\"Foo\") — reflective loading (with init=true)");
        System.out.println("    5. Subclass init        — initializing a subclass triggers parent init");
        System.out.println("    6. Main class           — the class containing main()");
        System.out.println();
        System.out.println("  Passive uses that do NOT trigger initialization:");
        System.out.println("    1. Foo.COMPILE_TIME_CONSTANT  — inlined by compiler");
        System.out.println("    2. Foo[].class                — array type reference");
        System.out.println("    3. ClassLoader.loadClass()    — loads but does not init");
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------
    // DEMO 5: Thread safety of class initialization
    // -----------------------------------------------------------------------------------------
    static void demoInitializationThreadSafety() {
        System.out.println("=== Class Initialization Thread Safety ===\n");

        System.out.println("  The JVM guarantees that <clinit> (static initializer) runs:");
        System.out.println("    1. Exactly once per class per class loader");
        System.out.println("    2. In a thread-safe manner (only one thread executes it)");
        System.out.println("    3. Other threads wait until initialization completes");
        System.out.println();
        System.out.println("  This is why the Initialization-on-Demand Holder pattern works:");
        System.out.println();
        System.out.println("    public class Singleton {");
        System.out.println("        private Singleton() {}");
        System.out.println("        private static class Holder {");
        System.out.println("            static final Singleton INSTANCE = new Singleton();");
        System.out.println("        }");
        System.out.println("        public static Singleton getInstance() {");
        System.out.println("            return Holder.INSTANCE; // triggers Holder.<clinit> on first call");
        System.out.println("        }");
        System.out.println("    }");
        System.out.println();
        System.out.println("  This is lazy, thread-safe, and requires no synchronization in user code.");
        System.out.println("  The JVM does the synchronization for free during class initialization.\n");
    }

    // -----------------------------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            CLASS LOADING — INTERVIEW DEMO                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // 1. Class loader hierarchy
        demoClassLoaderHierarchy();

        // 2. Dynamic loading with Class.forName() vs loadClass()
        demoDynamicLoading();

        // 3. Custom class loader
        demoCustomClassLoader();

        // 4. Class initialization order and active vs passive uses
        demoClassInitialization();

        // 5. Thread safety of class initialization
        demoInitializationThreadSafety();

        System.out.println("=== Key Takeaways for Interviews ===");
        System.out.println("  1. Loading -> Verification -> Preparation -> Resolution -> Initialization.");
        System.out.println("  2. Parent delegation: child asks parent first. Prevents core class spoofing.");
        System.out.println("  3. Class identity = (fully qualified name + class loader). Two loaders = two types.");
        System.out.println("  4. Class.forName() initializes; ClassLoader.loadClass() does not.");
        System.out.println("  5. Static initializers are thread-safe (JVM guarantees). Basis for holder pattern.");
        System.out.println("  6. Custom class loaders: override findClass(), NOT loadClass() (preserves delegation).");
    }
}
