package edu.byu.yc.typechecker.symboltable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.byu.yc.typechecker.ASTUtilities;
import edu.byu.yc.typechecker.AbstractTypeCheckerVisitor;
import edu.byu.yc.typechecker.environment.ASTEnvironment;


/**
 * @author Samuel Nuttall
 * <p>
 * An ASTNode visitor that creates a symbol table to be used by the Type Checker
 */
public class SymbolTableVisitor extends AbstractTypeCheckerVisitor {

    private static Logger logger = LoggerFactory.getLogger(SymbolTableVisitor.class);


    public SymbolTableVisitor(SymbolTable symbolTable) {
        super(symbolTable);
    }


    /**
     * Visit FieldDeclarations and store the field types and names in the symbol table
     *
     * @param fd FieldDeclaration node being visited
     * @return true to visit children
     */
    @Override
    public boolean visit(FieldDeclaration fd) {
        String fieldName = ASTUtilities.getFieldDeclarationName(fd);

        //AST nameType binding
        ASTNameType fieldNameType = new ASTNameType(fieldName, fd.getType().toString());

        getSymbolTable().addField(getCurClassFQN(), fieldNameType);
        return true;
    }

    /**
     * Visits method declarations and stores the method return types, method names, and parameter
     * types and names in the symbol table
     *
     * @param md MethodDeclaration that is being visited
     * @return true to visit children
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(MethodDeclaration md) {

        String methodName = md.getName().toString();
        String methodType = md.getReturnType2().toString();
        setCurMethodName(methodName);

        ASTNameType method = new ASTNameType(methodName, methodType);

        List<ASTNameType> params = new ArrayList<>();
        List<ASTNameType> localVariables = new ArrayList<>();
        if (!md.parameters().isEmpty() && md.parameters().get(0) instanceof SingleVariableDeclaration) {
            List paramDeclarations = md.parameters();

            for (SingleVariableDeclaration declaration : (List<SingleVariableDeclaration>) paramDeclarations) {
                String paramName = declaration.getName().toString();
                String paramType = declaration.getType().toString();
                ASTNameType paramNT = new ASTNameType(paramName, paramType);
                params.add(paramNT);
            }
        }

        List<ASTNode> statements = md.getBody().statements();

        for (ASTNode statement : statements) {
            if (statement instanceof VariableDeclarationStatement) {
                VariableDeclarationStatement vd = ((VariableDeclarationStatement) statement);

                if (vd.fragments().get(0) instanceof VariableDeclarationFragment) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) vd.fragments().get(0);
                    String variableName = fragment.getName().toString();

                    ASTNameType localVariable = new ASTNameType(variableName, vd.getType().toString());
                    localVariables.add(localVariable);
                }
            }
        }

        getSymbolTable().addMethod(getCurClassFQN(), method, params, localVariables);
        return true;
    }

}