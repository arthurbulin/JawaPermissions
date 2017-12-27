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
import org.bukkit.entity.Player;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
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
    
    public static boolean getPing(){
        try {
            return restClient.ping();
        } catch (IOException ex) {
            Logger.getLogger(ESHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
//*****************GET CALLS*******************
    /**Takes a player's UUID and will request the player's stored data from the ElasticSearch database by constructing the GetRequest here then sending the get
     * request to generalGet(GetRequest). That method will return the Response as a Map<String, Object>.
     * @param uuid*
     * @return */
    public static Map<String, Object> getPlayerData(UUID uuid){
        //restClient = JawaPermissions.getESClient();
        if (JawaPermissions.debug){
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Get has been called for player UUID: " + uuid.toString());
        }
        //Create the getRequest.
        getRequest = new GetRequest("mc","players",uuid.toString());
        response = generalGet(getRequest);
        
        if (response == null){
            return null;
        } else return response;
    }
    
    /** This will construct a GetRequest for the ElasticSearch's database that should contain Minecraft server info.
     * @param serverName
     * @return
     */
    public static Map<String, Object> getServerData(String serverName){
        //restClient = JawaPermissions.getESClient();
        getRequest = new GetRequest("servers", "server", serverName);
        response = generalGet(getRequest);
        return response;
    }
    
    /** Sends the GetRequest to the ElasticSearch server and returns the response as a source map.
     * @param getRequest
     * @return
     */
    public static Map<String, Object> generalGet(GetRequest getRequest){
        //Asynchronously get the response. Async is important so that the server doesn't stop and wait for a responce.
        restClient.getAsync(getRequest, new ActionListener<GetResponse>() {
            
            @Override
            public void onResponse(GetResponse getResponse) {
                response = getResponse.getSourceAsMap();
                //Debug response data
                if (JawaPermissions.debug){
                    System.out.println(JawaPermissions.pluginSlug + handlerSlug + "generalGet response: " + response);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                //If the something fails
                if (e instanceof ElasticsearchException) {
                    if (((ElasticsearchException) e).status() == RestStatus.NOT_FOUND){
                        if (JawaPermissions.debug){
                            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Player was not found in the ElasticSearch index.");
                            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Will return null response and call firstTimePlayer() or firstServerRun().");
                        }
                        notInES = true;
                    }
                }
                System.out.println(e.toString());
            }
            
        });
        
        if (notInES){
            return null;
        } else return response;
    }
    
    
    
//*****************Index CALLS*******************
    /** Indexes the player's data to the ElasticSearch database. This should only be used to add a new player to the DB. This method constructs the request and passes it to
     * generalIndex(indexRequest) to process the actual indexing.
     * @param uuid
     * @param playerData
     * @return
     */
    public static boolean indexPlayerData(UUID uuid, Map<String, Object> playerData){
        indexRequest = new IndexRequest("mc", "players", uuid.toString()).source(playerData);
        return generalIndex(indexRequest);
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
    
    
//*****************Specialized Calls**************
    /** Called on playerJoinEvent. Will search for a player in the index by UUID. If not present will return false.
     * @param player
     * @return
     */
    public static void searchForReturningPlayer(Player player) {
        searchRequest = new SearchRequest("mc");
        searchSourceBuilder = new SearchSourceBuilder();
        
        searchSourceBuilder.query(QueryBuilders.matchQuery("_id", player.getUniqueId().toString()));
        searchRequest.source(searchSourceBuilder);
        
        restClient.searchAsync(searchRequest, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                searchHits = searchResponse.getHits();
                long totalHits = searchHits.totalHits;
                
                if (totalHits == 0){ //If not found in Index
                    //TODO implement first time player methods
                    Map<String, Object> playerData = JawaPermissions.playerDataHandler.firstTimePlayer(player);
                    indexPlayerData(player.getUniqueId(), playerData);
                    cachePlayerRank(player);
                    player.sendMessage(JawaPermissions.newPlayerMessage);
                    System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " has been indexed successfully.");
                    
                }else { //If player is found in index
                    if (JawaPermissions.debug){
                        System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Player found in ElasticSearch. Caching player rank.");
                    }
                    cachePlayerRank(player);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
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
}
