/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions;

import com.maxmind.geoip2.DatabaseReader;
import java.io.File;
import java.util.logging.Logger;
import jawamaster.jawapermissions.commands.RankInfo;
import jawamaster.jawapermissions.commands.BanPlayer;
import jawamaster.jawapermissions.commands.PlayerCommentCommand;
import jawamaster.jawapermissions.commands.playerinfo.BanInfo;
import jawamaster.jawapermissions.commands.playerinfo.PlayerInfo;
import jawamaster.jawapermissions.commands.playerinfo.GetUUID;
import jawamaster.jawapermissions.commands.ReloadPermissions;
import jawamaster.jawapermissions.commands.setRank;
import jawamaster.jawapermissions.commands.testCommand;
import jawamaster.jawapermissions.commands.unbanPlayer;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.handlers.PlayerDataHandler;
import jawamaster.jawapermissions.listeners.PlayerJoin;
import jawamaster.jawapermissions.listeners.PlayerQuit;
import jawamaster.jawapermissions.commands.playerinfo.WhoCommand;
import jawamaster.jawapermissions.handlers.AutoElevateHandler;
import jawamaster.jawapermissions.handlers.PlayerInfoHandler;
import jawamaster.jawapermissions.listeners.AsyncPlayerKickListener;
import jawamaster.jawapermissions.listeners.OnPlayerInfoLoaded;
import net.jawasystems.jawacore.JawaCore;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Arthur Bulin
 */
public class JawaPermissions extends JavaPlugin {
    
    private static final Logger LOGGER = Logger.getLogger("JawaPermissions");
    
    //Declare plugin and permission permissionsHandler
    public static PermissionsHandler permissionsHandler;
    public static JawaPermissions plugin;
    public static PlayerDataHandler playerDataHandler;
    
    //Declare configuration variables
    public static Configuration config;
    public static String newPlayerMessage;
    public static boolean debug;
    private static boolean altCheck;
    private static int altCheckLevel;
    private static boolean geoIPCheck;
    private static String geoIPDB;
    
    
    @Override
    public void onEnable(){
        loadConfig();

        plugin = this;
        
        //Load permissions
        PermissionsHandler.load();

        //Load the auto elevate list
        AutoElevateHandler.getAutoElevateList();
        
        //Load GeoIP Database
        geoIPCheck = PlayerInfoHandler.getIPDatabase(geoIPDB);
        
        //Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerKickListener(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerInfoLoaded(), this);
        
        //Register Commands
        this.getCommand("uuid").setExecutor(new GetUUID());
        this.getCommand("setrank").setExecutor(new setRank());
        this.getCommand("ban").setExecutor(new BanPlayer());
        this.getCommand("testcommand").setExecutor(new testCommand());
        this.getCommand("unban").setExecutor(new unbanPlayer());
        this.getCommand("reloadperms").setExecutor(new ReloadPermissions());
        this.getCommand("who").setExecutor(new WhoCommand());
        this.getCommand("baninfo").setExecutor(new BanInfo());
        this.getCommand("playerinfo").setExecutor(new PlayerInfo());
        this.getCommand("rankinfo").setExecutor(new RankInfo());
        this.getCommand("playercomment").setExecutor(new PlayerCommentCommand());
        
    }
    
    @Override
    public void onDisable(){

    }
    
    /** Loads the configuration file from storage and loads the values into static references within the plugin.
     */
    public void loadConfig(){
        //System.out.print(pluginSlug + "Loading configuration from file.");
        //Handle the config generation and loading
        this.saveDefaultConfig();
        config = this.getConfig();
        
        //Initialize config values
        debug = config.getBoolean("debug",false);
        altCheck = config.getBoolean("alt-notification.enable", false);
        altCheckLevel = config.getInt("alt-notification.minimum-immunity", 1);
        
        newPlayerMessage = config.getString("new-player-message", "You have been installed!");

        JawaCore.receiveConfigurations(this.getClass().getName(), config);
        //System.out.println(this.getClass().getName());
        if (debug){
            LOGGER.info("Debug is turned on! This is not recommended unless you are a dev or are tracking a problem!");
            LOGGER.info("If you are experiencing problems in a clean run environment please contact the dev on github.");
            LOGGER.info("If you are not running in a clean environment (just Jawa plugins) then please do not report your issue to the dev at this time.");
            LOGGER.info("You may switch debug to OFF by changing the debug paramater in the config to false.");
        } else {
            LOGGER.info("Debug is turned off");
        }
        
        geoIPDB = config.getString("geoip-database", "GeoLite2-City.mmdb");
        
    }
    
        //Return the plugin
    public static JawaPermissions getPlugin() {
        return plugin;
    }
    
    public FileConfiguration getConfiguration(){
        return plugin.getConfig();
    }
    
    public static int altCheckLevel(){
        return altCheckLevel;
    }
    
    public static boolean checkForAlts(){
        return altCheck;
    }
    


}
