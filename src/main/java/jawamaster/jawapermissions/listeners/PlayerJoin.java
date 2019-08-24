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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissibleBase;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

        //Subscribe user to correct chat permission channels.
        if (target.hasPermission("jawachat.opchat") || target.isOp()) {
            Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, target);
        }
        Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, target);

        if (target.getFirstPlayed() != 0) {
            //Should async update the player's data
            Bukkit.getScheduler().runTaskAsynchronously(JawaPermissions.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    try {
                        HashMap<String, Object> playerData = ESHandler.getPlayerData(target);
                        HashMap<String, Object> updateData = new JSONObject();

                        Bukkit.getLogger().log(Level.FINEST, "Recording last login date for player: {0} as {1}", new Object[]{target.getUniqueId().toString(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)});
                        updateData.put("last-login", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                        //update player name in the 
                        if (!playerData.get("name").equals(target.getName())) {
                            updateData.put("name", target.getName());
                        }

                        //Update player nameData
                        JSONArray nameData = new JSONArray();

                        //Change nameData to name-data
                        ((Collection) playerData.get("name-data")).forEach((name) -> {
                            System.out.print(name);
                            nameData.add((String) name);
                        });

                        if (!nameData.contains(target.getName())) {
                            nameData.add(target.getName());
                            updateData.put("name-data", nameData);
                        }

                        //update player ip data (target.getAddress().getHostName())
                        JSONArray ips = new JSONArray();
                        ((Collection) playerData.get("ip")).forEach((ip) -> {
                            ips.add((String) ip);
                        });
                        ;
                        if (ips.contains(target.getAddress().getAddress())) {
                            ips.add(target.getAddress().getAddress());
                            updateData.put("ip", ips);
                        }

                        ESHandler.updateData(target, updateData);

                    } catch (IOException ex) {
                        Logger.getLogger(PlayerJoin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }

    }

}
