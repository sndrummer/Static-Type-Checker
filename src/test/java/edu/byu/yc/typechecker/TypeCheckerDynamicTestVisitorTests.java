package edu.byu.yc.typechecker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;

import edu.byu.yc.typechecker.symboltable.SymbolTable;


/**
 * @author Samuel Nuttall
 *
 * This class uses edu.byu.yc.typechecker.TypeCheckerDynamicTestVisitor to create Dynamic Test Nodes
 * and test the type proofs generated by the program.
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TypeCheckerDynamicTestVisitorTests {
    private final String root = System.getProperty("user.dir");
    private final String testfile = System.getenv("testfile2");

    private final File adderFile = new File(new File(root, "test-files"), testfile);
    private final String adderContents = TypeChecker.readFile(adderFile.getPath());

    private ASTNode node;
    private ArrayList<DynamicNode> typeProofTests;


    @BeforeAll
    public void setup() {
        node = TypeChecker.parse(adderContents);

        SymbolTable symbolTable = TypeChecker.createSymbolTable(node);
        TypeCheckerDynamicTestVisitor v = new TypeCheckerDynamicTestVisitor(symbolTable);
        node.accept(v);
        typeProofTests = v.getInfixTests();
    }


    @TestFactory
    public Stream<DynamicNode> typeProof() {
    	return typeProofTests.stream();
    }

}