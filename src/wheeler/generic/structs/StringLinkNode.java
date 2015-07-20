/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import wheeler.generic.data.StringHandler;

/**
 *
 * @author Greg
 */
public class StringLinkNode implements IStringNode {
    
    private static long instanceIndex = 0;
    
    // Two nodes; one before, one after
    public StringLinkNode prev;
    public StringLinkNode next;
    
    // The value of this node
    public String value;
    
    // An extra value used only by our StringLinkedList
    private int myChainIndex;
    
    // An instance signature used to make sure nodes are pointing to the right nodes
    private long myInstance;
    
    
    
    /// Constructors ///
    
    public StringLinkNode(String val){
        value = val;
        myChainIndex = -1;
        myInstance = instanceIndex++;
    }
    
    
    
    /// Functions ///
    
    // Put the given node after this one and before the next
    public StringLinkNode insert(String str){
        StringLinkNode node = new StringLinkNode(str);
        
        if (next != null) next.prev = node;
        node.next = next;
        next = node;
        node.prev = this;
        
        return node;
    }
    
    
    // Add the given value to the list
    /*public void add(String value){
        if(next != null && next.afterThis(value, true)){ // Goes after the next one in the list
            next.add(value);
        }else{ // Comes directly after this one; no subsequent node or its value comes after the current value
            insert(value);
        }
    }*/
    
    
    // Does the given string occur in the list
    /*public boolean contains(String str){
        if (StringHandler.areEqual(str, value, false)) return true;
        return (next != null && next.afterThis(str, true))
                ? next.contains(str)
                : false;
    }*/
    
    
    // Does the given string come after the value in this node?
    public boolean afterThis(String str, boolean includeMatch){
        if (value == null) return true;
        if (includeMatch && StringHandler.areEqual(value, str, false)) return true;
        return StringHandler.strAafterB(str, value);
    }
    // Does the given string come before the value in this node?
    public boolean beforeThis(String str, boolean includeMatch){
        if (value == null) return false;
        if (includeMatch && StringHandler.areEqual(value, str, false)) return true;
        return StringHandler.strAbeforeB(str, value);
    }
    
    
    // Get the last node in the list
    /*public StringLinkNode last(){
        return (next != null)
                ? next.last()
                : this;
    }*/
    
    
    // Get the length of the list, including this node if appropriate
    /*public int length(){
        int myValue = (value != null) ? 1 : 0;
        return (next != null)
            ? next.length() + myValue
            : myValue;
    }*/
    
    
    // Get the index for this section of linked nodes
    /*public int getChainIndex(){
        return (myChainIndex < 0 && prev != null)
                ? prev.getChainIndex()
                : myChainIndex;
    }*/
    public int getChainIndex(){
        StringLinkNode node = this;
        while (node.myChainIndex < 0 && node.prev != null) node = node.prev;
        return node.myChainIndex;
    }
    public void clearChainIndex(){ myChainIndex = -1; }
    public void setChainIndex(int i){ myChainIndex = i; }
    public boolean chainIndexSet(){ return myChainIndex != -1; }
    
    // Let's bend the rule about "operate from without" here; this class IS structured around double-linking
    public void remove(){
        if (next != null) next.prev = prev;
        if (prev != null) prev.next = next;
        next = null; prev = null;
    }
    public boolean wasRemoved(){
        return (next == null) && (prev == null);
    }
    
    @Override
    public StringLinkNode getNext(){ return next; }
    @Override
    public String getValue(){ return value; }
    public long getInstanceNumber(){ return myInstance; }
    
}
