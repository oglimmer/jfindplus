/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

/**
 * Helps to analyze the program arguments from the command line
 * 
 * @author oli
 * 
 */
public class ProgramArguments {

    private String[] baseFile;
    private String[] containerExtensions = { "jar", "ear", "war" };
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

    public ProgramArguments(String[] args) throws IOException {


	List<String> baseFileStringList = new ArrayList<String>();

	// is there a parameter for "name/size/content" check?
	boolean foundCheck = false;

	for (int i = 0; i < args.length; i++) {
	    String argument = args[i];
	    if (argument.startsWith("-")) {
		argument = argument.substring(1);
		if ("ext".equalsIgnoreCase(argument)) {
		    String param = args[++i];
		    containerExtensions = param.split(";");
		} else if ("cfilter".equalsIgnoreCase(argument)) {
		    String param = args[++i];
		    classFilter = param;
		    regExClassFilter = false;
		} else if ("cfilterex".equalsIgnoreCase(argument)) {
		    String param = args[++i];
		    classFilter = param;
		    regExClassFilter = true;
		} else if ("jfilter".equalsIgnoreCase(argument)) {
		    String param = args[++i];
		    jarFilter = param;
		    regExJarFilter = false;
		} else if ("jfilterex".equalsIgnoreCase(argument)) {
		    String param = args[++i];
		    jarFilter = param;
		    regExJarFilter = true;
		} else if ("noContainerNames".equalsIgnoreCase(argument)) {
		    showContainerNames = false;
		} else if ("separator".equalsIgnoreCase(argument)) {
		    showSeparator = true;
		} else if ("statistics".equalsIgnoreCase(argument)) {
		    showStatistics = true;
		} else if ("version".equalsIgnoreCase(argument)) {
		    printInfo();
		    System.exit(1);
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
		    System.err.println("Illegal option " + argument);
		    System.exit(1);
		}
	    } else {
		if (command == null) {
		    if (argument.length() != 1) {
			System.err.println("Illegal command " + argument);
			System.exit(1);
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
	    System.err.println("No command given");
	    printInfo();
	    System.exit(1);
	}

	baseFile = baseFileStringList.toArray(new String[baseFileStringList
		.size()]);

	if (isCommandSubset() && baseFile.length != 2) {
	    System.err
		    .println("The j command must have two file objects of type jar.");
	    System.exit(1);
	}
	if (isCommandSubset() && jarFilter != null) {
	    System.err.println("The j command must have a jar filter.");
	    System.exit(1);
	}
	if (isCommendFind() && baseFile.length == 0) {
	    System.err
		    .println("The f command must have at least one file object of type 'directory'.");
	    System.exit(1);
	}
	if (isCommandDuplicate() && baseFile.length == 0) {
	    System.err
		    .println("The d command must have at least one file object of type 'directory'.");
	    System.exit(1);
	}
    }

    private void printInfo() throws IOException {

	Manifest manifest = getManifest();

	System.out
		.println("Usage: JFindPlus <command> <filter> <options> <file object>");
	System.out.println("Version "
		+ manifest.getMainAttributes().getValue(
			"Implementation-Version"));
	System.out.println("");
	System.out.println("where possible commands include:");
	System.out
		.println("  f			to find/print all classes in jar files. <file object> must be one or more base directory names");
	System.out
		.println("  d			to find duplicated classes. <file object> must be one or more base directory names");
	System.out
		.println("  j			to check jars for subsets of classes. <file object> must be two jar files");
	System.out.println("");
	System.out.println("where possible filter parameters are:");
	System.out
		.println("  -cfilter		(d,f,j) a simple not case sensitive 'in-string' filter for classes to be included in the output");
	System.out
		.println("  -cfilterex		(d,f,j) a regular expression filter for classes to be included in the output");
	System.out
		.println("  -jfilter		(d,f) a simple not case sensitive 'in-string' filter for jars to be included in the output");
	System.out
		.println("  -jfilterex		(d,f) a regular expression filter for jars to be included in the output");
	System.out.println("");
	System.out.println("where possible options include:");
	System.out.println("  -version		shows the version info");
	System.out.println("  -statistics		shows statistics");
	System.out.println("  -checkInnerclasses	checks also Inner-Classes");
	System.out
		.println("  -noContainerNames	(d,f), container names are hidden");
	System.out
		.println("  -separator		(d,f), shows a separator line between class names");
	System.out
		.println("  -ext			(d,f), default is 'jar;war;ear', define other file extensions to be searched, multiple extensions are separated by ;");
	System.out
		.println("  -showEqualFiles	(j), shows all files which are content equal in both jars");
	System.out
		.println("  -nameCheck		(j), searchs for missing files in one of the both jars");
	System.out
		.println("  -sizeCheck		(j), checks if a file has the same size in both jars");
	System.out
		.println("  -contentCheck		(j), checks if a file has the same content in both jars");
	System.out
		.println("  -allCheck		(j), default, does all checks -nameCheck -sizeCheck -contentCheck");
	System.out
		.println("  -allFiles		(j), processes all file types, default is 'only .class'");
	System.out.println("");
	System.out
		.println("The j command (jar diff) uses the following symbols at the start of each line:");
	System.out
		.println("  +    This class is only available in the first jar");
	System.out
		.println("  -    This class is only available in the second jar");
	System.out
		.println("  *    The size of this class is different in both jars");
	System.out
		.println("  #    The size of this class is the same but the binary content is different in both jars");
	System.out
		.println("  =    The size and the binary content of this class is exactly the same in both jars (not shown by default)");
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
