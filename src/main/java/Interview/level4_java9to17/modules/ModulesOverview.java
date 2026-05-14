package interview.level4_java9to17.modules;

// LEVEL: Senior
// =============================================================================
// INTERVIEW Q&A: Java 9 Module System (JPMS — Java Platform Module System)
// =============================================================================
//
// Q: "What is the module system and why was it introduced?"
// A: "JPMS (Project Jigsaw) was introduced in Java 9 to solve three problems:
//     (1) Reliable configuration — explicit dependencies replace fragile
//     classpath ordering. (2) Strong encapsulation — packages are private by
//     default; only exported packages are accessible to other modules.
//     (3) Scalable platform — the JDK itself was modularized (java.base,
//     java.sql, etc.), allowing custom runtime images with only needed modules
//     (via jlink), reducing footprint for microservices and IoT."
//
// Q: "What is module-info.java?"
// A: "It's the module descriptor file placed at the root of the module source
//     directory. It declares the module name, what it requires (dependencies),
//     what it exports (public API), and what services it provides/uses."
//
// Q: "What's the difference between requires and requires transitive?"
// A: "requires declares a dependency for the current module only. requires
//     transitive propagates the dependency to any module that reads this one.
//     Example: java.sql requires transitive java.logging, so any module that
//     requires java.sql also gets java.logging automatically."
//
// Q: "What is the unnamed module?"
// A: "Code on the classpath (not in any module) is placed in the 'unnamed
//     module.' It can read all named modules, and exports all its packages.
//     This is how legacy code works with JPMS without modification."
//
// Q: "What is the automatic module?"
// A: "When you place a JAR without module-info.java on the module path (not
//     classpath), it becomes an automatic module. Its name is derived from the
//     JAR filename or Automatic-Module-Name manifest entry. It exports all
//     packages and can read all other modules."
//
// Q: "Can I still use classpath with Java 9+?"
// A: "Yes. The module system is opt-in. Code on the classpath works as before
//     via the unnamed module. You can gradually modularize."
//
// =============================================================================

import java.lang.module.ModuleDescriptor;
import java.util.Set;

/**
 * This file demonstrates JPMS concepts that can be shown in a single file.
 * A full module demo requires multiple source directories and module-info.java
 * files, so this class focuses on:
 *   - Runtime module introspection
 *   - Explaining module-info.java syntax (in comments)
 *   - Key concepts and interview talking points
 */
public class ModulesOverview {

    // =========================================================================
    // MODULE-INFO.JAVA SYNTAX REFERENCE (cannot be in this file — shown as comments)
    // =========================================================================
    //
    // --- module-info.java (placed at root of module source tree) ---
    //
    //   module com.myapp.core {
    //
    //       // 1. REQUIRES — declare dependencies
    //       requires java.sql;                    // compile + runtime dependency
    //       requires transitive java.logging;     // propagated to dependents
    //       requires static java.compiler;        // compile-time only (optional at runtime)
    //
    //       // 2. EXPORTS — make packages accessible to other modules
    //       exports com.myapp.core.api;           // public API — accessible to all modules
    //       exports com.myapp.core.spi            // qualified export — only to specific modules
    //           to com.myapp.plugin;
    //
    //       // 3. OPENS — allow deep reflection (for frameworks like Spring, Hibernate)
    //       opens com.myapp.core.model;           // reflective access to all modules
    //       opens com.myapp.core.entity            // qualified open — only to specific modules
    //           to com.google.gson;
    //
    //       // 4. USES — declare a service this module consumes
    //       uses com.myapp.core.spi.Plugin;
    //
    //       // 5. PROVIDES — declare a service implementation
    //       provides com.myapp.core.spi.Plugin
    //           with com.myapp.core.impl.DefaultPlugin;
    //   }
    //
    // =========================================================================

    // -------------------------------------------------------------------------
    // 1. Runtime module introspection — examining the module graph
    // -------------------------------------------------------------------------
    static void moduleIntrospection() {
        System.out.println("=== 1. Runtime Module Introspection ===");

        // Every class knows its module
        Module myModule = ModulesOverview.class.getModule();
        System.out.println("  This class's module: " + myModule.getName());
        // When not in a named module, getName() returns null (unnamed module)

        Module stringModule = String.class.getModule();
        System.out.println("  String's module: " + stringModule.getName()); // java.base

        // Examine java.base module descriptor
        ModuleDescriptor descriptor = stringModule.getDescriptor();
        System.out.println("  java.base version: " + descriptor.version().orElse(null));
        System.out.println("  java.base is open: " + descriptor.isOpen());
        System.out.println("  java.base is automatic: " + descriptor.isAutomatic());

        // List some exports from java.base
        System.out.println("\n  java.base exports (first 10):");
        descriptor.exports().stream()
                .filter(e -> e.targets().isEmpty()) // unqualified exports only
                .map(ModuleDescriptor.Exports::source)
                .sorted()
                .limit(10)
                .forEach(pkg -> System.out.println("    - " + pkg));
    }

    // -------------------------------------------------------------------------
    // 2. Examining module dependencies
    // -------------------------------------------------------------------------
    static void moduleDependencies() {
        System.out.println("\n=== 2. Module Dependencies ===");

        // java.sql depends on java.base, java.logging, java.xml, java.transaction.xa
        ModuleLayer layer = ModuleLayer.boot();
        layer.findModule("java.sql").ifPresent(sqlModule -> {
            ModuleDescriptor desc = sqlModule.getDescriptor();
            System.out.println("  java.sql requires:");
            desc.requires().forEach(req ->
                    System.out.println("    - %s %s".formatted(
                            req.name(),
                            req.modifiers().isEmpty() ? "" : req.modifiers())));

            System.out.println("\n  java.sql exports:");
            desc.exports().forEach(exp ->
                    System.out.println("    - " + exp.source()));
        });
    }

    // -------------------------------------------------------------------------
    // 3. Listing all platform modules
    // -------------------------------------------------------------------------
    static void listPlatformModules() {
        System.out.println("\n=== 3. Platform Modules (JDK) ===");

        ModuleLayer layer = ModuleLayer.boot();
        Set<Module> modules = layer.modules();

        long javaModules = modules.stream()
                .map(Module::getName)
                .filter(name -> name != null && name.startsWith("java."))
                .count();
        long jdkModules = modules.stream()
                .map(Module::getName)
                .filter(name -> name != null && name.startsWith("jdk."))
                .count();

        System.out.println("  Total modules in boot layer: " + modules.size());
        System.out.println("  java.* modules: " + javaModules);
        System.out.println("  jdk.* modules: " + jdkModules);

        System.out.println("\n  Key java.* modules:");
        modules.stream()
                .map(Module::getName)
                .filter(name -> name != null && name.startsWith("java."))
                .sorted()
                .forEach(name -> System.out.println("    - " + name));
    }

    // -------------------------------------------------------------------------
    // 4. ServiceLoader (modular services)
    // -------------------------------------------------------------------------
    static void serviceLoaderConcept() {
        System.out.println("\n=== 4. ServiceLoader & Module Services ===");

        System.out.println("  The module system integrates with ServiceLoader:");
        System.out.println();
        System.out.println("  // In API module:");
        System.out.println("  module com.myapp.api {");
        System.out.println("      exports com.myapp.api;");
        System.out.println("      uses com.myapp.api.PaymentProcessor;   // declares service consumption");
        System.out.println("  }");
        System.out.println();
        System.out.println("  // In implementation module:");
        System.out.println("  module com.myapp.stripe {");
        System.out.println("      requires com.myapp.api;");
        System.out.println("      provides com.myapp.api.PaymentProcessor");
        System.out.println("          with com.myapp.stripe.StripeProcessor;  // registers implementation");
        System.out.println("  }");
        System.out.println();
        System.out.println("  // Loading services at runtime:");
        System.out.println("  ServiceLoader<PaymentProcessor> loader = ServiceLoader.load(PaymentProcessor.class);");
        System.out.println("  loader.stream()");
        System.out.println("      .map(ServiceLoader.Provider::get)");
        System.out.println("      .forEach(p -> p.process(payment));");
    }

    // -------------------------------------------------------------------------
    // 5. jlink — custom runtime images
    // -------------------------------------------------------------------------
    static void jlinkConcept() {
        System.out.println("\n=== 5. jlink — Custom Runtime Images ===");

        System.out.println("  jlink creates minimal JRE images with only needed modules:");
        System.out.println();
        System.out.println("  $ jlink --module-path $JAVA_HOME/jmods:mods \\");
        System.out.println("          --add-modules com.myapp \\");
        System.out.println("          --output myapp-runtime");
        System.out.println();
        System.out.println("  Benefits:");
        System.out.println("    - Smaller Docker images (30-50 MB vs 300+ MB full JDK)");
        System.out.println("    - Faster startup");
        System.out.println("    - Reduced attack surface");
        System.out.println("    - Self-contained (no JRE needed on target)");
        System.out.println();
        System.out.println("  Requirement: application must be fully modularized");
        System.out.println("  (all JARs must have module-info.java or use automatic modules)");
    }

    // -------------------------------------------------------------------------
    // 6. Migration strategies
    // -------------------------------------------------------------------------
    static void migrationStrategies() {
        System.out.println("\n=== 6. Migration Strategies ===");

        System.out.println("  Bottom-up migration (recommended):");
        System.out.println("    1. Start with leaf dependencies (no dependents)");
        System.out.println("    2. Add module-info.java to each");
        System.out.println("    3. Work upward to the application module");
        System.out.println();
        System.out.println("  Top-down migration:");
        System.out.println("    1. Add module-info.java to your application");
        System.out.println("    2. Put dependencies on module path as automatic modules");
        System.out.println("    3. Gradually modularize dependencies");
        System.out.println();
        System.out.println("  Key command-line flags for migration:");
        System.out.println("    --add-exports module/package=target   (export a package)");
        System.out.println("    --add-opens module/package=target     (open for reflection)");
        System.out.println("    --add-reads module=target             (add readability)");
        System.out.println("    --add-modules module                  (add root module)");
        System.out.println("    --illegal-access=warn|deny|permit     (removed in Java 17)");
    }

    // -------------------------------------------------------------------------
    // 7. Comparison: classpath vs module path
    // -------------------------------------------------------------------------
    static void comparison() {
        System.out.println("\n=== 7. Classpath vs Module Path ===");
        System.out.println("  ┌──────────────────────┬─────────────────────┬───────────────────────┐");
        System.out.println("  │ Aspect               │ Classpath           │ Module Path            │");
        System.out.println("  ├──────────────────────┼─────────────────────┼───────────────────────┤");
        System.out.println("  │ Dependencies         │ Implicit (hope)     │ Explicit (requires)    │");
        System.out.println("  │ Encapsulation        │ All public = open   │ Only exported packages │");
        System.out.println("  │ Duplicate detection   │ No (first wins)     │ Error at startup       │");
        System.out.println("  │ Split packages       │ Silently merged     │ Error at startup       │");
        System.out.println("  │ Reflection           │ Always allowed      │ Only for opened pkgs   │");
        System.out.println("  │ Runtime errors       │ ClassNotFoundException │ Fail-fast at startup │");
        System.out.println("  │ jlink support        │ No                  │ Yes                    │");
        System.out.println("  └──────────────────────┴─────────────────────┴───────────────────────┘");
    }

    // -------------------------------------------------------------------------
    // 8. Key interview talking points
    // -------------------------------------------------------------------------
    static void interviewTalkingPoints() {
        System.out.println("\n=== 8. Key Interview Talking Points ===");

        System.out.println("  1. JPMS adds strong encapsulation and reliable configuration");
        System.out.println("  2. module-info.java: requires, exports, opens, uses, provides");
        System.out.println("  3. The JDK is modularized: java.base, java.sql, java.logging...");
        System.out.println("  4. Three module types: named, automatic, unnamed");
        System.out.println("  5. requires transitive propagates dependencies");
        System.out.println("  6. exports vs opens: compile-time access vs reflection access");
        System.out.println("  7. jlink creates minimal runtime images");
        System.out.println("  8. Migration is optional — classpath still works");
        System.out.println("  9. Spring/Hibernate use --add-opens for reflection access");
        System.out.println(" 10. ServiceLoader is the standard plugin/SPI mechanism");
    }

    // -------------------------------------------------------------------------
    // main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   Java 9: Module System (JPMS) Overview         ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        moduleIntrospection();
        moduleDependencies();
        listPlatformModules();
        serviceLoaderConcept();
        jlinkConcept();
        migrationStrategies();
        comparison();
        interviewTalkingPoints();
    }
}
