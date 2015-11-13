/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.data;

import java.util.Calendar;
import wheeler.generic.structs.DateTime;

/**
 *
 * @author Greg
 */
public class TimeHandler {
    
    /// Variables ///
    
    
    
    /// Functions ///
    
    /*public static String getDatestamp(){
        Calendar thisDate = Calendar.getInstance();
        int month = thisDate.get(Calendar.MONTH) + 1;
        int day = thisDate.get(Calendar.DAY_OF_MONTH);
        int year = thisDate.get(Calendar.YEAR);
        String result = ((month < 10) ? "0" : "") + Integer.toString(month);
        result += "-" + ((day < 10) ? "0" : "") + Integer.toString(day);
        result += "-" + Integer.toString(year);
        return result;
    }
    public static String getDateTag(){
        Calendar thisDate = Calendar.getInstance();
        int month = thisDate.get(Calendar.MONTH) + 1;
        int day = thisDate.get(Calendar.DAY_OF_MONTH);
        int year = thisDate.get(Calendar.YEAR);
        String result = Integer.toString(year);
        result += "-" + ((month < 10) ? "0" : "") + Integer.toString(month);
        result += "-" + ((day < 10) ? "0" : "") + Integer.toString(day);
        return result;
    }*/
    
    
    /**Gets the current time represented in string form. y=Year, M=Month, d=Day, h=Hour, m=Minute, s=Second, z=Milli
     * @param format The string that will determine the format of the string returned
     * @return What else?
     */
    public static String getTimestamp(String format){
        return DateTime.now().toString(format);
    }
    
    
    public static String getClockTime(boolean military, boolean includeSeconds){
        Calendar thisDate = Calendar.getInstance();
        int hour = thisDate.get(Calendar.HOUR_OF_DAY);
        int minute = thisDate.get(Calendar.MINUTE);
        int second = thisDate.get(Calendar.SECOND);
        String append = (military) ? "" : (" " + ((hour < 12) ? "AM" : "PM"));
        if(!military){
            hour = hour % 12;
            if (hour == 0) hour = 12;
        }
        String result = ((hour < 10) ? "0" : "") + Integer.toString(hour);
        result += ":" + ((minute < 10) ? "0" : "") + Integer.toString(minute);
        if (includeSeconds) result += ":" + ((second < 10) ? "0" : "") + Integer.toString(second);
        return result + append;
    }
    
    
    public static int parseClock(String clock){
        String[] digits = StringHandler.parseIntoArray(clock, ":");
        int total = 0;
        for(int i = 0; i < digits.length; i++){
            total = (60*total) + Integer.valueOf(digits[i]);
        }
        return total;
    }
    
    
    public static int getHour(){ return getHour(true); }
    public static int getHour(boolean military){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (!military){ hour = hour % 12; if (hour == 0) hour = 12; }
        return hour;
    }
    public static int getMinutes(){
        return Calendar.getInstance().get(Calendar.MINUTE);
    }
    public static int getSeconds(){
        return Calendar.getInstance().get(Calendar.SECOND);
    }
    
    
    public static long ticks(){ return System.currentTimeMillis(); }
    
    
    public static int daysInMonth(int month, int year){
        switch(month){
            // February
            case 2:
                return (year % 4 == 0) ? 29 : 28;
            // September
            // April
            // June
            // November
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            // All the rest
            default:
                return 31;
        }
    }
    
}
