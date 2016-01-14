/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import wheeler.generic.data.MathHandler;
import wheeler.generic.data.StringHandler;
import wheeler.generic.error.QuietException;

/**
 * A list of Strings.
 * Contains a single-linked list of value/node nodes.
 * Uses loops rather than recursion to better handle large lists (avoids extensive strings of nested calls).
 */
@SuppressWarnings("EqualsAndHashcode") // We only need to override the equals function; hashcode is irrelevant
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
        String[] result = new String[MathHandler.min(size, length() - offset)];
        StringNode node = listHeader.next;
        for(int i = 0; i < offset; i++) node = node.next;
        for(int i = 0; i < result.length; i++){
            result[i] = node.value;
            node = node.next;
        }
        
        // Return the array
        return result;
    }
    protected static String[] getArray(IStringList list, int size, int offset){
        // Check the parameters
        if (size < 0) throw new QuietException("Cannot call getArray with a size less than zero");
        if (offset < 0) throw new QuietException("Cannot call getArray with an offset less than zero");
        
        // Move to the start of what we're getting (first node whose value we are collecting)
        IStringNode firstNode = list.getHeader().getNext();
        for(int i = 0; i < offset; i++){
            if (firstNode == null) break;
            firstNode = firstNode.getNext();
        }
        
        // Find out how many nodes we're grabbing (may be less than either size or length)
        IStringNode node = firstNode;
        int numNodes = 0;
        // For each node present, count and move to the next (maximum of size times)
        // Don't worry about performance; this will never be worse than a length() call
        for(int i = 0; i < size; i++){
            if (node == null) break;
            numNodes++;
            node = node.getNext();
        }
        
        // Grab the nodes and put their values into an array
        String[] result = new String[numNodes];
        node = firstNode;
        for(int i = 0; (i < result.length) && (node != null); i++){
            result[i] = node.getValue();
            node = node.getNext();
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
    protected static String[] getTruncatedArray(IStringList list, int size, int flex){
        // Test the parameters
        if (size < 0) throw new QuietException("Cannot call getTruncatedArray with a size less than zero");
        if (flex < 0) throw new QuietException("Cannot call getTruncatedArray with a flex less than zero");
        
        // Determine if we need to append the "more" tag
        IStringNode header = list.getHeader();
        IStringNode node = header.getNext();
        // Starting with the first with-value node, move ahead size+flex nodes;
        //  if there is a node after them, it will not be included in the list thus requiring the tag
        for(int i = 0; i < size + flex; i++){
            if (node == null) break;
            node = node.getNext();
        }
        boolean appendMoreTag = (node != null);
        
        // Either get the full list (will have at most size + flex items)
        //  or get a list with size items and a "more" tag
        if(appendMoreTag){
            // The array we will be returning: size plus the more tag
            String[] result = new String[size + 1];
            
            // Figure out how much "more" is.
            // Node from earlier is the first node after size+flex; count that and all nodes after it
            int more = flex; while(node != null){ more++; node = node.getNext(); }
            
            // Put values into the array
            node = header.getNext();
            for(int i = 0; i < size; i++){
                result[i] = node.getValue();
                node = node.getNext();
            }
            
            // Add the more tag and return the array
            result[result.length - 1] = " " + (more) + " more...";
            return result;
        }else{
            return list.toArray();
        }
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
        if (str == null) throw new NullPointerException();
        
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
        // Prevent nulls
        for (String str : array) if (str == null) throw new NullPointerException();
        
        // Go to the end of the list
        StringNode node = listHeader;
        while(node.next != null)
            node = node.next;
        
        // Add each string to the end of the list
        for(String str : array){
            node.next = new StringNode(str);
            node = node.next;
        }
        
        return this;
    }
    
    
    // Add a list of strings to the end of the list
    @Override
    public StringList add(IStringList list){
        // Prevent nulls
        IStringNode node = list.getHeader();
        while ((node = node.getNext()) != null)
            if (node.getValue() == null) throw new NullPointerException();
        
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
            if (StringHandler.areEqual(node.next.value, str)) return false;
            node = node.next;
        }
        
        // Didn't find it, add it
        node.next = new StringNode(str);
        return true;
    }
    
    
    // Insert a string at the given position. If the list is too small to insert at the given position, throws an exception
    public void insert(String str, int position){
        // Prevent nulls and obvious bad-indexes
        if (str == null) throw new NullPointerException();
        if (position < 0) throw new ArrayIndexOutOfBoundsException();
        
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
        // Prevent nulls and obvious bad-indexes
        for (String str : array) if (str == null) throw new NullPointerException();
        if (position < 0) throw new ArrayIndexOutOfBoundsException();
        
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
            if (StringHandler.areEqual(node.value, str)) count++;
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
        for (String str : strs) if(str == null) throw new NullPointerException();
        
        // Cut out all nodes that match
        StringNode node = listHeader; int count = 0;
        while(node.next != null){
            // Does the next string match any of the strings we are removing?
            boolean matchFound = false;
            for(String str : strs){
               if(StringHandler.areEqual(node.next.value, str)){
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
        if (str == null) throw new NullPointerException();
        
        // Cut out the first node that matches
        StringNode node = listHeader;
        while(node.next != null){
            if(StringHandler.areEqual(node.next.value, str)){
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
        // If we actually have a null, that's a problem. As such, prevent nulls here as well
        if (str == null) throw new NullPointerException();
        
        // Look for a match
        StringNode node = listHeader;
        while((node = node.next) != null){
            if (StringHandler.areEqual(str, node.value)) return true;
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
    
    // Check if this list is equal to another object (StringList or otherwise)
    @Override
    public boolean equals(Object o){
        if (o instanceof StringList)
            return contentsMatch(this, (StringList)o);
        return false;
    }
    
    public static boolean contentsMatch(IStringList list1, IStringList list2){
        // May the runners take their positions
        IStringNode thisNode = list1.getHeader();
        IStringNode thatNode = list2.getHeader();
        
        // Loop until we have a verdict
        while(true){
            // Move up a node
            thisNode = thisNode.getNext();
            thatNode = thatNode.getNext();
            
            // Check if the ends of both lists have been reached
            if ((thisNode == null) && (thatNode == null))
                return true;
            
            // Check if one of the lists is shorter than the other
            if ((thisNode == null) || (thatNode == null))
                return false;
            
            // Check if the nodes have different values
            if (!StringHandler.areEqual(thisNode.getValue(), thatNode.getValue()))
                return false;
            
            // Both nodes had values, said values matched, move on to the next pair of nodes
        }
    }
    
}
