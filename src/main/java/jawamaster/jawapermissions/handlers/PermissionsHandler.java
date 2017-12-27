/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Arthur Bulin
 */
public class PermissionsHandler {
    private final JawaPermissions plugin;
    private final boolean loaded = false;
    private final String handlerSlug = "[PermissionsHandler] ";
    
    /** PermissionHandler(JawaPermissions plugin) initializes the permissions handler. This handler will handle loading, reloading, removing, and
     * permission checks for the plugin.
     * @param plugin **/
    public PermissionsHandler(JawaPermissions plugin){
        this.plugin = plugin;
    }
    
    /** reload() loads the permissions from yml files found in the ./permissions folder and converts them into a JSONObject. That JSONObject is
     * then committed to JawaPermissions.worldPermissions<String,JSONObject> where String is the name of the minecraft world and JSONObject is the
     * JSON generated from the yml file
     * @throws java.io.FileNotFoundException
     * @throws org.json.simple.parser.ParseException **/
    public void reload() throws FileNotFoundException, ParseException{
        //TODO recache all online player's ranks
        final File permFiles =  new File(JawaPermissions.getPlugin().getDataFolder() + "/permissions");
        permFiles.mkdirs();
        File[] fileList = permFiles.listFiles();
        
        if (JawaPermissions.debug){
            System.out.print(JawaPermissions.pluginSlug + handlerSlug + "Permissions are being checked in: " + permFiles);
        }
        
        for(File f: fileList){ //Iterate over fileList
            Yaml yaml = new Yaml(); //Create Yaml object for permission retreival
            String currentWorld = f.getName();
            
            if(currentWorld.indexOf('.') > 0){ //extract name of world from file name
                currentWorld = currentWorld.substring(0, currentWorld.indexOf('.'));
            }
            
            if (JawaPermissions.debug){
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Loading permissions from file: " + f.getName());
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Current world name identified as: " + currentWorld);
            }
            
            try { //Read in the permissions files one at a time
                BufferedReader reader = new BufferedReader(new FileReader(f));
                JSONParser parser = new JSONParser();
                
                // TODO using * in the permissions file by itself to give all permissions generates an error. Find a way around that.
                Object objPerms = yaml.load(reader); //Read the yaml file into an object
                String stringPerms = JSONValue.toJSONString(objPerms); //Convert object containing yaml data into a JSONString
                JSONObject jsonPerms = (JSONObject) parser.parse(stringPerms); //Convert the JSONString into a JSONObject for later use
                
                JawaPermissions.worldPermissions.put(currentWorld, jsonPerms);
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(JawaPermissions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /** Loads the permissions on server enable.
     * @throws java.io.FileNotFoundException
     * @throws org.json.simple.parser.ParseException **/
    public void load() throws FileNotFoundException, ParseException{
        if (loaded) return;
        reload();
        
    }
    
    /** Redirects permissions check to the propper has method based on the instanceof the commandSender. This returns false for any commandSender that
     * is not a Player, ConsoleCommandSender, or BlockCommandSender.
     * @param commandSender
     * @param perm
     * @return */
    public boolean has(CommandSender commandSender, String perm){
        if (commandSender instanceof ConsoleCommandSender) return true; //Console has all power
        else if (commandSender instanceof Player) return has((Player) commandSender, perm); //Resolve permissions for Players
        else if (commandSender instanceof BlockCommandSender) return has((BlockCommandSender) commandSender, perm); //Resolver permissions for Command Blocks
        else return false;
    }
    
    /** Resolves permissions for BlockCommandSenders. NOT COMPLETE.
     * @param commandSender
     * @param perm
     * @return */
    public boolean has(BlockCommandSender commandSender, String perm){
        // TODO settup command block permissions
        return true;
    }
    
    /** Checks to see if a player has a specific permission. This is the end-track reference for all Player permission checking. All other has methods or
     * hasPermission methods will divert here to check permissions for players.
     * @param player.*
     * @param perm
     * @return */
    public boolean has(Player player, String perm) {
        String currentWorld = player.getWorld().getName();
        String rank = JawaPermissions.playerRank.get(player.getUniqueId());
        
        JSONObject rankData = (JSONObject) JawaPermissions.worldPermissions.get(currentWorld).get(rank);
        
        JSONArray permissions = (JSONArray) rankData.get("permissions");
        JSONArray prohibitions = (JSONArray) rankData.get("prohibitions");

        if (JawaPermissions.debug){
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "has call for " + player.getName() + " for permission: " + perm);
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " has group permissions: " + permissions);
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " has group prohibitions: " + prohibitions);
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " has permission in group perms: " + permissions.contains(perm));
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " has prohibitions in group prohibs: " + prohibitions.contains(perm));
        }
        
        if (permissions.contains(perm) && !prohibitions.contains(perm)) {
            if (JawaPermissions.debug){
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " has permission and does not have a prohibition.");
            }
            return true;
        }
        
        else {
            if (JawaPermissions.debug){
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " does not have permission or has a prohibition.");
            }
            return false;
        }


    }
    
    //TODO Create rank set
    public boolean setRank(Player player, String rank){
        
        return true;
    }
    
    public String getGroup(Player player){
        Map<String, Object> playerData = ESHandler.getPlayerData(player.getUniqueId());
        
        if (playerData == null){
            return null;
        }
        
        String rank = (String) playerData.get("rank");
        if (JawaPermissions.debug){
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + player.getName() + " has rank: " + rank);
        }
        return rank;
    }
    
}