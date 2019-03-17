package edu.byu.yc.typechecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class AltTypeCheckerDynamicTestVisitorTests {

    private final String root = System.getProperty("user.dir");
    private final String testfile = System.getProperty("Adder.java");
    //private final File adderFile = new File(new File(root, "test-files"), testfile);



    private final File adderFile = new File(new File(root, "test-files"), "Adder.java");
    private final String adderContents = TypeChecker.readFile(adderFile.getPath());



    private ASTNode node;
    private ArrayList<DynamicNode> infixTests;
    

    @BeforeAll
    public void setup() {
        node = TypeChecker.parse(adderContents);
       
        Map<List<String>, String> st = TypeChecker.getSymbolTable(node);
        AltTypeCheckerDynamicTestVisitor v = new AltTypeCheckerDynamicTestVisitor(st);
        node.accept(v);
        infixTests = v.infixTests;
    }
    
    
    @TestFactory
    public Stream<DynamicNode> typeProof() {
//    	return infixTests.stream();
    	return Stream.of(infixTests.get(1));
    	
    }
}
