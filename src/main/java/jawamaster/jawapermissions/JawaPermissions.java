/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.commands.RankInfo;
import jawamaster.jawapermissions.commands.banPlayer;
import jawamaster.jawapermissions.commands.playerinfo.BanInfo;
import jawamaster.jawapermissions.commands.playerinfo.PlayerInfo;
import jawamaster.jawapermissions.commands.playerinfo.GetUUID;
import jawamaster.jawapermissions.commands.reloadPermissions;
import jawamaster.jawapermissions.commands.setRank;
import jawamaster.jawapermissions.commands.testCommand;
import jawamaster.jawapermissions.commands.unbanPlayer;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.handlers.PlayerDataHandler;
import jawamaster.jawapermissions.listeners.PlayerJoin;
import jawamaster.jawapermissions.listeners.PlayerQuit;
import jawamaster.jawapermissions.commands.playerinfo.WhoCommand;
import jawamaster.jawapermissions.handlers.FileHandler;
import jawamaster.jawapermissions.listeners.AsyncPlayerKickListener;
import jawamaster.jawapermissions.listeners.OnPlayerInfoLoaded;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Arthur Bulin
 */
public class JawaPermissions extends JavaPlugin {
    //Declare plugin and permission permissionsHandler
    public static PermissionsHandler permissionsHandler;
    public static JawaPermissions plugin;
    public static PlayerDataHandler playerDataHandler;
    public static Event rankChangeEvent;
    
    //Declare HashMap Storage for the loaded Permissions
    public static HashMap<UUID, String> playerRank;
    public static HashMap<UUID, String> autoElevate;
    
    
    //Declare configuration variables
    public static Configuration config;
    public static String eshost, newPlayerMessage, defaultWorld, serverName;
    public static boolean debug;
    
    public final static String pluginSlug = "[JawaPermissions] ";
    
    
    //Return the plugin
    public static JawaPermissions getPlugin() {
        return plugin;
    }
    
    @Override
    public void onEnable(){
        Bukkit.getLogger().setLevel(Level.FINEST);
        loadConfig();

        plugin = this;
        try {
            permissionsHandler = new PermissionsHandler();
        } catch (IOException ex) {
            Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
        }
        autoElevate = FileHandler.getAutoElevateList();
        playerDataHandler = new PlayerDataHandler(this, autoElevate);
        
        //Initialize the permission storage HashMap
        playerRank = new HashMap(); 
        
        //Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
        //getServer().getPluginManager().registerEvents(new PlayerPreJoin(), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerKickListener(), this);
        getServer().getPluginManager().registerEvents(new OnPlayerInfoLoaded(), this);
        
        //Register Commands
        this.getCommand("uuid").setExecutor(new GetUUID());
        this.getCommand("setrank").setExecutor(new setRank());
        this.getCommand("ban").setExecutor(new banPlayer());
        this.getCommand("testcommand").setExecutor(new testCommand());
        this.getCommand("unban").setExecutor(new unbanPlayer());
        this.getCommand("reloadperms").setExecutor(new reloadPermissions());
        this.getCommand("who").setExecutor(new WhoCommand());
        this.getCommand("baninfo").setExecutor(new BanInfo());
        this.getCommand("playerinfo").setExecutor(new PlayerInfo());
        this.getCommand("rankinfo").setExecutor(new RankInfo());
    }
    
    @Override
    public void onDisable(){

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
        serverName = (String) config.get("servername");
        defaultWorld = config.getString("default-world");
        newPlayerMessage = config.getString("new-player-message", "You have been installed!");

        if (debug){
            System.out.println(pluginSlug + "Debug is turned on! This is not recommended unless you are a dev or are tracking a problem!");
            System.out.println(pluginSlug + "If you are experiencing problems in a clean run environment please contact the dev on github.");
            System.out.println(pluginSlug + "If you are not running in a clean environment (just Jawa plugins) then please do not report your issue the dev at this time.");
            System.out.println(pluginSlug + "You may switch debug to OFF by changing the debug paramater in the config to false.");
        } else {
            System.out.println(pluginSlug + "Debug is turned off");
        }
    }

}
