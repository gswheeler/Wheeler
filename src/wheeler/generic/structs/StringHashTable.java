/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import wheeler.generic.error.QuietException;

/**
 * A basic hash table for adding, checking, and removing Strings faster than using a StringList.
 * Maintains a set of lists in which Strings are stored based on the provided hashkey.
 */
public class StringHashTable {
    
    /// Constructors
    public StringHashTable(int hashSize){
        if (hashSize < 1)
            throw new QuietException("Must supply a hashkey greater than or equal to 1 (was " + hashSize + ")");
        lists = new StringList[hashSize];
        for(int i = 0; i < lists.length; i++){
            lists[i] = new StringList();
        }
    }
    
    
    
    /// Variables
    /** A list of StringLists that contains the values stored in the object */
    private StringList[] lists = null;
    /** Internal value used by the Get function. Contains the index of the next list to iterate through */
    private int getIndex = 0;
    /** Internal value used by the Get function. Points to the next StringNode to collect a value from */
    private StringNode getNode = null;
    
    
    
    /// Functions
    
    // Add a string
    public StringHashTable add(String str) throws Exception{
        // Insert directly into the list to save the time of iterating across to the end as a normal add would do
        lists[hash(str)].insert(str,0);
        return this;
    }
    public boolean addUnique(String str) throws Exception{
        return lists[hash(str)].addUnique(str);
    }
    
    
    // Remove a string
    public int remove(String str) throws Exception{
        return lists[hash(str)].remove(str);
    }
    
    
    // Does the collection contain the provided string?
    public boolean contains(String str) throws Exception{
        return lists[hash(str)].contains(str);
    }
    
    
    // Count the number of strings in the collection
    public int count(){
        int count = 0;
        for (StringList list : lists) count += list.length();
        return count;
    }
    
    
    /**Returns the strings stored in the table iterator-style
     * @return The next string in the dictionary, null if the last string was returned by the previous call or if the table is empty.
     */
    public String get(){
        // Loop until we find a legitimate node or run out of lists to check
        while(true){
            // If the "get" node has a value, grab it and advance the node
            if(getNode != null){
                String result = getNode.value;
                getNode = getNode.next;
                return result;
            }

            // If the index is invalid, should be an indication that all values have been returned and it's time for a null
            if(getIndex > lists.length - 1){
                resetGet();
                return null;
            }

            // If we've reached this point, we need to get a "get" node from the next list
            // Get the first node from the list our index is pointing at, advance the index, and loop back to the "get" check
            getNode = lists[getIndex].getHeader().next;
            getIndex++;
        }
    }
    /** Resets the Get function to start returning values from the beginning */
    public void resetGet(){
        getIndex = 0;
        getNode = null;
    }
    
    
    // Pull Strings out of the table as the final act of the object
    public String pull(){
        for(StringList list : lists){
            if (list.any()) return list.pullFirst();
        }
        return null;
    }
    
    
    // Hash function
    protected int hash(String str){
        return hash(str, lists.length);
    }
    public static int hash(String str, int hash){
        int result = str.toLowerCase().hashCode() % hash;
        return (result < 0) ? ((-1) * result) : result;
    }
    
}
