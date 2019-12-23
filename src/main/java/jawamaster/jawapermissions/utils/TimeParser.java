/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** This class deals with time and date parsing.
 *
 * @author alexander
 */
public class TimeParser {

    /** Takes a LocalDateTime converted to a string and outputs a string in the form
     * of Month Day, Year at H:M.
     * @param time
     * @return 
     */
    public static String getHumanReadableDateTime(String time) {
        LocalDateTime parsedTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String hRTime = parsedTime.getMonth().toString() + " "
                + String.valueOf(parsedTime.getDayOfMonth()) + ", "
                + parsedTime.getYear() + " at "
                + parsedTime.getHour() + ":"
                + parsedTime.getMinute() + " "
                + java.util.TimeZone.getDefault().getDisplayName();

        return hRTime;

    }

    /** Takes a LocalDateTime converted to a string and outputs false if that LocalDateTime is 
     * after the current time. I.E. false if in the past. true 
     * @param to
     * @return 
     */
    public static boolean isNowExpired(String to) {
        LocalDateTime toTime = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return toTime.isAfter(LocalDateTime.now());

    }

}
