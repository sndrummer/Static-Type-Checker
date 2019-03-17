package edu.byu.yc.typechecker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

import edu.byu.yc.typechecker.symboltable.ASTNameType;
import edu.byu.yc.typechecker.symboltable.SymbolTable;

/**
 * @author Samuel Nuttall
 * <p>
 * This visitor uses the symbol table to construct a typeTable and check the types of variables and
 * infix expressions that are used
 */
public class TypeCheckerVisitor extends AbstractTypeCheckerVisitor {
    private Logger logger = LoggerFactory.getLogger(TypeCheckerVisitor.class);
    private Map<ASTNode, String> typeTable = new HashMap<>(); //Assign nodes to the type that they are?
    private ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(this);

    private static final String UNKNOWN_TYPE = "$UNKNOWN";


    /**
     * Constructor that takes in a symbol table to do the type checking
     *
     * @param symbolTable used for typechecking
     */
    public TypeCheckerVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }

    /**
     * Visit the ASTNode SimpleNames and then look them up in a typeTable to see if they are valid
     *
     * @param sn ASTNode SimpleName
     * @return false to not search children
     */
    @Override
    public boolean visit(SimpleName sn) {
        if (getCurClassFQN() != null) {
            lookup(sn.getIdentifier(), sn);
        }
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
        String expressionType;
        ASTNameType expressionNameType = null;
        //Find if the variable is in the symbol table
        while (!parentFound && parent != null) {
            parent = parent.getParent();

            if (parent instanceof Assignment) {
                Assignment assignment = (Assignment) parent;
                expressionType = typeTable.get(assignment.getLeftHandSide());
                expressionNameType = new ASTNameType(assignment.getLeftHandSide().toString(), expressionType);
            } else if (parent instanceof ReturnStatement) {
                expressionType = getSymbolTable().getMethodReturnType(getCurClassFQN(), getCurMethodName());
                expressionNameType = new ASTNameType(getCurMethodName(), expressionType);

            } else if (parent instanceof VariableDeclarationFragment) {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) parent;
                expressionType = typeTable.get(fragment.getName());
                expressionNameType = new ASTNameType(fragment.getName().toString(), expressionType);
            } else if (parent instanceof MethodDeclaration) {

                MethodDeclaration md = (MethodDeclaration) parent;
                String methodName = md.getName().toString();
                parentFound = getSymbolTable().methodExists(getCurClassFQN(), methodName);

            } else if (parent instanceof FieldDeclaration) {
                FieldDeclaration fd = (FieldDeclaration) parent;
                String fieldName = ASTUtilities.getFieldDeclarationName(fd);
                parentFound = getSymbolTable().fieldExists(getCurClassFQN(), fieldName);
                expressionType = getSymbolTable().getFieldType(getCurClassFQN(), fieldName);
                expressionNameType = new ASTNameType(fieldName, expressionType);
            }
        }

        if (!parentFound) {
            logger.error("Unable to find context for infix expression {}", ie);
            return false;
        }

        expressionEvaluator.evaluateExpression(expressionNameType, ie);
        return true;

    }


    /**
     * Lookup to see if the SimpleName is in the symbol table
     * Get the type of the node and add it to the symbolTable
     *
     * @param name name of the SimpleName ASTNode
     * @param node The ASTNode
     */
    private void lookup(String name, ASTNode node) {
        String type = UNKNOWN_TYPE;
        if (name.equals(getCurClassSN())) {
            typeTable.put(node, getCurClassFQN());
        } else if (isField(name)) {
            type = getSymbolTable().getFieldType(getCurClassFQN(), name);
            typeTable.put(node, type);
        } else if (isMethod(name)) {
            type = getSymbolTable().getMethodReturnType(getCurClassFQN(), name);
            typeTable.put(node, type);
        } else if (isParam(name)) {
            type = getSymbolTable().getParameterType(getCurClassFQN(), getCurMethodName(), name);
            typeTable.put(node, type);
        } else if (getCurMethodName() != null) {
            type = getSymbolTable().getLocalVariableType(getCurClassFQN(), getCurMethodName(), name);
            typeTable.put(node, type);
        } else if (getSymbolTable().getValidTypes().get(getCurClassFQN()).contains(name)) {

           typeTable.put(node, name);
        }
        else typeTable.put(node, type);

        logger.debug("TYPE-TABLE: {}", typeTable);
        logger.debug("VALID TYPES: {}", getSymbolTable().getValidTypes().get(getCurClassFQN()));

    }

    /**
     * Check if the simpleName is a class field
     *
     * @param name name of the node
     * @return true if is a field
     */
    private boolean isField(String name) {
        return getSymbolTable().fieldExists(getCurClassFQN(), name);
    }


    /**
     * Check if the simpleName is a name of a method
     *
     * @param name name of the node
     * @return true if is a method in the current class
     */
    private boolean isMethod(String name) {
        return getSymbolTable().methodExists(getCurClassFQN(), name);
    }


    /**
     * Check if the simpleName is a param of a method
     *
     * @param name name of the node
     * @return true if is a method param in the current class
     */
    private boolean isParam(String name) {
        return getSymbolTable().parameterExists(getCurClassFQN(), getCurMethodName(), name);
    }

    public Map<ASTNode, String> getTypeTable() {
        return typeTable;
    }


}
