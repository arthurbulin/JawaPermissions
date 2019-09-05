/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package jawamaster.jawapermissions.handlers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.PlayerDataObject;
import jawamaster.jawapermissions.events.PlayerRankChange;
import jawamaster.jawapermissions.utils.ESRequestBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.json.JSONObject;

/**
 *
 * @author Arthur Bulin
 */
public class ESHandler {

    public static RestHighLevelClient restClient;

    public JawaPermissions plugin;
//    static Map<String, Object> response;
//    public static Map<String, Object> dataMap;
//    public static GetRequest getRequest;
//    public static IndexRequest indexRequest;
//    public static SearchRequest searchRequest;
//    public static SearchSourceBuilder searchSourceBuilder;
//    public static SearchHits searchHits;

    private final static String handlerSlug = "[ESHandler] ";
    private static boolean notInES = false;
    private final static String REDMESSAGEPLUG = ChatColor.RED + "> ";
    private final static String GREENMESSAGEPLUG = ChatColor.GREEN + "> ";
    

    public ESHandler(JawaPermissions plugin) {
        this.plugin = plugin;
        ESHandler.restClient = JawaPermissions.getESClient();
    }
    
    public static boolean alreadyIndexed(UUID target) {
        GetRequest getRequest = new GetRequest("players", target.toString());
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        
        try {
            return restClient.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("CHECK ELASTIC SEARCH!!");
            return false;
        }
    }

//*****************Index CALLS*******************
    /**
     * Indexes the player's data to the ElasticSearch database. This should only
     * be used to add a new player to the DB. This method constructs the request
     * and passes it to generalIndex(indexRequest) to process the actual
     * indexing.
     *
     * @param target
     * @param playerData
     * @return
     */
    public static boolean indexPlayerData(UUID target, JSONObject playerData) {
        //indexRequest = new IndexRequest("mc", "players", target.toString()).source(playerData);
        IndexRequest indexRequest = new IndexRequest("players")
                .id(target.toString())
                .source(playerData.toMap());
        restClient.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                //System.out.println(indexResponse.toString());
                //cachePlayerRank(target);
                //plugin.getLogger().log(Level.SEVERE, handlerSlug);
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
//    public static void setPlayerRank(CommandSender commandSender, Player target, String rank) {
//        Map<String, Object> jsonMap = new HashMap();
//        jsonMap.put("rank", rank);
//
//        //UpdateRequest updateRequest = new UpdateRequest("mc", "players", target.getUniqueId().toString()).doc(jsonMap);
//        UpdateRequest updateRequest = new UpdateRequest("players", target.getUniqueId().toString()).doc(jsonMap, XContentType.JSON);
//
//        restClient.updateAsync(updateRequest, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {
//            @Override
//            public void onResponse(UpdateResponse updateResponse) {
//                Bukkit.getServer().broadcastMessage(ChatColor.DARK_GREEN + JawaPermissions.pluginSlug + target.getDisplayName() + "'s rank has been set to " + rank);
//
//                //Cache the player rank
//                JawaPermissions.playerRank.put(target.getUniqueId(), rank);
//
//                //Resubscribe to proper channels, but this doesnt unsubscribe them, they need to relog for that
//                if (target.hasPermission("jawachat.opchat") || target.isOp()) {
//                    Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, target);
//                }
//                Bukkit.getServer().getPluginManager().subscribeToPermission(Server.BROADCAST_CHANNEL_USERS, target);
//
//                //Call event for rank change
//                JawaPermissions.getPlugin().getServer().getPluginManager().callEvent(new PlayerRankChange(target, rank));
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                System.out.println(e);
//                e.printStackTrace();
//            }
//        });
//
//    }

    public static void updateData(Player target, JSONObject updateData) {
        UpdateRequest updateRequest = new UpdateRequest("players", target.getUniqueId().toString())
                .doc(updateData.toMap());

        restClient.updateAsync(updateRequest, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                System.out.println(JawaPermissions.pluginSlug + " " + target.getName() + "'s details have been updated.");
            }

            @Override
            public void onFailure(Exception arg0) {
                System.out.println(JawaPermissions.pluginSlug + target.getName() + "'s information update failed.");
                arg0.printStackTrace();
            }
        });
    }

////###########################  BAN METHODS   ########################################
//    /**
//     * Will check the player's ban status and return the player's ban data if
//     * the player is currently banned.
//     *
//     * @param target
//     * @return
//     * @throws java.io.IOException
//     */
//    public static Map<String, Object> checkBanStatus(UUID target) throws IOException {
//        //TODO Clean this mess up
//        //TODO Convert all to MultiGetRequest
//        SearchRequest searchRequest = new SearchRequest("players");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        searchSourceBuilder.query(QueryBuilders.matchQuery("_id", target.toString()));
//        searchRequest.source(searchSourceBuilder);
//
//        SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
//
//        SearchHits hits = searchResponse.getHits();
//        SearchHit[] hitsl = hits.getHits();
//        Long numHits = hits.getTotalHits().value;
//
//        boolean isBanned = (boolean) hitsl[0].getSourceAsMap().get("banned");
//        Map<String, Object> returnObj = new HashMap();
//        if (isBanned) {
//            SearchRequest bannedSearchRequest = new SearchRequest("bans");
//            searchSourceBuilder = new SearchSourceBuilder();
//
//            searchSourceBuilder.query(QueryBuilders.matchQuery("_id", target.toString()));
//            bannedSearchRequest.source(searchSourceBuilder);
//
//            SearchResponse bannedSearchResponse = restClient.search(bannedSearchRequest, RequestOptions.DEFAULT);
//
//            SearchHit[] bannedHits = bannedSearchResponse.getHits().getHits();
//            returnObj.put("bans", bannedHits[0].getSourceAsMap());
//        }
//
//        returnObj.put("player", hitsl[0].getSourceAsMap());
//
//        //TODO resolve timed bans
//        //This will return all of the player data, but the ban data is all contained there! with the keys banned, current-ban, and previous-bans
//        if (numHits == 1) {
//            return returnObj;
//        } else {
//            return null;
//        }
//
//    }
    
    /** This will run and Async bulk request and print generic indexing success or failure messages to the commandSender.
     * TODO later this will accept a message object so that good response can be sent to the players and server without 
     * having to overload the hell out of the call.
     * @param request
     * @param messageToo 
     */
    public static void runAsyncBulkRequest(BulkRequest request, CommandSender messageToo){
        restClient.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse arg0) {
                for (BulkItemResponse resp : arg0.getItems()){
                    if (!resp.isFailed()) messageToo.sendMessage(GREENMESSAGEPLUG + "Request to index: " + resp.getIndex() + " for _id: " + resp.getId() + " has been successful!" );
                    else {
                        messageToo.sendMessage(REDMESSAGEPLUG + "Request to index: " + resp.getIndex() + " for _id: " + resp.getId() + " has failed!!!");
                        messageToo.sendMessage(REDMESSAGEPLUG + "Failure Message: " + resp.getFailureMessage());
                        messageToo.sendMessage(REDMESSAGEPLUG + "Give your technical administrator this id to trace the error in the log: " +  resp.toString());
                        System.out.println(resp.toString() + ": runAsyncBulkRequest failure!! FailureMessage: " + resp.getFailureMessage());
                    }
                }
            }

            @Override
            public void onFailure(Exception arg0) {
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Something severe happend in runAsyncBulkRequest!!");
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Exception:");
                Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, arg0);
            }
        });
    }
    
    public static PlayerDataObject runMultiIndexSearch(MultiSearchRequest multiSearchRequest, PlayerDataObject pdObject) {

        try {
            MultiSearchResponse mSResponse = restClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
            for (MultiSearchResponse.Item resp : mSResponse.getResponses()) {
                //if it isnt a failure add it
                if (!resp.isFailure() && (resp.getResponse().getHits().getHits().length == 1)) {
                    pdObject.addSearchData(resp.getResponse().getHits().getHits()[0].getIndex(), resp.getResponse().getHits().getHits()[0].getSourceAsMap());
                }
            }
        } catch (IOException ex) {
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Something severe happend in runMultiIndexSearch!!");
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Exception:");
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pdObject;
    }

    public static void whoLookUp(CommandSender commandSender, Player target) throws IOException {
        //System.out.println("test");
        SearchResponse searchResponse = restClient.search(ESRequestBuilder.buildSearchRequest("players", "_id", target.getUniqueId().toString()), RequestOptions.DEFAULT);

        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        //long totalHits = searchHits.totalHits;

        Map<String, Object> playerData = hits[0].getSourceAsMap();
        if (commandSender instanceof Player) {
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + "> " + target.getDisplayName() + "'s current player data.");
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > User name: " + ChatColor.WHITE + target.getName());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Rank: " + ChatColor.WHITE + playerData.get("rank"));
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > IP: " + ChatColor.WHITE + target.getAddress().getAddress().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > UUID: " + ChatColor.WHITE + target.getUniqueId().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Current Mode: " + ChatColor.WHITE + target.getGameMode().toString());
            ((Player) commandSender).sendMessage(ChatColor.DARK_GREEN + " > Current World: " + ChatColor.WHITE + target.getWorld().getName());
        }

    }
    
    /** This allows a findOfflinePlayer call that moves player existence checking to this method.
     * This method is backed by findOfflinePlayer(String target).
     * @param target
     * @param commandSender
     * @return 
     */
    public static SearchHit findOfflinePlayer(String target, CommandSender commandSender){
        SearchHit hits = findOfflinePlayer(target);
        if (hits == null){
            commandSender.sendMessage("That target player was not found the Elastic Database. Please be sure to use the exact minecraft name and not their nickname!");
            return null;
        } else {
            return hits;
        }
    }
    /** Will return the UUID of an offline player
     * @param target
     * @return 
     */
    public static SearchHit findOfflinePlayer(String target) {
        try {
            SearchRequest playerSearchRequest = new SearchRequest("players");
            SearchSourceBuilder playerSearchSourceBuilder = new SearchSourceBuilder();
            
            playerSearchSourceBuilder.query(QueryBuilders.matchQuery("name", target));
            playerSearchRequest.source(playerSearchSourceBuilder);
            
            SearchResponse playerSearchResponse = restClient.search(playerSearchRequest, RequestOptions.DEFAULT);
            
            if (JawaPermissions.debug) System.out.print(JawaPermissions.pluginSlug + handlerSlug + " Search Response: " + playerSearchResponse);
            SearchHit[] hits = playerSearchResponse.getHits().getHits();
            if (hits.length != 1) return null;
            
            return hits[0];
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }
    
    /**
     * This will return a PlayerDataObject containing all user data. If the
     * player is not found this will return null
     * @param ident
     * @param getData
     * @return
     */
    public static PlayerDataObject findOfflinePlayer(String ident, boolean getData) {
        PlayerDataObject pdObject;
        if (getData) {
            try {
                SearchResponse sResponse = restClient.search(ESRequestBuilder.buildSearchRequest("players", "name", ident), RequestOptions.DEFAULT);
                SearchHit[] hits = sResponse.getHits().getHits();
                pdObject = new PlayerDataObject(UUID.fromString(hits[0].getId()));
                pdObject.addPlayerData(hits[0].getSourceAsMap());
            } catch (IOException ex) {
                Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }

        } else {
            SearchHit hit = findOfflinePlayer(ident);
            pdObject = new PlayerDataObject(UUID.fromString(hit.getId()));
            pdObject.addPlayerData(findOfflinePlayer(ident).getSourceAsMap());
        }
        return pdObject;

    }
}
