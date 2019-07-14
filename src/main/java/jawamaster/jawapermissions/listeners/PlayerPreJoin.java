/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.listeners;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import static jawamaster.jawapermissions.handlers.ESHandler.indexPlayerData;
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
    public static void onPlayerJoin(AsyncPlayerPreLoginEvent event) throws IllegalArgumentException, IllegalAccessException, IOException{
        //Check user ban status and retreive the user info
        Map<String, Object> playerData = ESHandler.checkBanStatus(event.getUniqueId());
        
        if (!(playerData == null)){ //If data isnt null
            if ((boolean) playerData.get("banned")){ //If user is banned disallow joining and end the event
               
                String message = "You have been banned for: " +((String) (((Map<String, Object>) playerData.get("current-ban"))).get("reason")).trim() + ". This ban will end on: " + LocalDateTime.parse(((CharSequence) (((Map<String, Object>) playerData.get("current-ban"))).get("end-of-ban"))).format(DateTimeFormatter.ISO_DATE_TIME);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);
            
            } else { //If user isnt banned load their data adjust for changes

                //Load player data
                JawaPermissions.playerRank.put(event.getUniqueId(), (String) playerData.get("rank"));
                
                //TODO check for information change. IP change. name change. update last join
                Map<String, Object> updateData = new HashMap();
                
                //if (!((List) playerData.get("ips")).contains(event.getAddress().getHostAddress())){
                    //TODO Add IP to new player data
                //}
                
                if (!playerData.get("name").equals(event.getName())){
                    //new player name and add old player name to old player names list
                    updateData.put("name", event.getName());
                    
                    Set<String> aliases;
                    
                    if (playerData.containsKey("aliases")) { //If they have existing aliases
                        aliases = (Set<String>) playerData.get("aliases"); //Get the alias list
                        aliases.add((String) playerData.get("name")); //add the previous name to it
                        updateData.put("aliases", aliases); //Add the alias set to the update map
                    } else { //If there are no existing aliases
                        aliases = new HashSet();
                        aliases.add((String) playerData.get("name"));
                        updateData.put("aliases", aliases);
                    }
                    
                }
                
                updateData.put("last-login", LocalDateTime.now());
                //TODO update player info
            }
        } else { //if user data is null. So is a new user.
            playerData = new HashMap();
            playerData.put("first-login", LocalDateTime.now());
            playerData.put("last-login", LocalDateTime.now());
            playerData.put("last-logout", "none");
            playerData.put("name", event.getName());
            playerData.put("rank", "guest"); //TODO pull basic rank from permissions files and immunity levels
            playerData.put("banned", false);
            playerData.put("play-time", 0);
            playerData.put("nick", "$$");
            playerData.put("tag", "$$");
            playerData.put("star", "$$");
            
            Set<Object> ipSet = new HashSet();
            ipSet.add(event.getAddress().getAddress());
            playerData.put("ip",ipSet);

            indexPlayerData(event.getUniqueId(), playerData);
            JawaPermissions.playerRank.put(event.getUniqueId(), "guest");

        }
        
        
        
        
        //Call player data update
    }
    
}
