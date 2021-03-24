package com.serli.oracle.of.bacon.utils;

public final class Utils {
    private Utils(){}

    public static String getenv(String name, String defaultValue){
        return System.getenv(name) != null ? System.getenv(name) : defaultValue;
    }

    public static void print(String msg, String content){
        System.out.println(msg + ":" + content);
    }
}
