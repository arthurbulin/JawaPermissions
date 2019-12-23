/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

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
    
    /** Creates the player data for committing to the ElasticSearch index.
     * @param name
     * @param ip
     * @return
     */
    public static JSONObject firstTimePlayer(String name, String ip){
        JSONObject playerData = new JSONObject();
        playerData.put("first-login", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        playerData.put("last-login", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        playerData.put("last-logout", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        playerData.put("play-time", 0);
        
        playerData.put("name", name);
        playerData.put("name-data", nameData(name, new JSONArray()));
        playerData.put("rank", "guest"); //TODO pull basic rank from permissions files and immunity levels
        
        playerData.put("banned", false);
        playerData.put("nick", "");
        playerData.put("nick-data", new JSONArray());
        playerData.put("tag", "false");
        playerData.put("star", "");
        playerData.put("ip", ip);
        playerData.put("ips",ipData(ip, new JSONArray()));

        if (JawaPermissions.debug){
            System.out.print(JawaPermissions.pluginSlug + handlerSlug + "firstTimePlayer data created as follows: " + playerData.toString());
        }
        
        return playerData;
    }
    
    /** Assemble a JSONObject that contains information for that specific ban and returns it in a top level ban object.
     * @param commandSender
     * @param parsedArguments
     * @param banTime
     * @return
     */
    public static JSONObject assembleBanData(CommandSender commandSender, HashMap<String,String> parsedArguments, LocalDateTime banTime){
        JSONObject topLevelBanObject = new JSONObject();
        JSONObject banData = new JSONObject();
        String endOfBanDate = assessBanTime(parsedArguments, banTime);
               
        banData.put("reason", parsedArguments.get("r"));
       
        if (commandSender instanceof Player){
            banData.put("banned-by", ((Player) commandSender).getUniqueId().toString());
            banData.put("via-console", false);
        } else {
            System.out.println("by argument: " + parsedArguments.get("b"));
            String adminUUID = ESHandler.findOfflinePlayer((String) parsedArguments.get("b")).getId();
            System.out.println("adminUUID: "+ adminUUID);
            if (adminUUID == null) return null;
            
            banData.put("banned-by", adminUUID);
            banData.put("via-console", true);
        }
        
        
        banData.put("banned-until", endOfBanDate);
        //banData.put("latest-ban", banTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        banData.put("active", true);
        banData.put("ban-lock", false);
        
        topLevelBanObject.put(banTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), banData);
        return topLevelBanObject;
    }
    
    public static JSONObject assembleUnBanData(CommandSender commandSender, HashMap<String, String> parsedArguments, String unBanTime, String banTime){
        //Assemble unban information for the ban index
        JSONObject banData = new JSONObject();
        JSONObject topLevelBanObject = new JSONObject();
        banData.put("unreason", parsedArguments.get("r"));
        banData.put("active", false);

        if (commandSender instanceof Player) {
            banData.put("unbanned-by", ((Player) commandSender).getUniqueId().toString());
            banData.put("unbanned-via-console", false);
        }
        else if (commandSender instanceof ConsoleCommandSender) {
            //Make sure they gave a valid name and get the UUID
            String commandSenderUUID;
            commandSenderUUID = ESHandler.findOfflinePlayer(parsedArguments.get("b")).getId();
            
            if (commandSenderUUID == null) {
                commandSender.sendMessage("Elastic Search failed to find that administrative user's UUID! Please make sure you are using the correct username and not your nickname!");
                commandSender.sendMessage("It is also possible that multipe players were found! Be exact!");
                return null;
            }
            
            //Plug the uuid into the ban index
            banData.put("unbanned-via-console", true);
            banData.put("unbanned-by", commandSenderUUID);
            banData.put("unbanned-on", unBanTime);
        }
        topLevelBanObject.put(banTime, banData);
        return topLevelBanObject;
    }
    
    /** Evaluates ban time and creates a string representing it in LocalDateTime format.
     * If ban has no end date then string is "forever".
     * @param time
     * @param dateTime
     * @return 
     */
    public static String assessBanTime(HashMap<String,String> time,LocalDateTime dateTime) {
        LocalDateTime adjustedDateTime = dateTime;
        if (time.containsKey("d") || time.containsKey("h") || time.containsKey("m")) {
            if (time.containsKey("d")) {
                adjustedDateTime.plusDays(Long.valueOf(time.get("d")));
            }
            if (time.containsKey("h")) {
                adjustedDateTime.plusHours(Long.valueOf(time.get("h")));
            }
            if (time.containsKey("m")) {
                adjustedDateTime.plusMinutes(Long.valueOf(time.get("m")));
            }
            return adjustedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            return "forever";
        }  
    }
    
    /** *  Determine if an ip address is contained within the player's history set.If it isn't then add it and return it.
     * If it is already in the array then return null.
     * @param ipAddress
     * @param ipData
     * @return 
     */
    public static JSONArray ipData(String ipAddress, JSONArray ipData){
        if (!ipData.toList().contains(ipAddress)) {
            ipData.put(ipAddress);
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
        System.out.println("namdData method:" + nameData);
        if (!nameData.toList().contains(name)){
            nameData.put(name);
            System.out.println("nameData method:" + nameData);
            return nameData;
        } else return null;
    }

    /** *  Determine if a user's nick is already saved in their historical nick data.
     * If it isn't add it and return the JSONArray else it will return null.Should
     * be executed after checking if saved nick does not equal new nick.
     * @param nick
     * @param nickData
     * @return 
     */
    public static JSONArray nickData(String nick, JSONArray nickData) {
        if (!nickData.toList().contains(nick)) {
            nickData.put(nick);
            return nickData;
        } else return null;
    }
    
    public static JSONObject starData(JSONObject starData, String value) {
        switch (value) {
            case "new": {
                starData.put("promote", false);
                starData.put("probation", false);
                starData.put("consult", false);
                break;
            }
            case "promote": {
                starData.put("promote", !Boolean.valueOf(String.valueOf(starData.get("promote"))));
                break;
            }
            case "probation": {
                starData.put("probation", !Boolean.valueOf(String.valueOf(starData.get("probation"))));
                break;
            }
            case "consult": {
                starData.put("consult", !Boolean.valueOf(String.valueOf(starData.get("consult"))));
                break;
            }
            
        }

        return starData;

    }
    
    public static JSONObject createPlayerRankChangeData(String fromRank, String toRank, String byWhom){
        JSONObject playerData = new JSONObject();
        JSONObject rankData = new JSONObject();
        JSONObject rankDataTop = new JSONObject();
        
        rankData.put("from-rank", fromRank);
        rankData.put("to-rank", toRank);
        rankData.put("changed-by", byWhom);
        
        rankDataTop.put(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), rankData);
        
        playerData.put("rank", toRank);
        playerData.put("rank-data", rankDataTop);
        
        return playerData;
    }
    
    /** Generate small JSONObject for updating a player's tag.
     * @param tag
     * @return 
     */
    public static JSONObject createPlayerTagChangeData(String tag){
        JSONObject tagChange = new JSONObject();
        
        tagChange.put("tag", tag);
        
        return tagChange;
    }
    
    public static JSONObject createPlayerNickChangeData(String nick){
        JSONObject nickChange = new JSONObject();
        nickChange.put("nick", nick);
        return nickChange;
    }
    
    /** Resolve a player name to a PlayerDataObject. This will return a data filled
     * PlayerDataObject if the player is found. This first checks is a player is online.
     * If they are then the UUID is extracted and the PlayerDataObject is populated with
     * data from a UUID index search. If the player is not online this calls ESHandler.findOfflinePlayer(target, true)
     * so that a data filled PlayerDataObject is returned. If no exact match is found to the player name field
     * this will return null to specify that a player of that name was not found. This sends
     * a unified error message to the commandSender that informs them the user was not found.
     * @param commandSender
     * @param target
     * @return
     * @throws IOException 
     */
    public static PlayerDataObject validatePlayer(CommandSender commandSender, String target) throws IOException{
        Player onlinePlayer = JawaPermissions.plugin.getServer().getPlayer(target);
        PlayerDataObject pdObject = null;
        if (onlinePlayer == null){ //If not online
            pdObject = ESHandler.findOfflinePlayer(target, true);
        } else { //If online. This should never be null then because the player has already been found
            pdObject = ESHandler.getPlayerData(target);
        }
        
        if (pdObject == null){
            commandSender.sendMessage(ChatColor.RED + " > Error: " + target + " was not resolvable as an online or offline player. Please be sure the name is exact and retry your command.");
            return null;
        } else {    
            return pdObject;
        }
    }
    
}
