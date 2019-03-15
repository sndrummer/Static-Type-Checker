package edu.byu.yc.typechecker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import edu.byu.yc.typechecker.symboltable.SymbolTable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samuel Nuttall
 */
public class AdderTest {

    private static Logger logger = LoggerFactory.getLogger(SymbolTableTests.class);

    private static final String TEST_DIR = "test-files";

    private final String root = System.getProperty("user.dir");
    private final File adderFile = new File(new File(root, TEST_DIR), "Adder.java");



    private final String adder = TypeChecker.readFile(adderFile.getPath());

    //Expected fully qualified name of the class
    private static final String PACKAGE_FQN = "edu.byu.yc.tests";


    /**
     * Verifies that classExists method is working properly
     */
    @Test
    @DisplayName("Test classExists")
    public void testClassExists() {
        SymbolTable adderSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(adder));
        assertTrue(adderSymbolTable.classExists(PACKAGE_FQN + ".Adder"));
        assertFalse(adderSymbolTable.classExists(PACKAGE_FQN + ".MyClass"));
    }

    /**
     * Test some basic type checking
     */
    @Test
    @DisplayName("Test Basic Type checking of the file")
    public void testBasicTypeChecking() {
        //1. Step 1 create Symbol Table
        SymbolTable adderSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(adder));
        //Step 2. create a TypeTable
        logger.debug("Adder symbol Table: {}", adderSymbolTable);
    }


}
