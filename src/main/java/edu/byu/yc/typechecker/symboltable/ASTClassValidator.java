package edu.byu.yc.typechecker.symboltable;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Set;

/**
 * @author Samuel Nuttall
 *
 * Helper class to store the results of QualifiedClassVisitor and pass them to the second visitor to
 * to see if the type is declared in the current environment
 */
public class ASTClassValidator {

    private Set<String> declaredTypes;
    private ASTNode rootNode;
    private String packageName;

    public ASTClassValidator(Set<String> declaredTypes, ASTNode rootNode, String packageName) {
        this.declaredTypes = declaredTypes;
        this.rootNode = rootNode;
        this.packageName = packageName;
    }

    public Set<String> getDeclaredTypes() {
        return declaredTypes;
    }

    public ASTNode getRootNode() {
        return rootNode;
    }

    public String getPackageName() {
        return packageName;
    }
}
