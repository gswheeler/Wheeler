/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.stringsort;

import wheeler.generic.data.StringHandler;

/**
 * A StringSorter subclass that endeavors to sort strings as they are by Windows Explorer.
 * Appropriate for programs that displaying things such as lists of filepaths to the user.
 */
public class WindowsExplorer extends StringSorter{
    
    /**The StringSorter subclass used to order strings similar to how filepaths are sorted by Windows Explorer.
     * @param checkCase Should case be considered when sorting strings?
     */
    public WindowsExplorer(boolean checkCase){
        super(checkCase);
    }
    
    @Override
    protected int compareStringsAfterCase(String strA, String strB){
        // A few things to keep track of as we are going through the strings
        int preference = 0; // This is set if we find a "defer" character in at least one of the strings
        
        // Loop until we have an answer
        while(true){
            // Check if either string (or both) has run out
            if ((strA.length() == 0) && (strB.length() == 0)) return preference; // Both done, return the preferred answer as determined by any 'defer' characters
            if (strA.length() == 0) return -1; // Strings are equal except second is longer
            if (strB.length() == 0) return 1;  // Strings are equal except second is shorter
            
            // Check if both strings start with numeric values
            String startsWithDigitRegex = "\\d.*";
            if((strA.matches(startsWithDigitRegex)) && (strB.matches(startsWithDigitRegex))){
                // Get the two numbers
                String numStrA = "";
                while(strA.matches(startsWithDigitRegex)){
                    numStrA += StringHandler.charAt(strA, 0);
                    strA = strA.substring(1);
                }
                String numStrB = "";
                while(strB.matches(startsWithDigitRegex)){
                    numStrB += StringHandler.charAt(strB, 0);
                    strB = strB.substring(1);
                }
                
                // Compare the two numerically (a bit more complex than it sounds, as Longs can only have so many digits)
                // First, remove any leading zeroes (do not contribute to the number's value, will come into effect later)
                String nA = StringHandler.trimLeadingCharacters(numStrA, "0");
                String nB = StringHandler.trimLeadingCharacters(numStrB, "0");
                // Now, if one number is longer than the other, it will mean its value is inherently greater
                if (nA.length() < nB.length()) return -1;
                if (nA.length() > nB.length()) return 1;
                // Their lengths match; check each digit's value to see if they hold different values
                for(int i = 0; i < nA.length(); i++){
                    int valA = numbers.indexOf(StringHandler.charAt(nA, i));
                    int valB = numbers.indexOf(StringHandler.charAt(nB, i));
                    if (valA < valB) return -1;
                    if (valA > valB) return 1;
                }
                // The numbers had the same numerical value; if one had leading zeroes, it comes first
                if (numStrA.length() > numStrB.length()) return -1;
                if (numStrA.length() < numStrB.length()) return 1;
                
                // The digits were exactly the same. Having removed them from the strings, time to loop
                continue;
            }
            
            // At this point, remove the first characters if they are the same (loop to check the following characters)
            if(StringHandler.areEqual(StringHandler.charAt(strA, 0), StringHandler.charAt(strB, 0), caseSensitive)){
                strA = strA.substring(1);
                strB = strB.substring(1);
                continue;
            }
            // From here-on-out, the strings will never start with the same character
            
            // See if there's a "defer" character in either string (or both)
            int deferIndexA = defers.indexOf(StringHandler.charAt(strA, 0));
            int deferIndexB = defers.indexOf(StringHandler.charAt(strB, 0));
            // If they both had a defer character, set preference as per how they match up (if it isn't already set)
            if((deferIndexA > -1) && (deferIndexB > -1)){
                if (preference == 0) preference = (deferIndexA < deferIndexB) ? -1 : 1; // Remember; not the same
                // Remove the skip characters and loop
                strA = strA.substring(1);
                strB = strB.substring(1);
                continue;
            }
            // If only one has a defer character, set preference accordingly (if it isn't already set)
            if(deferIndexA > -1){
                if (preference == 0) preference = 1; // Will push the first string after the second
                // Remove the skip character and loop
                strA = strA.substring(1);
                continue;
            }
            if(deferIndexB > -1){
                if (preference == 0) preference = -1; // Will push the first string before the second
                // Remove the skip character and loop
                strB = strB.substring(1);
                continue;
            }
            
            // At this point there's nothing fancy left; determine the characters' precedence indexes and compare
            // Numbers, deferrals, and matching characters have been done away with; this is the moment of truth
            int indexA = determineLexicographicIndex(StringHandler.charAt(strA, 0));
            int indexB = determineLexicographicIndex(StringHandler.charAt(strB, 0));
            if(indexA < indexB)
                return -1;
            else
                return 1;
        }
    }
    private int determineLexicographicIndex(String c){
        // Easy one; is it in the precedence characters list (index-zero)
        // Includes all printable ASCII characters that can be used in filepaths (besides filepath dividers)
        int index = precedence.indexOf(c);
        if (index > -1) return index;
        
        // Next try; is it a filepath divider (comes directly before all characters in the precedence list)
        index = dividers.indexOf(c);
        if (index > -1) return (0 - dividers.length()) + index; // Push "array-length" slots before zero, move up "index" slots
        
        // Next try; is it a printable character with an undetermined precedence (comes after the precedence characters)
        // This comes up because some characters cannot be used in filepaths
        if (StringHandler.isPrintable(c)) return precedence.length() + ((int)c.charAt(0));
        
        // Next try; is it a character outside of basic ASCII (comes after all printable characters)
        // This includes accented characters and alternate forms of some ASCII characters. Sorry.
        if (c.charAt(0) > 127) return precedence.length() + 128 + ((int)c.charAt(0));
        
        // Last try; it's an ASCII character that isn't printable (goes before all printable and non-writable characters)
        // This allows for sorting amongst tab-delineated data strings that lead with their 'sort' value (a tab goes before ANY filepath character)
        return Integer.MIN_VALUE + ((int)c.charAt(0));
    }
    
    // The various ASCII characters that can be found in Windows filepaths
    protected String numbers = "0123456789"; // Numbers in order of increasing value
    protected String precedence = 
              " !#$%&(),.;@[]^_`{}~+="  // All of the 'special' characters allowed by Windows
            + "0123456789"              // Numbers in order of increasing value
            + "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ" // The alphabet, lower and upper case side-by-side
        ;
    protected String defers = "'-";    // Characters that only play a part if the strings are otherwise equal
    protected String dividers = "\\/"; // Characters used to divide segments of a filepath
    
}
