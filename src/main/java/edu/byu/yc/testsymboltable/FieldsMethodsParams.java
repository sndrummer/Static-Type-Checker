package edu.byu.yc.testsymboltable;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author Samuel Nuttall
 */
public class FieldsMethodsParams {

    private String cheese = "CHEEEEESE";

    public int num = 1;

    float fl = 4f;

    private ASTNode node;

    private int add(int num1, int num2) {
        return num1 + num2 + (int)fl;
    }

    public String hello(String name) {

        return "Hello " + name + "I LIKE " + cheese;
    }

}
