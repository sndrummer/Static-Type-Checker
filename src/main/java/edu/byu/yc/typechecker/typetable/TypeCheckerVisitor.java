package edu.byu.yc.typechecker.typetable;

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.byu.yc.typechecker.ASTUtilities;
import edu.byu.yc.typechecker.AbstractTypeCheckerVisitor;
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
    private Set<String> primitiveTypes = new HashSet<>();
    private ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(this);

    private static final String UNKNOWN_TYPE = "$UNKNOWN";


    public TypeCheckerVisitor(SymbolTable symbolTable) {
        super(symbolTable);

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
        if (isField(name)) {
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
        } else typeTable.put(node, type);


    }

    private boolean isField(String name) {
        return getSymbolTable().fieldExists(getCurClassFQN(), name);
    }

    private boolean isMethod(String name) {
        return getSymbolTable().methodExists(getCurClassFQN(), name);
    }

    private boolean isParam(String name) {
        return getSymbolTable().parameterExists(getCurClassFQN(), getCurMethodName(), name);
    }

    public Map<ASTNode, String> getTypeTable() {
        return typeTable;
    }


}
