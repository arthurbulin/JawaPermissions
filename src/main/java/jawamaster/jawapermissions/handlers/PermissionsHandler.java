/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.Rank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Arthur Bulin
 */
public class PermissionsHandler {
    private final JawaPermissions plugin;
    private final boolean loaded = false;
    private final static String handlerSlug = "[PermissionsHandler] ";
    
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
    public static void reload() throws FileNotFoundException, ParseException, IOException{
        //load the player immunity. This will also be used to check for valid ranks
        loadImmunity();
        
        //Get the locations for the permissions files
        final File permFiles =  new File(JawaPermissions.getPlugin().getDataFolder() + "/permissions");
        
        //Generate directories if they don't exist
        permFiles.mkdirs();
        
        //File list of permissions files
        File[] fileList = permFiles.listFiles();
        
        if (JawaPermissions.debug){
            System.out.print(JawaPermissions.pluginSlug + handlerSlug + "Permissions are being checked in: " + permFiles);
        }
        
        //Iterate over filelist
        for(File f: fileList){
            //Yaml yaml = new Yaml(); //Create Yaml object for permission retreival
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(f);
            String currentWorld = f.getName();
            
            if(currentWorld.indexOf('.') > 0){ //extract name of world from file name
                currentWorld = currentWorld.substring(0, currentWorld.indexOf('.'));
            }
            
            if (JawaPermissions.debug){
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Loading permissions from file: " + f.getName());
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Current world name identified as: " + currentWorld);
            }
            
            //System.out.println(yaml.getKeys(true));
           // System.out.println(yaml.getKeys(false));
            for (String rank: yaml.getKeys(false)){
                
                if (!JawaPermissions.rankMap.containsKey(rank)){
                    //Create a new rank object and commit permissions to it
                    //System.out.println("Getting Immunity level for: " + rank);
                    
                    JawaPermissions.rankMap.put(rank, new Rank(rank, JawaPermissions.immunityLevels.get(rank)));
                }
                
                JawaPermissions.rankMap.get(rank).addWorld(currentWorld, yaml.getConfigurationSection(rank).getStringList("permissions"), yaml.getConfigurationSection(rank).getStringList("prohibitions"));
                
                String inherits;
                if (yaml.getConfigurationSection(rank).contains("inherits")){ //If rank has inheritence
                    
                    inherits = yaml.getConfigurationSection(rank).getString("inherits"); //Get inherited rank
                    
                    while (true) { //iterate over until we hit a rank that doesnt have inherited perms
                        //Add permissions list from inherited rank
                        JawaPermissions.rankMap.get(rank).addPermissions(currentWorld, yaml.getConfigurationSection(inherits).getStringList("permissions"));
                        //Add prohibitions list from inherited rank
                        JawaPermissions.rankMap.get(rank).addProhibitions(currentWorld, yaml.getConfigurationSection(inherits).getStringList("prohibitiosn"));
                        
                        //Get the next inherited rank and repeat above
                        if (yaml.getConfigurationSection(inherits).contains("inherits")) inherits = yaml.getConfigurationSection(inherits).getString("inherits");
                        else break; //end loop and return to rank iteration
                    }
                }
                
            }

        }
    }
    
    /** Loads the permissions on server enable.
     * @throws java.io.FileNotFoundException
     * @throws org.json.simple.parser.ParseException **/
    public void load() throws FileNotFoundException, ParseException, IOException{
        if (loaded) return;
        reload();
        
    }
    
    public static void loadImmunity() throws IOException{
        final File immunityFile =  new File(JawaPermissions.getPlugin().getDataFolder() + "/immunity.yml");
        Yaml yaml = new Yaml(); //Create Yaml object for permission retreival
        
        BufferedReader reader;
        BufferedWriter writer;
        try {
            reader = new BufferedReader(new FileReader(immunityFile));
            JawaPermissions.immunityLevels = (HashMap<String,Integer>) yaml.load(reader);
            
            JawaPermissions.immunityLevels.keySet().stream().filter((s) -> ((JawaPermissions.immunityLevels.get(s) == 0) && (!"owner".equals(s)))).map((s) -> {
                JawaPermissions.immunityLevels.put(s, 1);
                return s;
            }).map((s) -> {
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Error in immunity levels. No rank can have immunity of 0 except owner.");
                return s;
            }).forEach((s) -> {
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + s + " set to immunity level 1.");
            });
            
            if (!JawaPermissions.immunityLevels.containsValue(0)) {
                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Either the owner rank has not been defined or was not set at immunity 0. Owner rank has been injected with immunity 0.");
                JawaPermissions.immunityLevels.put("owner", 0);
            } //Override and always set owner immunity to 0
            
            if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Immunity levels loaded as follows: " + JawaPermissions.immunityLevels);
       
            //TODO write out corrected immunity file
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PermissionsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /** Will return true if player can perform operation on target.Will return
     * false if player CANNOT perform operation on target.This is backed by immunityCheck(CommandSender commandSender, UUID target)
     * @param commandSender
     * @param target
     * @return 
     */
    public boolean immunityCheck(CommandSender commandSender, Player target) {        
        return immunityCheck(commandSender, target.getUniqueId());
    }
    
    /** Will return true if player can perform operation on target.Will return false if player CANNOT perform operation on target.
     * This is backed by onlineImmunityCheck(UUID commandSender, UUID target) and offlineImmunityCheck(UUID commandSender, UUID target)
     * @param commandSender
     * @param target
     * @return 
     */
    public boolean immunityCheck(CommandSender commandSender, UUID target){
        if (commandSender instanceof ConsoleCommandSender) return true; //This way console can perform actions on owners
        else if(commandSender instanceof BlockCommandSender) return false; //Command blocks shouldnt be able to do this
        
        if (plugin.getServer().getPlayer(target) == null) return offlineImmunityCheck(((Player) commandSender).getUniqueId(), target);
        else return onlineImmunityCheck(((Player) commandSender).getUniqueId(), target);
    }
    
    /** Will return true if player can perform operation on target. Will return false if player CANNOT perform operation on target.
     * This will only work for an ONLINE target and should only be accessed by this class.
     * @param commandSender
     * @param target
     * @return 
     */
    private boolean onlineImmunityCheck(UUID commandSender, UUID target){
        int senderImmunity = JawaPermissions.immunityLevels.get(JawaPermissions.playerRank.get(commandSender));
        int targetImmunity = JawaPermissions.immunityLevels.get(JawaPermissions.playerRank.get(target));
        return senderImmunity < targetImmunity;
    }
    
    /** Will return true if player can perform operation on target. Will return false if player CANNOT perform operation on target.
     * This will only work for an OFFLINE target and should only be accessed by this class.
     * @param commandSender
     * @param target
     * @return 
     */
    private boolean offlineImmunityCheck(UUID commandSender, UUID target) {
        //TODO create the logic to determine offline immunity checks. Right now this just allows it.
        return true;
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
        //This will deal with any null rank problems
        String playerRank = JawaPermissions.playerRank.get(player.getUniqueId());
        if (JawaPermissions.playerRank.get(player.getUniqueId()) == null) {
            System.out.println("Something went wrong when checking permission: " + perm + " Assuming guest permissions for player: " + player.getName());
            playerRank = "guest";
        }
        
        if (JawaPermissions.debug){
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Has call line 212 for player: " + player.getName());
            
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Player has rank: " + playerRank);
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Rank object is: " + JawaPermissions.rankMap.get(playerRank).getRankName());
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Permission being checked is: " + perm);
            System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Player has permission: " + JawaPermissions.rankMap.get(playerRank).hasPermission(player.getWorld().getName(), perm));
        }
        //Bukkit.getServer().getPluginManager().getDefaultPermissions(true);
        
        return JawaPermissions.rankMap.get(playerRank).hasPermission(player.getWorld().getName(), perm);
    }
       
}