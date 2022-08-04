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
}
