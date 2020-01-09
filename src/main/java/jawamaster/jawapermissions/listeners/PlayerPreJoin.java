/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package jawamaster.jawapermissions.listeners;

import java.io.IOException;
import java.util.Map;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.PlayerDataObject;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.utils.ESRequestBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.elasticsearch.action.search.MultiSearchRequest;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerPreJoin implements Listener {
    
    @EventHandler
    public static void onPlayerJoin(AsyncPlayerPreLoginEvent event) throws IllegalArgumentException, IllegalAccessException, IOException {
        //Check user ban status and retreive the user info
        MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
        multiSearchRequest.add(ESRequestBuilder.buildSearchRequest("players", "_id", event.getUniqueId().toString()));
        multiSearchRequest.add(ESRequestBuilder.buildSearchRequest("bans", "_id", event.getUniqueId().toString()));
        PlayerDataObject rawSearchData = ESHandler.runMultiIndexSearch(multiSearchRequest,new PlayerDataObject(event.getUniqueId()));
        
        //if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[PlayerPreJoin] playerData: " + playerData);

        if (rawSearchData.containsPlayerData()) { //If data isnt null
            
            //If a user is banned
            //Below has some stupid converting bullshit because ElasticSearch returns Objects instead of their string type
            if (rawSearchData.isBanned()) { //If user is banned disallow joining and end the event

                String bannedUntil = rawSearchData.getBannedUntil(rawSearchData.getLatestBanDate());
                
                String message = "You have been banned for: "
                        + rawSearchData.getBanReason(rawSearchData.getLatestBanDate());
                if (!"forever".equals(bannedUntil)) {
                    //message += ". This ban will end on: " + TimeParser.getHumanReadableDateTime();
                    //TODO FIXME give ban end dates
                        //+ LocalDateTime.parse(((CharSequence) (((Map<String, Object>) playerData.get("current-ban"))).get("end-of-ban"))).format(DateTimeFormatter.ISO_DATE_TIME);
                }
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message);   
            }
            
            //Should speed things up moving this from join event to prejoin event
            JawaPermissions.playerRank.put(event.getUniqueId(), rawSearchData.getRank());
        } else if (JawaPermissions.autoElevate.containsKey(event.getUniqueId())){ //Check for auto elevation
            JawaPermissions.playerRank.put(event.getUniqueId(), JawaPermissions.autoElevate.get(event.getUniqueId()));
            
        } else {
            JawaPermissions.playerRank.put(event.getUniqueId(), "guest"); // TODO get basic rank from immunity levels
        }
    }
}
