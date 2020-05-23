/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package jawamaster.jawapermissions.listeners;

import java.io.IOException;
import java.util.Map;
import jawamaster.jawapermissions.JawaPermissions;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.ESHandler;
import net.jawasystems.jawacore.utils.ESRequestBuilder;
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
        

    }
}
