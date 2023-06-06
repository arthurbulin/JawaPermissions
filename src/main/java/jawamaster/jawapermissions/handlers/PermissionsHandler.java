/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package jawamaster.jawapermissions.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.Rank;
import net.jawasystems.jawacore.PlayerManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class PermissionsHandler {

    private static final Logger LOGGER = Logger.getLogger("JawaPermissions][PermissionsHandler");
    //New rank object method
    private static final Map<String, Rank> RANKMAP = new HashMap();

    /** Loads the permissions.yml files for each world from the JawaPermissions/permissions
     * folder and generates the needed rank objects. If the permissions files do not exists
     * this will save the default file. 
     */
    public static void reload() {
        //load the player immunity. This will also be used to check for valid ranks
//        loadImmunity();

        //Get the locations for the permissions files
        final File permFiles = new File(JawaPermissions.getPlugin().getDataFolder() + "/permissions");

        //Generate directories if they don't exist
        permFiles.mkdirs();

        //File list of permissions files
        File[] fileList = permFiles.listFiles();
        if (fileList.length == 0) JawaPermissions.getPlugin().saveResource("permissions/world.yml", false);
        if (JawaPermissions.debug) {
            LOGGER.log(Level.INFO, "Permissions are being checked in: {0}", permFiles);
        }

        // TODO FIXME build in case insensativity
        //Iterate over filelist
        for (File f : fileList) {
            //Yaml yaml = new Yaml(); //Create Yaml object for permission retreival
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(f);
            String currentWorld = f.getName();

            if (currentWorld.indexOf('.') > 0) { //extract name of world from file name
                currentWorld = currentWorld.substring(0, currentWorld.indexOf('.'));
            }

            if (JawaPermissions.debug) {
                LOGGER.log(Level.INFO, "Loading permissions from file: {0}", f.getName());
                LOGGER.log(Level.INFO, "Current world name identified as: {0}", currentWorld);
            }

            //System.out.println(yaml.getKeys(true));
            // System.out.println(yaml.getKeys(false));
            for (String rank : yaml.getKeys(false)) {

                if (!RANKMAP.containsKey(rank)) {
                    //Create a new rank object and commit permissions to it
                    
                    int immunityDefault = 100;
                    if (rank.equalsIgnoreCase("owner")) {
                        immunityDefault = 0;
                    }//override and set the owner rank to 0 no matter what
                        RANKMAP.put(rank, new Rank(
                                rank, 
                                yaml.getConfigurationSection(rank).getInt("immunity", immunityDefault), 
                                yaml.getConfigurationSection(rank).getString("color", "f"), 
                                yaml.getConfigurationSection(rank).getString("description", "No Description has been provided for this rank"),
                                yaml.getConfigurationSection(rank).getString("requirements", "No Requirements have been provided for this rank")));
                }

                RANKMAP.get(rank).addWorld(currentWorld, yaml.getConfigurationSection(rank).getStringList("permissions"), yaml.getConfigurationSection(rank).getStringList("prohibitions"));

                String inherits;
                if (yaml.getConfigurationSection(rank).contains("inherits")) { //If rank has inheritence

                    inherits = yaml.getConfigurationSection(rank).getString("inherits"); //Get inherited rank

                    while (true) { //iterate over until we hit a rank that doesnt have inherited perms
                        //Add permissions list from inherited rank
                        //System.out.println(JawaPermissions.pluginSlug + handlerSlug + " Inherit iteration for rank: " + rank + ". Rank is inheriting: " + inherits);
                        RANKMAP.get(rank).addPermissions(currentWorld, yaml.getConfigurationSection(inherits).getStringList("permissions"));
                        //Add prohibitions list from inherited rank
                        RANKMAP.get(rank).addProhibitions(currentWorld, yaml.getConfigurationSection(inherits).getStringList("prohibitions"));

                        //Get the next inherited rank and repeat above
                        if (yaml.getConfigurationSection(inherits).contains("inherits")) {
                            inherits = yaml.getConfigurationSection(inherits).getString("inherits");
                        } else {
                            break; //end loop and return to rank iteration
                        }
                    }
                }

            }

        }
    }

    /** Calls the reload() method at the time of enable.
     */
    public static void load() {
        reload();

    }

//    public static void loadImmunity() throws IOException {
//        final File immunityFile = new File(JawaPermissions.getPlugin().getDataFolder() + "/immunity.yml");
//        Yaml yaml = new Yaml(); //Create Yaml object for permission retreival
//
//        BufferedReader reader;
//        BufferedWriter writer;
//        try {
//            reader = new BufferedReader(new FileReader(immunityFile));
//            JawaPermissions.immunityLevels = (HashMap<String, Integer>) yaml.load(reader);
//
//            JawaPermissions.immunityLevels.keySet().stream().filter((s) -> ((JawaPermissions.immunityLevels.get(s) == 0) && (!"owner".equals(s)))).map((s) -> {
//                JawaPermissions.immunityLevels.put(s, 1);
//                return s;
//            }).map((s) -> {
//                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Error in immunity levels. No rank can have immunity of 0 except owner.");
//                return s;
//            }).forEach((s) -> {
//                System.out.println(JawaPermissions.pluginSlug + handlerSlug + s + " set to immunity level 1.");
//            });
//
//            if (!JawaPermissions.immunityLevels.containsValue(0)) {
//                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Either the owner rank has not been defined or was not set at immunity 0. Owner rank has been injected with immunity 0.");
//                JawaPermissions.immunityLevels.put("owner", 0);
//            } //Override and always set owner immunity to 0
//
//            if (JawaPermissions.debug) {
//                System.out.println(JawaPermissions.pluginSlug + handlerSlug + "Immunity levels loaded as follows: " + JawaPermissions.immunityLevels);
//            }
//
//            //TODO write out corrected immunity file
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(PermissionsHandler.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    
    /**
     * Will return true if player can perform operation on target. Will return
     * false if player CANNOT perform operation on target.This is backed by
     * immunityCheck(CommandSender commandSender, UUID target)
     *
     * @param commandSender
     * @param target
     * @return
     */
    public static boolean immunityCheck(CommandSender commandSender, Player target) {
        return immunityCheck(commandSender, target.getUniqueId());
    }

    /**
     * Will return true if player can perform operation on target.Will return
     * false if player CANNOT perform operation on target. This is backed by
     * onlineImmunityCheck(UUID commandSender, UUID target) and
     * offlineImmunityCheck(UUID commandSender, UUID target)
     *
     * @param commandSender
     * @param target
     * @return
     */
    public static boolean immunityCheck(CommandSender commandSender, UUID target) {
        if (commandSender instanceof ConsoleCommandSender) {
            return true; //This way console can perform actions on owners
        } else if (commandSender instanceof BlockCommandSender) {
            return false; //Command blocks shouldnt be able to do this
        } //if (plugin.getServer().getPlayer(target) == null) return offlineImmunityCheck(((Player) commandSender).getUniqueId(), target);
        else {
            return onlineImmunityCheck(((Player) commandSender).getUniqueId(), target);
        }
    }

    /**
     * Will return true if player can perform operation on target. Will return
     * false if player CANNOT perform operation on target. This will only work
     * for an ONLINE target and should only be accessed by this class.
     *
     * @param commandSender
     * @param target
     * @return
     */
    private static boolean onlineImmunityCheck(UUID commandSender, UUID target) {

        //int senderImmunity = JawaPermissions.immunityLevels.get(PlayerManager.getPlayerDataObject(commandSender).getRank());
        int senderImmunity = RANKMAP.get(PlayerManager.getPlayerDataObject(commandSender).getRank()).getImmunity();
        //System.out.println("immunity check: " + JawaPermissions.playerRank.get(target));
        //int targetImmunity = JawaPermissions.immunityLevels.get(PlayerManager.getPlayerDataObject(target).getRank());
        int targetImmunity = RANKMAP.get(PlayerManager.getPlayerDataObject(target).getRank()).getImmunity();
        return senderImmunity < targetImmunity;
    }

    public static boolean offlineImmunityCheck(CommandSender commandSender, UUID target, String targetRank) {
        return offlineImmunityCheck(((Player) commandSender).getUniqueId(), targetRank);
    }

    /**
     * Will return true if player can perform operation on target. Will return
     * false if player CANNOT perform operation on target. This will only work
     * for an OFFLINE target and should only be accessed by this class.
     *
     * @param commandSender
     * @param target
     * @return
     */
    private static boolean offlineImmunityCheck(UUID commandSender, String targetRank) {
        int senderImmunity = RANKMAP.get(PlayerManager.getPlayerDataObject(commandSender).getRank()).getImmunity();
        int targetImmunity = RANKMAP.get(targetRank).getImmunity();
        return senderImmunity < targetImmunity;
    }

    /** Returns true if target is immune to adminRank's action. Will return false if
     * the target is NOT immune. This will examine the immunity that has been set in 
     * the permissions yml file. Lower immunity is more powerful up to zero. If the
     * immunity is less than it is true. Greater or equal returns false.
     * @param adminRank
     * @param targetRank
     * @return
     */
    public static boolean isImmune(String adminRank, String targetRank) {
        if (JawaPermissions.debug) 
            LOGGER.log(Level.INFO, "isImmune({0},{1}) {2},{3}", new Object[]{adminRank, targetRank, RANKMAP.get(adminRank.toLowerCase()).getImmunity(), RANKMAP.get(targetRank.toLowerCase()).getImmunity()});
        return RANKMAP.get(adminRank.toLowerCase()).getImmunity() >= RANKMAP.get(targetRank.toLowerCase()).getImmunity();
    }
    
    /** Get the immunity integer for rank.
     * @param rank The rank to get immunity for
     * @return Integer for the rank's immunity.
     */
    public static int getImmunity(String rank){
        return RANKMAP.get(rank).getImmunity();
    }

    /**
     * Redirects permissions check to the proper has method based on the
     * instanceof the commandSender. This returns false for any commandSender
     * that is not a Player, ConsoleCommandSender, or BlockCommandSender.
     *
     * @param commandSender
     * @param perm
     * @return
     */
    public static boolean has(CommandSender commandSender, String perm) {
        if (commandSender instanceof ConsoleCommandSender) {
            return true; //Console has all power
        } else if (commandSender instanceof Player) {
            return has((Player) commandSender, perm); //Resolve permissions for Players
        } else if (commandSender instanceof BlockCommandSender) {
            return has((BlockCommandSender) commandSender, perm); //Resolver permissions for Command Blocks
        } else {
            return false;
        }
    }

    /**
     * Resolves permissions for BlockCommandSenders. NOT COMPLETE.
     *
     * @param commandSender
     * @param perm
     * @return
     */
    public static boolean has(BlockCommandSender commandSender, String perm) {
        // TODO settup command block permissions
        return true;
    }

    /**
     * Checks to see if a player has a specific permission. This is the
     * end-track reference for all Player permission checking. All other has
     * methods or hasPermission methods will divert here to check permissions
     * for players.
     *
     * @param player.
     *
     * @param perm
     * @return
     */
    public static boolean has(Player player, String perm) {
        if (JawaPermissions.debug) System.out.println("Player class:" +player.getClass().getName());
        //This will deal with any null rank problems
        String playerRank = PlayerManager.getPlayerDataObject(player).getRank();
        //System.out.print(playerRank);
        if (playerRank == null) {
            LOGGER.log(Level.SEVERE, "Something went wrong when checking permission: {0} Assuming guest permissions for player: {1}", new Object[]{perm, player.getName()});
            playerRank = "guest";
        }

        boolean has = RANKMAP.get(playerRank).hasPermission(player.getWorld().getName(), perm);

        if (JawaPermissions.debug) {
            LOGGER.log(Level.INFO, "Has call for player: {0} Rank: {1} RankObject: {2} Perm: {3}", new Object[]{player.getName(), RANKMAP.get(playerRank).getRankName(), perm, has});
        }
        //Bukkit.getServer().getPluginManager().getDefaultPermissions(true);

        return has;
    }
    
    /**
     * Returns true if a rank exists.Returns false if a rank does not exist.This is case sensitive.
     * @param rank 
     * @return  
     */
    public static boolean rankExists(String rank) {
        if (RANKMAP.containsKey(rank)) {
            return true;
        } else {
            return false;
        }
    }
    
    /** Returns a list of the ranks that can be set.
     * @return 
     */
    public static Set rankList(){
        return RANKMAP.keySet();
    }
    
    public static List<String> getRankList(){
        return new ArrayList(RANKMAP.keySet());
    }
    
    /** Returns the ChatColor for the specified rank
     * @param rank
     * @return 
     */
    public static ChatColor getRankColor(String rank){
        if (!rankExists(rank)){
            return ChatColor.WHITE;
        } else {
            return RANKMAP.get(rank.toLowerCase()).getChatColor();
        }
    }
    
    /** Returns a string for the rank's description. This returns a generic message
     * aboud no info if a rank doesn not exist. This casts rank to all lowercase.
     * @param rank
     * @return 
     */
    public static String getRankDescription(String rank) {
        return RANKMAP.get(rank.toLowerCase()).getDescription();
    }
    
    /** Returns a string for the rank's requirement. This returns a generic message
     * aboud no info if a rank doesn not exist. This casts rank to all lowercase.
     * @param rank
     * @return 
     */
    public static String getRankRequirements(String rank) {
        return RANKMAP.get(rank.toLowerCase()).getRequirements();
    }
    
    /** Sends a rank description message to a player.
     * @param commandSender
     * @param rank 
     */
    public static void sendDescription(CommandSender commandSender, String rank) {
        commandSender.sendMessage(ChatColor.GREEN + "> Rank description for " + PermissionsHandler.getRankColor(rank) + rank.toLowerCase());
        commandSender.sendMessage(ChatColor.GREEN + ">" + ChatColor.translateAlternateColorCodes('&', PermissionsHandler.getRankDescription(rank)));
    }
    
    /** Sends a rank requirements message to a player.
     * @param commandSender
     * @param rank 
     */
    public static void sendRequirements(CommandSender commandSender, String rank) {
        commandSender.sendMessage(ChatColor.GREEN + "> Rank requirements for " + PermissionsHandler.getRankColor(rank) + rank.toLowerCase());
        commandSender.sendMessage(ChatColor.GREEN + ">" + ChatColor.translateAlternateColorCodes('&', PermissionsHandler.getRankRequirements(rank)));
    }
}
