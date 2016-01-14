/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import wheeler.generic.data.LogicHandler;
import wheeler.generic.data.MathHandler;

/**
 * A list optimized for gathering strings and then returning an array or StringList (simple in function, not structure).
 * Contains a single-linked list of value/node pairs stored in reverse order
 * (this way new values are added at the "head" rather than requiring iteration across the list to the "tail").
 */
public class StringSimpleList implements IStringList {
    
    /// Variables ///
    
    protected StringList list = new StringList();
    
    
    
    /// Constructors ///
    
    public StringSimpleList(){}
    public StringSimpleList(String str){
        add(str);
    }
    public StringSimpleList(String[] data){
        for (String str : data) add(str);
    }
    
    
    
    /// Functions ///
    
    @Override
    public final StringSimpleList add(String str){
        list.insert(str, 0);
        return this;
    }
    
    
    @Override
    public StringSimpleList add(String[] array){
        for (String str : array) list.insert(str, 0);
        return this;
    }
    
    
    @Override
    public StringSimpleList add(IStringList sList){
        IStringNode node = sList.getHeader();
        while((node = node.getNext()) != null)
            list.insert(node.getValue(), 0);
        return this;
    }
    
    
    @Override
    public boolean contains(String str){
        return list.contains(str);
    }
    
    
    @Override
    public String pullFirst(){
        return list.pullLast();
    }
    @Override
    public String getFirst(){
        return list.getLast();
    }
    
    
    @Override
    public String pullLast(){
        return list.pullFirst();
    }
    @Override
    public String getLast(){
        return list.getFirst();
    }
    
    
    @Override
    public int count(String str){
        return list.count(str);
    }
    
    
    @Override
    public int remove(String str){
        return list.remove(str);
    }
    @Override
    public int remove(String[] strs){
        return list.remove(strs);
    }
    
    
    public void remove(int index){
        list.remove(list.length() - 1 - index);
    }
    
    
    @Override
    public String[] toArray(){
        return list.getReversedArray();
    }
    
    
    public String[] getArray(int size){
        /*
        String[] result = new String[LogicHandler.min(size, count)];
        for(int i = 0; i < count - size; i++) list.get();
        for(int i = 0; i < result.length; i++) result[result.length - i - 1] = list.get();
        list.get(); // Drop that last null
        */
        size = MathHandler.min(size, list.length());
        return LogicHandler.reverseArray(list.getArray(size, list.length() - size));
    }
    
    public StringList toStringList(){
        StringList result = new StringList();
        StringNode node = list.getHeader();
        while((node = node.next) != null){
            result.insert(node.value, 0);
        }
        return result;
    }
    
    @Override
    public StringNode getHeader(){
        return toStringList().getHeader();
    }
    
    
    
    /// Getters and Setters ///
    
    @Override
    public int length(){ return list.length(); }
    @Override
    public boolean any(){ return list.any(); }
    @Override
    public boolean isEmpty(){ return list.isEmpty(); }
    
}
