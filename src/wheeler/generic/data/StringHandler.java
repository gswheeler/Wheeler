/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import java.util.concurrent.atomic.AtomicLong;
import wheeler.generic.error.QuietException;
import wheeler.generic.structs.StringList;

/**
 *
 * @author Greg
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
    
    public static boolean defaultCaseSensitivity = false;
    
    
    
    /// Functions
    
    // 
    public static boolean contains(String subject, String check){
        return contains(subject, check, defaultCaseSensitivity);
    }
    @SuppressWarnings("IndexOfReplaceableByContains") // IndexOf tends to work better (don't have to worry about regex special characters)
    public static boolean contains(String subject, String check, boolean caseSensitive){
        if(!caseSensitive){
            subject = subject.toLowerCase();
            check = check.toLowerCase();
        }
        return (subject.indexOf(check) != -1);
    }
    
    public static boolean areEqual(String subject, String check, boolean caseSensitive){
        if ((subject == null) && (check == null)) return true;
        if ((subject == null) || (check == null)) return false;
        
        if(!caseSensitive){
            subject = subject.toLowerCase();
            check = check.toLowerCase();
        }
        return subject.equals(check);
    }
    
    
    // Make all characters lowercase, sparing escaped characters if specified
    public static String toLowerCase(String subject, boolean preserveEscaped){
        if (!preserveEscaped) return subject.toLowerCase();
        
        String processed = "";
        int sIndex;
        while((sIndex = subject.indexOf("\\")) != -1){
            // Add all characters up to and including the first slash
            processed += subject.substring(0, sIndex + 1).toLowerCase();
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
        targets = targets.toLowerCase();
        while(true){
            boolean trim = false;
            for(int i = 0; i < targets.length(); i++){
                if(str.toLowerCase().endsWith(charAt(targets, i))){
                    trim = true;
                    break;
                }
            }
            if (!trim) return str;
            str = str.substring(0, str.length()-1);
        }
    }
    
    
    // Remove specified characters from the start of a string
    public static String trimLeadingCharacters(String str, String targets){
        if (str == null) return null;
        targets = targets.toLowerCase();
        while(true){
            boolean trim = false;
            for(int i = 0; i < targets.length(); i++){
                if(str.toLowerCase().startsWith(charAt(targets, i))){
                    trim = true;
                    break;
                }
            }
            if (!trim) return str;
            str = str.substring(1);
        }
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
        while (subject.length() > 0 && contains(targets, charAt(subject, 0), true))
            subject = subject.substring(1);
        return trimTrailingCharacters(subject, targets);
    }
    
    
    // Put quotes around a string
    public static String addQuotes(String subject){ return "\"" + subject + "\""; }
    
    
    // Parse a string into an array of strings based on the provided separator
    public static String[] parseIntoArray(String subject, String separator){
        // If we're passed an empty string, return an empty array
        if (subject.equals("")) return new String[0];
        
        // Get everything before the next separator string, recursive
        StringList list = new StringList();
        while(contains(subject, separator)){
            list.insert(subject.substring(0, subject.indexOf(separator)), 0);
            subject = subject.substring(subject.indexOf(separator) + 1);
        }
        
        // If the leftovers are not empty, add them
        if (!subject.equals("")) list.insert(subject, 0);
        
        // Return the list in the right order
        return list.getReversedArray();
    }
    
    
    // Turn an array into a parsable string
    public static String arrayToString(String[] list, String separator){
        if (list.length == 0) return "";
        String str = list[0];
        for(int i = 1; i < list.length; i++)
            str += separator + list[i];
        return str;
    }
    
    
    // Check if an array is sorted
    public static boolean arrayIsSorted(String[] array){
        for(int i = 0; i < array.length - 1; i++){
            if (strAafterB(array[i], array[i+1])) return false;
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
    
    
    // Translate a byte size into a "readable" format
    public static String toReadableFileSize(long lngBytes){
        String[] suffixes = {"B", "KB", "MB", "GB", "TB"}; int index = 0;
        Double bytes = Double.valueOf(Long.toString(lngBytes));
        while((bytes >= 1000.0) && (index < suffixes.length - 1)){
            bytes = bytes / 1024.0;
            index++;
        }
        int strLen = (bytes < 100.0) ? 4 : 3;                // Format is either 100, 99.9, or 0.00
        String byteStr = Double.toString(bytes);
        if (byteStr.indexOf(".") == -1) byteStr += ".00";    // Make sure trailing decimals are in place
        byteStr = (byteStr.length() > strLen)
                ? byteStr.substring(0, strLen)
                : byteStr;
        // Don't bother with a decimal if we're dealing with raw bytes
        if ((index == 0) && (byteStr.indexOf(".") != -1)) byteStr = byteStr.substring(0, byteStr.indexOf("."));
        return byteStr + " " + suffixes[index];
    }
    
    
    // Pad a file with leading zeros
    public static String leadingZeroes(int number, int minSize){
        String result = Integer.toString(number);
        for (int i = result.length(); i < minSize; i++) result = "0" + result;
        return result;
    }
    
    
    // Sort an array of strings. Use array sorting as opposed to linked list sorting to keep things quick
    //   (possible since size is constant)
    public static String[] sortStrings(String[] strArray){
        // Check if it's already sorted
        if (arrayIsSorted(strArray)) return strArray;
        
        String[] result = new String[strArray.length];
        
        // {_} <- 2 => {2}
        // {4,_} <- 2 => {_,4} <- 2 => {2,4}
        
        for(int i = 0; i < strArray.length; i++){
            // Get the insert index
            int index = 0;
            while (result[index] != null && strAafterB(strArray[i], result[index])) index++;
            
            // Shift all trailing elements over
            for (int j = i; j > index; j--) result[j] = result[j - 1];
            
            // Insert string
            result[index] = strArray[i];
        }
        
        return result;
    }
    
    
    // Compare two strings; -1 if the first comes first, +1 if the second comes first, 0 if they are the same
    public static boolean strAbeforeB(String sA, String sB){ return compareStrings(sA, sB) < 0; }
    public static boolean strAafterB(String sA, String sB){ return compareStrings(sA, sB) > 0; }
    public static int compareStrings(String sA, String sB){
        indexA = 0; indexB = 0; strA = sA; strB = sB;
        while(true){
            String cA = getStrAChar(); String cB = getStrBChar();
            if((cA == null) && (cB == null)){ return 0; }
            if(cA == null){ return -1; } if(cB == null){ return 1; }
            int aVal = getCharValue(cA); int bVal = getCharValue(cB);
            if(aVal < bVal){ return -1; } if(aVal > bVal){ return 1; }
        }
        //throw new Exception("Inconclusive comparison between strings \"" + sA + "\" and \"" + sB + "\"");
    }
    private static String[] charArray = {".", "!", "#", "$", "%", "&", "(", ")", ",", ";", "@", "[", "]", "^", "_", "`", "{", "}", "~", "'", "-",
                                            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h",
                                            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private static String[] skipChars = {"'", "-"};
    private static int indexA = 0;
    private static int indexB = 0;
    private static String strA = null;
    private static String strB = null;
    private static String getStrAChar(){
        if(indexA == strA.length()){ return null; }
        String c = strA.substring(indexA++, indexA);
        if(isSkipChar(c)){ c = getStrAChar(); }
        return c;
    }
    private static String getStrBChar(){
        if(indexB == strB.length()){ return null; }
        String c = strB.substring(indexB++, indexB);
        if(isSkipChar(c)){ c = getStrBChar(); }
        return c;
    }
    private static int getCharValue(String c){
        for(int i = 0; i < charArray.length; i++)
            if (charArray[i].equalsIgnoreCase(c)) return i;
        return -1;
    }
    private static boolean isSkipChar(String c){
        for(int i = 0; i < skipChars.length; i++)
            if (skipChars[i].equalsIgnoreCase(c)) return true;
        return false;
    }
    
    
    // Printable characters range from 32 to 126
    // Note: 32 is (space), so that particular character may not be desirable under certain circumstances
    public static boolean isPrintable(String str){
        for(int i = 0; i < str.length(); i++){
            char val = str.charAt(i);
            if ((val < 32) || (val > 126)) return false;
        }
        return true;
    }
    public static boolean isWritable(String str){
        for(int i = 0; i < str.length(); i++){
            char val = str.charAt(i);
            if ((val < 0) || (val > 255)) return false;
        }
        return true;
    }
    public static String getPrintable(String str){
        if (isPrintable(str)) return str;
        String result = "";
        for(int i = 0; i < str.length(); i++){
            char val = str.charAt(i);
            if ((val < 32) || (val > 126)) continue;
            result += charAt(str, i);
        }
        return result;
    }
    
    
    // Escape a String into one that will always be printable
    // Use markers to indicate where following characters should be interpreted as a numeric value
    // Mark these characters by duplicating them (like %% in command scripts)
    // # - 2 digits [0-31]
    // % - 3 digits [127-999]
    // $ - 4 digits [1000-9999]
    // & - 5 digits [10000-99999]
    public static String escape(String str){
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
    
    
    // Unescape one of our escaped strings. Will allow fail-to-cast errors to check for integrity
    public static String unescape(String str){
        if (!isPrintable(str))
            throw new QuietException("Tried to unescape a string that wasn't even printable:\n" + str);
        String result = "";
        while(str.length() > 0){
            String unescape = unescapeChar(str); // Get actualChar+escapedChar
            result += unescape.substring(0, 1);  // Append the actualChar (the first character)
            str = str.substring(unescape.length()-1); // The remainder is to be removed from the string
        }
        return result;
    }
    // Return a string compsed of the next unescaped character followed by its escaped form
    protected static String unescapeChar(String str){
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
    
    
    
    // Look through a string for characters that are not readily visible and replace them with their plaintext counterparts
    public static String[] specialsWithReadable = {
        "\r\\r",
        "\n\\n",
        "\t\\t"
    };
    public static String toReadable(String str){
        if (str == null) return "(null)";
        if (str.length() == 0) return "\"\"";
        String result = "";
        for(int i = 0; i < str.length(); i++){
            // Look at each character in the string
            String subject = charAt(str, i);
            for(int j = 0; j < specialsWithReadable.length; j++){
                // If the "character" part of an array string matches the character, replace it with the "plaintext" component
                if(subject.equals(charAt(specialsWithReadable[j], 1))){
                    subject = specialsWithReadable[j].substring(1);
                    break;
                }
            }
            // Either we didn't find any matches or we found one and replaced the character with its counterpart
            result += subject;
        }
        return result;
    }
    
    
    // Is the string empty? Specify if whitespace is to be discounted
    public static boolean isEmpty(String str, boolean ignoreWhitespace){
        // If it's empty, true. If not, false if whitespace counts
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
                _alphaNumericPid = toAlphaNumeric(LogicHandler.getProcessId());
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
            + toAlphaNumeric(_iteratingTick.getAndIncrement());
    }
    public static void setUniqueStringDivider(String divider){ _uniqueStringDivider = divider; }
    
    
    // Convert a number to an alphanumeric string
    public static String toAlphaNumeric(long num){
        //String numStr = Long.toString(num);
        //int[] numArray = new int[numStr.length()];
        //for(int i = 0; i < numStr.length(); i++)
        //    Integer.valueOf(numStr.substring(i,i+1));
        //int[] newArray = convertBase(numArray, 10, 36);
        
        //int[] array = LogicHandler.convertBase(num, 36);
        //String newStr = "";
        //for(int i = 0; i < array.length; i++)
        //    newStr += (array[i] < 10)
        //            ? Integer.toString(array[i])
        //            : StringHandler.alphaArray[array[i] - 10];
        //return newStr;
        
        // Just make use of our alphaArray and the digits 0-9
        long base = 10 + alphaArray.length;
        if (num == 0) return "0";
        String result = "";
        // A hyphen is not alphanumeric; use a leading zero instead (cannot lead-zero nothing or zero)
        if(num < 0){ result = "0"; num = LogicHandler.negate64(num); }
        while(num > 0){
            int i = (int)(num % base);
            result = ((i < 10) ? Integer.toString(i) : alphaArray[i-10]) + result;
            num = num / base;
        }
        return result;
    }
    
}
