/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.io.IOException;
import java.time.LocalDateTime;
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
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
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
    public static boolean indexPlayerData(Player target, Map<String, Object> playerData){
        indexRequest = new IndexRequest("mc", "players", target.getUniqueId().toString()).source(playerData);
        
        restClient.indexAsync(indexRequest, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                //System.out.println(indexResponse.toString());
                cachePlayerRank(target);
                target.sendMessage(JawaPermissions.newPlayerMessage);
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + target.getName() + " has been indexed successfully.");
            }
            
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();   
            }
        });
        
        return true;
        //return generalIndex(indexRequest);
    }
    
    /** Create the IndexRequest for the server if it doesn't already exist. This is passed to generalIndex(indexRequest).
     * @param serverName
     * @param serverData
     * @return
     */
    public static boolean indexServerData(String serverName, Map<String, Object> serverData){
        indexRequest = new IndexRequest("mc", "server", serverName).source(serverData);
        return true;
    }
    
    /** Performs the actual index request with the ElasticSearch server.
     * @param indexRequest
     * @return
     */
    public static boolean generalIndex(IndexRequest indexRequest){
        //TODO Index General Request
        return true;
    }
    
//*****************Search CALLS*******************
    
    
    /** Runs the general search requests and returns SearchHits. These can then be used to construct a GetRequest later.
     * @param searchRequest
     * @return
     */
    public static SearchHits generalSearch(SearchRequest searchRequest){
        
        restClient.searchAsync(searchRequest, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "generalSearch has returned these hits: " + searchResponse.toString());
                searchHits = searchResponse.getHits();
            }
            
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
        return searchHits;
    }
//*****************Update CALLS*******************
    
    //TODO Create rank set
    public static void setPlayerRank(CommandSender commandSender, Player target, String rank){
        // TODO update player rank in Elasticsearch

        Map<String, Object> jsonMap = new HashMap();
        jsonMap.put("rank", rank);
        
        UpdateRequest updateRequest = new UpdateRequest("mc", "players", target.getUniqueId().toString()).doc(jsonMap);
        
        restClient.updateAsync(updateRequest, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
//                if (commandSender instanceof Player) ((Player) commandSender).sendMessage(target.getName() + "'s rank has been set to " + rank + ".");
//                else System.out.println(target.getName() + "'s rank has been set to " + rank + ".");
//                
//                //TODO color these entries
//                target.sendMessage(commandSender.getName() + " has set your rank to " + rank + ".");
                Bukkit.getServer().broadcastMessage(ChatColor.DARK_GREEN + JawaPermissions.pluginSlug + target.getDisplayName() + "'s rank has been set to " + rank);

                cachePlayerRank(target);
                JawaPermissions.getPlugin().getServer().getPluginManager().callEvent(new PlayerRankChange(target, rank));
            }
            
            @Override
            public void onFailure(Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        });

    }
    
    public static void updatePlayerData(Player player, Map<String,Object> updateData) {
        UpdateRequest updateRequest = new UpdateRequest("mc", "players", player.getUniqueId().toString()).doc(updateData);
    }
    
    
//*****************Specialized Calls**************
    /** Called on playerJoinEvent. Will search for a player in the index by UUID. If not present will return false.
     * @param player
     * @return
     */
    public static Map<String, Object> searchForReturningPlayer(Player player) throws IOException {
        searchRequest = new SearchRequest("mc");
        searchSourceBuilder = new SearchSourceBuilder();
        
        searchSourceBuilder.query(QueryBuilders.matchQuery("_id", player.getUniqueId().toString()));
        searchRequest.source(searchSourceBuilder);
        
        //System.out.println("test");
        SearchResponse searchResponse = restClient.search(searchRequest);
        
        searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        long totalHits = searchHits.totalHits;
        
        if (totalHits == 0){ //If not found in Index
            //TODO implement first time player methods
            Map<String, Object> playerData = JawaPermissions.playerDataHandler.firstTimePlayer(player);
            indexPlayerData(player, playerData);
            
            //TODO deal with different IP update and resolve possible ban
            //resolveIP();
            
            return playerData;
        }else { //If player is found in index
            
            Map<String, Object> playerData = hits[0].getSourceAsMap();
            
            if (!(Boolean) playerData.get("banned")){
                if (JawaPermissions.debug){
                    System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Player found in ElasticSearch. Caching player rank.");
                }
                
                cachePlayerRank(player); //TODO get rid of this
            }
        }
        
        return null;
    }
    
    /** Caches player rank from the elasticsearch index. This should only be called by searchForReturningPlayers or on a permissions reload.
     * @param player
     */
    public static void cachePlayerRank(Player player){
        //Asynchronously get the response. Async is important so that the server doesn't stop and wait for a responce.
        getRequest = new GetRequest("mc","players", player.getUniqueId().toString());
        
        restClient.getAsync(getRequest, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                response = getResponse.getSourceAsMap();
                String cacheRank = JawaPermissions.playerRank.put(player.getUniqueId(), (String) response.get("rank"));
                if (player.hasPermission("jawachat.opchat") || player.isOp()) Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, player);
                Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, player);
                //Debug response data
                if (JawaPermissions.debug){
                    System.out.println(JawaPermissions.pluginSlug + handlerSlug + cacheRank);
                    System.out.println(JawaPermissions.pluginSlug + handlerSlug + "cachePlayerRank response: " + response);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                //If the something fails
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + " Something went wrong and the player's rank wasn't able to be found. The player will be set as guest.");
                JawaPermissions.playerRank.put(player.getUniqueId(),"guest"); //TODO pull entry level rank from perm files
                System.out.println(e.toString());
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
//        oldBanData.put("reason", playerBanData.get("reason"));
//        oldBanData.put("banned-by", playerBanData.get("banned-by"));
//        oldBanData.put("unbanned-by", commandSender.getName());
//        oldBanData.put("date", playerBanData.get("date"));
//        oldBanData.put("unban-date", LocalDateTime.now());
        
        //System.out.println("new ban Data");
        //Create a holder for the data that will overwrite the old data
        Map<String, Object> newBanData = new HashMap();
//        newBanData.put("active", false);
//        newBanData.put("reason", "none");
//        newBanData.put("banned-by", "none");
//        newBanData.put("unbanned-by", commandSender.getName());
//        newBanData.put("date", LocalDateTime.now());
//        newBanData.put("unban-date", LocalDateTime.now());
        
        //Creat a hash set to hold the pervious ban data
        Set<Object> previousBans = new HashSet();
        if (playerBanData.containsKey("previous-bans")) { //If the player already has previous ban data on file load it
            previousBans = (Set<Object>) playerBanData.get("previous-bans");
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

    public static void loadBannedPlayers() {
        getRequest = new GetRequest("servers", "server", "main");
        
        restClient.getAsync(getRequest, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse response) {
                response.getSourceAsMap().get("banned-uuids");
            }

            @Override
            public void onFailure(Exception e) {
                if (e instanceof ElasticsearchException) { 
                    ESHandler.createServerData();
                }
            }
        });
    }

  //Server assembly and retreival
    private static void createServerData() {
        Map<String, Object> serverData = new HashMap();
        Set<UUID> ownerUUID = new HashSet();
        
        ownerUUID.add(UUID.fromString("e429c687-3bf2-4a54-99a9-8ab0c1ef4e8b"));
        
        serverData.put("name", JawaPermissions.serverName);
        serverData.put("last-startup", LocalDateTime.now());
        serverData.put("owner-uuids", ownerUUID);
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
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > IP: " + ChatColor.WHITE + target.getAddress().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > UUID: " + ChatColor.WHITE + target.getUniqueId().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Current Mode: " + ChatColor.WHITE + target.getGameMode().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Current World: " + ChatColor.WHITE + target.getWorld().getName());
}
        
    }
}