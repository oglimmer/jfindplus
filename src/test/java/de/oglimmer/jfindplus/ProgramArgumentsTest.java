package de.oglimmer.jfindplus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramArgumentsTest {

    @Test
    void noCommandThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProgramArguments(new String[]{}));
    }

    @Test
    void illegalCommandThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProgramArguments(new String[]{"xyz"}));
    }

    @Test
    void illegalOptionThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProgramArguments(new String[]{"f", "-bogus", "/tmp"}));
    }

    @Test
    void findCommandRequiresDirectory() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProgramArguments(new String[]{"f"}));
    }

    @Test
    void duplicateCommandRequiresDirectory() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProgramArguments(new String[]{"d"}));
    }

    @Test
    void subsetCommandRequiresTwoJars() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProgramArguments(new String[]{"j", "one.jar"}));
    }

    @Test
    void versionThrowsWithHelpText() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new ProgramArguments(new String[]{"-version"}));
        assertTrue(ex.getMessage().contains("Usage: JFindPlus"));
    }

    @Test
    void validFindCommand() {
        var pa = new ProgramArguments(new String[]{"f", "/tmp"});
        assertTrue(pa.isCommendFind());
        assertFalse(pa.isCommandDuplicate());
        assertFalse(pa.isCommandSubset());
    }

    @Test
    void validDuplicateCommand() {
        var pa = new ProgramArguments(new String[]{"d", "/tmp"});
        assertTrue(pa.isCommandDuplicate());
        assertFalse(pa.isCommendFind());
    }

    @Test
    void validSubsetCommand() {
        var pa = new ProgramArguments(new String[]{"j", "a.jar", "b.jar"});
        assertTrue(pa.isCommandSubset());
    }

    @Test
    void classFilterOption() {
        var pa = new ProgramArguments(new String[]{"f", "-cfilter", "MyClass", "/tmp"});
        assertEquals("MyClass", pa.getClassFilter());
        assertFalse(pa.isRegExClassFilter());
    }

    @Test
    void regexClassFilterOption() {
        var pa = new ProgramArguments(new String[]{"f", "-cfilterex", "My.*Class", "/tmp"});
        assertEquals("My.*Class", pa.getClassFilter());
        assertTrue(pa.isRegExClassFilter());
    }

    @Test
    void jarFilterOption() {
        var pa = new ProgramArguments(new String[]{"f", "-jfilter", "commons", "/tmp"});
        assertEquals("commons", pa.getJarFilter());
        assertFalse(pa.isRegExJarFilter());
    }

    @Test
    void statisticsOption() {
        var pa = new ProgramArguments(new String[]{"f", "-statistics", "/tmp"});
        assertTrue(pa.isShowStatistics());
    }

    @Test
    void noContainerNamesOption() {
        var pa = new ProgramArguments(new String[]{"f", "-noContainerNames", "/tmp"});
        assertFalse(pa.isShowContainerNames());
    }

    @Test
    void defaultsToAllChecks() {
        var pa = new ProgramArguments(new String[]{"j", "a.jar", "b.jar"});
        assertTrue(pa.isShowDifferentNames());
        assertTrue(pa.isShowDifferentSize());
        assertTrue(pa.isShowDifferentHash());
        assertFalse(pa.isShowEqualFiles());
    }

    @Test
    void explicitNameCheckOnly() {
        var pa = new ProgramArguments(new String[]{"j", "-nameCheck", "a.jar", "b.jar"});
        assertTrue(pa.isShowDifferentNames());
        assertFalse(pa.isShowDifferentSize());
        assertFalse(pa.isShowDifferentHash());
    }

    @Test
    void customExtensions() {
        var pa = new ProgramArguments(new String[]{"f", "-ext", "zip;sar", "/tmp"});
        assertArrayEquals(new String[]{"zip", "sar"}, pa.getContainerExtensions());
    }
}
