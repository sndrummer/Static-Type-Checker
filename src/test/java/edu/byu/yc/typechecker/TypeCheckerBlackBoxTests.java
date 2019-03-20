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
 *
 * Tests to see that the type checker works as expected
 */
public class TypeCheckerBlackBoxTests {

    private static Logger logger = LoggerFactory.getLogger(TypeCheckerBlackBoxTests.class);

    private static final String TEST_DIR = "test-files/typechecker";

    private final String root = System.getProperty("user.dir");

    private final File emptyFile = new File(new File(root, TEST_DIR), "Empty.java");
    private final File fieldsFile = new File(new File(root, TEST_DIR), "Fields.java");
    private final File arithmeticFile = new File(new File(root, TEST_DIR), "Arithmetic.java");

    private final File widenFile = new File(new File(root, TEST_DIR), "Widening.java");
    private final File testFile = new File(new File(root, TEST_DIR), "TestTC.java");



    private final String empty = TypeChecker.readFile(emptyFile.getPath());
    private final String fields = TypeChecker.readFile(fieldsFile.getPath());
    private final String arithmetic = TypeChecker.readFile(arithmeticFile.getPath());
    private final String widening = TypeChecker.readFile(widenFile.getPath());
    private final String testComplex = TypeChecker.readFile(testFile.getPath());

    //Package names
    private static final String PACKAGE_FQN = "edu.byu.yc.tests";
    private static final String CLASS_FQN_EMPTY = PACKAGE_FQN + ".Empty";
    private static final String CLASS_FQN_FIELDS = PACKAGE_FQN + ".Fields";
    private static final String CLASS_FQN_ARITHMETIC = PACKAGE_FQN + ".Arithmetic";
    private static final String CLASS_FQN_TEST = PACKAGE_FQN + ".TestTC";

    /**
     * Tests that an empty class will display no errors and the logger will show only the class name
     * as part of the symbol table
     */
    @Test
    @DisplayName("Test Empty Class with Type Checker Visitor")
    public void testEmptyClass() {

        ASTNode node = TypeChecker.parse(empty);
        SymbolTable emptySymbolTable = TypeChecker.createSymbolTable(node);

        logger.info("FieldsSymbolTable {}", emptySymbolTable);
    }

    /**
     * Tests that no errors are shown and the logger shows all the correct types generated in the type
     * table
     */
    @Test
    @DisplayName("Test Fields Class with Type Checker Visitor")
    public void testFieldsClass() {

        ASTNode node = TypeChecker.parse(fields);
        SymbolTable fieldsSymbolTable = TypeChecker.createSymbolTable(node);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(fieldsSymbolTable);
        node.accept(typeCheckerVisitor);

    }

    /**
     * Tests the arithmetic class grants a certificate (shown by the logger) for the infix
     * expression in addRight And that it shows an error with addWrong() where a non-numeric type is
     * added
     */
    @Test
    @DisplayName("Test Arithmetic class with Type Checker Visitor")
    public void testArithmetic() {

        ASTNode node = TypeChecker.parse(arithmetic);
        SymbolTable arithmeticST = TypeChecker.createSymbolTable(node);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(arithmeticST);
        node.accept(typeCheckerVisitor);

        Map<ASTNode, String> tt = typeCheckerVisitor.getTypeTable();

    }

    /**
     * Tests that the widening works with the type checker
     */
    @Test
    @DisplayName("Test Infix Widening")
    public void testWidening() {

        ASTNode node = TypeChecker.parse(widening);
        SymbolTable wideningST = TypeChecker.createSymbolTable(node);

        logger.info("FieldsSymbolTable {}", wideningST);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(wideningST);
        node.accept(typeCheckerVisitor);

        Map<ASTNode, String> tt = typeCheckerVisitor.getTypeTable();


    }


    /**
     * Tests TestTC file that has more complex fields and expressions to resolve the types
     * Refer to the file
     */
    @Test
    @DisplayName("Test TestTC file")
    public void testTypeCheckerComplex() {

        ASTNode node = TypeChecker.parse(testComplex);
        SymbolTable arithmeticST = TypeChecker.createSymbolTable(node);

        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(arithmeticST);
        node.accept(typeCheckerVisitor);

        Map<ASTNode, String> tt = typeCheckerVisitor.getTypeTable();

    }

}
