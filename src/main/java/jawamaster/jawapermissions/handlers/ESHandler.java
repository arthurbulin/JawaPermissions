/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.handlers;

import java.util.Map;
import java.util.UUID;
import jawamaster.jawapermissions.JawaPermissions;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestHighLevelClient;

/**
 *
 * @author Arthur Bulin
 */
public class ESHandler {
    public static RestHighLevelClient restClient;
    public JawaPermissions plugin;
    static Map<String, Object> response;
    
    public ESHandler(JawaPermissions plugin){
        this.plugin = plugin;
    }
    
    //GET calls
    /**getPlayerData takes a player's UUID and will request the player's stored data from the ESd
     * @param uuid*
     * @return */
    public static Map<String, Object> getPlayerData(UUID uuid){
       restClient = JawaPermissions.getESClient();
       
       if (JawaPermissions.debug){
           System.out.println("ES Databse call initiated in method getPlayerData."
                + "\nGet has been called for player UUID: " + uuid.toString()
                + "\nGet request will run after this line prints.");
       }
       GetRequest getRequest = new GetRequest("mc","players",uuid.toString());
        
        restClient.getAsync(getRequest, new ActionListener<GetResponse>() {
            
            @Override
            public void onResponse(GetResponse getResponse) {
                response = getResponse.getSourceAsMap();
                if (JawaPermissions.debug){
                    System.out.println(response);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                System.out.println(e.toString());
            }
            
        });
        return response;
    }
    
}
