/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import javax.swing.JFrame;
import wheeler.generic.data.DialogFactory;
import wheeler.generic.data.StringHandler;
import wheeler.generic.error.LogicException;

/**
 * WARNING: WORK IN PROGRESS, HAS NOT BEEN PROPERLY TESTED. A list of strings stored in sorted (alphabetical) order.
 * Contains a double-linked list of value/node/node nodes.
 */
public class StringSortedList implements IStringList {
    
    // The start of this list
    private StringLinkNode header;
    
    // Use these for quick access
    private StringLinkNode[] index;
    private int[] lengths;
    private StringLinkNode _lastNode;
    
    // Use these to optimize indexing
    private int optLength = 10;
    private int minLength = 5;
    private int maxLength = 20;
    
    
    /// Constructor ///
    public StringSortedList(){
        header = new StringLinkNode(null);
        _lastNode = header;
        recalibrateIndex(true);
    }
    public StringSortedList(String str){
        this();
        add(str);
    }
    public StringSortedList(String[] array){
        this();
        add(array);
    }
    
    
    
    /// Functions ///
    
    // Add a value to the list
    @Override
    public final StringSortedList add(String value){
        // First, see if we can take a shortcut and put it at the end of the list
        if(lastNode().afterThis(value, true)){
            lastNode().insert(value);
            lengths[lastNode().getChainIndex()]++;
            return this;
        }
        recalibrateIndex(false);
        
        // If not, move to the node that comes directly before the point of insertion and insert it
        // Note; will insert after any duplicate nodes
        StringLinkNode node = getNodeBefore(value, true);
        node.insert(value);
        lengths[node.getChainIndex()]++;
        
        // At this point, check if it makes sense to recalibrate
        recalibrateIndex(false);
        
        return this;
    }
    
    
    // Add an array of strings
    @Override
    public final StringSortedList add(String[] array){
        if (array.length == 0) return this;
        
        // Add each string in turn; trust in our basic optimization logic
        for(String str : array)
            add(str);
        
        return this;
    }
    
    
    // Add a list of strings
    @Override
    public StringSortedList add(IStringList list){
        if (list.isEmpty()) return this;
        
        // Not a whole lot we can do here:
        // If it isn't sorted we shouldn't really do so here; that's the whole point of this list
        // If they aren't all after the last node we really can't do much optimization-wise
        
        // Just add each node in turn and trust to our basic optimization infrastructure
        IStringNode node = list.getHeader();
        while((node = node.getNext()) != null){
            add(node.getValue());
        }
        
        return this;
    }
    
    
    // Does this list contain the given string?
    @Override
    public boolean contains(String value){
        recalibrateIndex(false);
        StringLinkNode node = getNodeBefore(value, true);
        return StringHandler.areEqual(node.value, value, false);
    }
    
    
    // Count the number of times a string occurs in the list
    @Override
    public int count(String value){
        recalibrateIndex(false);
        // Make us right before the first occurence
        StringLinkNode node = getNodeBefore(value, false); int count = 0;
        // If we hit the end of the list, no other matches possible
        while((node = node.next) != null){
            // If the string comes before (and is not) the value of this node, no more matches
            if (node.beforeThis(value, false)) break;
            // Otherwise, the string is held by this node
            if (StringHandler.areEqual(node.value, value, false)) count++;
        }
        return count;
    }
    
    
    
    // Remove a set of strings from the list
    @Override
    public int remove(String str){ String[] strs = {str}; return remove(strs); }
    @Override
    public int remove(String[] strs){
        if (strs.length == 0 || !any()) return 0;
        
        recalibrateIndex(false);
        
        // Make sure they're in order
        strs = StringHandler.sortStrings(strs);
        
        // Move to the node before the first string to remove. Make sure we're BEFORE any instances of the string
        StringLinkNode node = getNodeBefore(strs[0], false);
        
        // For each string, remove all instances of the string, then move to the node just before the next one
        boolean needRecalibrate = false;
        int count = 0;
        for(int i = 0; i < strs.length; i++){
            // While there are nodes to consider and they match the string to remove, remove them
            while(node.next != null && StringHandler.areEqual(node.next.value, strs[i], false)){
                // See if there are nodes being removed that have an entry in the index
                if (node.next.chainIndexSet()) needRecalibrate = true;
                // Have node.next swallow itself; it will set all values for node and node.next.next
                node.next.remove();
                lengths[node.getChainIndex()]--;
                count++;
            }
            // If there isn't a next string to move to, stop here
            if (i + 1 == strs.length) break;
            // Move up to the node that comes just before the next string
            while (node.next != null && node.next.afterThis(strs[i+1], false)) node = node.next;
        }
        
        // Done a lot of work; if we've hacked off a significant portion of a chain or removed an indexed node, recalibrate
        recalibrateIndex(needRecalibrate);
        
        // Clear the last-node variable
        _lastNode = null;
        
        return count;
    }
    
    
    // Get the index that comes before the given value
    private int getIndex(String value){
        int low = 0;
        int high = index.length - 1;
        while(high != low){
            // 0 and 1; go for 1
            // 0 and 2; go for 1
            // 0 and 3; go for 2
            int newI = ((high - low + 1) / 2) + low;
            if(index[newI].beforeThis(value, true))
                high = newI - 1; // If it comes before or occurs at this index, target index must be lower than this one
            else
                low = newI; // Otherwise the insertion point is this index or a subsequent one
        }
        return high;
    }
    
    
    // Return the node that is/will be before the first occurance of the string in the list
    // Alternately gets the last occurance of the string in the list if it's already in there
    private StringLinkNode getNodeBefore(String value, boolean getLastInstance){
        StringLinkNode node = index[getIndex(value)];
        while(node.next != null && node.next.afterThis(value, getLastInstance)) node = node.next;
        return node;
    }
    
    
    // Check if it makes sense to recalibrate the index. If yes or we're forcing this, do so
    private void recalibrateIndex(boolean force){
        boolean recalibrate = false;
        if (force){
            recalibrate = true;
        }else{
            for(int i = 0; i < lengths.length; i++){
                if ((lengths[i] < minLength) || (lengths[i] > maxLength) || index[i].wasRemoved()){
                    recalibrate = true;
                    break;
                }
            }
        }
        if (recalibrate) recalibrateIndexWorker();
    }
    // Re-calibrate the indexes; start with the header, register every optLength nodes
    private void recalibrateIndexWorker(){
        int count = length() + 1;
        
        // Must have at least one for the header, last index should not have less than minLength nodes after it
        int numI = 1;
        while (count >= minLength + optLength){ numI++; count -= optLength; }
        index = new StringLinkNode[numI];
        lengths = new int[numI];
        
        // Populate the index, along with the lengths array
        StringLinkNode node = header;
        // Remove any chain indexing
        while ((node = node.next) != null)
            node.clearChainIndex();
        // Index the header
        node = header;
        int i = 0;
        index[i] = node; node.setChainIndex(i); i++;
        // Index every optLength nodes, informing them of their index
        while(i < numI){
            lengths[i-1] = optLength;
            for (int j = 0; j < optLength; j++) node = node.next;
            index[i] = node; node.setChainIndex(i); i++;
        }
        lengths[i-1] = length(node);
    }
    
    
    // Set the performance counts
    public void setIndexCounts(int opt, int min, int max) throws Exception{
        // Obvious checks
        if (opt < 1) throw new Exception("Optimal length must be greater than zero");
        if (min < 0) throw new Exception("Mimimum length cannot be less than zero");
        if (max < 0) throw new Exception("Maximum length cannot be less than zero");
        
        // Set the values
        optLength = opt;
        minLength = min;
        maxLength = max;
    }
    
    
    // Make sure the list is accurate; report the first out-of-order nodes
    public boolean isValid(JFrame caller) throws Exception{
        // What we are checking for:
        /*Header nodes:
         * must not be null
         * must have a null value
         * must not have a prev node
         * must have a chain value of zero
         */
        /*List nodes
         * Cannot have a null value
         * Must have a prev node
         * Must not come before their "next" node (if they have one)
         * Must be their prev's next and their next's prev
         * Must have a value that triggers the right index
         */
        /*Chain values
         * Must occur in ascending order
         * Must point to an index with the same value as the node with that index
         * Must point to a node whose index is set
         */
        /*Length values
         * Must have a number of nodes in the chain equal to the stored length
         * The nodes must point to the correct indexed node
         */
        
        
        // Test the header node
        if(header == null){
            return invalidMsg(caller, "the header was somehow null");
        }
        if(header.value != null){
            return invalidMsg(caller, "the header's value was not null:\n" + header.value);
        }
        if(header.prev != null){
            return invalidMsg(caller, "the header's prev field was set:\n"
                    + ((header.prev.value != null) ? header.prev.value : "(null))"));
        }
        if(!header.chainIndexSet()){
            return invalidMsg(caller, "the header's chain index field was not set");
        }
        if(header.getChainIndex() != 0){
            return invalidMsg(caller, "the header's chain index was " + header.getChainIndex() + ", not zero like we expected");
        }
        
        // Test each list node
        StringLinkNode node = header.next;
        int count = 1;
        int lastChainIndex = 0;
        int lastIndexCount = 0;
        while(node != null){
            if(node.value == null){
                return invalidMsg(caller, "node " + count + " had a null value");
            }
            if(node.prev == null){
                return invalidMsg(caller, "didn't have a prev value for node " + count + "\n" + node.value);
            }
            if(node.next != null && node.next.value != null
                    && StringHandler.strAafterB(node.value, node.next.value)){
                return invalidMsg(caller, "strings " + count + " and " + (count+1) + " were out of order:\n"
                        + node.value + "\n" + node.next.value);
            }
            if(node.next != null){
                if (node.next.prev == null)
                    return invalidMsg(caller, "the prev field of the node after node " + count + " was unset");
                if (node.getInstanceNumber() != node.next.prev.getInstanceNumber())
                    return invalidMsg(caller, "the prev field of the node after node " + count
                            + " was pointing at node instance\n" + node.next.prev.getInstanceNumber()
                            + "\nwhereas the node in question is node instance\n" + node.getInstanceNumber());
            }
            if(node.prev != null){
                if (node.prev.next == null)
                    return invalidMsg(caller, "the next field of the node before node " + count + " was unset");
                if (node.getInstanceNumber() != node.prev.next.getInstanceNumber())
                    return invalidMsg(caller, "the next field of the node before node " + count
                            + " was pointing at node instance\n" + node.prev.next.getInstanceNumber()
                            + "\nwhereas the node in question is node instance\n" + node.getInstanceNumber());
            }
            if(node.chainIndexSet()){
                if(node.getChainIndex() > lastChainIndex){
                    lastChainIndex = node.getChainIndex();
                    lastIndexCount = count;
                }else{
                    return invalidMsg(caller, "found chain index " + node.getChainIndex() + " in node "
                            + count + "after finding index " + lastChainIndex + " in node " + lastIndexCount
                            + ":\n" + node.value);
                }
                if(index[node.getChainIndex()].getInstanceNumber() != node.getInstanceNumber()){
                    return invalidMsg(caller, "the node with index " + node.getChainIndex()
                            + " did not match the node registered at that index:\n"
                            + node.getInstanceNumber() + "\n" + index[node.getChainIndex()].getInstanceNumber());
                }
            }
            // Always checked: nesting this block to maintian readability
            {
                int indexVal = getIndex(node.value);
                StringLinkNode indexNode = index[indexVal];
                if((indexNode.value != null) && (!StringHandler.strAbeforeB(indexNode.value, node.value)))
                    return invalidMsg(caller, "getIndex, when called with value\n" + node.value
                            + "\non node " + count + " returned index\n" + indexVal + "\n which pointed at a node with value\n"
                            + indexNode.value + "\nwhich is not BEFORE the requested value");
                while(!StringHandler.areEqual(indexNode.value, node.value, true)){
                    indexNode = indexNode.next;
                    if(StringHandler.strAafterB(indexNode.value, node.value))
                        return invalidMsg(caller, "calling getIndex with value\n" + node.value
                                + "\nreturned index " + indexVal + "; working up the list,\n" + indexNode.value
                                + "\n, which comes after the call value, was encountered before the call value.");
                }
            }
            node = node.next; count++;
        }
        
        // Test the chain index
        for(int i = 0; i < index.length; i++){
            node = index[i];
            if(!node.chainIndexSet()){
                return invalidMsg(caller, "the node at index " + i + " did not have its index set");
            }
            if(node.getChainIndex() != i){
                return invalidMsg(caller, "the node at index " + i + " did not have its index set to "
                        + i + "; was set to " + node.getChainIndex());
            }
            if(node.wasRemoved()){
                return invalidMsg(caller, "the node at index " + i + " had been removed");
            }
        }
        
        // Test the lengths index
        if (index.length != lengths.length)
            return invalidMsg(caller, "the chain index had a different number of values than the lengths index ("
                    + index.length + " chains and " + lengths.length + " lengths)");
        for(int i = 0; i < lengths.length; i++){
            node = index[i];
            for(int j = 0; j < lengths[i]; j++){
                if (node == null)
                    return invalidMsg(caller, "hit the end of a list when looking along the length of a chain (the chain at index "
                            + i + " only had " + j + " items when it was supposed to have " + lengths[i] + ")");
                if (node.getChainIndex() != i)
                    return invalidMsg(caller, "chain " + i + " claimed to have " + lengths[i] + " items but item "
                            + j + " claimed to be a part of chain " + node.getChainIndex());
                node = node.next;
            }
            if(i + 1 < lengths.length){
                if (node == null)
                    return invalidMsg(caller, "there were supposed to be items after chain " + i + ", but the end of the list was found instead");
                if (node.getChainIndex() != i + 1)
                    return invalidMsg(caller, "the first item after chain " + i
                            + " was supposed to be part of the next chain, but it claimed to be part of chain "
                            + node.getChainIndex());
            }else{
                if (node != null)
                    return invalidMsg(caller, "items were found after the end of the last chain");
            }
        }
        
        // Everything passed
        return true;
    }
    private boolean invalidMsg(JFrame caller, String detail) throws Exception{
        String message = "There's a bug in our StringSortedList: " + detail;
        if (caller == null) throw new LogicException(message);
        DialogFactory.message(caller, message);
        return false;
    }
    
    
    // Get the header as a makeshift "get" operator
    @Override
    public StringLinkNode getHeader(){ return header; }
    
    
    // Pull out the first concrete value
    @Override
    public String pullFirst(){
        if (header.next == null) return null;
        
        // Decrement the lengths counter for the header; make it so recalibration is all but certain
        lengths[0]--;
        
        // Whenever we hit an indexed node, have the list recalibrate itself
        // If this would be a performance problem, reset the counters using setIndexCounts()
        boolean hitIndexedNode = header.next.chainIndexSet();
        
        // Grab the value and eat the node. Recalibrate if the index has been invalidated
        String value = header.next.value;
        header.next.remove();
        if (hitIndexedNode) recalibrateIndex(true);
        return value;
    }
    @Override
    public String getFirst(){
        return (header.next != null) ? header.next.value : null;
    }
    
    
    // Get the last value in the list
    @Override
    public String getLast(){
        return lastNode().value;
    }
    // Get the last value in the list, then remove it
    @Override
    public String pullLast(){
        if (header.next == null) return null;
        
        // Grab the value
        StringLinkNode node = lastNode();
        
        // Whenever we hit an indexed node, have the list recalibrate itself
        // If this would be a performance problem, reset the counters using setIndexCounts()
        boolean hitIndexedNode = node.chainIndexSet();
        
        // Have the node remove itself from the list, return the value
        _lastNode = node.prev;
        node.remove();
        if (hitIndexedNode) recalibrateIndex(true);
        return node.value;
    }
    
    
    // Get an array of Strings from the contents of this list.
    // If this struct is being used to accomodate scope, this in not recommended.
    // If it's just to make sure strings are sorted, it's fine
    @Override
    public String[] toArray(){
        String[] result = new String[length()];
        StringLinkNode node = header.next;
        for(int i = 0; i < result.length; i++){
            result[i] = node.value;
            node = node.next;
        }
        return result;
    }
    
    
    // 
    private StringLinkNode lastNode(){
        if (_lastNode == null || _lastNode.wasRemoved())
            _lastNode = index[index.length-1];
        while (_lastNode.next != null)
            _lastNode = _lastNode.next;
        
        return _lastNode;
    }
    
    
    // Get the length of the list
    @Override
    public int length(){
        return length(header);
    }
    // Get the length of a chain of nodes, starting from the given one
    private static int length(StringLinkNode node){
        int count = 0;
        while(node != null){
            if (node.value != null) count++;
            node = node.next;
        }
        return count;
    }
    
    @Override
    public boolean any(){ return header.next != null; }
    @Override
    public boolean isEmpty(){ return header.next == null; }

    @Override
    public StringSortedList getNew() {
        return new StringSortedList();
    }
    
    
}
