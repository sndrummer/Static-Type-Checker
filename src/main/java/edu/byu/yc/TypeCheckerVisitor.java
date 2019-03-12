package edu.byu.yc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeCheckerVisitor extends TypeCheckerAbstractVisitor {
    private Map<List<String>, String> symbolTable;
    private Map<ASTNode, String> typeTable;
    private Logger logger = LoggerFactory.getLogger(TypeCheckerVisitor.class);
    private HashSet<String> prims;
    private HashSet<InfixExpression.Operator> arithmeticOperators;
    private static final String BOOLEAN = "boolean";
    private static final String INT = "int";

    public TypeCheckerVisitor(Map<List<String>, String> st) {
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
    }

    @Override
    public boolean visit(SimpleName sn) {
        lookup(sn.getIdentifier(), sn);
        return false;
    }

    @Override
    public boolean visit(InfixExpression ie) {
        typeTable.put(ie, UNKNOWN_TYPE);
        if (!ie.getOperator().equals(InfixExpression.Operator.PLUS)) {
            logger.error("unsupported operation: {}", ie.getOperator());
            return false;
        }
        ASTNode lhs = ie.getLeftOperand();
        lhs.accept(this);
        String lType = typeTable.get(lhs);
        ASTNode rhs = ie.getRightOperand();
        if (!isPrimitive(lType)) {
            logger.error("Tried to add a non-primitive type: {}", lType);
            return false;
        }
        if (!lType.equals(INT)) {
            logger.error("Tried to add a non-numeric type: {}", lType);
            return false;
        }
        rhs.accept(this);
        String rType = typeTable.get(rhs);
        if (!rType.equals(INT)) {
            logger.error("Tried to add a non-numeric type: {}", rType);
            return false;
        }
        typeTable.put(ie, INT);
        return false;
    }

    private boolean isPrimitive(String t) {
        return prims.contains(t);
    }

    private void lookup(String name, ASTNode node) {
        ArrayList<String> c = new ArrayList<>(context);
        c.add(name);
        while (true) {
            if (symbolTable.containsKey(c)) {
                typeTable.put(node, symbolTable.get(c));
                return;
            }
            if (c.size() > 1) {
                logger.debug("Removing from {}:", c);
                int last = c.size() - 2;
                c.remove(last);
                logger.debug("Removed: {}", c);
            } else {
                logger.debug("Could not find type {} in context {}", name, context);
                typeTable.put(node, UNKNOWN_TYPE);
                return;
            }
        }
    }

    public Map<ASTNode, String> getTypeTable() {
        return typeTable;
    }

}
