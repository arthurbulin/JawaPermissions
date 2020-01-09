/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.listeners;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.JSONObject;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerQuit implements Listener {
    
    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Bukkit.getServer().getPluginManager().unsubscribeFromPermission(Server.BROADCAST_CHANNEL_USERS, player);
        
         Bukkit.getScheduler().runTaskAsynchronously(JawaPermissions.plugin, new Runnable() {
             @Override
            public void run() {
                
                 JSONObject logoutUpdate = new JSONObject();
                logoutUpdate.put("last-logout", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                 ESHandler.asyncUpdateData(player, logoutUpdate);
                 if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[PlayerQuitEvent] updating " + player.getName() + "'s last logout time.");
            }
         });
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(JawaPermissions.plugin, new Runnable() {
            public void run() {
                if (Bukkit.getServer().getPlayer(player.getName()) == null){
                    String removed = JawaPermissions.playerRank.remove(player.getUniqueId());
                    
                    if (JawaPermissions.debug) {
                        System.out.println(JawaPermissions.pluginSlug + "[PlayerQuitEvent] "+ player.getName() + " with rank " + removed + ", has been removed from the player cache.");
                    }
                    
                } else {
                    //Do nothing
                }
                
            }
        }, 20); //set to 20 ticks(1s), before this was 600 ticks.
        
    }
}
