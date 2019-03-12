package edu.byu.yc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class NoAllCapsVisitorTests {

    private final String root = System.getProperty("user.dir");

    private final File noAllCapsFile = new File(new File(root, "test-files"), "NoAllCaps.java");
    private final File hasAllCapsFile = new File(new File(root, "test-files"), "HasAllCaps.java");
    private final String noAllCaps = TypeChecker.readFile(noAllCapsFile.getPath());
    private final String hasAllCaps = TypeChecker.readFile(hasAllCapsFile.getPath());

    @Test
    public void propertyHoldsTest() {
        assertTrue(TypeChecker.getAllCaps(TypeChecker.parse(noAllCaps)).isEmpty());
    }

    @Test
    public void propertyDoesNotHoldTest() {
        Set<String> ac = TypeChecker.getAllCaps(TypeChecker.parse(hasAllCaps));
        assertFalse(ac.isEmpty());
        assertEquals(1, ac.size());
        assertTrue(ac.contains("STUFF"));
    }

}
