package edu.byu.yc.symboltable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * @author Samuel Nuttall
 * <p>
 * Traverse ImportDeclaration and TypeDeclaration nodes and assemble a set of types that are declared
 * in the program. The PackageDeclaration node can be used to determine the fully qualified name of
 * each declared class. import statements make classes available without qualifiers, so be sure to
 * record the unqualified class names that are imported.
 */
public class QualifiedClassVisitor extends ASTVisitor {

    private Set<String> declaredClasses = new HashSet<>();
    private String packageName = "";
    private Map<String, String> simpleNameToFullyQualifiedName = new HashMap<>();


    /**
     * Stores the import Statements in declaredClasses set so that the valid classes to be used within
     * a file can be determined
     *
     * @param node ImportDeclaration node
     * @return true to visit children
     */
    @Override
    public boolean visit(ImportDeclaration node) {
        declaredClasses.add(node.getName().toString());
        return true;
    }

    /**
     * Visits TypeDeclaration nodes and stores them in the declaredClasses set so that if the type is
     * used elsewhere in the class it can be determined valid
     *
     * @param node TypeDeclaration node
     * @return true to visit all children
     */
    @Override
    public boolean visit(TypeDeclaration node) {
        String declarationName = node.getName().toString();
        ASTNode parent = node.getParent();

        if (parent instanceof BodyDeclaration) {
            TypeDeclaration parentTD = (TypeDeclaration) parent;
            String parentQN = simpleNameToFullyQualifiedName.get(parentTD.getName().toString());
            String qualifiedName =  parentQN + "." + declarationName;
            simpleNameToFullyQualifiedName.put(declarationName, qualifiedName);
            declaredClasses.add(qualifiedName);
        }
        else {

            String qualifiedName =  packageName + "." + declarationName;
            simpleNameToFullyQualifiedName.put(declarationName, qualifiedName);
            declaredClasses.add(qualifiedName);
        }

        return true;
    }


    /**
     * Visit the packageDeclaration node and get the current package name
     *
     * @param node ASTNode Package declaration
     * @return true to visit children
     */
    @Override
    public boolean visit(PackageDeclaration node) {
        packageName = node.getName().toString();
        return true;
    }

    public Set<String> getDeclaredClasses() {
        return declaredClasses;
    }

    public String getPackageName() {
        return packageName;
    }

    public Map<String, String> getSimpleNameToFullyQualifiedName() {
        return simpleNameToFullyQualifiedName;
    }
}
