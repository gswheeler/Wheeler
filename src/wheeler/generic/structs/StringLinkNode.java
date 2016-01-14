/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import wheeler.generic.data.StringHandler;

/**
 * A double-linked list node with a String value.
 * Used primarily by the StringSortedList class, which sorts string and maintains an index
 */
public class StringLinkNode implements IStringNode {
    
    private static long instanceIndex = 0;
    
    // Two nodes; one before, one after
    public StringLinkNode prev;
    public StringLinkNode next;
    
    // The value of this node
    public String value;
    
    // Extra values used only by StringSortedList to manage its internal indexes
    private int myChainIndex;
    private boolean removed;
    
    // An instance signature used to make sure nodes are pointing to the right nodes
    private final long myInstance;
    
    
    
    /// Constructors ///
    
    public StringLinkNode(String val){
        value = val;
        myChainIndex = -1;
        removed = false;
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
    
    
    // Does the given string come after the value in this node?
    public boolean afterThis(String str, boolean includeMatch){
        if (value == null) return true;
        if (includeMatch && StringHandler.areEqual(value, str)) return true;
        return StringHandler.strAafterB(str, value);
    }
    // Does the given string come before the value in this node?
    public boolean beforeThis(String str, boolean includeMatch){
        if (value == null) return false;
        if (includeMatch && StringHandler.areEqual(value, str)) return true;
        return StringHandler.strAbeforeB(str, value);
    }
    
    
    // Get the index for this section of linked nodes (StringSortedList only)
    public int getChainIndex(){
        StringLinkNode node = this;
        while ((node != null) && !node.chainIndexSet()) node = node.prev;
        return (node != null) ? node.myChainIndex : -1;
    }
    public void clearChainIndex(){ myChainIndex = -1; }
    public void setChainIndex(int i){ myChainIndex = i; }
    public boolean chainIndexSet(){ return myChainIndex != -1; }
    
    // Let's bend the rule about "operate from without" here; this class IS structured around double-linking
    public void remove(){
        if (next != null) next.prev = prev;
        if (prev != null) prev.next = next;
        next = null; prev = null;
        removed = true;
    }
    public boolean wasRemoved(){
        return removed;
    }
    
    @Override
    public StringLinkNode getNext(){ return next; }
    @Override
    public String getValue(){ return value; }
    public long getInstanceNumber(){ return myInstance; }
    
}
