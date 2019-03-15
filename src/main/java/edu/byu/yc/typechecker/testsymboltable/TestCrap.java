package edu.byu.yc.typechecker.testsymboltable;

import java.util.ArrayList;

/**
 * @author Samuel Nuttall
 */
public class TestCrap {

    private static int j = 2 + 2;

    public static void main(String[] args) {

        int cheese = 5;                    //0000 0000 0101
        System.out.println(5 << 3);       //0000 0001 0100

        ArrayList<String> context = new ArrayList<>();
        context.add("Poop");
        ArrayList<String> c = new ArrayList<>(context);

        float d = 12f + 54f;
        double e = 15D + 13D;
        long longV = 12l + 13l;


        //The hierarchy is short -> int --> float -->
        // WIDENING!!!! long to float is considered widening!!!!!!

        //HERE IS THE WIDENING RULES HIERARCHY

//        short s = 1;
//        short a = 44;
//        int shortToInt = s + a;
//        double floatToDouble = 2F + 2F;
//        float longToFloat = 2l + 2l;
//        float intToFloat = 2 + 2;
//        double mixedToDouble = 4f + 2l;

        System.out.println(c);

    }
}
