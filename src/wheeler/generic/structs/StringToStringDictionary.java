/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import wheeler.generic.data.StringHandler;
import wheeler.generic.error.QuietException;

/**
 * Contains key/value pairs that can be set and retrieved
 */
public class StringToStringDictionary {
    
    /// Variables ///
    
    /** The linked lists of values used to store key/value pairs. Not using StringLists because traditional StringList functions don't work here. */
    protected StringNode[] lists;
    /** Internal value used by the Get function. Contains the index of the next list to iterate through */
    private int getIndex = 0;
    /** Internal value used by the Get function. Points to the next StringNode to collect a key/value pair from */
    private StringNode getNode = null;
    
    /**Creates a new dictionary using the specified hash value
     * @param hashsize The approximate hash distribution of the supplied key/value pairs. The bigger, the better.
     */
    public StringToStringDictionary(int hashsize){
        if (hashsize < 1)
            throw new QuietException("Must supply a hashkey greater than or equal to 1 (was " + hashsize + ")");
        lists = new StringNode[hashsize];
        for(int i = 0; i < lists.length; i++){
            lists[i] = new StringNode();
        }
    }
    
    
    /**Searches the dictionary for the relevant key, returns its value if present
     * @param key The Key whose Value we are looking to return
     * @return The Value associated with the provided Key, null if there isn't one
     */
    public String get(String key){
        // Look in the relevant list
        StringNode node = lists[hash(key)]; // Grab the list header
        
        // While there are entries in the list, check the current node and return the value or move to the next one
        while((node = node.next) != null){
            KeyValuePair set = fromDataString(node.value);
            if (StringHandler.areEqual(key, set.key)) return set.value;
        }
        
        // No matches found, return null
        return null;
    }
    
    
    /**Associates the provided value with the provided key.
     * If a value is already associated with the key, said value is replaced with the new one.
     * @param key The key to associate a value with.
     * @param value The value to associate with the key.
     * @return If a value is already associated with the key this value is returned, null otherwise.
     */
    public String set(String key, String value){
        // Look in the relevant list
        StringNode node = lists[hash(key)]; // Grab the list header
        
        // Look through nodes but make sure we always have a reference to a node in case we need to add one to the list
        while(node.next != null){
            // While there is a node after this one, check it and either set its value or move to that node
            KeyValuePair set = fromDataString(node.next.value);
            if(StringHandler.areEqual(key, set.key)){
                node.next.value = new KeyValuePair(key, value).toDataString();
                return set.value;
            }
            node = node.next;
        }
        
        // No value currently associated with the key, add one
        node.next = new StringNode(new KeyValuePair(key, value).toDataString());
        return null;
    }
    
    
    /**Removes any value associated with the provided key
     * @param key The key to clear any associated value from
     * @return True if the key had a value associated with it, false otherwise
     */
    public boolean clear(String key){
        // Look in the relevant list
        StringNode node = lists[hash(key)]; // Grab the list header
        
        // Look through nodes but make sure we end up with a reference to the node before the target one
        while(node.next != null){
            // While there is a node after this one, check it and either remove it from the list or move to that node
            if(StringHandler.areEqual(key, fromDataString(node.next.value).key)){
                node.next = node.next.next;
                return true;
            }
            node = node.next;
        }
        
        // No value currently associated with the key, so nothing to do
        return false;
    }
    
    
    /**Returns the key/value pairs stored in the dictionary iterator-style
     * @return A KeyValuePair object containing the next key/value pair in the dictionary, null if the last key/value pair was returned by the previous call or if the dictionary is empty.
     */
    public KeyValuePair get(){
        // Loop until we find a legitimate node or run out of lists to check
        while(true){
            // If the "get" node has a value, grab it, extract the key/value pair, and advance the node
            if(getNode != null){
                String result = getNode.value;
                getNode = getNode.next;
                return fromDataString(result);
            }

            // If the index is invalid, should be an indication that all key/value pairs have been returned and it's time for a null
            if(getIndex > lists.length - 1){
                resetGet();
                return null;
            }

            // If we've reached this point, we need to get a "get" node from the next list
            // Get the first node from the list our index is pointing at, advance the index, and loop back to the "get" check
            getNode = lists[getIndex].next;
            getIndex++;
        }
    }
    /** Resets the Get function to start returning values from the beginning */
    public void resetGet(){
        getIndex = 0;
        getNode = null;
    }
    
    
    /**Return the number of key/value pairs in the dictionary
     * @return The number of key/value pairs currently in the dictionary
     */
    public int count(){
        int count = 0;
        for(StringNode node : lists){
            while ((node = node.next) != null) count++;
        }
        return count;
    }
    
    
    /**Take the Key string and return the list that should contain it
     * @param str The Key string for a key-value pair
     * @return The index of the list that contains the key-value pair with this key
     */
    protected int hash(String str){
        return StringHashTable.hash(str, lists.length);
    }
    
    
    /**
     * A key and its associated value
     */
    public class KeyValuePair{
        /** The Key value */
        public String key;
        /** The Value value */
        public String value;
        
        /**A Key and its associated Value
         * @param keyStr The key
         * @param valStr The value
         */
        public KeyValuePair(String keyStr, String valStr){
            key = keyStr;
            value = valStr;
        }
        
        /**Convert the key/value pair into a string that can be later translated back into a key/value pair
         * @return A string that fromDataString can use to produce a key/value pair with the same values
         */
        protected String toDataString(){
            return StringHandler.escape(key) + "\t" + value;
        }
    }
    
    
    /**Take a Key/Value pair's data
     * @param str
     * @return 
     */
    protected KeyValuePair fromDataString(String str){
        int tabIndex = str.indexOf("\t");
        return new KeyValuePair(
                StringHandler.unescape(str.substring(0, tabIndex)),
                str.substring(tabIndex + 1)
            );
    }
    
}
