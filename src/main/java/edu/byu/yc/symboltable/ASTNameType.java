package edu.byu.yc.symboltable;

/**
 * @author Samuel Nuttall
 *
 * Simple tuple class to organize pairings of names with their types
 */
public class ASTNameType {

    private String name;
    private String type;

    public ASTNameType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
