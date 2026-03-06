/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

abstract public class FinderBase {

    protected static final String[] nestedContainersExtensions = {".jar", ".ear", ".war"};

    protected ProgramArguments params;
    protected Statistics statistics = new Statistics();
    protected Set<String> allClasses = new HashSet<>();

    private String processedFile;

    public FinderBase(ProgramArguments params, String processedFile) {
        this.params = params;
        this.processedFile = processedFile;
    }

    public void processFind() throws IOException {
        statistics.start();
        for (String baseDir : params.getBaseFileString()) {
            scanDir(new File(baseDir));
        }

        generateOutput();

        if (params.isShowStatistics()) {
            statistics.print(this);
        }
    }

    abstract protected void generateOutput();

    abstract protected void scanDir(File baseDir) throws IOException;

    abstract public int getNumberProcessedClasses();

    abstract protected void addClassToList(File zipToCheck,
            String toCheckOriginalFilename, File parentFile, String name,
            ZipEntry zipEntry);

    public int getNumberClasses() {
        return this.allClasses.size();
    }

    protected void checkFile(File zipToCheck, String toCheckOriginalFilename,
            File parentFile) throws IOException {

        try (var zipFile = new ZipFile(zipToCheck)) {
            for (var en = zipFile.entries(); en.hasMoreElements(); ) {
                var ze = en.nextElement();

                if (!ze.isDirectory()) {
                    String name = ze.getName();
                    boolean processed = false;

                    for (String nce : nestedContainersExtensions) {
                        if (name.endsWith(nce)) {
                            processed = true;
                            File tmpFile = createTempContainerFile(zipFile, ze, nce);
                            try {
                                checkFile(tmpFile, name, zipToCheck);
                            } finally {
                                tmpFile.delete();
                            }
                        }
                    }

                    if (!processed) {
                        if (processedFile == null || name.endsWith(processedFile)) {
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
    }

    private File createTempContainerFile(ZipFile zipFile, ZipEntry ze,
            String containerFileExtension) throws IOException {
        File tmp = File.createTempFile("temporaryContainerFile",
                containerFileExtension);
        try (var is = zipFile.getInputStream(ze);
             var fos = new FileOutputStream(tmp)) {
            is.transferTo(fos);
        }
        return tmp;
    }
}
