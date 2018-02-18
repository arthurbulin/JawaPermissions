/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.listeners;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jawamaster.jawapermissions.JawaPermissibleBase;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerJoin implements Listener{
    
    /** This is the event handler for player join events. This SHOULD NOT be called except by a player joining the server as it connects the
     * player to the JawaPermissibleBase through Java reflection. Calling this outside of a regular player join will have unknown effects. Just
     * DON'T DO IT.
     * @param event
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.NoSuchFieldException **/
    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, IOException{
        
        //System.out.print("ESPing during player join: " + ESHandler.getPing());
        Player target = event.getPlayer(); //Get player from event
        //JawaPermissions.handler.commitRank(player);
        Class ply = target.getClass().getSuperclass(); //Get the player class
        Field field = ply.getDeclaredField("perm"); //Get the perm field for modification
        field.setAccessible(true); //Make the field accessible if it isn't already
        field.set(target, new JawaPermissibleBase(target, JawaPermissions.getPlugin())); //Set the player's perm field to the PermissibleBase
        ESHandler.searchForReturningPlayer(target);
        Map<String, Object> playerData = ESHandler.checkBanStatus(target.getUniqueId());
       
        if (!(playerData == null)){
            if ((boolean) playerData.get("banned")){
                target.kickPlayer("You have been banned for: " +((String) (((Map<String, Object>) playerData.get("current-ban"))).get("reason")).trim() + ". This ban will end on: " + LocalDateTime.parse(((CharSequence) (((Map<String, Object>) playerData.get("current-ban"))).get("end-of-ban"))).format(DateTimeFormatter.ISO_DATE_TIME));
            }
        }
        
        System.out.print(event.isAsynchronous());
        
//        //TODO check for information change
//            // IP change
//            // name change
//            // update last join
//        Map<String, Object> updateData = new HashMap();
//        
//        if (!((Map<String,String>) playerData.get("ips")).containsKey(target.getAddress().toString())){
//            //Add IP to new player data
//        }
//        
//        if (!playerData.get("name").equals(target.getName())){
//            //new player name and add old player name to old player names list
//            updateData.put("name", target.getName());
//            
//            Set<String> aliases;
//            
//            if (playerData.containsKey("aliases")) { //If they have existing aliases
//                aliases = (Set<String>) playerData.get("aliases"); //Get the alias list
//                aliases.add((String) playerData.get("name")); //add the previous name to it
//                updateData.put("aliases", aliases); //Add the alias set to the update map
//            } else { //If there are no existing aliases
//                aliases = new HashSet();
//                aliases.add((String) playerData.get("name"));
//                updateData.put("aliases", aliases);
//            }
//                    
//        }
//
//        updateData.put("last-login", LocalDateTime.now());
//        
        //Call player data update
    }
    
}
