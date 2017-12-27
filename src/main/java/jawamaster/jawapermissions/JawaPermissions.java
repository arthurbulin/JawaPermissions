/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.commands.getUUID;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import jawamaster.jawapermissions.listeners.PlayerJoin;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Arthur Bulin
 */
public class JawaPermissions extends JavaPlugin {
    //Declare plugin and permission handler
    public static PermissionsHandler handler;
    public static JawaPermissions plugin;
    public static RestHighLevelClient restClient;
    public static ESHandler eshandler;
    
    //Declare HashMap Storage for the loaded Permissions
    public static HashMap<String,JSONObject> worldPermissions;
    
    
    //Declare configuration variables
    public static Configuration config;
    public static String eshost;
    public static int esport;
    public static boolean debug;
    private static CredentialsProvider credentialsProvider;
    private static String esUser, esPass;
    
    //Return the plugin
    public static JawaPermissions getPlugin() {
        return plugin;
    }
    
    //Return the ES High Level Client
    public static RestHighLevelClient getESClient(){
        return restClient;
    }
    
    //Return the ESHandler
    public static ESHandler getESHandler(){
        return eshandler;
    }
    
    @Override
    public void onEnable(){
        loadConfig();
        startESHandler();
        //Initialize passable instances
        plugin = this;
        handler = new PermissionsHandler(this);
        
        //Initialize the permission storage HashMap
        worldPermissions = new HashMap();
        
        
        //Load permissions. Try-catch to deal with possible exceptions.
        try {
            handler.load();
        } catch (FileNotFoundException | ParseException ex) {
            Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        
        //Register Commands
        this.getCommand("uuid").setExecutor(new getUUID());
    }
    
    @Override
    public void onDisable(){
        try {
            restClient.close();
        } catch (IOException ex) {
            Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /** Loads the configuration file from storage and loads the values into static references within the plugin.
     */
    public void loadConfig(){
        System.out.print("Loading configuration from file.");
        //Handle the config generation and loading
        this.saveDefaultConfig();
        config = this.getConfig();
        
        //Initialize config values
        debug = (Boolean) config.get("debug");
        eshost = (String) config.get("eshost");
        esport = (int) config.get("esport");
        esUser = config.getString("esuser");
        esPass = config.getString("espass");
        
        if (debug){
            System.out.println("Debug is turned on! This is not recommended unless you are a dev or are tracking a problem!");
            System.out.println("If you are experiencing problems in a clean run environment please contact the dev on github.");
            System.out.println("If you are not running in a clean environment (just Jawa plugins) then please do not report your issue the dev at this time.");
            System.out.println("You may switch debug to OFF by changing the debug paramater in the config to false.");
        } else {
            System.out.println("Debug is turned off");
        }
    }
    
    /** Create the elasticsearch handler istance needed to query the ElasticSearch db.
     * 
     */
    public void startESHandler(){
        eshandler = new ESHandler(this);
        
        //Load the needed credentials
        //credentialsProvider = new BasicCredentialsProvider();
       // credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esUser, esPass));

        //Save authentication for later

        /*
        restClient = new RestHighLevelClient(RestClient.builder(new HttpHost(eshost, esport, "http")).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }));
        */
        
        //Initialize the restClient for global use
        restClient = new RestHighLevelClient(RestClient.builder(new HttpHost(eshost, esport, "http")).setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000);
            }
        }).setMaxRetryTimeoutMillis(60000));
        
        //Long annoying debug line for restClient connection
        if (debug){
            System.out.println("High Level Rest Client initialized at: " + restClient.toString());
            System.out.println("With host: " + eshost);
            System.out.println("on port: " + esport);
            boolean restPing = false;
            try {
                restPing = restClient.ping();
            } catch (IOException ex) {
                Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("ElasticSearch DB pings as: " + restPing );
        }
        
        // TODO Create asyncServer ping to keep the ElasticSearch Connection alive
        // TODO Create an intial data pull from the server to prevent 
        
    }
}