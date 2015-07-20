/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wheeler.generic.structs;

import java.util.Calendar;
import wheeler.generic.data.StringHandler;
import wheeler.generic.data.TimeHandler;

/**
 *
 * @author Greg
 */
public class DateTime {
    
    protected int year;
    protected int month;
    protected int day;
    protected int hour;
    protected int minute;
    protected int second;
    protected int milli;
    
    public DateTime(int y, int mo, int d){
        init(y, mo, d, 0, 0, 0, 0);
    }
    public DateTime(int y, int mo, int d, int h){
        init(y, mo, d, h, 0, 0, 0);
    }
    public DateTime(int y, int mo, int d, int h, int min){
        init(y, mo, d, h, min, 0, 0);
    }
    public DateTime(int y, int mo, int d, int h, int min, int s){
        init(y, mo, d, h, min, s, 0);
    }
    public DateTime(int y, int mo, int d, int h, int min, int s, int mil){
        init(y, mo, d, h, min, s, mil);
    }
    private void init(int y, int mo, int d, int h, int min, int s, int mil){
        year = y;
        month = mo;
        day = d;
        hour = h;
        minute = min;
        second = s;
        milli = mil;
        normalize();
    }
    private void normalize(){
        while (milli  > 1000){ second++; milli  -= 1000; }
        while (second > 60)  { minute++; second -= 60; }
        while (minute > 60)  { hour++;   minute -= 60; }
        while (hour   > 24)  { day++;    hour   -= 24; }
        while (month > 12) { year++; month -= 12; }
        while (day > TimeHandler.daysInMonth(month,year)){
            day -= TimeHandler.daysInMonth(month,year);
            month++;
            while (month > 12) {year++; month -= 12;}
        }
        while (month > 12) { year++; month -= 12; }
    }
    private void normalizeReverse(){
        while (milli  < 0)   { second--; milli  += 1000; }
        while (second < 0)   { minute--; second += 60; }
        while (minute < 0)   { hour--;   minute += 60; }
        while (hour   < 0)   { day--;    hour   += 24; }
        while (month  < 1 && year > 0){
            year--;
            month  += 12;
        }
        while (day < 1 && month > 0){
            month--;
            while (month < 1 && year > 0) {year--; month += 12;}
            day += TimeHandler.daysInMonth(month,year);
        }
        while (month < 1 && year > 0)  {year--; month += 12;}
    }
    
    public static DateTime now(){
        Calendar nowDate = Calendar.getInstance();
        return new DateTime(
            nowDate.get(Calendar.YEAR),
            nowDate.get(Calendar.MONTH)+1,
            nowDate.get(Calendar.DATE),
            nowDate.get(Calendar.HOUR_OF_DAY),
            nowDate.get(Calendar.MINUTE),
            nowDate.get(Calendar.SECOND),
            nowDate.get(Calendar.MILLISECOND)
        );
    }
    
    public static DateTime getTime(int h, int min){
        return getTime(h, min, 0, 0);
    }
    public static DateTime getTime(int h, int min, int s){
        return getTime(h, min, s, 0);
    }
    public static DateTime getTime(int h, int min, int s, int mil){
        Calendar nowDate = Calendar.getInstance();
        return new DateTime(
            nowDate.get(Calendar.YEAR),
            nowDate.get(Calendar.MONTH)+1,
            nowDate.get(Calendar.DATE),
            h, min, s, mil
        );
    }
    
    public static DateTime timespan(int h, int min, int s){
        return timespan(h, min, s, 0);
    }
    public static DateTime timespan(int h, int min, int s, int mil){
        return new DateTime(0, 0, 0, h, min, s, mil);
    }
    
    public void addHours(int num){ hour += num; normalize(); }
    public void addMinutes(int num){ minute += num; normalize(); }
    public void addSeconds(int num){ second += num; normalize(); }
    public int getHour(){return hour;}
    public int getMinute(){return minute;}
    public int getSecond(){return second;}
    public void removeSeconds(int num){ second -= num; normalizeReverse(); }
    public void clearMinute(){minute = 0;}
    public void clearMilli(){milli = 0;}
    
    public boolean lessThan(DateTime dateTime){ return compareTo(dateTime) < 0; }
    public boolean greaterThan(DateTime dateTime){ return compareTo(dateTime) > 0; }
    public boolean equalTo(DateTime dateTime){ return compareTo(dateTime) == 0; }
    public boolean lessThanEqualTo(DateTime dateTime){ return !greaterThan(dateTime); }
    public boolean greaterThanEqualTo(DateTime dateTime){ return !lessThan(dateTime); }
    
    public int compareTo(DateTime dateTime){
        if (year   != dateTime.year  ) return (year   > dateTime.year  ) ? 1 : -1;
        if (month  != dateTime.month ) return (month  > dateTime.month ) ? 1 : -1;
        if (day    != dateTime.day   ) return (day    > dateTime.day   ) ? 1 : -1;
        if (hour   != dateTime.hour  ) return (hour   > dateTime.hour  ) ? 1 : -1;
        if (minute != dateTime.minute) return (minute > dateTime.minute) ? 1 : -1;
        if (second != dateTime.second) return (second > dateTime.second) ? 1 : -1;
        if (milli  != dateTime.milli ) return (milli  > dateTime.milli ) ? 1 : -1;
        return 0;
    }
    
    public boolean hasPassed(){return hasPassed(true);}
    public boolean hasPassed(boolean rightNowCounts){
        return (rightNowCounts)
                ? lessThanEqualTo(now())
                : lessThan(now());
    }
    
    public static DateTime diff(DateTime dateTimeA, DateTime dateTimeB){
        DateTime greater; DateTime lesser;
        if(dateTimeA.greaterThan(dateTimeB)){
            greater = dateTimeA; lesser = dateTimeB;
        }else{
            greater = dateTimeB; lesser = dateTimeA;
        }
        DateTime diff = new DateTime(
                greater.year - lesser.year,
                greater.month - lesser.month,
                greater.day - lesser.day,
                greater.hour - lesser.hour,
                greater.minute - lesser.minute,
                greater.second - lesser.second,
                greater.milli - lesser.milli
            );
        diff.normalizeReverse();
        return diff;
    }
    
    
    public DateTime duplicate(){
        return new DateTime(year, month, day, hour, minute, second, milli);
    }
    
    
    /**Get the time represented here in string form. y=Year, M=Month, d=Day, h=Hour, m=Minute, s=Second, z=Milli
     * @param format The string that will determine the format of the string returned
     * @return What else?
     */
    public String toString(String format){
        String result = "";
        // For each segment of the format string, append the indicated data to the string
        String segment;
        while((segment = getSegment(format)) != null){
            // Get the data requested by the segment and how many leading zeroes should be used
            String c = StringHandler.charAt(segment, 0);
            int length = segment.length();
            
            // Get the right data, add to the string
            switch(c){
                case "y":
                    result += StringHandler.leadingZeroes(year, length);
                    break;
                case "M":
                    result += StringHandler.leadingZeroes(month, length);
                    break;
                case "d":
                    result += StringHandler.leadingZeroes(day, length);
                    break;
                case "h":
                    result += StringHandler.leadingZeroes(hour, length);
                    break;
                case "m":
                    result += StringHandler.leadingZeroes(minute, length);
                    break;
                case "s":
                    result += StringHandler.leadingZeroes(second, length);
                    break;
                case "z":
                    while(length > 3){ result += "0"; length--; }
                    String mStr = StringHandler.leadingZeroes(milli, 3);
                    result += mStr.substring(0, length);
                    break;
                default:
                    result += segment;
            }
            
            // Remove this segment from the format string to move on to the next one
            format = format.substring(segment.length());
        }
        return result;
    }
    private String getSegment(String source){
        // Is there anything left in the string?
        if ((source == null) || (source.length() == 0)) return null;
        
        // Get the first character; this will always be returned
        String c = StringHandler.charAt(source, 0);
        String result = c;
        // Check each following character in turn for matching ones
        for(int i = 1; i < source.length(); i++){
            // If the next character is not the same as the first, it isn't part of the segment
            if (!StringHandler.charAt(source, i).equals(c)) return result;
            // Otherwise, it is! Increase the length of the segment
            result += c;
        }
        // For those times when we segment takes up the entire string
        return result;
    }
    
    
    /// Getters and Setters ///
    
    public int year(){ return year; }
    public int month(){ return month; }
    public int day(){ return day; }
    public int hour(){ return hour; }
    public int minute(){ return minute; }
    public int second(){ return second; }
    public int milli(){ return milli; }
    
    
    
}
