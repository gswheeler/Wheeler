/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import wheeler.generic.data.enums.MessageStyle;

/**
 *
 * @author Greg
 */
public class DialogFactory {
    
    protected DialogFactory(){}
    
    public static String chooseOption(JFrame caller, String[] options, String message, String title){
        return (String) JOptionPane.showInputDialog(
                caller,
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null
            );
    }
    
    public static int customOption(JFrame caller, String[] options, String message, String title){
        int value = JOptionPane.showOptionDialog(
                caller,
                message,
                title,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null);
        if (value == JOptionPane.CLOSED_OPTION) return -1;
        if ((value >= 0) && (value < options.length)) return value;
        return -1;
    }
    
    public static String chooseFile(JFrame caller, String currentFolder){
        JFileChooser getFile = new JFileChooser();
        getFile.setCurrentDirectory(FileHandler.fileObject(currentFolder));
        return (getFile.showOpenDialog(caller) == JFileChooser.APPROVE_OPTION)
            ? getFile.getSelectedFile().getPath()
            : null;
    }
    
    public static String chooseFolder(JFrame caller, String currentFolder){
        JFileChooser getFile = new JFileChooser();
        getFile.setCurrentDirectory(FileHandler.fileObject(currentFolder));
        getFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return (getFile.showOpenDialog(caller) == JFileChooser.APPROVE_OPTION)
            ? getFile.getSelectedFile().getPath()
            : null;
    }
    
    public static String saveFile(JFrame caller, String currentFolder){
        JFileChooser getFile = new JFileChooser();
        getFile.setCurrentDirectory(FileHandler.fileObject(currentFolder));
        return (getFile.showSaveDialog(caller) == JFileChooser.APPROVE_OPTION)
            ? getFile.getSelectedFile().getPath()
            : null;
    }
    
    public static String getString(JFrame caller, String message, String caption){
        return getString(caller, message, caption, "");
    }
    public static String getString(JFrame caller, String message, String caption, String defStr){
        return (String) JOptionPane.showInputDialog(
                    caller,
                    message,
                    caption,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    defStr
                );
    }
    
    public static boolean optionYesNo(JFrame caller, String message){ return optionYesNo(caller, message, ""); }
    public static boolean optionYesNo(JFrame caller, String message, String title){
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(caller, message, title, JOptionPane.YES_NO_OPTION);
    }
    
    public static int optionYesNoCancel(JFrame caller, String message, String title){
        int retVal = JOptionPane.showConfirmDialog(caller, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
        if (retVal == JOptionPane.YES_OPTION) return 1;
        if (retVal == JOptionPane.NO_OPTION) return -1;
        return 0;
    }
    
    public static void message(JFrame caller, String message){
        JOptionPane.showMessageDialog(caller, message);
    }
    
    /**Display an exception to the user
     * @param caller JFrame that will display the dialog
     * @param message Human-readable message explaining what threw the exception or where it was caught
     * @param e The exception to print (exception type, failure message, stacktrace)
     * @param traceLevel 1 for relevant, 0 for none, -1 for FULL
     * @param indirection zero if errorMsg is called directly from the catch block
     */
    public static void errorMsg(JFrame caller, String message, Exception e, int traceLevel, int indirection){
        message(caller, message + "\n" + LogicHandler.exToString(e, traceLevel, 1 + indirection));
    }
    
    public static void message(JFrame caller, String message, MessageStyle style){
        JOptionPane.showMessageDialog(caller, message, null, fromMessageStyle(style));
    }
    
    private static int fromMessageStyle(MessageStyle style){
        switch(style){
            case Information:
                return JOptionPane.INFORMATION_MESSAGE;
            case Warning:
                return JOptionPane.WARNING_MESSAGE;
            case Question:
                return JOptionPane.QUESTION_MESSAGE;
            case Error:
                return JOptionPane.ERROR_MESSAGE;
            default:
                return JOptionPane.PLAIN_MESSAGE;
        }
    }
    
}
