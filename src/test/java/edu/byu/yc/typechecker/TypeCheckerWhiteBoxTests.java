package edu.byu.yc.typechecker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import edu.byu.yc.typechecker.symboltable.SymbolTable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samuel Nuttall
 */
public class TypeCheckerWhiteBoxTests {

    private static Logger logger = LoggerFactory.getLogger(TypeCheckerBlackBoxTests.class);

    private static final String TEST_DIR = "test-files/typechecker";

    private final String root = System.getProperty("user.dir");

    private final File emptyFile = new File(new File(root, TEST_DIR), "Empty.java");
    private final File fieldsFile = new File(new File(root, TEST_DIR), "Fields.java");
    private final File arithmeticFile = new File(new File(root, TEST_DIR), "Arithmetic.java");



    private final String empty = TypeChecker.readFile(emptyFile.getPath());
    private final String fields = TypeChecker.readFile(fieldsFile.getPath());
    private final String arithmetic = TypeChecker.readFile(arithmeticFile.getPath());


    //Package names
    private static final String PACKAGE_FQN = "edu.byu.yc.tests";
    private static final String CLASS_FQN_EMPTY = PACKAGE_FQN + ".Empty";
    private static final String CLASS_FQN_FIELDS = PACKAGE_FQN + ".Fields";
    private static final String CLASS_FQN_ARITHMETIC = PACKAGE_FQN + ".Arithmetic";

    private static final String UNKNOWN_TYPE = "$UNKNOWN";

    /**
     * Tests that an empty class will only have a type table with the ASTNode of the class declaration and
     * it's FQN and nothing else
     */
    @Test
    @DisplayName("Test Empty Class with Type Checker Visitor")
    public void testEmptyClass() {

        ASTNode node = TypeChecker.parse(empty);
        SymbolTable emptySymbolTable = TypeChecker.createSymbolTable(node);

        logger.info("FieldsSymbolTable {}", emptySymbolTable);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(emptySymbolTable);
        node.accept(typeCheckerVisitor);

        Map<ASTNode, String> tt = typeCheckerVisitor.getTypeTable();

        assertEquals(1, tt.size());
        assertTrue(tt.containsValue(CLASS_FQN_EMPTY));
    }

    /**
     * Tests that a class with a few valid fields will produce a type table with all the correct types
     * Refer to test-files/typechecker/Fields.java
     */
    @Test
    @DisplayName("Test Fields Class with Type Checker Visitor")
    public void testFieldsClass() {

        ASTNode node = TypeChecker.parse(fields);
        SymbolTable fieldsSymbolTable = TypeChecker.createSymbolTable(node);


        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(fieldsSymbolTable);
        node.accept(typeCheckerVisitor);

        Map<ASTNode, String> tt = typeCheckerVisitor.getTypeTable();
        logger.info("Type Table {}", tt);

        assertEquals(6, tt.size());

        assertTrue(tt.containsValue("String")); //Should appear twice in the type table since it resolves the type from imports
        assertTrue(tt.containsValue("boolean"));
        assertTrue(tt.containsValue("int"));
        assertTrue(tt.containsValue("edu.byu.yc.tests.Fields"));
        assertTrue(tt.containsValue("float"));

        assertTrue(tt.containsValue(CLASS_FQN_FIELDS));
    }

    /**
     * Tests the arithmetic class to see that addRight method resolves the type correctly
     */
    @Test
    @DisplayName("Test AddRight method")
    public void testAddRight() {

        ASTNode node = TypeChecker.parse(arithmetic);
        SymbolTable arithmeticST = TypeChecker.createSymbolTable(node);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(arithmeticST);
        node.accept(typeCheckerVisitor);

        Map<ASTNode, String> tt = typeCheckerVisitor.getTypeTable();
        boolean found = false;
        for (Map.Entry entry : tt.entrySet()) {
            boolean containsInfix = entry.getKey().toString().contains("a + b");
            if (containsInfix) {
                found = true;
                assertEquals("int", entry.getValue());
            }
        }

        assertTrue(found);

        logger.info("Type Table {}", tt);

    }

    /**
     * Tests the arithmetic class to see that addWrong has an unresolved type since it is adding
     * incompatible types.
     */
    @Test
    @DisplayName("Test AddWrong method")
    public void testAddWrong() {
        ASTNode node = TypeChecker.parse(arithmetic);
        SymbolTable arithmeticST = TypeChecker.createSymbolTable(node);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(arithmeticST);
        node.accept(typeCheckerVisitor);

        Map<ASTNode, String> tt = typeCheckerVisitor.getTypeTable();
        boolean found = false;
        for (Map.Entry entry: tt.entrySet()) {
            boolean containsInfix = entry.getKey().toString().contains("a + p");
            if (containsInfix) {
                found = true;
                assertEquals(UNKNOWN_TYPE, entry.getValue());
            }
        }

        assertTrue(found);

        logger.info("Type Table {}", tt);
    }

}
