/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

abstract public class ClassFinderBase extends FinderBase {

    protected static final String SEPARATOR = "-------------------------------------------------------------------------------------";

    protected Map<String, List<String>> classes = new HashMap<>();

    public ClassFinderBase(ProgramArguments params) {
        super(params, ".class");
    }

    protected abstract int getOutputMinOccurrenceNumber();

    protected void generateOutput() {
        for (var entry : classes.entrySet()) {
            var list = entry.getValue();
            if (list.size() >= getOutputMinOccurrenceNumber()) {
                if (params.isShowSeparator()) {
                    System.out.println(SEPARATOR);
                }
                System.out.println(entry.getKey());
                if (params.isShowContainerNames()) {
                    for (String file : list) {
                        System.out.println("\t" + file);
                    }
                }
            }
        }
    }

    protected void scanDir(File baseDir) throws IOException {
        File[] fileList = baseDir.listFiles(this::acceptFile);
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

        classes.computeIfAbsent(className, k -> new ArrayList<>())
                .add(parentFile == null
                        ? toCheck.getAbsolutePath()
                        : parentFile.getAbsolutePath() + "!" + toCheckOriginalFilename);
    }

    public int getNumberProcessedClasses() {
        return this.classes.size();
    }

    private boolean acceptFile(File pathname) {
        String name = pathname.getName();
        for (String fileExtension : params.getContainerExtensions()) {
            if (name.endsWith("." + fileExtension)) {
                return true;
            }
        }
        return pathname.isDirectory();
    }
}
