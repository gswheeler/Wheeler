/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

/**
 *
 * @author Greg
 */
public class StringHashTable {
    
    /// Constructors
    public StringHashTable(int hashSize){
        lists = new StringList[hashSize];
    }
    
    
    
    /// Variables
    private StringList[] lists = null;
    private int getIndex = 0;
    private StringNode getNode = null;
    
    
    
    /// Functions
    
    // Add a string
    public void add(String str) throws Exception{
        lists[hash(str)].insert(str,0);
    }
    public void addUnique(String str) throws Exception{
        if (!contains(str)) add(str);
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
        for (int i = 0; i < lists.length; i++) count += lists[i].length();
        return count;
    }
    
    
    // Get the values out of the collection iterator-style
    public String get(){
        // We need to cache the result in multiple places
        String result;
        
        // If we've got a "get" node in play, advance it and return its value
        if(getNode != null){
            result = getNode.value;
            getNode = getNode.next;
            return result;
        }
        
        // If the index is invalid, should be an indication that all values have been returned and it's time for a null
        if(getIndex < 0 || getIndex == lists.length){
            resetGet(); return null;
        }
        
        // If we've reached this point, we need to get a "get" node from the next list and advance the index
        // Get the first node from the list our index is pointing at, advance the index, and loop back to the "get" check
        getNode = lists[getIndex].getHeader().next;
        getIndex++;
        return get();
    }
    public void resetGet(){
        getIndex = 0; getNode = null;
    }
    
    
    // Pull Strings out of the table as the final act of the object
    public String pull(){
        for(int i = 0; i < lists.length; i++){
            if (lists[i].any()) return lists[i].pullFirst();
        }
        return null;
    }
    
    
    // Get a linked list of StringNodes, starting with a header, allowing for the enumeration of all values
    /* Can't do this; it would tie the lists together within the hash table itself */
    /*public StringNode getHeader(){
        StringNode header = new StringNode(null);
        StringNode node = header;
        for(int i = 0; i < lists.length; i++){
            // Move to the last node of the current list (no-op on first pass allows us to skip iterating over the last one)
            while (node.next != null) node = node.next;
            // Latch the first node of the next list onto the tail of the current "complete" list
            node.next = lists[i].getHeader().next;
        }
        return header;
    }*/
    
    
    // Hash function
    private int hash(String str){
        //int hash = str.toLowerCase().hashCode() % lists.length;
        //return (hash < 0) ? hash * -1 : hash;
        return hash(str, lists.length);
    }
    public static int hash(String str, int hash){
        int result = str.toLowerCase().hashCode() % hash;
        return (result < 0) ? ((-1) * result) : result;
    }
    
}
