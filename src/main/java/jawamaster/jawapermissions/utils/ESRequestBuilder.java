/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.utils;

import org.bukkit.entity.Player;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;

/**
 *
 * @author alexander
 */
public class ESRequestBuilder {
    /** Returns a newly composed UpdateRequest object. Data is the JSONObject 
     * containing the update information. Index is the index being updated. id
     * is the value of the _id of the index. upsert will determine if the data is
     * created if it doesn't already exist.
     * @param data
     * @param index
     * @param id
     * @param upsert
     * @return 
     */
    public static UpdateRequest updateRequestBuilder(JSONObject data, String index, String id, boolean upsert){
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest
                .index(index)
                .id(id)
                .doc(data.toMap())
                .docAsUpsert(upsert);
        return updateRequest;
    }
    
    /** Returns a SearchRequest ready to be run. This is very simple and can also be done in place.
     * However I found I was doing it a lot and decided to standardize it.
     * @param index
     * @param searchField
     * @param queryValue
     * @return 
     */
    public static SearchRequest buildSearchRequest(String index, String searchField, String queryValue){
        return new SearchRequest(index)
                        .source(new SearchSourceBuilder()
                                .query(QueryBuilders.matchQuery(searchField, queryValue)));
    }
    
    public static void addToMultiSearchRequest(MultiSearchRequest request, String index, String docValue, String queryValue){
        request.add(buildSearchRequest(index, docValue, queryValue));
    }
    
    public static MultiSearchRequest buildSingleMultiSearchRequest(String index, String docValue, String queryValue){
        MultiSearchRequest request = new MultiSearchRequest();
        request.add(buildSearchRequest(index, docValue, queryValue));
        return request;
        
    }
    
    public static UpdateRequest requestFieldRemoval(Player player,String homeName){
        UpdateRequest request = new UpdateRequest();
        Script scr = new Script("ctx._source.remove('" + homeName + "')");
        request.index("homes").id(player.getUniqueId().toString()).script(scr);
        return request;
    }
    
    public static SearchRequest getAllOfIndex(String index){
        SearchRequest sRequest = new SearchRequest(index);
        sRequest.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));
        return sRequest;
    }
    
    public static IndexRequest createIndexRequest(String index, String id, JSONObject data){
        IndexRequest indexRequest = new IndexRequest()
                .index(index)
                .id(id)
                .source(data.toMap());
        
        return indexRequest;
    }
    
    public static DeleteRequest deleteDocumentRequest(String index, String id){
        DeleteRequest deleteRequest = new DeleteRequest()
                .index(index)
                .id(id);
        return deleteRequest;
    }
}
