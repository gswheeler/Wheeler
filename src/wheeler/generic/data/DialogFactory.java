/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import wheeler.generic.data.enums.MessageStyle;
import wheeler.generic.logging.Logger;

/**
 * Contains static functions for presenting the user with dialogues used for input or messaging.
 */
public class DialogFactory {
    
    protected DialogFactory(){}
    
    public static String chooseOption(JFrame caller, String[] options, String message){
        return chooseOption(caller, options, message, "");
    }
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
    
    public static int customOption(JFrame caller, String[] options, String message){
        return customOption(caller, options, message, "");
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
    
    public static String getString(JFrame caller, String message){
        return getStringWithDefault(caller, message, "", "");
    }
    public static String getString(JFrame caller, String message, String title){
        return getStringWithDefault(caller, message, title, "");
    }
    public static String getStringWithDefault(JFrame caller, String message, String defStr){
        return getStringWithDefault(caller, message, "", defStr);
    }
    public static String getStringWithDefault(JFrame caller, String message, String title, String defStr){
        return (String) JOptionPane.showInputDialog(
                    caller,
                    message,
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    defStr
                );
    }
    
    public static boolean optionYesNo(JFrame caller, String message){
        return optionYesNo(caller, message, "");
    }
    public static boolean optionYesNo(JFrame caller, String message, String title){
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(caller, message, title, JOptionPane.YES_NO_OPTION);
    }
    
    public static int optionYesNoCancel(JFrame caller, String message){
        return optionYesNoCancel(caller, message, "");
    }
    public static int optionYesNoCancel(JFrame caller, String message, String title){
        int retVal = JOptionPane.showConfirmDialog(caller, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
        if (retVal == JOptionPane.YES_OPTION) return 1;
        if (retVal == JOptionPane.NO_OPTION) return -1;
        return 0;
    }
    
    public static void message(JFrame caller, String message){
        message(caller, message, "");
    }
    public static void message(JFrame caller, String message, String title){
        JOptionPane.showMessageDialog(caller, message, title, JOptionPane.PLAIN_MESSAGE);
    }
    
    public static boolean optionOkCancel(JFrame caller, String message){
        return optionOkCancel(caller, message, "");
    }
    public static boolean optionOkCancel(JFrame caller, String message, String title){
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(caller, message, title, JOptionPane.OK_CANCEL_OPTION);
    }
    
    /**Display an exception to the user
     * @param caller JFrame that will display the dialog
     * @param message Human-readable message explaining what threw the exception or where it was caught
     * @param e The exception to print (exception type, failure message, stacktrace)
     * @param traceLevel 1 for relevant, 0 for none, -1 for FULL
     * @param indirection number of nested calls between the catch block and the call to this function (zero if called directly from the catch block, negatives don't work)
     */
    public static void errorMsg(JFrame caller, String message, Exception e, int traceLevel, int indirection){
        message(caller, message + "\n" + LogicHandler.exToString(e, traceLevel, 1 + indirection));
    }
    
    /**
     * When working with the console, prompt the user to press Enter to continue.
     * @param prompt The prompt to display to the user (does not print a newline or a trailing space).
     */
    public static void pressEnterToContinue(String prompt){
        System.out.print(prompt);
        pressEnterToContinue();
    }
    
    /**
     * When working with the console, wait for the user to press Enter before continuing.
     */
    public static void pressEnterToContinue(){
        getStringFromConsole();
    }
    
    /**
     * When working with the console, get a String from the user.
     * @return The String entered by the user.
     */
    public static String getStringFromConsole(){
        return System.console().readLine();
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
