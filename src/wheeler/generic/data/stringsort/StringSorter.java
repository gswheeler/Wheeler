/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.stringsort;

/**
 * A class designed to handle the determination of whether one string comes before another string.
 * For best results, if a program sets the StringSorter used by StringHandler at some point, do so when the program is being initialized.
 */
public abstract class StringSorter {
    
    /**Constructor for the StringSort base class.
     * @param checkCase Is the operation case-sensitive?
     */
    protected StringSorter(boolean checkCase){
        caseSensitive = checkCase;
    }
    
    public boolean caseSensitive;
    
    /**Compares two strings to see which comes before the other (if such is the case).
     * @param strA The string against which the second string is compared
     * @param strB The string being compared against the first string
     * @return -1 if the first string before the second, 1 if it comes after, or 0 if the two are relationally equal
     */
    public int compareStrings(String strA, String strB){
        // If the operation isn't case-sensitive, make both strings lower-case before comparing them.
        if(!caseSensitive){
            strA = strA.toLowerCase();
            strB = strB.toLowerCase();
        }
        
        // Run the check
        return compareStringsAfterCase(strA, strB);
    }
    
    /**Compare two strings to see which comes before the other (if such is the case).
     *  The comparison is made as if the operation were case-sensitive
     *  (the strings will have been made lower-case if such is not the case).
     * @param strA The string against which the second string is compared (set to lowercase if operation is not case-sensitive)
     * @param strB The string being compared against the first string (set to lowercase if operation is not case-sensitive)
     * @return -1 if the first string before the second, 1 if it comes after, or 0 if the two are relationally equal
     */
    protected abstract int compareStringsAfterCase(String strA, String strB);
    
}
