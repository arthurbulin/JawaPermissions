/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jawamaster.jawapermissions.JawaPermissions;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerDataHandler {
    public final JawaPermissions plugin;
    public static final String handlerSlug = "[PlayerDataHandler] ";
    
    public PlayerDataHandler(JawaPermissions plugin){
        this.plugin = plugin;
    }
    
    /** Creates the player data for commiting to the ElasticSearch index.
     *
     * @param player
     * @return
     */
    public Map<String, Object> firstTimePlayer(Player player){
        Map<String, Object> playerData = new HashMap();
        playerData.put("first-login", LocalDateTime.now());
        playerData.put("last-login", LocalDateTime.now());
        playerData.put("last-logout", "none");
        playerData.put("name", player.getName());
        playerData.put("ip",player.getAddress().getAddress());
        playerData.put("rank", "guest"); //TODO pull basic rank from permissions files and immunity levels
        playerData.put("banned", false);
        playerData.put("play-time", 0);
        
        if (JawaPermissions.debug){
            System.out.print(JawaPermissions.pluginSlug + handlerSlug + "firstTimePlayer data created as follows: " + playerData.toString());
        }
        
        return playerData;
    }
    
}
