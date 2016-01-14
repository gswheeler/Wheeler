/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import wheeler.generic.error.QuietException;

/**
 * Class containing static functions that handle math-related logic
 */
public class MathHandler {
    
    public static int max(int iA, int iB){
        return (iA > iB) ? iA : iB;
    }
    
    
    public static int min(int iA, int iB){
        return (iA < iB) ? iA : iB;
    }
    
    
    public static int getRandomNumber(int range){ return getRandomNumber(0,range-1); }
    public static int getRandomNumber(int lowerBound, int upperBound){
        return ((int) (Math.random() * (upperBound - lowerBound + 1))) + lowerBound;
    }
    
    
    public static int greedyDivision(int number, int divisor){
        int offset = (number % divisor > 0) ? 1 : 0;
        return (number / divisor) + offset;
    }
    public static long greedyDivision64(long number, long divisor){
        long offset = (number % divisor > 0) ? 1 : 0;
        return (number / divisor) + offset;
    }
    
    
    public static int exp(int base, int pwr){
        int result = 1;
        for (int i = 0; i < pwr; i++) result *= base;
        return result;
    }
    public static long exp64(long base, long pwr){
        long result = 1;
        for (long i = 0; i < pwr; i++) result *= base;
        return result;
    }
    
    
    /**Takes a number and negates it. Throws an exception if this cannot be done (if it is bitwise impossible).
     * @param value The number to negate. Cannot be equal to Integer.MIN_VALUE.
     * @return The provided value negated (negative if positive, positive if negative)
     */
    public static long negate(int value){
        if (value == Integer.MIN_VALUE)
            throw new QuietException("Tried to negate " + value + ", which is not bitwise possible");
        return ((-1)*value);
    }
    /**Takes a Long value and negates it. Throws an exception if this cannot be done (if it is bitwise impossible).
     * @param value The number to negate. Cannot be equal to Long.MIN_VALUE.
     * @return The provided value negated (negative if positive, positive if negative)
     */
    public static long negate64(long value){
        if (value == Long.MIN_VALUE)
            throw new QuietException("Tried to negate " + value + ", which is not bitwise possible");
        return (((long)(-1))*value);
    }
}
