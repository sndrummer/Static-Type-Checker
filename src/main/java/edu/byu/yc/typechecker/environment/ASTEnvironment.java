package edu.byu.yc.typechecker.environment;

import org.eclipse.jdt.core.dom.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Samuel Nuttall
 *
 * ASTEnvironment class used to store information about the environment of an ASTNode
 */
public class ASTEnvironment {

    private List<ASTEnvironment> childEnvironments = new ArrayList<>();
    private String environmentName;
    private List<Type> types = new ArrayList<>();
    private ASTEnvironment parentEnv;
    private int numChildrenEnv = 0;

    private Map<String, Type> nameTypeMap;

    public ASTEnvironment(String environmentName, ASTEnvironment parentEnv) {
        this.environmentName = environmentName;
        this.parentEnv = parentEnv;

        if (parentEnv != null) {
            parentEnv.addChildEnvironment(this);
        }
    }

    /**
     * Add a child environment and return the child that was added
     *
     * @param env environment to add
     * @return child
     */
    public ASTEnvironment addChildEnvironment(ASTEnvironment env) {
        childEnvironments.add(env);
        numChildrenEnv++;
        return childEnvironments.get(childEnvironments.size()-1);
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public List<ASTEnvironment> getChildEnvironments() {
        return childEnvironments;
    }

    public List getTypes() {
        return types;
    }

    public ASTEnvironment getParentEnv() {
        return parentEnv;
    }

    public int getNumChildrenEnv() {
        return numChildrenEnv;
    }

    public Map<String, Type> getNameTypeMap() {
        return nameTypeMap;
    }


    public void addType(Type type) {
        types.add(type);
    }

    @Override
    public String toString() {
        return "ASTEnvironment{" +
                "environmentName='" + environmentName + '\'' +
                ", types=" + types +
                ", parentEnv=" + parentEnv +
                ", numChildrenEnv=" + numChildrenEnv +
                '}';
    }
}
