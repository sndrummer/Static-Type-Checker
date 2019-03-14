package edu.byu.yc.ref;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeCheckerAbstractVisitor extends ASTVisitor {
    private Logger logger = LoggerFactory
            .getLogger(TypeCheckerAbstractVisitor.class);
    protected ArrayList<String> context;
    protected static final String UNKNOWN_TYPE = "$UNKNOWN";

    public TypeCheckerAbstractVisitor() {
        context = new ArrayList<>();
    }

    @Override
    public boolean visit(PackageDeclaration pd) {
        String name = pd.getName().getFullyQualifiedName();
        if (context.isEmpty()
                || (context.size() == 1 && context.get(0).equals(name))) {
            context.add(name);
        } else {
            logger.warn(
                    "Found a package declaration but the current context is {}",
                    context);
        }
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration td) {
        String name = td.getName().getIdentifier();
        context.add(name);
        return true;
    }

    @Override
    public void endVisit(TypeDeclaration td) {
        String name = td.getName().getIdentifier();
        int last = context.size() - 1;
        String expected = context.get(last);
        if (name.equals(expected)) {
            context.remove(last);
        } else {
            logger.warn("Leaving declaration of {} but {} is the last name",
                    name, expected);
        }
    }

    protected String getFQN(Type t) {
        if (t.isPrimitiveType()) {
            return t.toString();
        }
        if (t.isSimpleType()) {
            SimpleType st = (SimpleType) t;
            Name n = st.getName();
            LinkedList<String> fqn = new LinkedList<>();
            while (n.isQualifiedName()) {
                QualifiedName qn = (QualifiedName) n;
                String name = qn.getName().getIdentifier();
                fqn.addFirst(name);
                n = qn.getQualifier();
            }
            if (n.isSimpleName()) {
                SimpleName sn = (SimpleName) n;
                fqn.addFirst(sn.getIdentifier());
            } else {
                logger.warn("Unexpected name type: {}", n);
            }
            return String.join(".", fqn);
        }
        return UNKNOWN_TYPE;
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
            return "float";
        }
        if (token.endsWith("l") || token.endsWith("L")) {
            return "long";
        }
        if (token.endsWith("d") || token.endsWith("D")) {
            return "double";
        }
        if (token.indexOf('.') == -1) {
            return "int";
        } else {
            return "double";
        }
    }

}
