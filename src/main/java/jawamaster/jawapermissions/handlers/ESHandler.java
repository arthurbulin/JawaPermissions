/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package jawamaster.jawapermissions.handlers;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.PlayerDataObject;
import jawamaster.jawapermissions.utils.ESRequestBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
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

    private static RestHighLevelClient restClient;

    private JawaPermissions plugin;
    private JavaPlugin javaplugin;
    
    
    private final static String handlerSlug = "[ESHandler] ";
    private static boolean notInES = false;
    private final static String REDMESSAGEPLUG = ChatColor.RED + "> ";
    private final static String GREENMESSAGEPLUG = ChatColor.GREEN + "> ";
    

    /** Public constructor for the Elasticsearch Database handler. This object
     * handles all calls to the database.
     * @param plugin 
     */
    public ESHandler(RestHighLevelClient restClient) {
        ESHandler.restClient = restClient;
    }
    
    /** Will search the Elasticsearch database syncrounously to find if a user has
     * already been indexed. If their UUID exists in the index it will return true.
     * @param target
     * @return 
     */
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

    /**Indexes the player's data to the ElasticSearch database. This should only
     * be used to add a new player to the database. This method constructs the request
     * and passes it to generalIndex(indexRequest) to process the actual
     * indexing.
     *
     * @param target
     * @param playerData
     * @return
     */
    public static boolean indexPlayerData(UUID target, JSONObject playerData) { //TODO evaluate if this can be replaced with the asyncUpdateData or a more generic request
        //indexRequest = new IndexRequest("mc", "players", target.toString()).source(playerData);
        IndexRequest indexRequest = new IndexRequest("players")
                .id(target.toString())
                .source(playerData.toMap());
        restClient.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + playerData.get("name") + " has been indexed successfully.");
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    /** This will update a player's data Async and will printout to the console success.
     * @param target
     * @param updateData 
     */
    public static void asyncUpdateData(Player target, JSONObject updateData) {
        UpdateRequest updateRequest = ESRequestBuilder.updateRequestBuilder(updateData, "players", target.getUniqueId().toString(), true);

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
    
    public static void asyncUpdateData(String targetUUID, JSONObject updateData) {
        UpdateRequest updateRequest = ESRequestBuilder.updateRequestBuilder(updateData, "players", targetUUID, true);

        restClient.updateAsync(updateRequest, RequestOptions.DEFAULT, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                System.out.println(JawaPermissions.pluginSlug + " " + targetUUID + "'s details have been updated.");
            }

            @Override
            public void onFailure(Exception arg0) {
                System.out.println(JawaPermissions.pluginSlug + targetUUID + "'s information update failed.");
                arg0.printStackTrace();
            }
        });
    }
    
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
    
    /** Executes a multi index search and returns the information in a PlayerDataObject.
     * @param multiSearchRequest
     * @param pdObject
     * @return 
     */
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

    /** Executes an lookup and writes out information to command sender.
     * @param commandSender
     * @param target
     * @throws IOException 
     */
    public static void whoLookUp(CommandSender commandSender, Player target) throws IOException { //TODO rewrite this and who command
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
    public static PlayerDataObject findOfflinePlayer(String ident, boolean getData) { //TODO clean up all fingOfflinePlayer methods
        PlayerDataObject pdObject;
        if (getData) {
            try {
                SearchResponse sResponse = restClient.search(ESRequestBuilder.buildSearchRequest("players", "name", ident), RequestOptions.DEFAULT);
                SearchHit[] hits = sResponse.getHits().getHits();
                if (hits.length != 1) return null; //If 1 entry is not found then return null.
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
    
    public static boolean singleUpdateRequest(UpdateRequest request){
        try {
            UpdateResponse response = restClient.update(request, RequestOptions.DEFAULT);
            System.out.println(response);
            if ((response.getResult() == Result.CREATED) || (response.getResult() == Result.UPDATED)) {
                return true;
            } else return false;
            
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public static SearchHit[] runSearchRequest(SearchRequest request){
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            return response.getHits().getHits();
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static boolean runSingleIndexRequest(IndexRequest indexRequest){
        try {
            IndexResponse response = restClient.index(indexRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public static boolean runSingleDocumentDeleteRequest(DeleteRequest deleteRequest){
        try {
            DeleteResponse response = restClient.delete(deleteRequest, RequestOptions.DEFAULT);
            return response.getResult().equals(Result.DELETED);
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public static MultiSearchResponse runMultiSearchRequest(MultiSearchRequest request){
        try {
            return restClient.msearch(request, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static PlayerDataObject getPlayerData(Player target) throws IOException{
        return getPlayerData(target.getUniqueId().toString());
    }
    
    public static PlayerDataObject getPlayerData(String target) throws IOException{
        PlayerDataObject pdObject = new PlayerDataObject(UUID.fromString(target));
        SearchResponse sResponse = restClient.search(ESRequestBuilder.buildSearchRequest("players", "_id", target), RequestOptions.DEFAULT);
        SearchHit[] hits = sResponse.getHits().getHits();
        pdObject.addPlayerData(hits[0].getSourceAsMap());
        return pdObject;
    }
    

}
