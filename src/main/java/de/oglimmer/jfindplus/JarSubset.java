/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * 
 * @author oli
 * 
 */
public class JarSubset extends FinderBase {

    private Map<File, List<ZEntry>> data = new HashMap<File, List<ZEntry>>();

    /**
     * 
     * @param params
     */
    public JarSubset(ProgramArguments params) {
	super(params, params.isClassesOnly() ? ".class" : null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.oglimmer.jfindplus.FinderBase#generateOutput()
     */
    @Override
    protected void generateOutput() {

	Iterator<List<ZEntry>> it = data.values().iterator();
	List<ZEntry> firstList = it.next();
	List<ZEntry> secondList = it.next();

	if (params.isShowEqualFiles()) {
	    searchEqualNames(firstList, secondList);
	}
	if (params.isShowDifferentNames()) {
	    compare(firstList, secondList, "+");
	    compare(secondList, firstList, "-");
	}
	if (params.isShowDifferentSize()) {
	    compateSize(firstList, secondList);
	}
	if (params.isShowDifferentHash()) {
	    compateContent(firstList, secondList);

	}
    }

    /**
     * 
     * @param firstList
     * @param secondList
     */
    private void searchEqualNames(List<ZEntry> firstList,
	    List<ZEntry> secondList) {
	for (ZEntry ze1 : firstList) {
	    for (ZEntry ze2 : secondList) {
		if (ze1.getName().equals(ze2.getName())
			&& ze1.getSize() == ze2.getSize()
			&& ze1.getCrc() == ze2.getCrc()) {
		    System.out.println("= " + ze1.getName());
		}
	    }
	}
    }

    /**
     * 
     * @param firstList
     * @param secondList
     */
    private void compateSize(List<ZEntry> firstList, List<ZEntry> secondList) {
	for (ZEntry ze1 : firstList) {
	    for (ZEntry ze2 : secondList) {
		if (ze1.getName().equals(ze2.getName())
			&& ze1.getSize() != ze2.getSize()) {
		    System.out.println("* " + ze1.getName() + " ["
			    + ze1.getSize() + " != " + ze2.getSize() + "]");
		}
	    }
	}
    }

    /**
     * 
     * @param firstList
     * @param secondList
     */
    private void compateContent(List<ZEntry> firstList, List<ZEntry> secondList) {
	for (ZEntry ze1 : firstList) {
	    for (ZEntry ze2 : secondList) {
		if (ze1.getName().equals(ze2.getName())
			&& ze1.getSize() == ze2.getSize()
			&& ze1.getCrc() != ze2.getCrc()) {
		    System.out.println("# " + ze1.getName());
		}
	    }
	}
    }

    /**
     * 
     * @param list1
     * @param list2
     * @param sign
     */
    private void compare(List<ZEntry> list1, List<ZEntry> list2, String sign) {
	for (ZEntry ze1 : list1) {
	    boolean found = false;
	    for (ZEntry ze2 : list2) {
		if (ze1.getName().equals(ze2.getName())) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		System.out.println(sign + " " + ze1.getName());
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.oglimmer.jfindplus.FinderBase#scanDir(java.io.File)
     */
    @Override
    protected void scanDir(File baseDir) throws IOException {
	checkFile(baseDir, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.oglimmer.jfindplus.FinderBase#getNumberProcessedClasses()
     */
    @Override
    public int getNumberProcessedClasses() {
	Set<String> allSet = new HashSet<String>();
	for (List<ZEntry> l : data.values()) {
	    for (ZEntry ze : l) {
		allSet.add(ze.getName());
	    }
	}
	return allSet.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.oglimmer.jfindplus.FinderBase#addClassToList(java.io.File,
     * java.lang.String, java.io.File, java.lang.String, java.util.zip.ZipEntry)
     */
    @Override
    protected void addClassToList(File zipToCheck,
	    String toCheckOriginalFilename, File parentFile, String name,
	    ZipEntry zipEntry) {

	allClasses.add(name);

	if (params.isNoInnerClasses() && name.contains("$")) {
	    return;
	}

	if (params.getClassFilter() != null) {
	    if (params.isRegExClassFilter()
		    && !Pattern.matches(params.getClassFilter(), name)) {
		return;
	    }
	    if (!params.isRegExClassFilter()
		    && !name.toLowerCase().contains(
			    params.getClassFilter().toLowerCase())) {
		return;
	    }
	}

	List<ZEntry> list = data.get(zipToCheck);
	if (list == null) {
	    list = new ArrayList<ZEntry>();
	    data.put(zipToCheck, list);
	}
	list.add(new ZEntry(name, zipEntry.getSize(), zipEntry.getCrc()));
    }

    /**
     * 
     * @author oli
     * 
     */
    static class ZEntry {
	private String name;
	private long size;
	private long crc;

	public ZEntry(String name, long size, long crc) {
	    super();
	    this.name = name;
	    this.size = size;
	    this.crc = crc;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public long getSize() {
	    return size;
	}

	public void setSize(long size) {
	    this.size = size;
	}

	public long getCrc() {
	    return crc;
	}

	public void setCrc(long crc) {
	    this.crc = crc;
	}
    }
}
