package edu.byu.yc.tests;
import java.lang.String;

public class Widening {


    public static int widen() {

        float d = 12f + 54f;
        double e = 15D + 13D;
        long longV = 12l + 13l;
        long l = 13L;
        float a = 11.2f;
        double asdf = 15D + 13L;

        return 5 + 2;
    }

    public static int invalid() {

        float d = 12D + 54f;
        float a = 11.2 + 2;

        return a;
    }



}