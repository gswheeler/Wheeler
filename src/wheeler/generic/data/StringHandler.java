/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import java.util.concurrent.atomic.AtomicLong;
import wheeler.generic.data.stringsort.JavaDefault;
import wheeler.generic.data.stringsort.StringSorter;
import wheeler.generic.error.QuietException;
import wheeler.generic.structs.IStringList;
import wheeler.generic.structs.IStringNode;
import wheeler.generic.structs.StringSimpleList;

/**
 * Class containing static functions that handle string-related logic
 */
public class StringHandler {
    
    /// Constructors
    protected StringHandler(){}
    
    
    
    /// Variables
    public static String[] alphaArray = {
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
        "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    public static String[] alphaNumeric = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
        "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    public static String[] hexadecimal = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
        "a", "b", "c", "d", "e", "f"};
    public static String whitespaceChars = "\r\n\t ";
    
    public static StringSorter stringSorter = new JavaDefault(false);
    
    
    
    /// Functions
    
    // 
    public static boolean contains(String subject, String check){
        return contains(subject, check, stringSorter.caseSensitive);
    }
    @SuppressWarnings("IndexOfReplaceableByContains") // IndexOf tends to work better (don't have to worry about special characters)
    public static boolean contains(String subject, String check, boolean caseSensitive){
        if(!caseSensitive){
            subject = subject.toLowerCase();
            check = check.toLowerCase();
        }
        return (subject.indexOf(check) != -1);
    }
    
    /**Check to see if the contents of two string objects are equal.
     *  Treats nulls as legitimate values.
     *  Uses the current StringSorter's check-case value to determine if case should be considered.
     * @param subject One string being looked at
     * @param check The string the first string is being compared to
     * @return True if both strings are null or non-null and have matching values (after case), false otherwise
     */
    public static boolean areEqual(String subject, String check){
        return areEqual(subject, check, stringSorter.caseSensitive);
    }
    /**Check to see if the contents of two string objects are equal. Treats nulls as legitimate values.
     * @param subject One string being looked at
     * @param check The string the first string is being compared to
     * @param caseSensitive Should case differences result in the strings being unequal
     * @return True if both strings are null or non-null and have matching values (after case), false otherwise
     */
    public static boolean areEqual(String subject, String check, boolean caseSensitive){
        if ((subject == null) && (check == null)) return true;  // They are both null, return true
        if ((subject == null) || (check == null)) return false; // One is null but not both, return false
        
        // Handle case sensitivity
        if(!caseSensitive){
            subject = subject.toLowerCase();
            check = check.toLowerCase();
        }
        
        // Perform the check and return the result
        return subject.equals(check);
    }
    
    
    // Make all characters lowercase, sparing escaped characters if specified
    public static String toLowerCase(String subject, boolean preserveEscaped){
        if (!preserveEscaped) return subject.toLowerCase();
        
        // While there are back-slashes, lowercase them and any preceding characters
        // while preserving the case of the characters following the slashes
        String processed = "";
        int sIndex;
        while((sIndex = subject.indexOf("\\")) != -1){
            // Add all characters up to and including the first slash
            processed += subject.substring(0, sIndex + 1).toLowerCase();
            // Remove these characters from the string
            subject = subject.substring(sIndex + 1);
            
            // If there are characters after the slash, add the first one
            // (understood as the escaped character that must be preserved)
            if(subject.length() > 0){
                processed += subject.substring(0, 1);
                subject = subject.substring(1);
            }
            // Loop around to handle the next slash, if one exists
        }
        
        // Stick the processed characters and the remaining characters together and return
        return processed + subject.toLowerCase();
    }
    
    
    // Remove trailing newlines from a string (both carriage return and newline characters)
    public static String trimNewlines(String str){
        return trimTrailingCharacters(str, "\r\n");
    }
    
    
    // Remove specified characters from the end of a string
    public static String trimTrailingCharacters(String str, String targets){
        if (str == null) return null;
        while(str.length() > 0 && contains(targets, charAt(str, str.length()-1), false))
            str = str.substring(0, str.length()-1);
        return str;
    }
    
    
    // Remove specified characters from the start of a string
    public static String trimLeadingCharacters(String str, String targets){
        if (str == null) return null;
        while (str.length() > 0 && contains(targets, charAt(str, 0), false))
            str = str.substring(1);
        return str;
    }
    
    
    // Remove a specified string from the end of a string if it ends with it (only trims once)
    public static String trimTailIfPresent(String str, String tail){
        return (str.toLowerCase().endsWith(tail.toLowerCase()))
                ? str.substring(0, str.length() - tail.length())
                : str;
    }
    
    
    // Remove a specified string from the start of a string if it starts with it (only trims once)
    public static String trimLeadIfPresent(String str, String lead){
        return (str.toLowerCase().startsWith(lead.toLowerCase()))
                ? str.substring(lead.length())
                : str;
    }
    
    
    // Remove characters from the start and end of a string
    public static String trimWhitespace(String subject){
        return trim(subject, whitespaceChars);
    }
    public static String trim(String subject, String targets){
        return trimTrailingCharacters(
                trimLeadingCharacters(subject, targets),
                targets
            );
    }
    
    
    // Put quotes around a string
    public static String addQuotes(String subject){ return "\"" + subject + "\""; }
    
    
    // Parse a string into an array of strings based on the provided separator
    public static String[] parseIntoArray(String subject, String separator){
        // If we're passed an empty string, return an empty array
        if (subject.equals("")) return new String[0];
        
        // We cannot use an empty string as a separator
        if (separator.equals(""))
            throw new QuietException("Cannot parse a string into an array using an empty string");
        
        // Get everything before the next separator string, recursive
        StringSimpleList list = new StringSimpleList();
        while(contains(subject, separator)){
            list.add(subject.substring(0, subject.indexOf(separator)));
            subject = subject.substring(subject.indexOf(separator) + separator.length());
        }
        
        // Add the leftovers; by our logic, starting or ending with the separator results in empty-string entries
        list.add(subject);
        
        // Return the list as an array
        return list.toArray();
    }
    
    
    // Take in an array of strings and concatenate them into a single string
    public static String concatArray(String[] array, String separator){
        // If the array is empty, return an empty string
        if (array.length == 0) return "";
        
        // The result string starts with the first item (no separator)
        String result = array[0];
        
        // Add all subsequent strings, each preceded by the separator
        for (int i = 1; i < array.length; i++) result += separator + array[i];
        
        // Return the result
        return result;
    }
    // Take in a list of strings and concatenate them into a single string
    public static String concatList(IStringList list, String separator){
        // If the list is empty, return an empty string
        IStringNode node = list.getHeader().getNext();
        if (node == null) return "";
        
        // The result string starts with the first item (no separator)
        String result = node.getValue();
        
        // Add all subsequent strings, each preceded by the separator
        while ((node = node.getNext()) != null) result += separator + node.getValue();
        
        // Return the result
        return result;
    }
    
    
    // Check if an array is sorted
    public static boolean arrayIsSorted(String[] array){
        return arrayIsSorted(array, stringSorter);
    }
    public static boolean arrayIsSorted(String[] array, StringSorter sorter){
        for(int i = 0; i < array.length - 1; i++){
            if (strAafterB(array[i], array[i+1], sorter)) return false;
        }
        return true;
    }
    
    
    // Check if an array contains a certain string
    /*public static boolean arrayContains(String[] array, String subject){ return arrayContains(array, subject, false); }
    public static boolean arrayContains(String[] array, String subject, boolean checkCase){
        for(int i = 0; i < array.length; i++){
            if (areEqual(array[i], subject, checkCase)) return true;
        }
        return false;
    }*/
    
    
    /**
     * Convert a number into a string with commas delineating thousands, millions, etc
     * @param number The number to format. Can be positive or negative.
     * @return A string, with comma-delineation, representing the provided number.
     * @throws Exception Thrown by commaDelineateNumber if the number, converted to a string, was not in an expected format.
     */
    public static String commaDelineate(int number) throws Exception{
        return commaDelineateNumber(Integer.toString(number));
    }
    /**
     * Convert a Long into a string with commas delineating thousands, millions, etc
     * @param number The Long with the value to format. Can be positive or negative.
     * @return A string, with comma-delineation, representing the provided number.
     * @throws Exception Thrown by commaDelineateNumber if the number, converted to a string, was not in an expected format.
     */
    public static String commaDelineate64(long number) throws Exception{
        return commaDelineateNumber(Long.toString(number));
    }
    /**
     * Take in a numerical string, add commas to delineate thousands, millions, etc
     * @param number The numerical string to format. Can start with +/- and can have a decimal value with at least one digit.
     * @return A string representing the provided number, now with comma-delineation
     * @throws Exception If the provided string was not in the proper format.
     */
    public static String commaDelineateNumber(String number) throws Exception{
        // Make sure we're dealing with an actual number string
        String format = "(|\\+|\\-)\\d+(|(\\.\\d+))";
        if (!number.matches(format))
            throw new Exception("Number string " + number + "does not match the expected format (" + format + ")");
        
        // Grab the sign if there is one
        String sign = "";
        if(number.startsWith("+") || number.startsWith("-")){
            sign = number.substring(0, 1);
            number = number.substring(1);
        }
        
        // Take off the decimal if one exists (don't comma-delineate that)
        String decimal = "";
        int decimalIndex = number.indexOf(".");
        if(decimalIndex > -1){
            decimal = number.substring(decimalIndex);
            number = number.substring(0, decimalIndex);
        }
        
        // For every three digits at the end of the string that are preceded by at least one digit,
        // add a comma followed by those three digits
        String formatted = "";
        while(number.length() > 3){
            formatted = "," + number.substring(number.length() - 3) + formatted;
            number = number.substring(0, number.length() - 3);
        }
        
        // Return the sign, the leading digits, the digits preceded by commas, and the decimal
        return sign + number + formatted + decimal;
    }
    
    
    // Translate a byte size into a "readable" format
    public static String toReadableFileSize(long lngBytes){
        String[] suffixes = {"B", "KB", "MB", "GB", "TB"}; int index = 0;
        Double bytes = Double.valueOf(Long.toString(lngBytes));
        while((bytes >= 1000.0) && (index < suffixes.length - 1)){
            bytes = bytes / 1024.0;
            index++;
        }
        int strLen = (bytes < 100.0) ? 4 : 3;           // Format is either 100, 99.9, or 0.00
        String byteStr = Double.toString(bytes);
        if (!contains(byteStr, ".")) byteStr += ".00";  // Make sure trailing decimals are in place, whether they are needed or not
        byteStr = (byteStr.length() > strLen)           // Remove unnecessary digits as well as the decimal point if appropriate
                ? byteStr.substring(0, strLen)
                : byteStr;
        // Don't bother with a decimal if we're dealing with raw bytes
        if ((index == 0) && (contains(byteStr, "."))) byteStr = byteStr.substring(0, byteStr.indexOf("."));
        return byteStr + " " + suffixes[index];
    }
    
    
    // Pad a file with leading zeros
    public static String leadingZeroes(int number, int minSize){
        String result = Integer.toString(number);
        for (int i = result.length(); i < minSize; i++) result = "0" + result;
        return result;
    }
    
    
    /**Convert a number to an alphanumeric string unique to that number.
     * Takes the base-ten system and converts the number to a base-36 string (the ten integers followed by the 26 letters).
     * If the number is a negative, converts to a positive and adds a leading zero.
     * @param num The number to convert
     * @return The provided number represented as base-36 characters (numerical and alphabetical)
     */
    public static String toAlphanumeric(long num){
        if (num == 0) return "0";
        if (num == Long.MIN_VALUE) return "01y2p0ij32e8e8";
        
        // A hyphen is not alphanumeric; if the number is a negative, use a leading zero instead (only zero starts with 0, and that can't be negative)
        String lead = "";
        if(num < 0){ lead = "0"; num = MathHandler.negate64(num); }
        
        // Starting from the ones slot, modulus by the base and add the appropriate character
        String result = "";
        long base = StringHandler.alphaNumeric.length;
        while(num > 0){
            result = StringHandler.alphaNumeric[(int)(num % base)] + result;
            num = num / base;
        }
        return lead + result;
    }
    
    
    /**Sorts an array of strings. Uses bubble-sorting.
     * Sorts according to the StringSorter object assigned to the stringSorter variable.
     * @param strArray The array of strings to sort. Order and contents are not changed.
     * @return A new array composed of the contents of the original array in sorted order.
     */
    public static String[] sortStrings(String[] strArray){
        return sortStrings(strArray, stringSorter);
    }
    /**Sorts an array of strings. Uses bubble-sorting
     * @param strArray The array of strings to sort. Order and contents are not changed.
     * @param sorter The StringSorter object to use when sorting the array.
     * @return A new array composed of the contents of the original array in sorted order.
     */
    public static String[] sortStrings(String[] strArray, StringSorter sorter){
        // The array we're going to be putting the strings into
        String[] result = new String[strArray.length];
        
        // For each item in the original array, insert and sort
        for(int i = 0; i < strArray.length; i++){
            // Insert the next item
            result[i] = strArray[i];
            
            // Moving from the insertion index up to the second item,
            // swap with the preceding item as long as the current item comes before the preceding item
            for(int j = i; j > 0; j--){
                // If the next item up does not come after the current item, no more swapping
                if (!strAafterB(result[j-1], result[j], sorter)) break;
                // The current item comes before the next item up; swap them
                LogicHandler.swap(result, j-1, j);
            }
        }
        
        // Return the result
        return result;
    }
    
    
    // Compare two strings; -1 if the first comes first, +1 if the second comes first, 0 if they are the same
    
    /**Does the first string come before the second?
     * The comparison is made using the StringSorter object assigned to the stringSorter variable.
     * @param strA The first string
     * @param strB The second string
     * @return True if the first string comes before the second, false otherwise
     */
    public static boolean strAbeforeB(String strA, String strB){
        return strAbeforeB(strA, strB, stringSorter);
    }
    /**Does the first string come before the second?
     * @param strA The first string
     * @param strB The second string
     * @param sorter The StringSorter object used to compare the two strings
     * @return True if the first string comes before the second, false otherwise
     */
    public static boolean strAbeforeB(String strA, String strB, StringSorter sorter){
        return compareStrings(strA, strB, sorter) < 0;
    }
    
    /**Does the first string come after the second?
     * The comparison is made using the StringSorter object assigned to the stringSorter variable.
     * @param strA The first string
     * @param strB The second string
     * @return True if the first string comes after the second, false otherwise
     */
    public static boolean strAafterB(String strA, String strB){
        return strAafterB(strA, strB, stringSorter);
    }
    /**Does the first string come after the second?
     * The comparison is made using the StringSorter object assigned to the stringSorter variable.
     * @param strA The first string
     * @param strB The second string
     * @param sorter The StringSorter object used to compare the two strings
     * @return True if the first string comes after the second, false otherwise
     */
    public static boolean strAafterB(String strA, String strB, StringSorter sorter){
        return compareStrings(strA, strB, sorter) > 0;
    }
    
    /**Compares two strings to determine whether the first string comes before, comes after, or coincides with the second string.
     * The comparison is made using the StringSorter object assigned to the stringSorter variable.
     * @param strA The string against which the other string is compared.
     * @param strB The string compared against the other other string.
     * @return -1 if the first string comes before the second, 1 if it comes after, and 0 if it coincides.
     */
    public static int compareStrings(String strA, String strB){
        return compareStrings(strA, strB, stringSorter);
    }
    /**Compares two strings to determine whether the first string comes before, comes after, or coincides with the second string.
     * @param strA The string against which the other string is compared.
     * @param strB The string compared against the other other string.
     * @param sorter The StringSorter object used to compare the two strings
     * @return -1 if the first string comes before the second, 1 if it comes after, and 0 if it coincides.
     */
    public static int compareStrings(String strA, String strB, StringSorter sorter){
        return sorter.compareStrings(strA, strB);
    }
    
    
    /**Determines if a string is printable.
     * Defines a printable string as one composed of only ASCII characters between (and including) 32 and 126.
     * @param str The string being checked
     * @return True if the value of EVERY character is greater than or equal to 32 AND less than or equal to 126, false otherwise.
     */
    public static boolean isPrintable(String str){
        for(int i = 0; i < str.length(); i++){
            if (!isPrintable(str.charAt(i))) return false;
        }
        return true;
    }
    /**Determines if a character, represented as an integer value, is printable.
     * Defines a printable character as an ASCII character whose value is greater than or equal to 32 AND less than or equal to 126.
     * @param c The numerical value of the character being checked; can be passed in as an Integer or a Character (int or char).
     * @return True if the provided value is in the range defined as "printable", false otherwise.
     */
    public static boolean isPrintable(int c){
        // Printable characters range from 32 to 126
        // Note: 32 is (space), so that particular character may not be desirable under certain circumstances
        return !((c < 32) || (c > 126));
    }
    
    /**Determines if a string can be read/written from a file using basic Input/Output streams.
     * When InputStream.read() is called, a value between 0 and 255 is returned; thus the wrong character will be read back out when a character greater than 255 is written to file.
     * A string is considered to be "writable" ("readable" is a trait perceived by humans) if the values of all of its characters are between 0 and 255.
     * @param str The string being checked
     * @return True if the value of every character in the string is greater than or equal to zero AND less than or equal to 255, false otherwise.
     */
    public static boolean isWritable(String str){
        for(int i = 0; i < str.length(); i++){
            if (!isWritable(str.charAt(i))) return false;
        }
        return true;
    }
    /**Determines if a character, represented as an integer value, is "writable".
     * Defines a writable character as one whose value is greater than or equal to 0 AND less than or equal to 255, the range for which characters can be written to and correctly read from file.
     * @param c The numerical value of the character being checked; can be passed in as an Integer or a Character (int or char).
     * @return True if the provided value is in the range defined as "writable", false otherwise.
     */
    public static boolean isWritable(int c){
        return !((c < 0) || (c > 255));
    }
    
    /**Makes sure strings are printable/readable, useful for when complicated strings are being displayed to the user.
     * If whitespace is printed, whitespace characters will be left as they are;
     *  otherwise, they will be treated as unprintable characters.
     * If unprintable characters are escaped,
     *  carriage returns, newlines, and tabs will be replaced with \r, \n, and \t respectively (unless whitespace is being printed),
     *  unprintable characters with values less than 256 will have their hexadecimal values printed as %hh,
     *  and all other unprintable characters will have their hexadecimal values printed as xhhhh;
     *  otherwise, they will all be removed from the string.
     * Please note: this is not the same as the escape-string function;
     *  the strings produced here cannot be un-escaped and should only be used for making strings human-readable.
     * @param str The string to make readable
     * @param printWhitespace Should special whitespace characters (such as newlines and tabs) be printed as whitespace?
     * @param escapeUnprintable Should unprintable characters be escaped or simply dropped?
     * @return The provided string with unprintable characters dropped or escaped
     */
    public static String getPrintable(String str, boolean printWhitespace, boolean escapeUnprintable){
        // Quick check for a no-op string
        if (isPrintable(str)) return str;
        
        // For each character, append to the result as appropriate
        String result = "";
        for(int i = 0; i < str.length(); i++){
            // Get the character and its numeric value
            String c = charAt(str, i);
            int val = str.charAt(i);
            
            // If it's printable, just append it
            if(isPrintable(c)){
                result += c;
                continue;
            }
            
            // If it's whitespace and we're printing whitespace as-is, append it
            if(printWhitespace && contains(whitespaceChars, c)){
                result += c;
                continue;
            }
            
            // It's unprintable; if we aren't escaping values, don't add anything
            if (!escapeUnprintable) continue;
            
            // Otherwise, append the character's value as a string
            if(areEqual(c, "\r")){
                result += "\\r";
            }else if(areEqual(c, "\n")){
                result += "\\n";
            }else if(areEqual(c, "\t")){
                result += "\\t";
            }
            else if(val < 256){
                // %hh
                result += "%";
                result += hexadecimal[val / 16];
                result += hexadecimal[val % 16];
            }else{
                // xhhhh
                result += "x";
                result += hexadecimal[val / 4096]; // Char values should never exceed 65,535 ((4096 * 16) - 1). If they do, this will throw an error
                result += hexadecimal[(val / 256) % 16];
                result += hexadecimal[(val / 16) % 16];
                result += hexadecimal[val % 16];
            }
        }
        return result;
    }
    
    
    // Escape a String into one that will always be printable
    // Use markers to indicate where subsequent characters should be interpreted as a numeric value
    //  Mark actual occurrences of these characters by duplicating them (like %% in command scripts)
    //  # - 2 digits [0-31]
    //  % - 3 digits [127-999]
    //  $ - 4 digits [1000-9999]
    //  & - 5 digits [10000-99999]
    //  The max (unsigned) value of char (a 16-bit number) is 65,535; we should never have a number with 6 or more digits
    // If the string is null or empty, return a special string
    public static String escape(String str){
        if (str == null) return "%n";
        if (str.equals("")) return "%e";
        String result = "";
        for(int i = 0; i < str.length(); i++){
            result += escapeChar(str.charAt(i));
        }
        return result;
    }
    protected static String escapeChar(int c){
        // Double-escape marker characters
        if (c == (int)'#') return "##";
        if (c == (int)'%') return "%%";
        if (c == (int)'$') return "$$";
        if (c == (int)'&') return "&&";
        // Escape non-readable characters
        if (c < 32)   return "#" + StringHandler.leadingZeroes(c, 2);
        if (c > 9999) return "&" + c;
        if (c > 999)  return "$" + c;
        if (c > 126)  return "%" + c;
        // Everything else is readable and can be safely used as-is
        return String.valueOf((char)c);
    }
    
    
    // Unescape one of our escaped strings. Will allow index and fail-to-cast errors to check for integrity
    public static String unescape(String str){
        if (str.equals("%n")) return null;
        if (str.equals("%e")) return "";
        if (!isPrintable(str))
            throw new QuietException("Tried to unescape a string that wasn't even printable:\n" + str);
        String result = "";
        while(str.length() > 0){
            String unescape = unescapeWorker(str); // Get actualChar+escapedChar
            result += unescape.substring(0, 1);  // Append the actualChar (the first character)
            str = str.substring(unescape.length()-1); // The remainder is to be removed from the string
        }
        return result;
    }
    // Return a string compsed of the next unescaped character followed by how it was stored in the escaped string
    protected static String unescapeWorker(String str){
        // Double-escaped marker characters
        if (str.startsWith("##")) return "###";
        if (str.startsWith("%%")) return "%%%";
        if (str.startsWith("$$")) return "$$$";
        if (str.startsWith("&&")) return "&&&";
        // Numerically-escaped characters (#00,%nnn,$nnnn,&nnnnn)
        if(str.startsWith("#")){
            String digits = str.substring(1,3);
            int numericVal = Integer.valueOf(digits);
            return String.valueOf((char)numericVal) + "#" + digits;
        }
        if(str.startsWith("%")){
            String digits = str.substring(1,4);
            int numericVal = Integer.valueOf(digits);
            return String.valueOf((char)numericVal) + "%" + digits;
        }
        if(str.startsWith("$")){
            String digits = str.substring(1,5);
            int numericVal = Integer.valueOf(digits);
            return String.valueOf((char)numericVal) + "$" + digits;
        }
        if(str.startsWith("&")){
            String digits = str.substring(1,6);
            int numericVal = Integer.valueOf(digits);
            return String.valueOf((char)numericVal) + "&" + digits;
        }
        // Just a readable character
        return str.substring(0,1) + str.substring(0,1);
    }
    
    
    // Is the string empty? Specify if whitespace is to be discounted
    public static boolean isEmpty(String str, boolean ignoreWhitespace){
        // If it's empty, true. If not, always false if whitespace counts
        if (str.length() == 0) return true;
        if (!ignoreWhitespace) return false;
        
        // Check each character of the string; if any are not in the whitespace array, false
        for(int i = 0; i < str.length(); i++){
            boolean isWhitespace = false;
            for(int j = 0; j < whitespaceChars.length(); j++){
                if(areEqual(charAt(str, i), charAt(whitespaceChars, j), true)){
                    isWhitespace = true;
                    break;
                }
            }
            if (!isWhitespace) return false;
        }
        return true;
    }
    
    
    // Replace string segments that match the search string with the provided string
    public static String replace(String subject, String target, String replace, boolean checkCase){
        String result = "";
        while(contains(subject, target, checkCase)){
            String tSubject = checkCase ? subject : subject.toLowerCase();
            String tTarget = checkCase ? target : target.toLowerCase();
            int index = tSubject.indexOf(tTarget);
            
            result += subject.substring(0, index) + replace;
            subject = subject.substring(index + target.length());
        }
        return result + subject;
    }
    
    // Replace string segments that match the regular expression with the provided string
    public static String replaceRegEx(String subject, String expr, String replace, boolean checkCase){
        // Check all possible substrings for a regex match
        // Start indexes range from base to the last character
        // End indexes start with the index after the current start index and ends one character beyond actual string end
        for(int i = 0; i < subject.length(); i++){
            for(int j = i + 1; j < subject.length() + 1; j++){
                String tSubject = checkCase ? subject : subject.toLowerCase();
                String tTarget = checkCase ? expr : expr.toLowerCase();
                
                if(tSubject.substring(i, j).matches(tTarget)){
                    String result = subject.substring(0, i) + replace;
                    subject = subject.substring(j);
                    return result + replaceRegEx(subject, expr, replace, checkCase);
                }
            }
        }
        
        return subject;
    }
    
    
    // Get the character at the specified index (in string form)
    public static String charAt(String str, int index){
        return str.substring(index, index + 1);
    }
    
    
    
    protected static String _alphaNumericPid = null;
    protected static AtomicLong _iteratingTick = null;
    protected static String _uniqueStringDivider = "-";
    
    // Get a String that will ALWAYS be unique
    public static String getUnique() throws Exception{
        if(_alphaNumericPid == null){
            LogicHandler.lock("_alphaNumericPid", 10);
            if (_alphaNumericPid == null)
                _alphaNumericPid = toAlphanumeric(LogicHandler.getProcessId());
            LogicHandler.release("_alphaNumericPid", 10);
        }
        if(_iteratingTick == null){
            LogicHandler.lock("_iteratingTick", 10);
            if (_iteratingTick == null)
                _iteratingTick = new AtomicLong(TimeHandler.ticks());
            LogicHandler.release("_iteratingTick", 10);
        }
        return
            _alphaNumericPid
            + _uniqueStringDivider
            + toAlphanumeric(_iteratingTick.getAndIncrement());
    }
    public static void setUniqueStringDivider(String divider){ _uniqueStringDivider = divider; }
    
}
