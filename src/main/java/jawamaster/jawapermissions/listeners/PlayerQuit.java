/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.listeners;

import jawamaster.jawapermissions.JawaPermissions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerQuit implements Listener {
    
    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        String removed = JawaPermissions.playerRank.remove(player.getUniqueId());
        if (JawaPermissions.debug) {
        System.out.println(JawaPermissions.pluginSlug + "[PlayerQuitEvent] "+ player.getName() + " with rank " + removed + ", has been removed from the player cache.");
        }
    }
}
