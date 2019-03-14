package edu.byu.yc.typechecker.testsymboltable;

import java.util.ArrayList;

/**
 * @author Samuel Nuttall
 */
public class TestCrap {

    public static void main(String[] args) {

        int cheese = 5;                    //0000 0000 0101
        System.out.println(5 << 3);       //0000 0001 0100

        ArrayList<String> context = new ArrayList<>();
        context.add("Poop");
        ArrayList<String> c = new ArrayList<>(context);

        float d = 12f + 54f;
        double e = 15D + 13D;
        long longV = 12l + 13l;


        System.out.println(c);

    }
}
