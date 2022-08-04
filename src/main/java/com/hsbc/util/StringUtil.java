package com.hsbc.util;

import java.util.Random;

/**
 * Utility for string handles
 * @author Leen Li
 */
public class StringUtil {

    /**
     * Check if the input string is empty
     * @param input string to be checked
     * @return weather string is empty.
     */
    public static boolean isEmpty(String input){
        return input == null || input.isEmpty() || input.trim().isEmpty();// trim should be replaced by strip() in java 11 or 17.
    }

    /**
     * get random string for specific length
     * @param length the length of return string
     * @return random string with length of input "length".
     */
    @Deprecated
    public static String randomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);//26小写+26大写+10个数字
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
