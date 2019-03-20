package edu.byu.yc.typechecker;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Samuel Nuttall
 */
public class ExpressionEvaluatorTest {

    private Logger logger = LoggerFactory.getLogger(ExpressionEvaluatorTest.class);

    private static final String UNKNOWN_TYPE = "$UNKNOWN";
    private Map<ASTNode, String> typeTable;
    private TypeCheckerDynamicTestVisitor typeCheckerDynamicTestVisitor;
    private List<String> operations = new ArrayList<>();

    private Map<InfixExpression.Operator, String> operatorToOperationMap = new HashMap<>();

    ArrayList<DynamicNode> tests = new ArrayList<>();

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
    //private Set<InfixExpression.Operator> arithmeticOperators = new HashSet<>();


    private boolean checksOut = false;
    //private String expressionType = UNKNOWN_TYPE;

    public ExpressionEvaluatorTest(TypeCheckerDynamicTestVisitor typeCheckerDynamicTestVisitor) {
        this.typeCheckerDynamicTestVisitor = typeCheckerDynamicTestVisitor;
        this.typeTable = typeCheckerDynamicTestVisitor.getTypeTable();
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

        //logger.info("operatorToOperationMap: {}", operatorToOperationMap);
    }

    private Set<String> numericPrimitives = new TreeSet<>(new NumericTypesComparator());

    class NumericTypesComparator implements Comparator<String> {
        public int compare(String type1, String type2) {
            return compareNumericTypes(type1, type2);
        }
    }

    public void evaluateExpression(ASTNameType expressionNameType, InfixExpression ie) {
        //logger.info("EVALUATING EXPRESSION {}", ie);
        typeTable.put(ie, UNKNOWN_TYPE); // init as unknown type until resolved
        if (!arithmeticOperators.contains(ie.getOperator())) {
            logger.error("unsupported operation: {}, {}", ie.getOperator(), ie);
            return;
        }

        ASTNode lhs = ie.getLeftOperand();
        ASTNode rhs = ie.getRightOperand();
        checkIfNumberLiteral(lhs);
        checkIfNumberLiteral(rhs);
        lhs.accept(typeCheckerDynamicTestVisitor);
        String lhType = typeTable.get(lhs);

        ArrayList<DynamicNode> tests = new ArrayList<>();
        //tests.add(typeCheckerDynamicTestVisitor.getTestNode().pop());

        //logger.info("LHS: {}, RHS: {}", lhs, rhs);
        rhs.accept(typeCheckerDynamicTestVisitor);
        String rhType = typeTable.get(rhs);


        if (isExpressionTypeCompatible(expressionNameType.getType(), lhType, rhType)) {
            logger.info("**** DEEMED VALID **** ===> {} {} = {}", expressionNameType.getType(), expressionNameType.getName(), ie);
            typeTable.put(ie, expressionNameType.getType());
        } else
            logger.error("Infix expression: {} {} = {}, is not type compatible or cannot be widened to type {}", expressionNameType.getType(), expressionNameType.getName(), ie, expressionNameType.getType());

        tests.add(typeCheckerDynamicTestVisitor.getTestNode().pop());
        tests.add(DynamicTest.dynamicTest(expressionNameType.getType() + " " + expressionNameType.getName() + " = "+ ie, () -> assertTrue(isExpressionTypeCompatible(expressionNameType.getType(), lhType, rhType))));

        typeCheckerDynamicTestVisitor.getTestNode().push(DynamicContainer.dynamicContainer("E |- " + ie + " : " + typeTable.get(ie), tests.stream()));
        typeCheckerDynamicTestVisitor.getTypeCheckerTests().add(DynamicContainer.dynamicContainer("E |- " + ie + " : " + typeTable.get(ie), tests.stream()));
    }

    private void checkIfNumberLiteral(ASTNode node) {
        if (node instanceof NumberLiteral) {
            //logger.info("YOU HAVE FOUND A NUMBER LITERAL: {}", lhs);
            NumberLiteral nl = (NumberLiteral) node;
            typeTable.put(node, getNumberLiteralType(nl));
        }
    }

    public static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    private String resolveType(ASTNode expressionItem) {
        //if (expressionItem.toString().)
        return null;
    }

    private boolean isExpressionTypeCompatible(String expressionType, String lhType, String rhType) {
        if (!(isNumericPrimitive(lhType) && isNumericPrimitive(rhType))) {
            return false;
        }

        if (expressionType != null) {
            return (compareNumericTypes(expressionType, lhType) >= 0 &&
                    compareNumericTypes(expressionType, rhType) >= 0);
        }

        return false;
    }

    public void test() {
        //logger.info("ORDER FOR PRIMITIVE TYPES: {}", numericPrimitives);

        short s = 1;
        short a = 44;
        int shortToInt = s + a;
        int asdf = shortToInt + s;
        double floatToDouble = 2F + 2F;
        float longToFloat = 2l + 2l;
        float intToFloat = 2 + 2;
        double mixedToDouble = 4f + 2l;

    }

    public boolean isPrimitive(String t) {
        if (t == null || t.equals(UNKNOWN_TYPE)) return false;
        return primitiveTypes.contains(t);
    }

    private boolean isNumericPrimitive(String t) {
        if (t == null || t.equals(UNKNOWN_TYPE)) return false;
        return numericPrimitives.contains(t);
    }


    /**
     * Evaluates the expression given to see if the types are resolved
     *
     * @return true if a certificate of type resolution is granted, false if there is a type mismatch
     */


    //Use a treeSet that overrides the compare to determine the hierarchy of numeric types
    //TODO IMPLEMENT THE WIDENING VALUES
        /*
        short s = 1;
        short a = 44;
        int shortToInt = s + a;
        double floatToDouble = 2F + 2F;
        float longToFloat = 2l + 2l;
        float intToFloat = 2 + 2;
        double mixedToDouble = 4f + 2l;
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
                //logger.error("Wrong type used {}, {}", prim1, prim2);
                return -1;
        }
    }


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

    public ArrayList<DynamicNode> getTests() {
        return tests;
    }
}
