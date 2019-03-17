package edu.byu.yc.typechecker;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.byu.yc.typechecker.symboltable.ASTNameType;

/**
 * @author Samuel Nuttall
 */
public class ExpressionEvaluator {

    private Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);

    private static final String UNKNOWN_TYPE = "$UNKNOWN";
    private Map<ASTNode, String> typeTable;
    private TypeCheckerVisitor typeCheckerVisitor;
    private List<String> operations = new ArrayList<>();

    private Map<InfixExpression.Operator, String> operatorToOperationMap = new HashMap<>();

    //Numeric primitives
    private static final String SHORT = "short";
    private static final String INT = "int";
    private static final String LONG = "long";
    private static final String FLOAT = "float";
    private static final String DOUBLE = "double";

    //Textual primitives
    private static final String BYTE = "byte";
    private static final String CHAR = "char";

    // Boolean and null primitives
    private static final String BOOLEAN = "boolean";
    private static final String NULL = "null";

    private Set<String> primitiveTypes = new HashSet<>();
    private List<InfixExpression.Operator> arithmeticOperators = new ArrayList<>();


    public ExpressionEvaluator(TypeCheckerVisitor typeCheckerVisitor) {
        this.typeCheckerVisitor = typeCheckerVisitor;
        this.typeTable = typeCheckerVisitor.getTypeTable();
        addOperators();
        addPrimitives();
        initOperatorToOperations();
    }

    private void addOperators() {
        arithmeticOperators.add(InfixExpression.Operator.TIMES);
        operations.add("multiply");
        arithmeticOperators.add(InfixExpression.Operator.DIVIDE);
        operations.add("divide");
        arithmeticOperators.add(InfixExpression.Operator.REMAINDER);
        operations.add("use modulo operator with");
        arithmeticOperators.add(InfixExpression.Operator.PLUS);
        operations.add("add");
        arithmeticOperators.add(InfixExpression.Operator.MINUS);
        operations.add("subtract");
        arithmeticOperators.add(InfixExpression.Operator.LEFT_SHIFT);
        operations.add("left bit-shift");
        arithmeticOperators.add(InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
        operations.add("right signed bit-shift");
        arithmeticOperators.add(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
        operations.add("right unsigned bit-shift");

    }

    private void initOperatorToOperations() {
        for (int i = 0; i < arithmeticOperators.size(); i++) {
            operatorToOperationMap.put(arithmeticOperators.get(i), operations.get(i));
        }
    }

    private Set<String> numericPrimitives = new TreeSet<>(new NumericTypesComparator());

    class NumericTypesComparator implements Comparator<String> {
        public int compare(String type1, String type2) {
            return compareNumericTypes(type1, type2);
        }
    }

    public void evaluateExpression(ASTNameType expressionNameType, InfixExpression ie) {
        typeTable.put(ie, UNKNOWN_TYPE); // init as unknown type until resolved
        if (!arithmeticOperators.contains(ie.getOperator())) {
            logger.error("unsupported operation: {}, {}", ie.getOperator(), ie);
            return;
        }

        ASTNode lhs = ie.getLeftOperand();
        ASTNode rhs = ie.getRightOperand();
        checkIfNumberLiteral(lhs);
        checkIfNumberLiteral(rhs);
        lhs.accept(typeCheckerVisitor);
        String lhType = typeTable.get(lhs);

        if (!isPrimitive(lhType)) {
            logger.error("Tried to {} a non-primitive type: {} --> {}", operatorToOperationMap.get(ie.getOperator()), lhType, ie);
            return;
        }
        if (isNumericPrimitive(lhType)) {
            logger.error("Tried to {} a non-numeric type: {} --> {}", operatorToOperationMap.get(ie.getOperator()), lhType, ie);
            return;
        }
        rhs.accept(typeCheckerVisitor);
        String rhType = typeTable.get(rhs);
        if (isNumericPrimitive(rhType)) {
            logger.error("Tried to {} a non-numeric type: {} --> {}", operatorToOperationMap.get(ie.getOperator()), rhType, ie);
            return;
        }


        if (isExpressionTypeCompatible(expressionNameType.getType(), lhType, rhType)) {
            logger.info("**** DEEMED VALID **** ===> {} {} = {}", expressionNameType.getType(), expressionNameType.getName(), ie);
            typeTable.put(ie, expressionNameType.getType());
        } else
            logger.error("Infix expression: {} {} = {}, is not type compatible or cannot be widened to type {}", expressionNameType.getType(), expressionNameType.getName(), ie, expressionNameType.getType());

    }

    private void checkIfNumberLiteral(ASTNode node) {
        if (node instanceof NumberLiteral) {
            NumberLiteral nl = (NumberLiteral) node;
            typeTable.put(node, getNumberLiteralType(nl));
        }
    }

    public static boolean isNumeric(String strNum) {
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }


    private boolean isExpressionTypeCompatible(String expressionType, String lhType, String rhType) {
        if (expressionType != null) {
            return (compareNumericTypes(expressionType, lhType) >= 0 &&
                    compareNumericTypes(expressionType, rhType) >= 0);
        }

        return false;
    }

    private boolean isPrimitive(String t) {
        if (t == null || t.equals(UNKNOWN_TYPE)) return false;
        return primitiveTypes.contains(t);
    }


    private boolean isNumericPrimitive(String t) {
        if (t == null || t.equals(UNKNOWN_TYPE)) return true;
        return !numericPrimitives.contains(t);
    }


    /**
     * Compare method for comparing and sorting numeric primitive  types
     *
     * @param prim1 primitive 1
     * @param prim2 primitive 2
     * @return int indicating the comparison
     */
    public int compareNumericTypes(String prim1, String prim2) {
        switch (prim1) {
            case SHORT:
                if (prim2.equals(SHORT))
                    return 0;
                else return -1;
            case INT:
                if (prim2.equals(SHORT)) {
                    return 1;
                } else if (prim2.equals(INT)) {
                    return 0;
                } else return -1;
            case LONG:
                if (prim2.equals(SHORT) || prim2.equals(INT)) {
                    return 1;
                } else if (prim2.equals(LONG)) {
                    return 0;
                } else return -1;
            case FLOAT:
                if (prim2.equals(DOUBLE)) {
                    return -1;
                } else if (prim2.equals(FLOAT)) {
                    return 0;
                } else return 1;
            case DOUBLE:
                if (prim2.equals(DOUBLE))
                    return 0;
                else return 1;
            default:
                return -1;
        }
    }


    /**
     * Add the supported primitive types to a set
     */
    private void addPrimitives() {
        primitiveTypes.add(SHORT);
        primitiveTypes.add(INT);
        primitiveTypes.add(LONG);
        primitiveTypes.add(FLOAT);
        primitiveTypes.add(DOUBLE);

        numericPrimitives.addAll(primitiveTypes); // Add the numeric primitives to separate set

        primitiveTypes.add(BYTE);
        primitiveTypes.add(CHAR);

        primitiveTypes.add(BOOLEAN);
        primitiveTypes.add(NULL);
    }


    /*
     * This function assumes that the NumberLiteral's token is a valid number
     * literal string.
     *
     * Since BooleanLiteral and CharacterLiteral are separate classes, this
     * function assumes that the type of the token is either int, long, float,
     * or double.
     *
     * See https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10
     * for more details.
     *
     * @returns A string representing the primitive type of the NumberLiteral
     */
    public static String getNumberLiteralType(final NumberLiteral nl) {
        String token = nl.getToken();
        if (token.endsWith("f") || token.endsWith("F")) {
            return FLOAT;
        }
        if (token.endsWith("l") || token.endsWith("L")) {
            return LONG;
        }
        if (token.endsWith("d") || token.endsWith("D")) {
            return DOUBLE;
        }
        if (token.indexOf('.') == -1) {
            return INT;
        } else {
            return DOUBLE;
        }
    }
}
