package edu.byu.yc.typechecker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.byu.yc.typechecker.environment.ASTEnvironment;
import edu.byu.yc.typechecker.symboltable.ASTClassValidator;
import edu.byu.yc.typechecker.symboltable.SymbolTable;

/**
 * @author Samuel Nuttall
 */
public class AbstractTypeCheckerVisitor extends ASTVisitor {

    private static Logger logger = LoggerFactory.getLogger(AbstractTypeCheckerVisitor.class);
    private SymbolTable symbolTable;
    private String curClassFQN; // Current class being explored
    private String curClassSN; // Current class simple name
    private String curMethodName; // Current class being explored

    private ASTClassValidator validator;
    private ASTEnvironment currentEnv;
    private Set<String> declaredTypeBaseNames = new HashSet<>();
    private Map<String, ASTEnvironment> nameEnvironmentMap = new HashMap<>();
    private Set<String> violations = new HashSet<>();

    public AbstractTypeCheckerVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;

        this.validator = symbolTable.getValidator();

        Set<String> declaredTypes = validator.getDeclaredTypes();

        for (String type : declaredTypes) {
            String baseName = getDeclaredTypeBaseName(type);
            declaredTypeBaseNames.add(baseName);
        }
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
        curClassSN = classSimpleName;
        curClassFQN = symbolTable.getClassSimpleToQualifiedName().get(classSimpleName);

        if (curClassFQN == null)
            logger.error("CLASS {} was not found", classSimpleName);

        ASTEnvironment parentEnvironment = null;
        String declarationName = td.getName().toString();
        ASTNode parent = td.getParent();

        if (parent instanceof TypeDeclaration) {
            TypeDeclaration parentTD = (TypeDeclaration) parent;
            String parentName = parentTD.getName().toString();
            parentEnvironment = nameEnvironmentMap.get(parentName);
        }

        currentEnv = new ASTEnvironment(declarationName, parentEnvironment);
        nameEnvironmentMap.put(declarationName, currentEnv);

        return true;
    }

    /**
     * Takes the Type node and returns its name within the context of the environment
     * in order to determine if its usage is valid. It will be compared against the declaredTypes
     * Strings to see if it matches one of the valid declaredTypes
     *
     * @param type An AST Type node
     * @return a String that gives context to the variables usage, for example Inner declared in the
     * Outer class returns Outer.Inner
     */
    private String resolveEnvironmentName(Type type) {
        ASTEnvironment rootEnv = currentEnv;
        List<String> envNames = new ArrayList<>();
        while (rootEnv != null) {
            envNames.add(rootEnv.getEnvironmentName());
            rootEnv = rootEnv.getParentEnv();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = envNames.size() - 1; i > -1; i--) {
            String envName = envNames.get(i);
            if (type.toString().equals(envName)) {
                continue;
            }
            sb.append(envName);
            if (i != 0) {
                sb.append(".");
            }
        }
        if (sb.charAt(sb.length() - 1) == '.') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Checks to see if the type that is used is contained in the valid declaredTypeBaseNames set.
     * If it is, then it is considered a valid usage and returns true.
     * If not then returns false.
     * Also takes the context into consideration to see that the environment name + type name may
     * be valid. Assumes heavily nested classes (more than 1 level) are static. Does not check that
     * Previous levels are instantiated.
     *
     * @param type AST type
     * @return boolean true or false depending on whether the type is used correctly
     */
    private boolean isValidTypeUsage(Type type) {
        boolean valid = declaredTypeBaseNames.contains(type.toString());
        if (!valid) {
            String environment = resolveEnvironmentName(type);
            valid = declaredTypeBaseNames.contains(environment + "." + type);
        }
        return valid;
    }


    /**
     * Simplifies a valid qualified name from the QualifiedClassVisitor by converting it
     * into a name that can be used within the file (removes package information), to easily
     * determine proper type usage from within a single file.
     *
     * @param qualifiedName A String of a valid qualifiedName from the QualifiedClassVisitor
     * @return String that is a simplified name
     */
    private String getDeclaredTypeBaseName(String qualifiedName) {
        String baseName = null;
        String packageName = validator.getPackageName();
        if (qualifiedName.contains(packageName)) {
            baseName = qualifiedName.replace((packageName + "."), "");
        } else {
            String[] imports = qualifiedName.split("\\.");
            baseName = imports[imports.length - 1];
        }
        return baseName;
    }



    /**
     * Returns all of the violations in a file
     *
     * @return set of String violations of type us
     */
    public Set<String> getViolations() {
        return violations;
    }

    /**
     * Visits SimpleType nodes to determine if the code being analyzed contains valid uses of a class
     * It checks if the classes used are either imported or defined within the class.
     * It does not check, however, whether an environment is static as that would overkill for this lab
     * so we will assume classes with more than 1 level of nesting are static (able to use outside)
     *
     * @param node SimpleType node
     * @return true to visit all children of the SimpleType node
     */
    @Override
    public boolean visit(SimpleType node) {
        currentEnv.addType(node);
        boolean valid = isValidTypeUsage(node);
        if (!valid) {
            violations.add(node.toString() + ":\n" + node.getParent().toString());
        } else {
            getSymbolTable().addValidType(getCurClassFQN(), node.getName().toString());
        }

        return true;
    }


    /**
     * In order to correctly identify the current environment, once a TypeDeclaration has been
     * traversed, endVisit makes the current environment the parent environment of the TD Node
     * that is now out of scope
     *
     * @param node TypeDeclaration node
     */
    @Override
    public void endVisit(TypeDeclaration node) {

        if (currentEnv != null) {
            currentEnv = currentEnv.getParentEnv();
        }
        super.endVisit(node);
    }


    /**
     * Visit a method declaration Node get the current method for the environment
     *
     * @param md methodDeclaration Node
     * @return true to visit children
     */
    @Override
    public boolean visit(MethodDeclaration md) {
        setCurMethodName(md.getName().toString());
        return true;
    }


    @Override
    public void endVisit(MethodDeclaration node) {
        setCurMethodName(null);
    }

    public String getCurClassFQN() {
        return curClassFQN;
    }

    public String getCurClassSN() {
        return curClassSN;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public String getCurMethodName() {
        return curMethodName;
    }

    public void setCurMethodName(String curMethodName) {
        this.curMethodName = curMethodName;
    }
}
