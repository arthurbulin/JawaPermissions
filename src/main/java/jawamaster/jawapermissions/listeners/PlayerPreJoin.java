/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package jawamaster.jawapermissions.listeners;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import static jawamaster.jawapermissions.handlers.ESHandler.indexPlayerData;
import jawamaster.jawapermissions.handlers.PlayerDataHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerPreJoin implements Listener {

    /* THis is a testing listener. It is an attempt to load the player data and make changes as the player is joining.
    * This will hopefully allow more to done as this is an asynch event. So the server will not be helpd up as the DB is queried and updated.
     */
    @EventHandler
    public static void onPlayerJoin(AsyncPlayerPreLoginEvent event) throws IllegalArgumentException, IllegalAccessException, IOException {
        //Check user ban status and retreive the user info
        Map<String, Object> playerData = ESHandler.checkBanStatus(event.getUniqueId());
        if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[PlayerPreJoin] playerData: " + playerData);

        if (!(playerData == null)) { //If data isnt null
            
            //If a user is banned
            if (Boolean.valueOf(String.valueOf(playerData.get("banned")))) { //If user is banned disallow joining and end the event

                String message = "You have been banned for: "
                        + ((String) (((Map<String, Object>) playerData.get("current-ban"))).get("reason")).trim()
                        + ". This ban will end on: "
                        + LocalDateTime.parse(((CharSequence) (((Map<String, Object>) playerData.get("current-ban"))).get("end-of-ban"))).format(DateTimeFormatter.ISO_DATE_TIME);

                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);

            
            //If a user isn't banned       
            } else { //If user isnt banned load their data adjust for changes

                //Load player data into the rank system
                JawaPermissions.playerRank.put(event.getUniqueId(), (String) playerData.get("rank"));

            }

            //If player data is null treat as a new player    
        } else { //if user data is null. So is a new user.
            playerData = PlayerDataHandler.firstTimePlayer(event.getName(), event.getAddress().getHostAddress());

            indexPlayerData(event.getUniqueId(), playerData);
            JawaPermissions.playerRank.put(event.getUniqueId(), (String) playerData.get("rank"));

        }

        //Call player data update
    }

}
