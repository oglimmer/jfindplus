JFindPlus
=========

Java command line tool that searches filesystems for class files inside
JAR, EAR, and WAR archives. It can list all classes, find duplicates
across multiple jars, and diff two jar files.

Install via Homebrew
--------------------
    brew tap oglimmer/jfindplus https://github.com/oglimmer/jfindplus
    brew install jfindplus

Build from Source
-----------------
Requires Java 17 or later.

    ./mvnw clean package

This produces target/jfindplus.jar.

Usage
-----
    jfindplus <command> [filter] [options] <file object>

Or if built from source:

    java -jar target/jfindplus.jar <command> [filter] [options] <file object>

Commands
--------
    f   Find and list all classes in jar files.
        <file object> must be one or more directories.

    d   Find duplicated classes (classes appearing in 2+ jars).
        <file object> must be one or more directories.

    j   Diff two jar files, comparing names, sizes, and content.
        <file object> must be exactly two jar files.

Filter Options
--------------
    -cfilter <str>      Case-insensitive substring filter on class names (d, f, j)
    -cfilterex <regex>  Regex filter on class names (d, f, j)
    -jfilter <str>      Case-insensitive substring filter on jar names (d, f)
    -jfilterex <regex>  Regex filter on jar names (d, f)

General Options
---------------
    -version            Show version info
    -statistics         Show processing statistics
    -checkInnerclasses  Include inner classes (excluded by default)
    -noContainerNames   Hide container names in output (d, f)
    -separator          Show separator lines between entries (d, f)
    -ext <exts>         File extensions to search, semicolon-separated (d, f)
                        Default: jar;war;ear

Jar Diff Options (j command only)
---------------------------------
    -nameCheck          Report files missing from one jar
    -sizeCheck          Report files with different sizes
    -contentCheck       Report files with different content (by CRC)
    -allCheck           All of the above (default)
    -showEqualFiles     Also show files that are identical
    -allFiles           Compare all file types, not just .class

Jar Diff Output Symbols
------------------------
    +   File only in the first jar
    -   File only in the second jar
    *   File size differs between jars
    #   Same size but different content
    =   Identical (only shown with -showEqualFiles)

Examples
--------
List all classes under /opt/libs:

    jfindplus f /opt/libs

Find duplicate classes, filtering for "Logger":

    jfindplus d -cfilter Logger /opt/libs

Diff two jars:

    jfindplus j lib-1.0.jar lib-2.0.jar

License
-------
LGPL (http://www.gnu.org/copyleft/lesser.html)
