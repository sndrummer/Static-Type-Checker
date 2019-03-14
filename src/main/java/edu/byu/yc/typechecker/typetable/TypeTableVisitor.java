package edu.byu.yc.typechecker.typetable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
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

import edu.byu.yc.typechecker.AbstractTypeCheckerVisitor;
import edu.byu.yc.typechecker.symboltable.SymbolTable;

/**
 * @author Samuel Nuttall
 *
 * This visitor is to get the local context and resolve the variables used using both local and outside
 * context
 */
public class TypeTableVisitor extends AbstractTypeCheckerVisitor {
    private Logger logger = LoggerFactory.getLogger(TypeTableVisitor.class);


    private SymbolTable symbolTable;
    private Map<ASTNode, String> typeTable = new HashMap<>(); //We'll see what this is used for?
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
        this.symbolTable = symbolTable;
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
        lookup(sn.getIdentifier(), sn);
        return false;
    }


    @Override
    public boolean visit(InfixExpression ie) {

        //get the parent of the infix expression
        ASTNode parent = ie.getParent();
        while (parent != null) {
            if (parent instanceof MethodDeclaration) {
                logger.debug("Parent is an instance of methodDeclaration");

                //TODO you are here

                //symbolTable.methodExists()
                break;
            }
            parent = parent.getParent();
        }


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
        return primitiveTypes.contains(t);
    }


    /**
     * Lookup to see if the name is in the symbol table
     *
     * @param name
     * @param node
     */
    private void lookup(String name, ASTNode node) {


       /* context.add(name);
        while (true) {
            if (symbolTable.containsKey(context)) { //check if it contains name?
                typeTable.put(node, symbolTable.get(context));
                return;
            }
            if (context.size() > 1) {
                logger.debug("Removing from {}:", context);
                int last = context.size() - 2;
                context.remove(last);
                logger.debug("Removed: {}", context);
            } else {
                logger.debug("Could not find type {} in context {}", name, context);
                typeTable.put(node, UNKNOWN_TYPE);
                return;
            }
        }*/
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
