/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.Manifest;

public class ProgramArguments {

    private String[] baseFile;
    private String[] containerExtensions = {"jar", "ear", "war"};
    private boolean showContainerNames = true;
    private boolean showSeparator = false;
    private String classFilter;
    private boolean regExClassFilter;
    private String jarFilter;
    private boolean regExJarFilter;
    private boolean showStatistics = false;
    private Character command;
    private boolean showEqualFiles = false;
    private boolean nameCheck = false;
    private boolean sizeCheck = false;
    private boolean contentCheck = false;
    private boolean checkInnerclasses = false;
    private boolean classesOnly = true;

    public ProgramArguments(String[] args) {
        var baseFileStringList = new ArrayList<String>();
        boolean foundCheck = false;

        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            if (argument.startsWith("-")) {
                argument = argument.substring(1);
                if ("ext".equalsIgnoreCase(argument)) {
                    containerExtensions = args[++i].split(";");
                } else if ("cfilter".equalsIgnoreCase(argument)) {
                    classFilter = args[++i];
                    regExClassFilter = false;
                } else if ("cfilterex".equalsIgnoreCase(argument)) {
                    classFilter = args[++i];
                    regExClassFilter = true;
                } else if ("jfilter".equalsIgnoreCase(argument)) {
                    jarFilter = args[++i];
                    regExJarFilter = false;
                } else if ("jfilterex".equalsIgnoreCase(argument)) {
                    jarFilter = args[++i];
                    regExJarFilter = true;
                } else if ("noContainerNames".equalsIgnoreCase(argument)) {
                    showContainerNames = false;
                } else if ("separator".equalsIgnoreCase(argument)) {
                    showSeparator = true;
                } else if ("statistics".equalsIgnoreCase(argument)) {
                    showStatistics = true;
                } else if ("version".equalsIgnoreCase(argument)) {
                    throw new IllegalArgumentException(buildHelpText());
                } else if ("showEqualFiles".equalsIgnoreCase(argument)) {
                    showEqualFiles = true;
                    foundCheck = true;
                } else if ("nameCheck".equalsIgnoreCase(argument)) {
                    nameCheck = true;
                    foundCheck = true;
                } else if ("sizeCheck".equalsIgnoreCase(argument)) {
                    sizeCheck = true;
                    foundCheck = true;
                } else if ("contentCheck".equalsIgnoreCase(argument)) {
                    contentCheck = true;
                    foundCheck = true;
                } else if ("allCheck".equalsIgnoreCase(argument)) {
                    nameCheck = true;
                    sizeCheck = true;
                    contentCheck = true;
                    foundCheck = true;
                } else if ("checkInnerclasses".equalsIgnoreCase(argument)) {
                    checkInnerclasses = true;
                } else if ("allFiles".equalsIgnoreCase(argument)) {
                    classesOnly = false;
                } else {
                    throw new IllegalArgumentException("Illegal option " + argument);
                }
            } else {
                if (command == null) {
                    if (argument.length() != 1) {
                        throw new IllegalArgumentException("Illegal command " + argument);
                    }
                    command = argument.charAt(0);
                } else {
                    baseFileStringList.add(argument);
                }
            }
        }

        if (!foundCheck) {
            nameCheck = true;
            sizeCheck = true;
            contentCheck = true;
        }

        if (command == null) {
            throw new IllegalArgumentException("No command given\n" + buildHelpText());
        }

        baseFile = baseFileStringList.toArray(new String[0]);

        if (isCommandSubset() && baseFile.length != 2) {
            throw new IllegalArgumentException(
                    "The j command must have two file objects of type jar.");
        }
        if (isCommandSubset() && jarFilter != null) {
            throw new IllegalArgumentException(
                    "The j command must not have a jar filter.");
        }
        if (isCommendFind() && baseFile.length == 0) {
            throw new IllegalArgumentException(
                    "The f command must have at least one file object of type 'directory'.");
        }
        if (isCommandDuplicate() && baseFile.length == 0) {
            throw new IllegalArgumentException(
                    "The d command must have at least one file object of type 'directory'.");
        }
    }

    private String buildHelpText() {
        var manifest = getManifest();
        var version = manifest.getMainAttributes().getValue("Implementation-Version");
        var versionLine = version != null ? "Version " + version + "\n" : "";
        return """
                Usage: JFindPlus <command> <filter> <options> <file object>
                %s
                where possible commands include:
                  f\t\t\tto find/print all classes in jar files. <file object> must be one or more base directory names
                  d\t\t\tto find duplicated classes. <file object> must be one or more base directory names
                  j\t\t\tto check jars for subsets of classes. <file object> must be two jar files

                where possible filter parameters are:
                  -cfilter\t\t(d,f,j) a simple not case sensitive 'in-string' filter for classes to be included in the output
                  -cfilterex\t\t(d,f,j) a regular expression filter for classes to be included in the output
                  -jfilter\t\t(d,f) a simple not case sensitive 'in-string' filter for jars to be included in the output
                  -jfilterex\t\t(d,f) a regular expression filter for jars to be included in the output

                where possible options include:
                  -version\t\tshows the version info
                  -statistics\t\tshows statistics
                  -checkInnerclasses\tchecks also Inner-Classes
                  -noContainerNames\t(d,f), container names are hidden
                  -separator\t\t(d,f), shows a separator line between class names
                  -ext\t\t\t(d,f), default is 'jar;war;ear', define other file extensions to be searched, multiple extensions are separated by ;
                  -showEqualFiles\t\t(j), shows all files which are content equal in both jars
                  -nameCheck\t\t(j), searchs for missing files in one of the both jars
                  -sizeCheck\t\t(j), checks if a file has the same size in both jars
                  -contentCheck\t\t(j), checks if a file has the same content in both jars
                  -allCheck\t\t(j), default, does all checks -nameCheck -sizeCheck -contentCheck
                  -allFiles\t\t(j), processes all file types, default is 'only .class'

                The j command (jar diff) uses the following symbols at the start of each line:
                  +    This class is only available in the first jar
                  -    This class is only available in the second jar
                  *    The size of this class is different in both jars
                  #    The size of this class is the same but the binary content is different in both jars
                  =    The size and the binary content of this class is exactly the same in both jars (not shown by default)\
                """.formatted(versionLine);
    }

    private Manifest getManifest() {
        try {
            String classContainer = ProgramArguments.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toString();
            URL manifestUrl = new URL("jar:" + classContainer
                    + "!/META-INF/MANIFEST.MF");
            return new Manifest(manifestUrl.openStream());
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return new Manifest();
    }

    public boolean isShowContainerNames() {
        return showContainerNames;
    }

    public String[] getContainerExtensions() {
        return containerExtensions;
    }

    public String[] getBaseFileString() {
        return baseFile;
    }

    public boolean isShowSeparator() {
        return showSeparator;
    }

    public String getClassFilter() {
        return classFilter;
    }

    public boolean isRegExClassFilter() {
        return regExClassFilter;
    }

    public boolean isShowStatistics() {
        return showStatistics;
    }

    public boolean isCommandDuplicate() {
        return command == 'd';
    }

    public boolean isCommandSubset() {
        return command == 'j';
    }

    public boolean isCommendFind() {
        return command == 'f';
    }

    public boolean isShowDifferentNames() {
        return nameCheck;
    }

    public boolean isShowDifferentSize() {
        return sizeCheck;
    }

    public boolean isShowDifferentHash() {
        return contentCheck;
    }

    public boolean isShowEqualFiles() {
        return showEqualFiles;
    }

    public boolean isNoInnerClasses() {
        return !checkInnerclasses;
    }

    public boolean isClassesOnly() {
        return classesOnly;
    }

    public String getJarFilter() {
        return jarFilter;
    }

    public boolean isRegExJarFilter() {
        return regExJarFilter;
    }
}
