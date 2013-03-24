/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 * @author oli
 * 
 */
abstract public class FinderBase {

    protected static final String[] nestedContainersExtensions = { ".jar",
	    ".ear", ".war" };

    protected ProgramArguments params;

    protected Statistics statistics = new Statistics();

    protected Set<String> allClasses = new HashSet<String>();

    private String processedFile;

    public FinderBase(ProgramArguments params, String processedFile) {
	this.params = params;
	this.processedFile = processedFile;
    }

    /**
     * Main processing (search, output)
     * 
     * @throws IOException
     */
    public void processFind() throws IOException {
	for (String baseDir : params.getBaseFileString()) {
	    scanDir(new File(baseDir));
	}

	generateOutput();

	if (params.isShowStatistics()) {
	    statistics.print(this);
	}
    }

    /**
     * 
     */
    abstract protected void generateOutput();

    /**
     * 
     * @param baseDir
     * @throws IOException
     */
    abstract protected void scanDir(File baseDir) throws IOException;

    /**
     * Returns the number of process class files, that means only class files
     * that matches a given filter are counted
     * 
     * @return
     */
    abstract public int getNumberProcessedClasses();

    /**
     * 
     * @param zipToCheck
     * @param toCheckOriginalFilename
     * @param parentFile
     * @param name
     * @param zipEntry
     */
    abstract protected void addClassToList(File zipToCheck,
	    String toCheckOriginalFilename, File parentFile, String name,
	    ZipEntry zipEntry);

    /**
     * Returns the number of all found class files
     * 
     * @return
     */
    public int getNumberClasses() {
	return this.allClasses.size();
    }

    /**
     * 
     * @param zipToCheck
     * @param toCheckOriginalFilename
     * @param parentFile
     * @throws IOException
     */
    protected void checkFile(File zipToCheck, String toCheckOriginalFilename,
	    File parentFile) throws IOException {

	ZipFile zipFile = new ZipFile(zipToCheck);
	for (Enumeration<? extends ZipEntry> en = zipFile.entries(); en
		.hasMoreElements();) {
	    ZipEntry ze = en.nextElement();

	    if (!ze.isDirectory()) {

		String name = ze.getName();

		boolean processed = false;

		for (String nce : nestedContainersExtensions) {
		    if (name.endsWith(nce)) {
			processed = true;
			File tmpFile = createTempContainerFile(zipFile, ze, nce);
			checkFile(tmpFile, name, zipToCheck);
			tmpFile.delete();
		    }
		}

		if (!processed) {
		    if (processedFile == null || name.endsWith(processedFile)) {
			// special behavior for .class files (we want to have
			// the typical java class file representation)
			if (name.endsWith(".class")) {
			    name = name.replace('/', '.');
			    if (name.contains(".")) {
				name = name.substring(0, name.lastIndexOf('.'));
			    }
			}
			addClassToList(zipToCheck, toCheckOriginalFilename,
				parentFile, name, ze);
		    }
		}
	    }
	}
    }

    /**
     * 
     * @param zipFile
     * @param ze
     * @param containerFileExtension
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    private File createTempContainerFile(ZipFile zipFile, ZipEntry ze,
	    String containerFileExtension) throws IOException,
	    FileNotFoundException {
	// get a Stream to the war/jar/ear file
	InputStream is = zipFile.getInputStream(ze);
	File tmp = File.createTempFile("temporaryContainerFile",
		containerFileExtension);
	FileOutputStream fos = new FileOutputStream(tmp);
	byte[] buff = new byte[1024 * 8];
	int len = 0;
	while ((len = is.read(buff)) > 0) {
	    fos.write(buff, 0, len);
	}
	fos.close();
	is.close();
	return tmp;
    }

}
