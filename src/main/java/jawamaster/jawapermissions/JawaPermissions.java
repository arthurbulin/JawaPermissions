/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.commands.banPlayer;
import jawamaster.jawapermissions.commands.getUUID;
import jawamaster.jawapermissions.commands.reloadPermissions;
import jawamaster.jawapermissions.commands.setRank;
import jawamaster.jawapermissions.commands.testCommand;
import jawamaster.jawapermissions.commands.unbanPlayer;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import jawamaster.jawapermissions.handlers.PlayerDataHandler;
import jawamaster.jawapermissions.listeners.PlayerJoin;
import jawamaster.jawapermissions.listeners.PlayerQuit;
import jawamaster.jawapermissions.commands.whoCommand;
import jawamaster.jawapermissions.listeners.PlayerPreJoin;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
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
    public static PlayerDataHandler playerDataHandler;
    public static Event rankChangeEvent;
    
    //TODO consider switching from JSONObject to SourceMap.
    //Declare HashMap Storage for the loaded Permissions
    public static HashMap<UUID, String> playerRank;
    public static HashMap<String, Integer> immunityLevels;
    
    //New rank object method
    public static Map<String, Rank> rankMap;
    
    //Declare configuration variables
    public static Configuration config;
    public static String eshost, newPlayerMessage, defaultWorld, serverName;
    public static int esport;
    public static boolean debug;
//    private static CredentialsProvider credentialsProvider;
//    private static String esUser, esPass;
    
    public final static String pluginSlug = "[JawaPermissions] ";
    
    //Return the plugin
    public static JawaPermissions getPlugin() {
        return plugin;
    }
    
    //Return the ES High Level Client
    public static RestHighLevelClient getESClient(){
        return restClient;
    }
    
    @Override
    public void onEnable(){
        System.out.println(Logger.GLOBAL_LOGGER_NAME);
        //System.out.println(Logger.);
        //System.out.println(Bukkit.getLogger());
        loadConfig();
        startESHandler();
        //Initialize passable instances
        plugin = this;
        handler = new PermissionsHandler(this);
        playerDataHandler = new PlayerDataHandler(this);
        
        //Initialize the permission storage HashMap
        playerRank = new HashMap(); 
        immunityLevels = new HashMap();
        
        //Store the rank objects here
        rankMap = new HashMap();
        
        //Load permissions. Try-catch to deal with possible exceptions.
        try {
            handler.load();
        } catch (FileNotFoundException | ParseException ex) {
            Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Register player events
        
        //Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
        getServer().getPluginManager().registerEvents(new PlayerPreJoin(), this);
        
        //Register Commands
        this.getCommand("uuid").setExecutor(new getUUID());
        this.getCommand("setrank").setExecutor(new setRank());
        this.getCommand("ban").setExecutor(new banPlayer());
        this.getCommand("testcommand").setExecutor(new testCommand());
        this.getCommand("unban").setExecutor(new unbanPlayer());
        this.getCommand("reloadperms").setExecutor(new reloadPermissions());
        this.getCommand("who").setExecutor(new whoCommand());
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
        System.out.print(pluginSlug + "Loading configuration from file.");
        //Handle the config generation and loading
        this.saveDefaultConfig();
        config = this.getConfig();
        
        //Initialize config values
        debug = (Boolean) config.get("debug");
        eshost = (String) config.get("eshost");
        esport = (int) config.get("esport");
        serverName = (String) config.get("servername");
//        esUser = config.getString("esuser");
//        esPass = config.getString("espass");
        defaultWorld = config.getString("default-world");
        if (config.isSet("new-player-message")){
            newPlayerMessage = (String) config.get("new-player-message");
        }
        
        if (debug){
            System.out.println(pluginSlug + "Debug is turned on! This is not recommended unless you are a dev or are tracking a problem!");
            System.out.println(pluginSlug + "If you are experiencing problems in a clean run environment please contact the dev on github.");
            System.out.println(pluginSlug + "If you are not running in a clean environment (just Jawa plugins) then please do not report your issue the dev at this time.");
            System.out.println(pluginSlug + "You may switch debug to OFF by changing the debug paramater in the config to false.");
        } else {
            System.out.println(pluginSlug + "Debug is turned off");
        }
    }
    
    /** Create the elasticsearch handler istance needed to query the ElasticSearch db.
     */
    public void startESHandler(){

        //Initialize the restClient for global use
        try{
        restClient = new RestHighLevelClient(RestClient.builder(new HttpHost(eshost, esport, "http")).setRequestConfigCallback((RequestConfig.Builder requestConfigBuilder) -> requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000)).setMaxRetryTimeoutMillis(60000));
        } catch (Exception e){
            System.out.println("Ooops");
            e.printStackTrace();
            
        }
        //Long annoying debug line for restClient connection
        if (debug){
            System.out.println(pluginSlug + "High Level Rest Client initialized at: " + restClient.toString());
            System.out.println(pluginSlug + "With host: " + eshost);
            System.out.println(pluginSlug + "on port: " + esport);
            boolean restPing = false;
            try {
                restPing = restClient.ping();
            } catch (IOException ex) {
                Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(pluginSlug + "ElasticSearch DB pings as: " + restPing );
        }
        
        eshandler = new ESHandler(this);
        
    }

}
