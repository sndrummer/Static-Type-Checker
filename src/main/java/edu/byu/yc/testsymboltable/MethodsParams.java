package edu.byu.yc.testsymboltable;

import java.util.StringJoiner;

/**
 * @author Samuel Nuttall
 */
public class MethodsParams {


    private int add(int num1, int num2) {
        return num1 + num2;
    }

    public String hello(String name) {
        return "Hello " + name;
    }

    String greeting(int age, String myName, String yourName, String favoriteSong, int timesListened) {

        String greeting1 = "Hello " + yourName + " my name is " + myName +". I am " + age + " years old";
        String greeting2 = "My favorite song is " + favoriteSong + ". I must have listened to it " +
                timesListened + " times!";

        StringJoiner joiner = new StringJoiner(greeting1, "\n", greeting2);
        return joiner.toString();

    }
}
