/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jawamaster.jawapermissions.JawaPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
     *
     * @param player
     * @return
     */
    public Map<String, Object> firstTimePlayer(Player player){
        Map<String, Object> playerData = new HashMap();
        playerData.put("first-login", LocalDateTime.now());
        playerData.put("last-login", LocalDateTime.now());
        playerData.put("last-logout", "none");
        playerData.put("name", player.getName());
        playerData.put("rank", "guest"); //TODO pull basic rank from permissions files and immunity levels
        playerData.put("banned", false);
        playerData.put("play-time", 0);
        playerData.put("nick", "$$");
        playerData.put("tag", "$$");
        playerData.put("star", "$$");
        
        Set<Object> ipSet = new HashSet();
        ipSet.add(player.getAddress().getAddress());
        playerData.put("ip",ipSet);

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
}
