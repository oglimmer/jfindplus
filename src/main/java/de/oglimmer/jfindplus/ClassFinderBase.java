/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * 
 * @author oli
 * 
 */
abstract public class ClassFinderBase extends FinderBase {

    protected static final String SEPARATOR = "-------------------------------------------------------------------------------------";

    protected Map<String, List<String>> classes = new HashMap<String, List<String>>();

    /**
     * 
     * @param showContainerNames
     * @param containerExtensions
     * @param showSeparator
     */
    public ClassFinderBase(ProgramArguments params) {
	super(params, ".class");
    }

    /**
     * Defines the number of occurrences of a class in different jars to be
     * shown in the output
     * 
     * @return
     */
    protected abstract int getOutputMinOccurrenceNumber();

    /**
     * generates the output to std out
     */
    protected void generateOutput() {
	for (String className : classes.keySet()) {
	    List<String> list = classes.get(className);
	    if (list.size() >= getOutputMinOccurrenceNumber()) {
		if (params.isShowSeparator()) {
		    System.out.println(SEPARATOR);
		}
		System.out.println(className);
		if (params.isShowContainerNames()) {
		    for (String file : list) {
			System.out.println("\t" + file);
		    }
		}
	    }
	}
    }

    /**
     * 
     * @param baseDir
     * @throws IOException
     */
    protected void scanDir(File baseDir) throws IOException {

	// find all desired files and scan them
	// step in subdirectories
	File[] fileList = baseDir.listFiles(fileFilter);
	if (fileList != null) {
	    for (File file : fileList) {
		if (file.isDirectory()) {
		    scanDir(file);
		} else {
		    statistics.addContainer();
		    checkFile(file, null, null);
		}
	    }
	}

    }

    /**
     * 
     */
    protected void addClassToList(File toCheck, String toCheckOriginalFilename,
	    File parentFile, String className, ZipEntry zipEntry) {

	allClasses.add(className);

	if (params.getClassFilter() != null) {
	    if (params.isRegExClassFilter()
		    && !Pattern.matches(params.getClassFilter(), className)) {
		return;
	    }
	    if (!params.isRegExClassFilter()
		    && !className.toLowerCase().contains(
			    params.getClassFilter().toLowerCase())) {
		return;
	    }
	}

	if (params.isNoInnerClasses() && className.contains("$")) {
	    return;
	}

	if (params.getJarFilter() != null) {
	    String fileNameToCheck = parentFile == null ? toCheck
		    .getAbsolutePath() : toCheckOriginalFilename;
	    if (params.isRegExJarFilter()
		    && !Pattern.matches(params.getJarFilter(), fileNameToCheck)) {
		return;
	    }
	    if (!params.isRegExJarFilter()
		    && !fileNameToCheck.toLowerCase().contains(
			    params.getJarFilter())) {
		return;
	    }
	}

	List<String> list = classes.get(className);
	if (list == null) {
	    list = new ArrayList<String>();
	    classes.put(className, list);
	}
	if (parentFile == null) {
	    list.add(toCheck.getAbsolutePath());
	} else {
	    list.add(parentFile.getAbsolutePath() + "!"
		    + toCheckOriginalFilename);
	}
    }

    /**
     * Returns the number of process class files, that means only class files
     * that matches a given filter are counted
     * 
     * @return
     */
    public int getNumberProcessedClasses() {
	return this.classes.size();
    }

    /**
     * 
     */
    private FileFilter fileFilter = new FileFilter() {

	@Override
	public boolean accept(File pathname) {
	    String name = pathname.getName();
	    for (String fileExtension : params.getContainerExtensions()) {
		if (name.endsWith("." + fileExtension)) {
		    return true;
		}
	    }
	    return pathname.isDirectory();
	}
    };

}
