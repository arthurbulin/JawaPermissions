/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.events.PlayerRankChange;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;


/**
 *
 * @author Arthur Bulin
 */
public class ESHandler {
    public static RestHighLevelClient restClient;


    
    public JawaPermissions plugin;
    static Map<String, Object> response;
    public static Map<String, Object> dataMap;
    public static GetRequest getRequest;
    public static IndexRequest indexRequest;
    public static SearchRequest searchRequest;
    public static SearchSourceBuilder searchSourceBuilder;
    public static SearchHits searchHits;
    
    private final static String handlerSlug = "[ESHandler] ";
    private static boolean notInES = false;
    
    
    public ESHandler(JawaPermissions plugin){
        this.plugin = plugin;
        ESHandler.restClient = JawaPermissions.getESClient();
    }
    
    
//*****************Index CALLS*******************
    /** Indexes the player's data to the ElasticSearch database. This should only be used to add a new player to the DB. This method constructs the request and passes it to
     * generalIndex(indexRequest) to process the actual indexing.
     * @param target
     * @param playerData
     * @return
     */
    public static boolean indexPlayerData(UUID target, Map<String, Object> playerData){
        indexRequest = new IndexRequest("mc", "players", target.toString()).source(playerData);
        
        restClient.indexAsync(indexRequest, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                //System.out.println(indexResponse.toString());
                //cachePlayerRank(target);
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + playerData.get("name") + " has been indexed successfully.");
            }
            
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();   
            }
        });
        
        return true;
    }
    
//*****************Update CALLS*******************
    
    public static void setPlayerRank(CommandSender commandSender, Player target, String rank){
        Map<String, Object> jsonMap = new HashMap();
        jsonMap.put("rank", rank);
        
        UpdateRequest updateRequest = new UpdateRequest("mc", "players", target.getUniqueId().toString()).doc(jsonMap);
        
        restClient.updateAsync(updateRequest, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_GREEN + JawaPermissions.pluginSlug + target.getDisplayName() + "'s rank has been set to " + rank);

                //Cache the player rank
                JawaPermissions.playerRank.put(target.getUniqueId(), rank);
                
                //Resubscribe to proper channels, but this doesnt unsubscribe them, they need to relog for that
                if (target.hasPermission("jawachat.opchat") || target.isOp()) Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, target);
                Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, target);
                
                //Call event for rank change
                JawaPermissions.getPlugin().getServer().getPluginManager().callEvent(new PlayerRankChange(target, rank));
            }
            
            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        });

    }

    
//###########################  BAN METHODS   ########################################
    /** Will check the player's ban status and return the player's ban data if the player is currently banned.
     * @param target 
     * @return  
     * @throws java.io.IOException  
     */
    public static Map<String, Object> checkBanStatus(UUID target) throws IOException {
        //TODO return previous ban data
        searchRequest = new SearchRequest("mc");
        searchSourceBuilder = new SearchSourceBuilder();
        
        searchSourceBuilder.query(QueryBuilders.matchQuery("_id", target.toString()));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restClient.search(searchRequest);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsl = hits.getHits();
        Long numHits = hits.totalHits;
//      TODO resolve timed bans
        
        //This will return all of the player data, but the ban data is all contained there! with the keys banned, current-ban, and previous-bans
        if ( numHits == 1) {
            return hitsl[0].getSourceAsMap();
        } else return null;

    }
    
    
    /** Updates a player's ban data allowing reason and length of ban time to be modified. 
     * @param commandSender
     * @param target
     * @param reason
     * @param time
     * @return
     * @throws IOException 
     */
    public static Map<String, Object> updateBan(CommandSender commandSender, UUID target, String reason, String time) throws IOException{
        Map<String, Object> newBanData = new HashMap();
        LocalDateTime endOfBan = PlayerDataHandler.assessBanTime(time);
        
        //newBanData.put("banned-by", commandSender.getName());
        newBanData.put("updated", LocalDateTime.now());
        newBanData.put("reason", reason); 
        newBanData.put("end-of-ban", endOfBan);
        
        //Use the dataMap generic map. I am unsure but i belive this will help with GC
        dataMap = new HashMap();
        
        dataMap.put("current-ban", newBanData);
        dataMap.put("banned", true);
        
        UpdateRequest updateRequest = new UpdateRequest("mc", "players", target.toString()).doc(dataMap);
        restClient.update(updateRequest);
        
        return newBanData;
        
    }
    
    /** Update the player's ban information then call for the ban to be removed from the server ban uuid list.
     * @param target
     * @param commandSender
     * @param playerBanData
     * @param action
     * @throws IOException 
     */
    public static void unbanPlayer(UUID target, CommandSender commandSender, Map<String, Object> playerBanData, int action) throws IOException{
        //TODO Resolve automated unban functions
        UpdateRequest updateRequest;
        //System.out.println("old ban data");
        //Create a holder for the old ban data we are about to wipe out
        Map<String, Object> oldBanData = (Map<String, Object>) playerBanData.get("current-ban");

        //Create a holder for the data that will overwrite the old data
        Map<String, Object> newBanData = new HashMap();

        //Creat a hash set to hold the pervious ban data
        Set<Object> previousBans;
        if (playerBanData.containsKey("previous-bans")) { //If the player already has previous ban data on file load it
            previousBans = new HashSet((Collection) playerBanData.get("previous-bans"));
        } else {
            previousBans = new HashSet();
        }
        
        //Add the old ban data to the previous bans set
        previousBans.add(oldBanData);
        //Add the previous bans to new data map
        newBanData.put("previous-bans", previousBans);
        newBanData.put("banned", false);
        
        //Generate our update request
        updateRequest = new UpdateRequest("mc", "players", target.toString()).doc(newBanData);
        
        UpdateResponse updateResponse = restClient.update(updateRequest);
        
    }
  
    public static void whoLookUp(CommandSender commandSender, Player target) throws IOException {
        searchRequest = new SearchRequest("mc");
        searchSourceBuilder = new SearchSourceBuilder();
        
        searchSourceBuilder.query(QueryBuilders.matchQuery("_id", target.getUniqueId().toString()));
        searchRequest.source(searchSourceBuilder);
        
        //System.out.println("test");
        SearchResponse searchResponse = restClient.search(searchRequest);
        
        searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        //long totalHits = searchHits.totalHits;
        
        Map<String, Object> playerData = hits[0].getSourceAsMap();
        if (commandSender instanceof Player){
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + "> " + target.getDisplayName() + "'s current player data.");
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > User name: " + ChatColor.WHITE + target.getName() );
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Rank: " + ChatColor.WHITE + playerData.get("rank"));
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > IP: " + ChatColor.WHITE + target.getAddress().getAddress().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > UUID: " + ChatColor.WHITE + target.getUniqueId().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Current Mode: " + ChatColor.WHITE + target.getGameMode().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Current World: " + ChatColor.WHITE + target.getWorld().getName());
}
        
    }
}