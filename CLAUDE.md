# Java Interview Prep Repository

## What This Is
Java interview preparation repo — 7 progressive levels from Core Java (Mid-Level) to Java 25/26 (Principal). Each level has runnable Java files with interview Q&A embedded as code + comments.

## Tech Stack
- **Java 21** with `--enable-preview`
- **Maven 3.9+** (maven-compiler-plugin 3.12.1)
- No test framework — all demos run via `main()` methods

## Build & Run
```bash
mvn compile
java --enable-preview -cp target/classes interview.<level>.<topic>.<ClassName>
```

## Project Structure
```
src/main/java/interview/
  level1_core/          — Core Java (Mid-Level): OOP, collections, strings, exceptions, generics, immutability, serialization, enums, hashcode/equals, sorting
  level2_java8/         — Java 8: streams, lambda, functional interfaces, optional, datetime, method references
  level3_multithreading/ — Concurrency (Senior): threads, executors, CompletableFuture, locks, synchronizers, fork-join, patterns
  level4_java9to17/     — Modern Java (Senior/Staff): var, switch expressions, text blocks, records, sealed classes, pattern matching, modules
  level5_java17to21/    — Cutting Edge (Staff): virtual threads, structured concurrency, scoped values, record patterns, sequenced collections
  level6_jvm_internals/ — JVM Deep Dive (Staff/Principal): memory model, GC, classloading, JIT, memory leaks, profiling
  level7_java25_26/     — Future (Principal): stream gatherers, flexible constructors, value types, primitive generics, compact source
```

## Conventions
- Each Java file is self-contained with a `main()` method demonstrating the concept
- Interview questions and answers are in the README.md (130+ questions)
- No test files — demos serve as verification
- Package naming: `interview.<level>.<topic>`

## Memory
On session start, read Claude memory at `~/.claude/projects/-Users-kanojik-Documents-Karan-Autodesk-AI-Development-Java_Interview/memory/MEMORY.md` for project status and progress tracking.
