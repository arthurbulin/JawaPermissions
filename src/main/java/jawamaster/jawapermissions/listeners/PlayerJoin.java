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
import java.util.logging.Level;
import jawamaster.jawapermissions.JawaPermissibleBase;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.PlayerDataObject;
import jawamaster.jawapermissions.handlers.ESHandler;
import static jawamaster.jawapermissions.handlers.ESHandler.indexPlayerData;
import jawamaster.jawapermissions.handlers.PlayerDataHandler;
import jawamaster.jawapermissions.utils.ESRequestBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.json.JSONArray;
import org.json.JSONObject;

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
        boolean installed = ESHandler.alreadyIndexed(target.getUniqueId());

        //Patch the player with the permissible base
        Class ply = target.getClass().getSuperclass(); //Get the player class
        Field field = ply.getDeclaredField("perm"); //Get the perm field for modification
        field.setAccessible(true); //Make the field accessible if it isn't already
        field.set(target, new JawaPermissibleBase(target, JawaPermissions.getPlugin())); //Set the player's perm field to the PermissibleBase

        //Should async update the player's data
        Bukkit.getScheduler().runTaskAsynchronously(JawaPermissions.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (installed) {

                    MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
                    multiSearchRequest.add(ESRequestBuilder.buildSearchRequest("players", "_id", target.getUniqueId().toString()));
                    PlayerDataObject pdObject = ESHandler.runMultiIndexSearch(multiSearchRequest, new PlayerDataObject(target.getUniqueId()));

                    System.out.print(pdObject.getRank());
                    JawaPermissions.playerRank.put(target.getUniqueId(), pdObject.getRank());
                    target.sendMessage("You have been loaded!");

                    //Subscribe user to correct chat permission channels.
                    if (target.hasPermission("jawachat.opchat") || target.isOp()) {
                        Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, target);
                    }
                    Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, target);

                    JSONObject updateData = new JSONObject();

                    Bukkit.getLogger().log(Level.FINEST, "Recording last login date for player: {0} as {1}", new Object[]{target.getUniqueId().toString(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)});
                    updateData.put("last-login", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));


                    //update player name and ip
                    if (pdObject.getName().equals(target.getName())) {
                        updateData.put("name", target.getName());
                    }
                    if (!pdObject.getIP().equals(target.getAddress().getAddress().toString())) {
                        updateData.put("ip", target.getAddress().getAddress().toString());
                    }

                    JSONArray nameData = PlayerDataHandler.nameData(target.getName(), pdObject.getNameArray());
                    System.out.print("player join:" + nameData);
                    if (nameData != null) {
                        System.out.println("isn't null:"+nameData);
                        updateData.put("name-data", nameData);
                    }

                    JSONArray ips = PlayerDataHandler.ipData(target.getAddress().getAddress().toString(), pdObject.getIPArray());
                    if (ips != null) {
                        updateData.put("ips", ips);
                    }

                    ESHandler.updateData(target, updateData);

                } else { //if user isn't installed this is a new user. install them
                    JSONObject newPlayerData = PlayerDataHandler.firstTimePlayer(target.getName(), target.getAddress().getAddress().toString());

                    indexPlayerData(target.getUniqueId(), newPlayerData);
                    JawaPermissions.playerRank.put(target.getUniqueId(), (String) newPlayerData.get("rank"));
                    target.sendMessage("You have been installed!");

                }
            }
        });

    }

}
