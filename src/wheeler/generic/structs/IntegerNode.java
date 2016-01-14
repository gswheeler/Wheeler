/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

/**
 * A single-linked list node with an Integer value.
 */
public class IntegerNode {
    
    /// Constructors
    public IntegerNode(int i){ value = i; }
    public IntegerNode(int i, IntegerNode node){
        this(i);
        next = node;
    }
    
    /// Variables
    public IntegerNode next = null;
    public int value;
    
}
