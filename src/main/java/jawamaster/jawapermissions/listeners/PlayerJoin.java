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
import java.util.Map;
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
        Map<String, Object> banData = ESHandler.checkBanStatus(target.getUniqueId());
        if (!(banData == null)){
            if ((boolean) banData.get("banned")){
                target.kickPlayer("You have been banned for: " +((String) (((Map<String, Object>) banData.get("current-ban"))).get("reason")).trim() + ". This ban will end on: " + LocalDateTime.parse(((CharSequence) (((Map<String, Object>) banData.get("current-ban"))).get("end-of-ban"))).format(DateTimeFormatter.ISO_DATE_TIME));
            }
        }
        

        
    }
    
}
