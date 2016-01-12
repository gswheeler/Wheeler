/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data.stringsort;

/**
 * A StringSorter subclass that compares strings the same way Java's String class does
 */
public class JavaDefault extends StringSorter{
    
    public JavaDefault(boolean checkCase){
        super(checkCase);
    }
    
    @Override
    protected int compareStringsAfterCase(String strA, String strB){
        int result = strA.compareTo(strB);
        if (result < 0) return -1;
        if (result > 0) return 1;
        return 0;
    }
    
}
