package test;

import de.hellbz.forge.Utils.Config;
import de.hellbz.forge.Utils.Data;
import de.hellbz.forge.Utils.FileOperation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple smoke tests that can be run manually:
 *   javac -cp "libs/json-20230618.jar;libs/jsoup-1.17.2.jar;build/classes" -d build/test src/test/ManualTests.java
 *   java -cp "libs/json-20230618.jar;libs/jsoup-1.17.2.jar;build/test" test.ManualTests
 */
public class ManualTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        testGetJsonValueNull();
        testGetJsonValueEmpty();
        testGetJsonValueSimple();
        testFileOperationBinaryVsText();
        testForgePattern();
        testNeoForgePatternFourPart();
        testForgeStartfilePattern();

        System.out.println("---");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String name) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static void assertEquals(Object expected, Object actual, String name) {
        assertTrue((expected == null && actual == null) || (expected != null && expected.equals(actual)), name + " (expected: " + expected + ", actual: " + actual + ")");
    }

    static void testGetJsonValueNull() {
        assertEquals(null, Data.getJsonValue(null, "version"), "getJsonValue(null)");
    }

    static void testGetJsonValueEmpty() {
        assertEquals(null, Data.getJsonValue("", "version"), "getJsonValue(empty)");
    }

    static void testGetJsonValueSimple() {
        assertEquals("3.6.0", Data.getJsonValue("{\"version\":\"3.6.0\"}", "version"), "getJsonValue(simple)");
    }

    static void testFileOperationBinaryVsText() throws Exception {
        String tempDir = "build/test-output";
        new File(tempDir).mkdirs();

        // Text file from classpath
        FileOperation textOp = FileOperation.downloadOrReadFile("/res/modInfo.json", tempDir + "/modInfo.json");
        assertTrue(textOp != null && textOp.getResponseCode() == 200, "downloadOrReadFile text");
        assertTrue(Files.exists(Paths.get(tempDir + "/modInfo.json")), "text file was written");

        // Binary file from classpath (use jsoup jar as test binary)
        FileOperation binOp = FileOperation.downloadOrReadFile("/libs/jsoup-1.17.2.jar", tempDir + "/jsoup-copy.jar");
        // This may fail if the JAR is not on classpath; just verify no crash
        assertTrue(binOp != null, "downloadOrReadFile binary returns result");
    }

    static void testForgePattern() {
        Pattern p = Config.Pattern_Forge;
        Matcher m = p.matcher("forge-1.20.4-49.0.3-installer.jar");
        assertTrue(m.find(), "Forge installer pattern matches");
        assertEquals("1.20.4", m.group(1), "Forge pattern minecraft version");
        assertEquals("49.0.3", m.group(2), "Forge pattern loader version");
    }

    static void testNeoForgePatternFourPart() {
        Pattern p = Config.Pattern_NeoForge;
        Matcher m = p.matcher("neoforge-26.1.2.2-beta-installer.jar");
        assertTrue(m.find(), "NeoForge 4-part pattern matches");
    }

    static void testForgeStartfilePattern() {
        Pattern p = Config.Pattern_Forge_startfile;
        Matcher m = p.matcher("forge-1.7.10-10.13.4.1614-1.7.10-universal.jar");
        assertTrue(m.matches(), "Forge 1.7.10 startfile pattern matches");
        assertEquals("1.7.10", m.group("minecraftVersion"), "Forge startfile minecraft version");
        assertEquals("10.13.4.1614", m.group("loaderVersion"), "Forge startfile loader version");
    }
}