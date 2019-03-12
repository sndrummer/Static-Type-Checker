package edu.byu.yc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author
 *
 */
public class SymbolTableVisitor extends TypeCheckerAbstractVisitor {
    private Logger logger = LoggerFactory.getLogger(SymbolTableVisitor.class);
    private Map<List<String>, String> table;

    public SymbolTableVisitor() {
        table = new HashMap<>();
        context = new ArrayList<>();
    }
    
    @Override
    public boolean visit(TypeDeclaration td) {
        super.visit(td);
        ArrayList<String> fqn = new ArrayList<>(context);
        table.put(fqn, String.join(".", fqn));
        return true;
    }

    @Override
    public boolean visit(FieldDeclaration fd) {
        Type t = fd.getType();
        for (Object o : fd.fragments()) {
            VariableDeclarationFragment f = (VariableDeclarationFragment) o;
            ArrayList<String> fqn = new ArrayList<>(context);
            fqn.add(f.getName().getIdentifier());
            logger.debug("Adding binding: {} -> {}", fqn, t);
            table.put(fqn, getFQN(t));
        }
        return true;
    }

    public Map<List<String>, String> getSymbolTable() {
        return table;
    }

}