/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import wheeler.generic.error.QuietException;
import wheeler.generic.structs.IntegerNode;
import wheeler.generic.structs.StringList;

/**
 * Class containing static functions that handle math-related logic
 */
public class MathHandler {
    
    /**Returns the larger of two numbers
     * @param iA One number to compare
     * @param iB A second number to compare
     * @return A number equal to the larger of the two provided numbers
     */
    public static int max(int iA, int iB){
        return (iA > iB) ? iA : iB;
    }
    
    
    /**Returns the lesser of two numbers
     * @param iA One number to compare
     * @param iB A second number to compare
     * @return A number equal to the lesser of the two provided numbers
     */
    public static int min(int iA, int iB){
        return (iA < iB) ? iA : iB;
    }
    
    
    /**Returns a random number based on a length range, useful for choosing a random item from an array.
     * @param range The length of the candidates range
     * @return A number between zero and range-1.
     */
    public static int getRandomNumber(int range){ return getRandomNumber(0,range-1); }
    /**Returns a random number based on the provided bounds.
     * For what it's worth, the function will return "correct" results when the arguments are swapped.
     * However, this will effectively remove the two smallest candidates (besides lowerbound) from the candidate pool.
     * So watch the order.
     * @param lowerBound The lesser boundary of the candidate range
     * @param upperBound The greater boundary of the candidate range
     * @return A number that is either equal to one of the boundary values or a value greater than the lesser and less than the greater
     */
    public static int getRandomNumber(int lowerBound, int upperBound){
        return ((int) (Math.random() * (upperBound - lowerBound + 1))) + lowerBound;
    }
    
    
    /**Divides one number by another number, adding one if there's a remainder
     * @param number The number to divide
     * @param divisor The number to divide the first by
     * @return If the first number is divisible by the second, returns the result. Otherwise, returns the result plus one.
     */
    public static int greedyDivision(int number, int divisor){
        int offset = (number % divisor > 0) ? 1 : 0;
        return (number / divisor) + offset;
    }
    /**Divides one number by another number, adding one if there's a remainder, 64-bit version
     * @param number The number to divide
     * @param divisor The number to divide the first by
     * @return If the first number is divisible by the second, returns the result. Otherwise, returns the result plus one.
     */
    public static long greedyDivision64(long number, long divisor){
        long offset = (number % divisor > 0) ? 1 : 0;
        return (number / divisor) + offset;
    }
    
    
    /**Performs an exponential multiplication using the two provided numbers
     * @param base The multiplication's base value
     * @param pwr The multiplication's power value
     * @return A number equal to base-to-the-pwr-th-power
     */
    public static int exp(int base, int pwr){
        int result = 1;
        for (int i = 0; i < pwr; i++) result *= base;
        return result;
    }
    /**Performs an exponential multiplication using the two provided numbers, 64-bit version
     * @param base The multiplication's base value
     * @param pwr The multiplication's power value
     * @return A number equal to base-to-the-pwr-th-power
     */
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
    
    
    /**A file we use to store prime numbers we identified in previous calls
     * @return The file's filepath
     */
    protected static String primeNumbersCache(){
        return FileHandler.composeFilepath(FileHandler.wheelerMathFolder(), "primes.txt");
    }
    /** A linked list of IntegerNodes containing all of the primes we've found so far, in ascending order */
    protected static IntegerNode primeNumbersList = null;
    
    
    /**Returns a prime number that is at least the provided minimum value
     * @param minValue The smallest number that the prime can be
     * @return The smallest prime number that is greater than or equal to the provided minimum value
     * @throws Exception In the event that there's a problem using our cache file or we fail to find a prime
     */
    public static int getPrime(int minValue) throws Exception{
        // If we haven't already pulled from/created the file, do so now
        if(primeNumbersList == null){
            // Check if we have cached primes from a previous call
            if(FileHandler.fileExists(primeNumbersCache())){
                // We have primes from earlier; read them in
                StringList primeStrings = FileHandler.readFile(primeNumbersCache(), true, true);
                // Start the list with the first prime (should always have at least one)
                primeNumbersList = new IntegerNode(Integer.valueOf(primeStrings.pullFirst()));
                IntegerNode lastPrime = primeNumbersList;
                // Read in the remaining primes
                while(primeStrings.any()){
                    lastPrime.next = new IntegerNode(Integer.valueOf(primeStrings.pullFirst()));
                    lastPrime = lastPrime.next;
                }
            }else{
                // Need to start from scratch. Leave us stuff to work with next time
                int[] firstPrimes = {2, 3, 5, 7, 11};
                IntegerNode lastPrime = null;
                FileHandler.ensureFolderExists(FileHandler.getParentFolder(primeNumbersCache()));
                for(int prime : firstPrimes){
                    // Check if this is the first prime
                    if(lastPrime == null){
                        // Start the list using the first prime
                        primeNumbersList = new IntegerNode(prime);
                        lastPrime = primeNumbersList;
                    }else{
                        // Add the prime to the list
                        lastPrime.next = new IntegerNode(prime);
                        lastPrime = lastPrime.next;
                    }
                    // Append each prime to the cached list
                    FileHandler.appendToFile(Integer.toString(prime), true, primeNumbersCache());
                }
            }
        }
        
        // Look for a qualified prime in our list
        IntegerNode lastPrime = primeNumbersList;
        while(true){
            if(lastPrime.value < minValue){
                // If this node contains the last prime, break and start prime-hunting
                // Keep the reference to the node so we can add nodes after it
                if (lastPrime.next == null) break;
                // Otherwise, move to the next prime in the list and check that
                lastPrime = lastPrime.next;
            }else{
                // We have a prime at least minValue; return it
                return lastPrime.value;
            }
        }
        
        // The prime for us lies beyond our composed list; iterate until we find it, marking new primes along the way
        int candidate = lastPrime.value;
        while(lastPrime.value < minValue){
            // The only even prime is 2. Because we added that earlier along with 3, lastPrime will always be odd.
            // Add 2 to get the next odd number
            candidate += 2;
            
            // A quick check; if we looped back to negative numbers it means we exceeded Integer.MAX_VALUE without finding anything
            if (candidate < 0)
                throw new QuietException("Failed to find a prime number larger than " + minValue);
            
            // The idea of a prime number is that it is evenly divisible by only itself and one.
            // All numbers can be broken down into a product of prime numbers.
            // If we go through our list of prime numbers and the candidate isn't divisible by any of them...
            //   we'll have the next prime number!
            // Once the check had been completed, loop to see if we have a new greatest-prime that is at least minValue
            IntegerNode node = primeNumbersList;
            while(true){
                // Dual check
                // 1: If we're out of primes, the candidate wasn't divisible by any of them and is thus prime
                // 2: If the current prime times itself is greater than the candidate, the candidate can only be
                //      formed using two numbers less than the prime or by one number less than the prime and
                //      another number greather than or equal to the prime. Since all primes less than the prime
                //      have been checked, neither case is possible and the candidate is thus prime
                if((node == null) || (node.value * node.value > candidate)){
                    // Put the prime at the end of the list and add to the cache file
                    lastPrime.next = new IntegerNode(candidate);
                    lastPrime = lastPrime.next;
                    FileHandler.appendToFile(Integer.toString(candidate), true, primeNumbersCache());
                    // Break from this loop and see if the new greatest prime is large enough
                    break;
                }
                
                
                // Check if the number is divisible by this prime; if so, the candidate isn't a prime number
                if (candidate % node.value == 0) break;
                
                // Otherwise, loop and check the next prime
                node = node.next;
            }
        }
        
        // At this point we had to look for a larger prime that was at least minValue and then found it
        return lastPrime.value;
    }
}
