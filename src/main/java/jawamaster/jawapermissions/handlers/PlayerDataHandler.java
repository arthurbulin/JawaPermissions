/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jawamaster.jawapermissions.JawaPermissions;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerDataHandler {
    public final JawaPermissions plugin;
    public static final String handlerSlug = "[PlayerDataHandler] ";
    
    public PlayerDataHandler(JawaPermissions plugin){
        this.plugin = plugin;
    }
    
    /** Creates the player data for commiting to the ElasticSearch index.
     * @param name
     * @param ip
     * @return
     */
    public static HashMap<String, Object> firstTimePlayer(String name, String ip){
        HashMap<String, Object> playerData = new HashMap();
        playerData.put("first-login", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        playerData.put("last-login", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        playerData.put("last-logout", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        playerData.put("play-time", 0);
        
        playerData.put("name", name);
        playerData.put("name-data", (new JSONArray()).add(name) );
        playerData.put("rank", "guest"); //TODO pull basic rank from permissions files and immunity levels
        
        playerData.put("banned", false);
        playerData.put("nick", new HashMap());
        playerData.put("tag", "false");
        playerData.put("star", new HashMap());
        
        playerData.put("ip",(new JSONArray()).add(ip));

        if (JawaPermissions.debug){
            System.out.print(JawaPermissions.pluginSlug + handlerSlug + "firstTimePlayer data created as follows: " + playerData.toString());
        }
        
        return playerData;
    }
    
    public static Map<String, Object> assembleBanData(CommandSender commandSender, String name, String reason, String endOfBan){
        
        Map<String, Object> banData = new HashMap();
        LocalDateTime endOfBanDate = assessBanTime(endOfBan);
        System.out.println(JawaPermissions.pluginSlug + " end of ban date" + endOfBanDate);
       
        banData.put("reason", reason);
       
        banData.put("banned-by", commandSender.getName());
        banData.put("player", name);
        
        banData.put("end-of-ban", endOfBanDate);
        banData.put("date", LocalDateTime.now());
        
        banData.put("active", true);
        banData.put("ban-lock", false);
        
        return banData;
    }
    
    public static LocalDateTime assessBanTime(String time){
        time = time.toLowerCase();
        Long formatted;
        if (time.endsWith("d")){
            formatted = Long.valueOf( time.substring(0,time.length()-1));
            return LocalDateTime.now().plusDays(formatted);
            
        } else if (time.endsWith("h")){
            formatted = Long.valueOf(time.substring(0, time.length()-1));
            return LocalDateTime.now().plusHours(formatted);
            
        } else if (time.endsWith("w")){
            formatted = Long.valueOf(time.substring(0, time.length()-1));
            return LocalDateTime.now().plusWeeks(formatted);
            
        } else if (time.endsWith("y")){
            formatted = Long.valueOf(time.substring(0, time.length()-1));
            return LocalDateTime.now().plusYears(formatted);
            
        } else {
            return LocalDateTime.now().plusYears(20);
//            return LocalDateTime.MAX;
        }
        
    }
    
    /** *  Determine if an ip address is contained within the player's history set.
     * If it isn't then add it and return it. If it is already in the set then 
     * return null.
     * @param ipAddress
     * @return 
     */
    public static JSONArray ipData(String ipAddress, JSONArray ipData){
        if (!ipData.contains(ipAddress)) {
            ipData.add(ipAddress);
            return ipData;
        } else {
            return null;
        }
    }
    
    /** Determine if a user's name is already saved in their historical name data.
     * If it isn't add it and return the JSONArray else it will return null. Should
     * be executed after checking if saved name does not equal new name.
     * @param name
     * @param nameData
     * @return 
     */
    public static JSONArray nameData(String name, JSONArray nameData){
        if (!nameData.contains(name)){
            nameData.add(name);
            return nameData;
        } else {
            return null;
        }
    }

    /** *  Determine if a user's nick is already saved in their historical nick data.
     * If it isn't add it and return the JSONArray else it will return null.Should
     * be executed after checking if saved nick does not equal new nick.
     * @param nick
     * @param nickData
     * @return 
     */
    public static JSONArray nickData (String nick, JSONArray nickData) {
        if (!nickData.contains(nick)) {
            nickData.add(nick);
            return nickData;
        } else {
            return null;
        }
    }
    
    
}
