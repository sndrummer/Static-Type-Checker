package edu.byu.yc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeCheckerDynamicTestVisitor extends TypeCheckerAbstractVisitor {
    private Map<List<String>, String> symbolTable;
    private Map<ASTNode, String> typeTable;
    private Logger logger = LoggerFactory.getLogger(TypeCheckerDynamicTestVisitor.class);
    private HashSet<String> prims;
    private HashSet<InfixExpression.Operator> arithmeticOperators;
    private static final String BOOLEAN = "boolean";
    private static final String INT = "int";

    public Stack<DynamicNode> testNode;
    public ArrayList<DynamicNode> infixTests;
    
    public TypeCheckerDynamicTestVisitor(Map<List<String>, String> st) {
        symbolTable = st;
        prims = new HashSet<>();
        prims.add(INT);
        prims.add(BOOLEAN);
        arithmeticOperators = new HashSet<>();
        arithmeticOperators.add(InfixExpression.Operator.TIMES);
        arithmeticOperators.add(InfixExpression.Operator.DIVIDE);
        arithmeticOperators.add(InfixExpression.Operator.REMAINDER);
        arithmeticOperators.add(InfixExpression.Operator.PLUS);
        arithmeticOperators.add(InfixExpression.Operator.MINUS);
        arithmeticOperators.add(InfixExpression.Operator.LEFT_SHIFT);
        arithmeticOperators.add(InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
        arithmeticOperators.add(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
        typeTable = new HashMap<>();
        testNode = new Stack<>();
        infixTests = new ArrayList<>(); 
    }

  
    @Override
    public boolean visit(SimpleName sn) {
        lookup(sn.getIdentifier(), sn);
        
        String testMsg = "E(" + sn.getIdentifier() + ") = " + typeTable.get(sn);
        DynamicTest snTest = DynamicTest.dynamicTest(testMsg, () -> {
        	Map<ASTNode,String> ttCopy = new HashMap<>(typeTable);
        	assertEquals(ttCopy.get(sn), ttCopy.get(sn));
        });
    	
        testNode.push(DynamicContainer.dynamicContainer("E |- " + sn + ":" + typeTable.get(sn), Stream.of(snTest)));
        
        return false;
    }

    @Override
    public boolean visit(InfixExpression ie) {
        typeTable.put(ie, UNKNOWN_TYPE);
        if (!ie.getOperator().equals(InfixExpression.Operator.PLUS) && !ie.getOperator().equals(InfixExpression.Operator.TIMES)) {
            logger.error("unsupported operation: {}", ie.getOperator());
            return false;
        }
        ASTNode lhs = ie.getLeftOperand();
        lhs.accept(this);
        String lType = typeTable.get(lhs);
        
        ArrayList<DynamicNode> tests = new ArrayList<>();
        tests.add(testNode.pop());
        
        ASTNode rhs = ie.getRightOperand();
        rhs.accept(this);
        String rType = typeTable.get(rhs);
        
        tests.add(testNode.pop());
        tests.add(DynamicTest.dynamicTest(lType + "=" + rType, () -> assertEquals(lType, rType)));
       
        // Should be in a separate method that returns the type for the expression
        if (lType.equals(INT) && lType.equals(rType)) {
        	typeTable.put(ie, INT);
        }
        
        testNode.push(DynamicContainer.dynamicContainer("E |- " + ie + ":" + typeTable.get(ie), tests.stream()));
        infixTests.add(DynamicContainer.dynamicContainer("E |- " + ie + ":" + typeTable.get(ie), tests.stream()));
        
        return false;
    }

    private void lookup(String name, ASTNode node) {
        ArrayList<String> c = new ArrayList<>(context);
        c.add(name);
        
        while (true) {
            if (symbolTable.containsKey(c)) {
                typeTable.put(node, symbolTable.get(c));
                break;
            }
            if (c.size() > 1) {
                logger.debug("Removing from {}:", c);
                int last = c.size() - 2;
                c.remove(last);
                logger.debug("Removed: {}", c);
            } else {
                logger.debug("Could not find type {} in context {}", name, context);
                typeTable.put(node, UNKNOWN_TYPE);
                break;
            }
        }
    }

    public Map<ASTNode, String> getTypeTable() {
        return typeTable;
    }

}
