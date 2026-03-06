# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JFindPlus is a Java CLI tool that searches filesystems for class files inside JAR/EAR/WAR archives. It supports three commands:
- `f` (find) - list all classes in jar files under given directories
- `d` (duplicate) - find classes that appear in multiple jar files
- `j` (jar subset) - diff two jar files comparing names, sizes, and content (CRC)

## Build

Maven project, Java 1.6 source/target. No external dependencies.

```bash
mvn package        # build jar to target/jfindplus.jar
mvn clean package  # clean and build
```

## Run

```bash
java -jar target/jfindplus.jar <command> [options] <file-objects>
```

## Architecture

Entry point: `de.oglimmer.JFindPlus.main()` — parses args via `ProgramArguments`, dispatches to one of three finders.

Class hierarchy:
- `FinderBase` (abstract) — core zip/jar scanning with recursive nested container extraction (jar-in-ear, etc.), template method pattern: `scanDir()` → `checkFile()` → `addClassToList()`, then `generateOutput()`
- `ClassFinderBase extends FinderBase` (abstract) — directory-walking finder for `f` and `d` commands. Stores classes in a `Map<className, List<containerPath>>`. Filters by class name, jar name (plain or regex), inner classes.
  - `SimpleFind` — outputs all found classes (min occurrence = 1)
  - `DuplicateFinder` — outputs only classes found in 2+ jars (min occurrence = 2)
- `JarSubset extends FinderBase` — compares two jar files directly. Uses `ZEntry` (name/size/crc) to detect missing files (`+`/`-`), size differences (`*`), content differences (`#`), and equal files (`=`).

Supporting classes:
- `ProgramArguments` — manual CLI argument parser (no library)
- `Statistics` — timing and count tracking, printed with `-statistics` flag

## Code Style

- Uses tab indentation
- Java 1.6 idioms (no generics diamond operator, no try-with-resources, manual stream closing)
- License: LGPL
