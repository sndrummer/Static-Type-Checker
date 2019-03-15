package edu.byu.yc.typechecker;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * @author Samuel Nuttall
 *
 * Utility Class for AST traversing to avoid code duplication
 */
public class ASTUtilities {

    private ASTUtilities() {

    }

    public static String getFieldDeclarationName(FieldDeclaration fd) {
        String fieldName = null;
        if (fd.fragments().get(0) instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) fd.fragments().get(0);
            fieldName = fragment.getName().toString();
        }

        return fieldName;
    }

}
