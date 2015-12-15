/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

/**
 *
 * @author Greg
 */
public interface IStringList {
    
    // Add a string. To help with in-line construction and use without caching, return a reference to the list
    IStringList add(String str);
    IStringList add(String[] array);
    IStringList add(IStringList list);
    
    // The state of the list
    boolean any();
    boolean isEmpty();
    
    /// Get the data ///
    // A single item
    String getFirst();
    String getLast();
    // And remove it from the list
    String pullFirst();
    String pullLast();
    // As an array
    String[] toArray();
    // Get the header for using enumerator-style (node = header; while((node = node.next) != null){})
    IStringNode getHeader();
    // Get the length
    int length();
    // Does the given string occur in the list?
    boolean contains(String str);
    // Get the number of times a string occurs in the list
    int count(String str);
    // Remove from the list
    int remove(String str);
    int remove(String[] strs);
    
}
