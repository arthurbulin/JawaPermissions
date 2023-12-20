/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.StandardMessages;
import net.jawasystems.jawacore.utils.TimeParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class BanPlayer implements CommandExecutor {
    private final Logger LOGGER = Logger.getLogger("BanPlayer");
    //private HashSet<String> acceptedFlags;
    //private Player targetPlayer;
    private int reasonLength = JawaPermissions.getPlugin().getConfig().getInt("ban-reason-length", 3);
    private final String[] USAGE = {ChatColor.GREEN + "> Usage of the /ban command:",
                                    ChatColor.GREEN + " > Available Options:",
                                    ChatColor.GREEN + "  > " + ChatColor.BLUE + "/ban [options] <player name> <reason>",
                                    ChatColor.GREEN + "  > silent -"+ ChatColor.YELLOW +" this will perform a silent ban. Only staff will be notified",
                                    ChatColor.GREEN + "  > d#h#m# -"+ ChatColor.YELLOW +" time to ban for. Can be used with one or all options of d, h, and m (days, hours, minutes). Replace # with number",
                                    ChatColor.GREEN + "  > b:<your username> -"+ ChatColor.YELLOW +" Only used if banning from console",
                                    ChatColor.GREEN + " > To update a ban:",
                                    ChatColor.GREEN + "  > /ban update:<ban id> <player name> <reason>-"+ ChatColor.YELLOW +" this will update the users current ban reason",
                                    ChatColor.GREEN + "  > Example:"+ ChatColor.YELLOW +" /ban update d20h1m5 Jawamaster being a twit",
                                    ChatColor.GREEN + "> Note: Reason length must be, at a minimum, "+ ChatColor.BLUE + "THREE" + ChatColor.GREEN + " words long"};
    //private LocalDateTime banDate;

    private static final HashMap<Integer,Integer> REQUESTTOTASKMAP = new HashMap();
    private static final HashMap<Integer, Boolean> REQUESTREADY = new HashMap();
    private static final HashMap<Integer, Integer> REQUESTITERATIONCOUNT = new HashMap();
    
    private int options = 0;
    private boolean update = false;
    private String updateID = "";
    private boolean silent = false;
    private boolean console = false;
    private boolean adminCached = false;
    private boolean playerCached = false;
    private String adminName = "";
    private String playerName = "";
    private LocalDateTime banLength;
    private String reason;

    //ban <-[u:o]> <player> <reason> <-[d:h:m]>
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        //Declare the needed variables for the assessment
        //USAGE = {"/ban <-[u|s]> -p <playername> -r <reason for ban> [<-d|h|m> <integer>] [<-b> <your username>]"};
        //ban <update|silent|d#h#m#> <playername> <reason>
        //up?d?a?t?e? si?l?e?n?t? d?[0-9]+?h?[0-9]+?m?[0-9]+? 
        //TODO add options for silent kick or ban
        //acceptedFlags = new HashSet(Arrays.asList("p", "r", "h", "flags", "d", "m", "u", "b","s"));

//###############################################################################
// Validate command input
//###############################################################################
        if (reasonLength < 3) reasonLength = 3;
        
        if (arg3 == null || arg3.length == 0) {
            for (String msg : USAGE) {
                commandSender.sendMessage(msg);
            }
            return true;
        }
        
        if (arg3.length < reasonLength + 1) {
            commandSender.sendMessage(ChatColor.RED + "> Error: Invalid number of arguments. Ensure that you have at least " + ChatColor.BOLD + reasonLength + ChatColor.RESET + ChatColor.RED + " words in your reason.");
            return true;
        }
        
        banLength = LocalDateTime.of(3000, 1, 1, 0, 0);

//        HashSet<String> test = new HashSet(Arrays.asList(Arrays.copyOfRange(arg3, 0, 3)));
        
        for (String option : Arrays.copyOfRange(arg3, 0, 3)) {
            if (option.matches("^up?d?a?t?e?:.*$")) {
                options++;
                updateID = option.split(":")[1];
                update = true;
            } else if (option.matches("^si?l?e?n?t?$")){
                options++;
                silent = true;
            } else if (option.matches("([dhm][0-9]+)+")){
                options++;
                int days;
                int minutes;
                int hours;
                
                try {
                    days = Integer.valueOf(option.replaceAll("[Hh][0-9]+", "").replaceAll("[Mm][0-9]+", "").replaceAll("[Dd]", ""));
                } catch (NumberFormatException e){
                    days = 0;
                }
                try {
                    minutes = Integer.valueOf(option.replaceAll("[Dd][0-9]+", "").replaceAll("[Hh][0-9]+", "").replaceAll("[Mm]", ""));
                } catch (NumberFormatException e){
                    minutes = 0;
                }
                try {
                    hours = Integer.valueOf(option.replaceAll("[Dd][0-9]+", "").replaceAll("[Mm][0-9]+", "").replaceAll("[Hh]", ""));
                } catch (NumberFormatException e){
                    hours = 0;
                }
//                LOGGER.log(Level.INFO, "{0}:{1}:{2}", new Object[]{days, minutes, hours});
                banLength = LocalDateTime.now().plusDays(days).plusHours(hours).plusMinutes(minutes);
            } else if (option.matches("^b:.*")){
                options++;
                if (!(commandSender instanceof Player)) {
                    console = true;
                    
                    //System.out.println("option matches: " + option.replace("b:", ""));
                    adminName = option.replaceFirst("b:", "");
                    adminCached = PlayerManager.isPlayerInCache(adminName);
//                    admin = PlayerManager.getPlayerDataObject(option.replaceFirst("b:", ""));
                    
//                    if (admin == null) {
//                        commandSender.sendMessage(ChatColor.RED + "> Error: That admin is not found! Try your actual minecraft name instead of nickname.");
//                        return true;
//                    }
                } else {
                    commandSender.sendMessage(ChatColor.RED + "> Error: You cannot use the by option (b:) as a player.");
                    return true;
                }
            }
        }
        
        reason = String.join(" ", Arrays.copyOfRange(arg3, options+1, arg3.length));
        
        playerName = arg3[options];
        playerCached = PlayerManager.isPlayerInCache(playerName);
        
        //Always assume the player running the command is the admin
        if (commandSender instanceof Player) {
            adminCached = true;
        } else if (console & !adminName.isBlank()) {
            LOGGER.log(Level.INFO, "ERROR: You must specify the b:<your username> to ban from the console");
            return true;
        } 
        
        if (adminCached && playerCached && !adminName.isBlank()) {
            banPlayer(PlayerManager.getPlayerDataObject(playerName), PlayerManager.getPlayerDataObject(adminName), commandSender, banLength, reason, updateID, update, console, silent);
        } else if (adminCached && playerCached) {
            banPlayer(PlayerManager.getPlayerDataObject(playerName), PlayerManager.getPlayerDataObject((Player) commandSender), commandSender, banLength, reason, updateID, update, console, silent);
        } else {
            int adminRequest = new Random().nextInt();
            int playerRequest = new Random().nextInt();
            if (!adminCached){
                PlayerManager.requestCaching(adminName, adminRequest);
                REQUESTREADY.put(adminRequest, false);
                REQUESTITERATIONCOUNT.put(adminRequest, 0);
            } else {
                REQUESTREADY.put(adminRequest, true);
            }
            
            if (!playerCached){
                PlayerManager.requestCaching(playerName, playerRequest);
                REQUESTREADY.put(playerRequest, false);
                REQUESTITERATIONCOUNT.put(playerRequest, 0);
            } else {
                REQUESTREADY.put(playerRequest, true);
            }
            
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(JawaPermissions.getPlugin(), () -> {
                if (!REQUESTREADY.get(adminRequest)){
                    REQUESTREADY.put(adminRequest, PlayerManager.getRequestStatus(adminRequest) != null);
                    REQUESTITERATIONCOUNT.put(adminRequest, REQUESTITERATIONCOUNT.get(adminRequest)+1);
                }
                
                if (!REQUESTREADY.get(playerRequest)){
                    REQUESTREADY.put(playerRequest, PlayerManager.getRequestStatus(playerRequest) != null);
                    REQUESTITERATIONCOUNT.put(playerRequest, REQUESTITERATIONCOUNT.get(playerRequest)+1);
                }
                
                if (REQUESTREADY.get(adminRequest) && REQUESTREADY.get(playerRequest)) {

                    //did we request admin?
                    //yes - get admin
                    //is it a good admin?
                    //yes - get admin
                    //no  - return failure
                    //no - get admin
                    PlayerDataObject adminPDO;
                    //did we request admin?
                    if (!adminCached) { //yes
                        if (PlayerManager.getRequestStatus(adminRequest)) {
                            adminPDO = PlayerManager.getOfflinePlayerFromCache(adminName);
                        } else {
                            LOGGER.log(Level.WARNING, "Error: That admin does not exist.");
                            //CLEANUP
                            cleanup(adminRequest, playerRequest, adminCached, playerCached);
                            return;
                        }
                    } else {
                        if (adminName.isBlank()) {
                            adminPDO = PlayerManager.getPlayerDataObject((Player) commandSender);
                        } else {
                            adminPDO = PlayerManager.getPlayerDataObject(adminName);
                        }
                    }

                    //did we request player?
                    //yes - get player
                    //is it a good player?
                    //yes - get player
                    //no  - return failure
                    //no - get player
                    PlayerDataObject targetPDO;
                    //did we request admin?
                    if (!playerCached) { //yes
                        if (PlayerManager.getRequestStatus(playerRequest)) {
                            targetPDO = PlayerManager.getOfflinePlayerFromCache(playerName);
                        } else {
                            if (commandSender instanceof Player) {
                                commandSender.sendMessage(ChatColor.RED + "> Error: " + playerName + " was not found.");
                            } else {
                                LOGGER.log(Level.WARNING, "{0}Error: player {1} was not found.", new Object[]{ChatColor.RED, playerName});
                            }
                            //CLEANUP
                            cleanup(adminRequest, playerRequest, adminCached, playerCached);
                            return;
                        }
                    } else {
                        targetPDO = PlayerManager.getPlayerDataObject(playerName);
                    }

                    //clean up requests
                    cleanup(adminRequest, playerRequest, adminCached, playerCached);
                    //ban player
                    banPlayer(targetPDO, adminPDO, commandSender, banLength, reason, updateID, update, console, silent);
                }

                boolean canceled = false;
                if (!adminCached) {
                    if (REQUESTITERATIONCOUNT.get(adminRequest) > 27) {
                        canceled = true;
                        Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(adminRequest));
                        PlayerManager.completeRequest(adminRequest);
                    }
                }

                if (!playerCached) {
                    if (REQUESTITERATIONCOUNT.get(playerRequest) > 27) {
                        if (!canceled) {
                            Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(playerRequest));
                        }
                        PlayerManager.completeRequest(playerRequest);
                    }
                }
                
            }, 5, 2);
            
            if (!adminCached){
                REQUESTTOTASKMAP.put(adminRequest, task);
            }
            if (!playerCached){
                REQUESTTOTASKMAP.put(playerRequest, task);
            }
        }
        
        
        return true;
    }
    
    private void cleanup(int adminRequest, int playerRequest, boolean adminCached, boolean playerCached){
        boolean canceled = false;
                if (!adminCached) {
                        canceled = true;
                        Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(adminRequest));
                        PlayerManager.completeRequest(adminRequest);
                }

                if (!playerCached) {
                        if (!canceled) {
                            Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(playerRequest));
                        }
                        PlayerManager.completeRequest(playerRequest);
                }
    }
    
    private void banPlayer(PlayerDataObject target, PlayerDataObject admin, CommandSender commandSender,  LocalDateTime banLength, String reason, String updateID, boolean update, boolean console, boolean silent){
       if (target == null) {
            commandSender.sendMessage(StandardMessages.getMessage(StandardMessages.Message.PLAYERNOTFOUND));
            return;
        }

        //Deal with someone trying to ban an owner rank
        if (target.getRank().equalsIgnoreCase("owner")){
            commandSender.sendMessage(ChatColor.RED + "> Error: You cannot ban a player with owner rank! This incident will be reported.");
            for (Player ply :Bukkit.getServer().getOnlinePlayers()){
                if (ply.hasPermission("jawachat.opchat")) ply.sendMessage(ChatColor.RED + "> WARNING: " + ChatColor.GRAY + admin.getName() + " has attempted to ban a player designated as owner. Console: " + console);
            }
            LOGGER.log(Level.SEVERE, "{0} has attempted to ban a player designated as owner. Console: {1}", new Object[]{admin.getName(), console});
            return;
        }
        
        //Immunity check
        if (PermissionsHandler.isImmune(admin.getRank(), target.getRank())){
            commandSender.sendMessage(ChatColor.RED + "> Error: That player is immune to your action! They are either higher or identical rank.");
            return;
        }
        
        if (update && target.isBanIDValid(updateID)){
            target.updateBan(updateID, reason, admin.getUniqueID().toString());
            commandSender.sendMessage(ChatColor.GREEN + "> " + target.getDisplayName() + ChatColor.RESET + ChatColor.GREEN + "'s ban information has been updated.");
            LOGGER.log(Level.INFO, "{0}''s ban data has been updated by {1}", new Object[]{target.getName(), admin.getName()});
            return;
        } else if (update && !target.isBanIDValid(updateID)) {
            commandSender.sendMessage(ChatColor.RED + "> Error: The ban ID is not valid");
            return;
        } else {
            target.banPlayer(reason, admin.getUniqueID(), console, banLength);
        }

        //Build ban string 
        if (target.isOnline()) {
            String banString = "You have been banned for: " + reason + ".";
            if (!banLength.isAfter(LocalDateTime.now().plusYears(10L))) {
                banString += " This ban will end on: " + TimeParser.getHumanReadableDateTime(banLength.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            target.getPlayer().kickPlayer(banString);
        }

        //Tell who needs to be told
        for (Player ply : JawaPermissions.plugin.getServer().getOnlinePlayers()) {
            if (!silent || ply.hasPermission("jawachat.opchat")) {
                ply.sendMessage(ChatColor.RED + "[Server] " + admin.getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " has banned " + target.getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " for: " + ChatColor.RED + reason);
            }
        }

        if (commandSender instanceof Player) {
            ((Player) commandSender).sendMessage(ChatColor.GREEN + "> " + target.getDisplayName() + ChatColor.GREEN + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat with the update option and detailed reason.");
        } else {
            System.out.println(target.getDisplayName() + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat with the update option to update ban.");
        } 
    }
}
