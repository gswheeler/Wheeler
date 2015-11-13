/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import wheeler.generic.data.LogicHandler;
import wheeler.generic.data.StringHandler;

/**
 *
 * @author Greg
 */
public class StringList implements IStringList {
    
    /// Constructors
    public StringList(){
        listHeader = new StringNode(null);
    }
    public StringList(String str){
        this();
        add(str);
    }
    public StringList(String[] array){
        this();
        add(array);
    }
    
    
    
    /// Variables
    
    // A header for the list. Does not contain a valid value
    private StringNode listHeader = null;
    
    
    
    /// Functions
    
    // Get the raw linked list of StringNodes
    /*public StringNode getList(){
        return listHeader.next;
    }*/
    
    
    // Get an array containing the values of the linked list
    @Override
    public String[] toArray(){
        // Create and populate an array of appropriate size
        String[] result = new String[length()];
        StringNode node = listHeader.next;
        for(int i = 0; i < result.length; i++){
            result[i] = node.value;
            node = node.next;
        }
        
        // Return the array
        return result;
    }
    
    
    // Get an array with at most X values
    public String[] getArray(int size){return getArray(size, 0);}
    public String[] getArray(int size, int offset){
        // Create and populate an array of appropriate size
        String[] result = new String[LogicHandler.min(size, length() - offset)];
        StringNode node = listHeader.next;
        for(int i = 0; i < offset; i++) node = node.next;
        for(int i = 0; i < result.length; i++){
            result[i] = node.value;
            node = node.next;
        }
        
        // Return the array
        return result;
    }
    
    
    // Get an array with a "more" tag if appropriate
    public String[] getTruncatedArray(int size, int flex){
        int length = length();
        return (length > size + flex)
                ? (new StringList(getArray(size))
                    .add(" " + (length-size) + " more..."))
                    .toArray()
                : getArray(size+flex);
    }
    
    
    // Get a reversed array
    // Allows strings to be added in reversed order (insert(0)) thus avoiding looping
    public String[] getReversedArray(){
        int length = length();
        StringNode node = listHeader.next;
        String[] results = new String[length];
        for(int i = 0; i < length; i++){
            results[length - i - 1] = node.value;
            node = node.next;
        }
        return results;
    }
    
    
    // Get a reversed list
    public StringList getReversedList(){
        StringList revList = new StringList();
        StringNode node = getHeader();
        while ((node = node.next) != null) revList.insert(node.value, 0);
        return revList;
    }
    
    
    // Provide a reference to this list to expand on the available operations when given an IStringList?
    
    
    // Get a count of how many nodes are in the list
    @Override
    public int length(){
        int count = 0; StringNode node = listHeader.next;
        while (node != null){ node = node.next; count++; }
        return count;
    }
    @Override
    public boolean any(){ return !isEmpty(); }
    
    
    // Returns whether or not this list is empty
    @Override
    public boolean isEmpty(){
        return listHeader.next == null;
    }
    
    
    // Add a string to the end of the list
    @Override
    public final StringList add(String str){
        // Prevent nulls
        //if (!str.equals(str)) return;
        //if(str == null) throw new Exception("StringList does not support null values");
        if(str == null) throw new NullPointerException();
        
        // Go to the end of the list
        StringNode node = listHeader;
        while(node.next != null)
            node = node.next;
        
        // Add the string
        node.next = new StringNode(str);
        
        return this;
    }
    
    
    // Add an array of strings to the end of the list
    @Override
    public final StringList add(String[] array){
        // Go to the end of the list
        StringNode node = listHeader;
        while(node.next != null)
            node = node.next;
        
        // Add each string to the end of the list
        for(int i = 0; i < array.length; i++){
            node.next = new StringNode(array[i]);
            node = node.next;
        }
        
        return this;
    }
    
    
    // Add a list of strings to the end of the list
    @Override
    public StringList add(IStringList list){
        // Go to the end of the list
        StringNode myNode = listHeader;
        while (myNode.next != null) myNode = myNode.next;
        
        // Get the list's header, add the value of each node in the list
        IStringNode newNode = list.getHeader();
        while((newNode = newNode.getNext()) != null){
            myNode.next = new StringNode(newNode.getValue());
            myNode = myNode.next;
        }
        
        return this;
    }
    
    
    // Add a string to the end of the list if it isn't already in the list
    public boolean addUnique(String str){
        // Prevent nulls
        if (str == null) throw new NullPointerException();
        
        // Look for the string
        StringNode node = listHeader;
        while(node.next != null){
            if (StringHandler.areEqual(node.next.value, str, false)) return false;
            node = node.next;
        }
        
        // Didn't find it, add it
        node.next = new StringNode(str);
        return true;
    }
    
    
    // Insert a string into the list. Inserts itself before the first string greater than it
    // Returns true if the value was unique, i.e. there wasn't a string in the list with the same value
    // Nevermind; just get StringLinkedList working
    /*public boolean insert(String str){
        // Prevent nulls
        //if (!str.equals(str)) return false;
        //if(str == null) throw new Exception("StringList does not support null values");
        if(str == null) throw new NullPointerException();
        
        // Find the first node with a greater value or the end of the list
        StringNode node = listHeader;
        while(node.next != null && !StringHandler.strAafterB(node.next.value, str))
            node = node.next;
        
        // Since the current node is either less than or equal to the new string, now is the time to check for uniqueness
        if(node.value != null && node.value.equalsIgnoreCase(str)) return false;
        
        // The next node is either null or comes after the "new" node. Insert "new" node as such
        node.next = new StringNode(str, node.next);
        return true;
    }
    
    
    // Insert an array of Strings
    public boolean[] insert(String[] strArray){
        boolean[] result = new boolean[strArray.length];
        for(int i = 0; i < strArray.length; i++)
            result[i] = insert(strArray[i]);
        return result;
    }*/
    
    
    // Insert a string at the given position. If the list is too small to insert at the given position, puts it at the end
    public void insert(String str, int position){
        // Prevent nulls
        //if (!str.equals(str)) return -1;
        //if(str == null) throw new Exception("StringList does not support null values");
        if(str == null) throw new NullPointerException();
        
        // Go to the specified position
        StringNode node = listHeader; int index = 0;
        while(index < position){
            if (node.next == null) throw new ArrayIndexOutOfBoundsException();
            node = node.next; index++;
        }
        
        // Add the string
        node.next = new StringNode(str, node.next);
    }
    
    
    // Insert a set of Strings starting at a specific index
    public void insert(String[] array, int position){
        // Go to the specified position
        StringNode node = listHeader; int index = 0;
        while(index < position){
            if (node.next == null) throw new ArrayIndexOutOfBoundsException();
            node = node.next; index++;
        }
        
        // Add the strings
        for (int i = 0; i < array.length; i++)
            node.next = new StringNode(array[array.length - i - 1], node.next);
    }
    
    
    // Count the number of times a string occurs in the list
    @Override
    public int count(String str){
        // Count all matching nodes
        StringNode node = listHeader; int count = 0;
        while((node = node.next) != null){
            if (StringHandler.areEqual(node.value, str, false)) count++;
        }
        return count;
    }
    
    
    // Remove a string from the list. Returns an integer indicating the number of times it was found
    @Override
    public int remove(String str){
        String[] strArray = {str};
        return remove(strArray);
    }
    @Override
    public int remove(String[] strs){
        // Prevent nulls
        for(int i = 0; i < strs.length; i++){
            if(strs[i] == null) throw new NullPointerException();
        }
        
        // Cut out all nodes that match
        StringNode node = listHeader; int count = 0;
        while(node.next != null){
            // Does the next string match any of the strings we are removing?
            boolean matchFound = false;
            for(int i = 0; i < strs.length; i++){
               if(node.next.value.equalsIgnoreCase(strs[i])){
                   matchFound = true;
                   break;
               }
            }
            // If yes, cut out the next node. Otherwise, move to it
            if(matchFound){
                node.next = node.next.next;
                count++;
            }else{
                node = node.next;
            }
        }
        return count;
    }
    
    
    // Remove the string at the given position
    public void remove(int index){
        StringNode node = listHeader;
        // Move to the node before the target index
        for(int i = 0; i < index; i++){
            if (node.next == null) throw new ArrayIndexOutOfBoundsException();
            node = node.next;
        }
        // Cut out the node at the target index
        if (node.next == null) throw new ArrayIndexOutOfBoundsException();
        node.next = node.next.next;
    }
    
    
    // Remove just the first instance of a string
    public boolean removeFirst(String str){
        // Prevent nulls
        if(str == null) throw new NullPointerException();
        
        // Cut out the first node that matches
        StringNode node = listHeader;
        while(node.next != null){
            if(node.next.value.equalsIgnoreCase(str)){
                node.next = node.next.next;
                return true;
            }else{
                node = node.next;
            }
        }
        return false;
    }
    
    
    // Get the string at the specified index. Returns null if there is no such "index"
    public String get(int index){
        StringNode node = listHeader.next;
        
        for(int i = 0; i < index; i++){
            if (node == null) return null;
            node = node.next;
        }
        
        return (node != null) ? node.value : null;
    }
    
    
    // Get the first string from the list and remove it. Returns null if the list is empty
    @Override
    public String pullFirst(){
        if (listHeader.next == null) return null;
        
        String result = listHeader.next.value;
        listHeader.next = listHeader.next.next;
        
        return result;
    }
    @Override
    public String getFirst(){
        if (listHeader.next == null) return null;
        return listHeader.next.value;
    }
    
    
    // Get the last string from the list and remove it. Returns null if the list is empty
    @Override
    public String pullLast(){
        if (listHeader.next == null) return null;
        
        // Move to the node before the last one in the list
        StringNode node = listHeader;
        while(node.next.next != null){
            node = node.next;
        }
        
        // Get the last node's value and drop it
        String result = node.next.value;
        node.next = null;
        
        return result;
    }
    @Override
    public String getLast(){
        StringNode node = listHeader;
        while(node.next != null){ node = node.next; }
        return node.value;
    }
    
    
    // Does the list contain the provided string?
    @Override
    public boolean contains(String str){
        // Prevent nulls
        //if (!str.equals(str)) return false;
        //if(str == null) throw new Exception("StringList does not support null values");
        if(str == null) throw new NullPointerException();
        
        // Look for a match
        StringNode node = listHeader;
        while((node = node.next) != null){
            if (node.value.equalsIgnoreCase(str)) return true;
        }
        return false;
    }
    
    
    // Get the header node to provide a makeshift enumerable
    @Override
    public StringNode getHeader(){
        return listHeader;
    }
    
    // Drop the list to make it empty
    public void clear(){
        listHeader.next = null;
    }

    @Override
    public StringList getNew() {
        return new StringList();
    }
    
}
