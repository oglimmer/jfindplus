/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class JarSubset extends FinderBase {

    private final List<ZEntry> firstEntries = new ArrayList<>();
    private final List<ZEntry> secondEntries = new ArrayList<>();
    private int scanCount = 0;

    public JarSubset(ProgramArguments params) {
        super(params, params.isClassesOnly() ? ".class" : null);
    }

    @Override
    protected void generateOutput() {
        if (params.isShowEqualFiles()) {
            searchEqualFiles(firstEntries, secondEntries);
        }
        if (params.isShowDifferentNames()) {
            printMissing(firstEntries, secondEntries, "+");
            printMissing(secondEntries, firstEntries, "-");
        }
        if (params.isShowDifferentSize()) {
            compareSizes(firstEntries, secondEntries);
        }
        if (params.isShowDifferentHash()) {
            compareContent(firstEntries, secondEntries);
        }
    }

    private void searchEqualFiles(List<ZEntry> first, List<ZEntry> second) {
        Map<String, ZEntry> secondByName = indexByName(second);
        for (var ze1 : first) {
            var ze2 = secondByName.get(ze1.name());
            if (ze2 != null && ze1.size() == ze2.size() && ze1.crc() == ze2.crc()) {
                System.out.println("= " + ze1.name());
            }
        }
    }

    private void compareSizes(List<ZEntry> first, List<ZEntry> second) {
        Map<String, ZEntry> secondByName = indexByName(second);
        for (var ze1 : first) {
            var ze2 = secondByName.get(ze1.name());
            if (ze2 != null && ze1.size() != ze2.size()) {
                System.out.println("* " + ze1.name() + " ["
                        + ze1.size() + " != " + ze2.size() + "]");
            }
        }
    }

    private void compareContent(List<ZEntry> first, List<ZEntry> second) {
        Map<String, ZEntry> secondByName = indexByName(second);
        for (var ze1 : first) {
            var ze2 = secondByName.get(ze1.name());
            if (ze2 != null && ze1.size() == ze2.size() && ze1.crc() != ze2.crc()) {
                System.out.println("# " + ze1.name());
            }
        }
    }

    private void printMissing(List<ZEntry> source, List<ZEntry> other, String sign) {
        Map<String, ZEntry> otherByName = indexByName(other);
        for (var ze : source) {
            if (!otherByName.containsKey(ze.name())) {
                System.out.println(sign + " " + ze.name());
            }
        }
    }

    private Map<String, ZEntry> indexByName(List<ZEntry> entries) {
        return entries.stream().collect(Collectors.toMap(ZEntry::name, Function.identity()));
    }

    @Override
    protected void scanDir(File baseDir) throws IOException {
        checkFile(baseDir, null, null);
        scanCount++;
    }

    @Override
    public int getNumberProcessedClasses() {
        Set<String> allSet = new HashSet<>();
        for (var ze : firstEntries) {
            allSet.add(ze.name());
        }
        for (var ze : secondEntries) {
            allSet.add(ze.name());
        }
        return allSet.size();
    }

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

        List<ZEntry> target = scanCount == 0 ? firstEntries : secondEntries;
        target.add(new ZEntry(name, zipEntry.getSize(), zipEntry.getCrc()));
    }

    record ZEntry(String name, long size, long crc) {}
}
