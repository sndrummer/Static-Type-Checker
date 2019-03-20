package edu.byu.yc.typechecker.testsymboltable;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;

import java.lang.String;

/**
 * @author Samuel Nuttall
 */
public class TestCrap {

    private static int j = 2 + 2;

    //private static ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();

    public static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {

        int aaa = 5;                    //0000 0000 0101
        System.out.println(5 << 3);       //0000 0001 0100

        ArrayList<String> context = new ArrayList<>();
        context.add("Poop");
        ArrayList<String> c = new ArrayList<>(context);


        double asdf = 15D + 13L;

        String poop = "poop";
        String notPoop = "not poop";
        char aChar = 'a';

        String newPoop = poop + notPoop + ' ' + aChar;

        ASTNode node = null;

        System.out.println(newPoop);


        boolean numeric = isNumeric("5.12f");
        boolean numeric1 = isNumeric("5.12d");
        boolean numeric2 = isNumeric("5.12");
        boolean numeric3 = isNumeric("15D");
        boolean numeric4 = isNumeric("13123123l");
        boolean numeric5 = isNumeric("13L");
        boolean numeric6 = isNumeric("13.4l");
        boolean numeric7 = isNumeric("a15D");

        System.out.println("Is numeric? " + numeric);
        System.out.println("Is numeric?1 " + numeric1);
        System.out.println("Is numeric?2 " + numeric2);
        System.out.println("Is numeric?3 " + numeric3);
        System.out.println("Is numeric?4 " + numeric4);
        System.out.println("Is numeric?5 " + numeric5);
        System.out.println("Is numeric?6 " + numeric6);
        System.out.println("Is numeric?7 " + numeric7);




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

       //expressionEvaluator.test();

    }
}
