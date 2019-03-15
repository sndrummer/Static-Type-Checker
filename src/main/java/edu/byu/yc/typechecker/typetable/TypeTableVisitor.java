package edu.byu.yc.typechecker.typetable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.byu.yc.typechecker.ASTUtilities;
import edu.byu.yc.typechecker.AbstractTypeCheckerVisitor;
import edu.byu.yc.typechecker.symboltable.SymbolTable;

/**
 * @author Samuel Nuttall
 * <p>
 * This visitor is to get the local context and resolve the variables used using both local and outside
 * context
 */
public class TypeTableVisitor extends AbstractTypeCheckerVisitor {
    private Logger logger = LoggerFactory.getLogger(TypeTableVisitor.class);


    private Map<ASTNode, String> typeTable = new HashMap<>(); //Assign nodes to the type that they are?
    private Set<String> primitiveTypes = new HashSet<>();
    private Set<InfixExpression.Operator> arithmeticOperators = new HashSet<>();
    private static final String UNKNOWN_TYPE = "$UNKNOWN";

    //Numeric primitives
    private static final String SHORT = "short ";
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

    public TypeTableVisitor(SymbolTable symbolTable) {
        super(symbolTable);
        addOperators();
        addPrimitives();
    }

    private void addPrimitives() {
        primitiveTypes.add(SHORT);
        primitiveTypes.add(INT);
        primitiveTypes.add(LONG);
        primitiveTypes.add(FLOAT);
        primitiveTypes.add(DOUBLE);

        primitiveTypes.add(BYTE);
        primitiveTypes.add(CHAR);

        primitiveTypes.add(BOOLEAN);
        primitiveTypes.add(NULL);
    }

    private void addOperators() {
        arithmeticOperators.add(InfixExpression.Operator.TIMES);
        arithmeticOperators.add(InfixExpression.Operator.DIVIDE);
        arithmeticOperators.add(InfixExpression.Operator.REMAINDER);
        arithmeticOperators.add(InfixExpression.Operator.PLUS);
        arithmeticOperators.add(InfixExpression.Operator.MINUS);
        arithmeticOperators.add(InfixExpression.Operator.LEFT_SHIFT);
        arithmeticOperators.add(InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
        arithmeticOperators.add(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
    }

    @Override
    public boolean visit(SimpleName sn) {
        if (getCurClassFQN() != null)
            lookup(sn.getIdentifier(), sn);
        return false;
    }


    /**
     * Visit the infix expression and resolve the type, make sure the operands are type compatible
     *
     * @param ie infix expression
     * @return false to stop exploring children and true to continue exploring children
     */
    @Override
    public boolean visit(InfixExpression ie) {

        //get the parent of the infix expression
        ASTNode parent = ie;
        boolean parentFound = false;
        while (!parentFound && parent != null) {
            parent = parent.getParent();
            if (parent instanceof MethodDeclaration) {

                MethodDeclaration md = (MethodDeclaration) parent;
                String methodName = md.getName().toString();

                boolean methodExists = getSymbolTable().methodExists(getCurClassFQN(), methodName);
                logger.debug("Method Exists? {}", methodExists);
                parentFound = methodExists;
            } else if (parent instanceof FieldDeclaration) {
                FieldDeclaration fd = (FieldDeclaration) parent;

                String fieldName = ASTUtilities.getFieldDeclarationName(fd);

                boolean fieldExists = getSymbolTable().fieldExists(getCurClassFQN(), fieldName);
                logger.debug("Field Exists? {}", fieldExists);
                parentFound = fieldExists;
            }
        }

        if (!parentFound) {
            logger.error("Unable to find context for infix expression {}", ie);
            return false;
        }


        //The hierarchy is short -> int --> float -->
        // WIDENING!!!! long to float is considered widening!!!!!!

        //HERE IS THE WIDENING RULES HIERARCHY

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

        typeTable.put(ie, UNKNOWN_TYPE); // init as unknown type until resolved
        if (!arithmeticOperators.contains(ie.getOperator())) {
            logger.error("unsupported operation: {}", ie.getOperator());
            return false;
        }
        ASTNode lhs = ie.getLeftOperand();
        lhs.accept(this);
        String lType = typeTable.get(lhs);
        ASTNode rhs = ie.getRightOperand();
        if (!isPrimitive(lType)) {
            logger.error("Tried to add a non-primitive type: {}", lType);
            logger.error("NODE: {}", lhs);
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

    public boolean isOperationCompatible() {
        return false;
    }

    private boolean isPrimitive(String t) {
        return primitiveTypes.contains(t);
    }


    /**
     * Lookup to see if the name is in the symbol table
     *
     * @param name
     * @param node
     */
    private void lookup(String name, ASTNode node) {
        logger.info("HERE IS THE NAME: {}", name);

//        logger.info("HERE IS THE Current context: {}", getCurClassFQN());
//        logger.info("Symbol Table: {}", getSymbolTable());

        // Check if it is a field
        String type = UNKNOWN_TYPE;
        if (isField(name)) {
            type = getSymbolTable().getFieldType(getCurClassFQN(), name);
            typeTable.put(node, type);
        } else if (isMethod(name)) {
            type = getSymbolTable().getFieldType(getCurClassFQN(), name);
            typeTable.put(node, type);
        } else if (getCurMethodName() != null) {
            logger.debug("The Current method name is: {}", getCurMethodName());


        }


//        boolean isField = getSymbolTable().fieldExists(getCurClassFQN(), name);
//        boolean isMethod = getSymbolTable().methodExists(getCurClassFQN(), name);
//        logger.info("Is a field?: {}", isField);
//        logger.info("Is method?: {}", isMethod);

        //if (getSymbolTable().)
//        context.add(name);
//            while (true) {
//                if (symbolTable.containsKey(context)) { //check if it contains name?
//                    typeTable.put(node, symbolTable.get(context));
//                    return;
//                }
//                if (context.size() > 1) {
//                    logger.debug("Removing from {}:", context);
//                    int last = context.size() - 2;
//                    context.remove(last);
//                    logger.debug("Removed: {}", context);
//                } else {
//                    logger.debug("Could not find type {} in context {}", name, context);
//                    typeTable.put(node, UNKNOWN_TYPE);
//                    return;
//                }
//        }
    }

    private boolean isField(String name) {
        return getSymbolTable().fieldExists(getCurClassFQN(), name);
    }

    private boolean isMethod(String name) {
        return getSymbolTable().methodExists(getCurClassFQN(), name);
    }

    public Map<ASTNode, String> getTypeTable() {
        return typeTable;
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
