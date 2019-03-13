package edu.byu.yc.symboltable;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Samuel Nuttall
 *
 * An ASTNode visitor that creates a symbol table to be used by the Type Checker
 */
public class SymbolTableVisitor extends ASTVisitor {

    private static Logger logger = LoggerFactory.getLogger(SymbolTableVisitor.class);
    private SymbolTable symbolTable;
    private String curClassFQN; // Current class being explored

    public SymbolTableVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * Visit TypeDeclaration nodes once more to keep track of the current class being visited
     *
     * @param td TypeDeclaration Node being visited
     * @return true to visit children
     */
    @Override
    public boolean visit(TypeDeclaration td) {

        String classSimpleName = td.getName().toString();
        logger.info("Class name {}", classSimpleName);
        curClassFQN = symbolTable.getClassSimpleToQualifiedName().get(classSimpleName);

        if (curClassFQN == null)
            logger.error("CLASS {} was not found", classSimpleName);

        return true;
    }

    /**
     * Visit FieldDeclarations and store the field types and names in the symbol table
     *
     * @param fd FieldDeclaration node being visited
     * @return true to visit children
     */
    @Override
    public boolean visit(FieldDeclaration fd) {
        String fieldName = null;
        if (fd.fragments().get(0) instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fd.fragments().get(0);
            fieldName = fragment.getName().toString();
        }

        ASTNameType fieldNameType = new ASTNameType(fieldName, fd.getType().toString());

        logger.info("Adding new field {}", fieldNameType);
        symbolTable.addField(curClassFQN, fieldNameType);
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

        ASTNameType method = new ASTNameType(methodName, methodType);

        List<ASTNameType> params = new ArrayList<>();
        if (!md.parameters().isEmpty() && md.parameters().get(0) instanceof SingleVariableDeclaration) {
            List paramDeclarations = md.parameters();

            for (SingleVariableDeclaration declaration : (List<SingleVariableDeclaration>) paramDeclarations) {
                String paramName = declaration.getName().toString();
                String paramType = declaration.getType().toString();
                ASTNameType paramNT = new ASTNameType(paramName, paramType);
                params.add(paramNT);
            }
        }

        logger.info("Adding new method {} {}", method, params);
        symbolTable.addMethod(curClassFQN, method, params);
        return true;
    }


}