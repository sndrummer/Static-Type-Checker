package edu.byu.yc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import edu.byu.yc.symboltable.SymbolTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samuel Nuttall
 * <p>
 * This class tests that the SymbolTable visitor correctly assembles a symbol table for a given java
 * program.
 */
public class SymbolTableTests {

    private static Logger logger = LoggerFactory.getLogger(SymbolTableTests.class);

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
    private final String packageFQN = "edu.byu.yc.tests";


    /**
     * Verifies that classExists method is working properly
     */
    @Test
    @DisplayName("Test classExists")
    public void testClassExists() {
        SymbolTable fieldsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(fields));
        assertTrue(fieldsSymbolTable.classExists(packageFQN + ".Fields"));
        assertFalse(fieldsSymbolTable.classExists(packageFQN + ".MyClass"));

        SymbolTable methodsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(methodsNoParams));
        assertTrue(methodsSymbolTable.classExists(packageFQN + ".MethodsNoParams"));
        assertFalse(methodsSymbolTable.classExists(packageFQN + ".Donkey"));


        SymbolTable methodsParamsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(methodsParams));
        assertTrue(methodsParamsSymbolTable.classExists(packageFQN + ".MethodsParams"));
        assertFalse(methodsSymbolTable.classExists(packageFQN + ".Donuts"));


        SymbolTable fmpSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(fieldsMethodsParams));
        assertTrue(fmpSymbolTable.classExists(packageFQN + ".FieldsMethodsParams"));
        assertFalse(methodsSymbolTable.classExists(packageFQN + ".Hello"));

    }

    /**
     * Verifies that methodExists() works, returns true when method exists, false when it does not
     */
    @Test
    @DisplayName("Test methodExists")
    public void testMethodExists() {
        String methodClassFQN = packageFQN + ".MethodsNoParams";
        SymbolTable methodsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(methodsNoParams));
        assertTrue(methodsSymbolTable.methodExists(methodClassFQN, "hello"));
        assertTrue(methodsSymbolTable.methodExists(methodClassFQN, "getNumber"));
        assertFalse(methodsSymbolTable.methodExists(methodClassFQN, "iMaMeThoD"));
    }

    /**
     * Verifies that fieldExists() works, returns true when fields exists, false when it does not
     */
    @Test
    @DisplayName("Test fieldExists")
    public void testFieldExists() {
        String fieldClassFQN = packageFQN + ".Fields";
        SymbolTable fieldsSymbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(fields));

        assertTrue(fieldsSymbolTable.fieldExists(fieldClassFQN, "cheese"));
        assertTrue(fieldsSymbolTable.fieldExists(fieldClassFQN, "num"));
        assertTrue(fieldsSymbolTable.fieldExists(fieldClassFQN, "fl"));
        assertTrue(fieldsSymbolTable.fieldExists(fieldClassFQN, "node"));

        assertFalse(fieldsSymbolTable.fieldExists(fieldClassFQN, "field"));
        assertFalse(fieldsSymbolTable.fieldExists(fieldClassFQN, "bacon"));

    }


    /**
     * Tests that the SymbolTableVisitor constructs a symbol table that identifies and categorizes
     * field names and types. The test parses test-files/Fields.java and compares the expected
     * results of the symbol table to the actual table generated by the visitor.
     */
    @Test
    @DisplayName("Test getFieldType, Field Names/Types in Symbol Table")
    public void testFields() {
        SymbolTable symbolTable = TypeChecker.createSymbolTable(TypeChecker.parse(fields));

        //Refer to test-files/Fields.java
        String fieldsFQN = packageFQN + ".Fields";
        String fieldType1 = symbolTable.getFieldType(fieldsFQN, "cheese");
        String fieldType2 = symbolTable.getFieldType(fieldsFQN, "num");
        String fieldType3 = symbolTable.getFieldType(fieldsFQN, "fl");
        String fieldType4 = symbolTable.getFieldType(fieldsFQN, "node");

        assertEquals("String", fieldType1);
        assertEquals("int", fieldType2);
        assertEquals("float", fieldType3);
        assertEquals("ASTNode", fieldType4);
    }

    /**
     * Tests that the SymbolTableVisitor constructs a symbol table that identifies and categorizes
     * method names and return types. The test parses test-files/Methods.java and compares the
     * expected results of the symbol table to the actual table generated by the visitor.
     */
    @Test
    @DisplayName("Test getMethodReturnType, Method Names/Return-Types in Symbol Table")
    public void testMethods() {
        String methodClassFQN = packageFQN + ".MethodsNoParams";
        SymbolTable methodsSymbolTable = TypeChecker.createSymbolTable(
                TypeChecker.parse(methodsNoParams));

        assertEquals("int", methodsSymbolTable.getMethodReturnType(
                methodClassFQN, "getNumber"));

        assertEquals("String", methodsSymbolTable.getMethodReturnType(
                methodClassFQN, "hello"));

        assertNotEquals("float", methodsSymbolTable.getMethodReturnType(
                methodClassFQN, "getNumber"));

        assertNotEquals("Object", methodsSymbolTable.getMethodReturnType(
                methodClassFQN, "getNumber"));
    }


    /**
     * Tests that the symbol table is storing the parameter types and that getParameterType works correctly
     */
    @Test
    @DisplayName("Test getParameterType, Parameter Names/Types in Symbol Table")
    public void testParameters() {
        String methodClassFQN = packageFQN + ".MethodsParams";
        SymbolTable methodsSymbolTable = TypeChecker.createSymbolTable(
                TypeChecker.parse(methodsParams));

        logger.debug("Table: \n{}", methodsSymbolTable.toString());

        //add(int num1, int num2)
        assertEquals("int", methodsSymbolTable.getParameterType(
                methodClassFQN, "add", "num1"));
        assertEquals("int", methodsSymbolTable.getParameterType(
                methodClassFQN, "add", "num2"));

        //String hello(String name)
        assertEquals("String", methodsSymbolTable.getParameterType(
                methodClassFQN, "hello", "name"));

        //String greeting(int age, String myName, String yourName, String favoriteSong, int timesListened)
        assertEquals("int", methodsSymbolTable.getParameterType(
                methodClassFQN, "greeting", "age"));
        assertEquals("String", methodsSymbolTable.getParameterType(
                methodClassFQN, "greeting", "myName"));
        assertEquals("String", methodsSymbolTable.getParameterType(
                methodClassFQN, "greeting", "yourName"));
        assertEquals("String", methodsSymbolTable.getParameterType(
                methodClassFQN, "greeting", "favoriteSong"));
        assertEquals("int", methodsSymbolTable.getParameterType(
                methodClassFQN, "greeting", "timesListened"));

        assertNotEquals("String", methodsSymbolTable.getParameterType(
                methodClassFQN, "hello", "aName"));
        assertNotEquals("float", methodsSymbolTable.getParameterType(
                methodClassFQN, "greeting", "timesListened"));

    }

    /**
     * Tests that the toString contains necessary information
     */
    @Test
    @DisplayName("Test toString method")
    public void testToString() {
        SymbolTable fmpTable = TypeChecker.createSymbolTable(
                TypeChecker.parse(fieldsMethodsParams));
        logger.debug("Table: \n{}", fmpTable.toString());

        String tableString = fmpTable.toString();


        assertTrue(tableString.contains("edu.byu.yc.tests.FieldsMethodsParams"));

        //Fields
        assertTrue(tableString.contains("cheese"));
        assertTrue(tableString.contains("num"));
        assertTrue(tableString.contains("fl"));
        assertTrue(tableString.contains("node"));

        assertTrue(tableString.contains("String"));
        assertTrue(tableString.contains("float"));
        assertTrue(tableString.contains("int"));
        assertTrue(tableString.contains("ASTNode"));

        //Methods
        assertTrue(tableString.contains("hello"));
        assertTrue(tableString.contains("add"));

        //Params
        assertTrue(tableString.contains("num1"));
        assertTrue(tableString.contains("num2"));
        assertTrue(tableString.contains("name"));

    }


}
