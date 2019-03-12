package edu.byu.yc;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * 
 * @author
 *
 */
public class NoAllCapsVisitor extends ASTVisitor {

    public NoAllCapsVisitor() {
        allCaps = new HashSet<>();
    }

    private Set<String> allCaps;

    @Override
    public boolean visit(SimpleName sn) {
        String name = sn.toString();
        if (name == name.toUpperCase()) {
            allCaps.add(name);
        }
        return true;
    }

    public Set<String> getAllCaps() {
        return allCaps;
    }

}