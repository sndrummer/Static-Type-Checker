package edu.byu.yc.typechecker;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.byu.yc.typechecker.symboltable.SymbolTable;

/**
 * @author Samuel Nuttall
 */
public class AbstractTypeCheckerVisitor extends ASTVisitor {

    private static Logger logger = LoggerFactory.getLogger(AbstractTypeCheckerVisitor.class);
    private SymbolTable symbolTable;
    private String curClassFQN; // Current class being explored

    public AbstractTypeCheckerVisitor(SymbolTable symbolTable) {
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

}
