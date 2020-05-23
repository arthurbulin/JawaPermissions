/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package jawamaster.jawapermissions.listeners;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import jawamaster.jawapermissions.JawaPermissibleBase;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.FileHandler;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerJoin implements Listener {

    /**
     * This is the event handler for player join events. This SHOULD NOT be
     * called except by a player joining the server as it connects the player to
     * the JawaPermissibleBase through Java reflection. Calling this outside of
     * a regular player join will have unknown effects. Just DON'T DO IT.
     *
     * @param event
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.NoSuchFieldException
     * @throws java.io.IOException *
     */
    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, IOException {

        Player target = event.getPlayer(); //Get player from event
       
        //Patch the player with the permissible base
        Class ply = target.getClass().getSuperclass(); //Get the player class
        Field field = ply.getDeclaredField("perm"); //Get the perm field for modification
        field.setAccessible(true); //Make the field accessible if it isn't already
        field.set(target, new JawaPermissibleBase(target, JawaPermissions.getPlugin())); //Set the player's perm field to the PermissibleBase
        
        PlayerDataObject pdObject = PlayerManager.getPlayerDataObject(target);
        //System.out.println("User is in autoelevate list:" + JawaPermissions.autoElevate.containsKey(target.getUniqueId()));
        if (JawaPermissions.autoElevate.containsKey(target.getUniqueId())) { //Check for auto elevation
            pdObject.setRank(JawaPermissions.autoElevate.get(target.getUniqueId()), UUID.fromString("00000000-0000-0000-0000-000000000000"), PermissionsHandler.getRankColor(JawaPermissions.autoElevate.get(target.getUniqueId())));
            target.sendMessage(ChatColor.GREEN + " > You have been tagged by the server's autoelevation protocol. You have been given the rank of " + ChatColor.BLUE + PermissionsHandler.getRankColor(JawaPermissions.autoElevate.get(target.getUniqueId())) + JawaPermissions.autoElevate.get(target.getUniqueId()) + ChatColor.GREEN + ". Disconnect and rejoin to get your new rank.");
            JawaPermissions.autoElevate.remove(target.getUniqueId());
            FileHandler.saveAutoElevateList();
            
        }

        //Subscribe user to correct chat permission channels.
        if (target.hasPermission("jawachat.opchat") || target.isOp()) {
            Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, target);
        }
        Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, target);
        
        //Moved all update functions into JawaCore as these are not specific to permissions

    }

}
