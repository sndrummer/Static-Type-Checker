package edu.byu.yc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class SymbolTableVisitorTests {

    private final String root = System.getProperty("user.dir");
    private final File adderFile = new File(new File(root, "test-files"), "Arithmetic.java");
    private final String adderContents = TypeChecker.readFile(adderFile.getPath());

    private ASTNode node;
    private ASTNode addRight;
    private ASTNode addWrong;
    private ASTNode a = null;
    private ASTNode b = null;
    private ASTNode p = null;
    private Map<ASTNode, String> typeTable;

    @BeforeAll
    public void setup() {
        node = TypeChecker.parse(adderContents);
        addRight = null;
        node.accept(new ASTVisitor() {
            @Override
            public boolean visit(InfixExpression ie) {
                if (addRight == null) {
                    addRight = ie;
                } else {
                    addWrong = ie;
                }
                return true;
            }

            @Override
            public boolean visit(SimpleName sn) {
                if (sn.getIdentifier().equals("a")) {
                    a = sn;
                    return true;
                }
                if (sn.getIdentifier().equals("b")) {
                    b = sn;
                    return true;
                }
                if (sn.getIdentifier().equals("p")) {
                    p = sn;
                    return true;
                }
                return true;
            }
        });
        Map<List<String>, String> st = TypeChecker.getSymbolTable(node);
        TypeCheckerVisitor v = new TypeCheckerVisitor(st);
        node.accept(v);
        typeTable = v.getTypeTable();
    }
    
    @Test
    @DisplayName("Ensure that fields' types are correct")
    public void test_Field_Types() {
        assertEquals("int", typeTable.get(a));
        assertEquals("int", typeTable.get(b));
        assertEquals("boolean", typeTable.get(p));
    }

    @Test
    @DisplayName("Test the type of Adder.addRight")
    public void test_Adder_addRight() {
        assertEquals("int", typeTable.get(addRight));
    }
    
    @Test
    @DisplayName("Make sure int+bool fails")
    public void test_Adder_addWrong() {
        assertEquals(TypeCheckerAbstractVisitor.UNKNOWN_TYPE, typeTable.get(addWrong));
    }
}
