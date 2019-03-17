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
public class WhiteBoxTests {

    private static Logger logger = LoggerFactory.getLogger(WhiteBoxTests.class);

    private static final String TEST_DIR = "test-files";

    private final String root = System.getProperty("user.dir");
    private final File fieldsFile = new File(new File(root, TEST_DIR), "Fields.java");
    private final File methodsNoParamsFile = new File(new File(root, TEST_DIR), "MethodsNoParams.java");
    private final File methodsParamsFile = new File(new File(root, TEST_DIR), "MethodsParams.java");
    private final File fieldsMethodsParamsFile = new File(new File(root, TEST_DIR), "FieldsMethodsParams.java");


    private final String fields = TypeChecker.readFile(fieldsFile.getPath());
    private final String methodsNoParams = TypeChecker.readFile(methodsNoParamsFile.getPath());
    private final String methodsParams = TypeChecker.readFile(methodsParamsFile.getPath());
    private final String fieldsMethodsParams = TypeChecker.readFile(fieldsMethodsParamsFile.getPath());

    //Expected fully qualified name of the class
    private static final String PACKAGE_FQN = "edu.byu.yc.tests";

    /**
     * Verifies that classExists method is working properly
     */
    @Test
    @DisplayName("Test classExists")
    public void testClassExists() {
        SymbolTable fieldsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(fields));
        assertTrue(fieldsSymbolTable.classExists(PACKAGE_FQN + ".Fields"));
        assertFalse(fieldsSymbolTable.classExists(PACKAGE_FQN + ".MyClass"));

        SymbolTable methodsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(methodsNoParams));
        assertTrue(methodsSymbolTable.classExists(PACKAGE_FQN + ".MethodsNoParams"));
        assertFalse(methodsSymbolTable.classExists(PACKAGE_FQN + ".Donkey"));


        SymbolTable methodsParamsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(methodsParams));
        assertTrue(methodsParamsSymbolTable.classExists(PACKAGE_FQN + ".MethodsParams"));
        assertFalse(methodsSymbolTable.classExists(PACKAGE_FQN + ".Donuts"));


        SymbolTable fmpSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(fieldsMethodsParams));
        assertTrue(fmpSymbolTable.classExists(PACKAGE_FQN + ".FieldsMethodsParams"));
        assertFalse(methodsSymbolTable.classExists(PACKAGE_FQN + ".Hello"));

    }

}
