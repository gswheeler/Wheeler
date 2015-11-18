/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

/**
 * A single-linked list node with a String value.
 */
public class StringNode implements IStringNode {
    
    /// Constructors
    public StringNode(){ value = null; }
    public StringNode(String str){ value = str; }
    public StringNode(String str, StringNode node){
        this(str);
        next = node;
    }
    
    /// Variables
    public StringNode next = null;
    public String value;
    
    
    /// Getters and Setters
    @Override
    public StringNode getNext(){ return next; }
    @Override
    public String getValue(){ return value; }
    
}
