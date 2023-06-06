/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.StandardMessages;
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
public class unbanPlayer implements CommandExecutor {
    private final Logger LOGGER = Logger.getLogger("UnbanPlayer");
    public final String[] USAGE = {
        ChatColor.GREEN + "> Usage of the /unban command:",
        ChatColor.GREEN + " > " + ChatColor.BLUE + "/unban [b:<your username>] <playername> <reason for unban>",
        ChatColor.GREEN + "  > b:<your username> -"+ ChatColor.YELLOW +" Only used if unbanning from console",
        ChatColor.GREEN + " > Example:"+ ChatColor.YELLOW +" /unban Jawamaster No longer being a twit",
        ChatColor.GREEN + "> Note: Reason length must be, at a minimum, "+ ChatColor.BLUE + "THREE" + ChatColor.GREEN + " words long"};
    //public HashSet<String> acceptedFlags = new HashSet(Arrays.asList("p","r","b"));
    private int reasonLength = JawaPermissions.getPlugin().getConfig().getInt("unban-reason-length", 3);
    
    private static final HashMap<Integer, Integer> ADMINREQUESTTOTASK = new HashMap();
    private static final HashMap<Integer, Integer> PLAYERREQUESTTOTASK = new HashMap();
    private static final HashMap<Integer, Boolean> REQUESTREADY = new HashMap();
    private static final HashMap<Integer, Integer> REQUESTITERATIONCOUNT = new HashMap();

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
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
        
        Integer options = 0;
        boolean adminCached;
        boolean playerCached;
        String adminName;
        String playerName;
        
        if (commandSender instanceof Player) {
            adminCached = true;
            playerCached = PlayerManager.isPlayerInCache(arg3[0]);
            adminName = ((Player) commandSender).getName();
            playerName = arg3[0];
        } else {
            if (arg3[0].matches("^b:.*")) {
                options = 1;
                adminCached = PlayerManager.isPlayerInCache(arg3[0].replaceFirst("b:", ""));
                playerCached = PlayerManager.isPlayerInCache(arg3[1]);
                
                adminName = arg3[0].replaceFirst("b:", "");
                playerName = arg3[1];
            } else {
                LOGGER.log(Level.INFO, "ERROR: You must specify the b:<your username> to unban from the console");
                return true;
            }
            //returnAdminDataObject(arg3, commandSender);
        } 
                
        if (adminCached && playerCached){
            unbanPlayer(arg3, commandSender, PlayerManager.getPlayerDataObject(adminName), PlayerManager.getPlayerDataObject(playerName), options, options > 0);
        } else {
            Random rand = new Random();
            
            int adminRequest = rand.nextInt();
            if (!adminCached){
                PlayerManager.requestCaching(adminName, adminRequest);
                REQUESTREADY.put(adminRequest, false);
                REQUESTITERATIONCOUNT.put(adminRequest, 0);
            }
            
            int playerRequest = rand.nextInt();
            if (!playerCached){
                PlayerManager.requestCaching(playerName, playerRequest);
                REQUESTREADY.put(playerRequest, false);
                REQUESTITERATIONCOUNT.put(playerRequest, 0);
            }
                        
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(JawaPermissions.getPlugin(), () -> {
                //Check if admin request is ready, if false get the status. If the admin is already cached this will not check
                if (!REQUESTREADY.get(adminRequest)){
                    REQUESTREADY.put(adminRequest, PlayerManager.getRequestStatus(adminRequest) != null);
                    REQUESTITERATIONCOUNT.put(adminRequest, REQUESTITERATIONCOUNT.get(adminRequest)+1);
                }
                
                //Check if the player request is ready, if false get the status. If the player is already cached this will not check
                if (!REQUESTREADY.get(playerRequest)){
                    REQUESTREADY.put(playerRequest, PlayerManager.getRequestStatus(adminRequest) != null);
                    REQUESTITERATIONCOUNT.put(playerRequest, REQUESTITERATIONCOUNT.get(playerRequest)+1);
                }
                
                //If both requests are ready start processing the results
                if (REQUESTREADY.get(playerRequest) && REQUESTREADY.get(adminRequest)){
                    
                    if (!adminCached && !PlayerManager.getRequestStatus(adminRequest)){
                        LOGGER.log(Level.INFO, "Error: That admin is not found! Try your actual minecraft name instead of nickname.");
                    } else if (!PlayerManager.getRequestStatus(playerRequest)){
                        if (!adminCached){
                            LOGGER.log(Level.INFO, "Error: That admin is not found! Try your actual minecraft name instead of nickname.");
                        } else {
                            commandSender.sendMessage(StandardMessages.getMessage(StandardMessages.Message.PLAYERNOTFOUND));
                        }
                    } else {

                        //If the admin was not cached to start with the admin must be offline which forces b: and console=true
                        if (!adminCached) {
                            unbanPlayer(arg3, commandSender, PlayerManager.getPlayerDataObject(adminName), PlayerManager.getPlayerDataObject(playerName), 1, true);
                        } else {
                            unbanPlayer(arg3, commandSender, PlayerManager.getPlayerDataObject(adminName), PlayerManager.getPlayerDataObject(playerName), 0, false);
                        }
                    }
                    
                    boolean canceled = false;
                    //The admin was not cached so we need to cancel the task and mark it complete
                    if (!adminCached) {
                        PlayerManager.completeRequest(adminRequest);
                        Bukkit.getServer().getScheduler().cancelTask(ADMINREQUESTTOTASK.get(adminRequest));
                        ADMINREQUESTTOTASK.remove(adminRequest);
                        REQUESTREADY.remove(adminRequest);
                        canceled = true;
                    }
                    
                    //The admin was not cached so we need to cancel the task and mark it complete
                    if (!playerCached){
                        PlayerManager.completeRequest(playerRequest);
                        if (!canceled){
                            Bukkit.getServer().getScheduler().cancelTask(PLAYERREQUESTTOTASK.get(playerRequest));
                        }
                        PLAYERREQUESTTOTASK.remove(playerRequest);
                        REQUESTREADY.remove(playerRequest);
                    }
                } 
                //If the event has not resolved by now cancel it
                else if ((REQUESTITERATIONCOUNT.containsKey(adminRequest) && REQUESTITERATIONCOUNT.get(adminRequest) > 27) || ( REQUESTITERATIONCOUNT.containsKey(playerRequest) && REQUESTITERATIONCOUNT.get(playerRequest) > 27)){
                    boolean canceled = false;
                    //The admin was not cached so we need to cancel the task and mark it complete
                    if (!adminCached) {
                        PlayerManager.completeRequest(adminRequest);
                        Bukkit.getServer().getScheduler().cancelTask(ADMINREQUESTTOTASK.get(adminRequest));
                        ADMINREQUESTTOTASK.remove(adminRequest);
                        REQUESTREADY.remove(adminRequest);
                        canceled = true;
                    }
                    
                    //The admin was not cached so we need to cancel the task and mark it complete
                    if (!playerCached){
                        PlayerManager.completeRequest(playerRequest);
                        if (!canceled){
                            Bukkit.getServer().getScheduler().cancelTask(PLAYERREQUESTTOTASK.get(playerRequest));
                        }
                        PLAYERREQUESTTOTASK.remove(playerRequest);
                        REQUESTREADY.remove(playerRequest);
                    }
                    
                    if (!adminCached){
                        LOGGER.log(Level.INFO, "Error: The database did not respond to the request. Please contact your server administrator.");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "> Error: The database did not respond to the request. Please contact your server administrator.");
                    }
                }
                
            }, 5, 2);
             
            if (!adminCached){
                ADMINREQUESTTOTASK.put(adminRequest, task);
            }
            
            if (!playerCached){
                PLAYERREQUESTTOTASK.put(playerRequest, task);
            }
        }
        
        
        
        return true;
    }
    
//    private void returnAdminDataObject(String[] arg3, CommandSender commandSender, PlayerDataObject adminPDO){
//        if (arg3[0].matches("^b:.*")) {
//                int options = 1;
//                boolean console = true;
////                PlayerDataObject admin = PlayerManager.getPlayerDataObject(arg3[0].replaceFirst("b:", ""));
//                //LOGGER.log(Level.INFO, arg3[0]);
//                if (adminPDO == null) { 
//                    LOGGER.log(Level.INFO, "Error: That admin is not found! Try your actual minecraft name instead of nickname.");
//                } else {
//                    
//                }
//            } else {
//                LOGGER.log(Level.INFO, "ERROR: You must specify the b:<your username> to unban from the console");
//            }
//    }
    
//    private void returnPlayerDataObject(String[] arg3, int options, CommandSender commandSender,PlayerDataObject adminPDO){
//        PlayerDataObject target = PlayerManager.getPlayerDataObject(arg3[options]);
//        if (target == null) { 
//            commandSender.sendMessage(StandardMessages.getMessage(StandardMessages.Message.PLAYERNOTFOUND));
//            return;
//        }
//        
//        
//    }
    
    private void unbanPlayer(String[] arg3, CommandSender commandSender, PlayerDataObject adminPDO, PlayerDataObject targetPDO, int options, boolean console){
        //Immunity check
        if ((commandSender instanceof Player) && PermissionsHandler.isImmune(adminPDO.getRank(), targetPDO.getRank())){
            commandSender.sendMessage(ChatColor.RED + "> Error: That player is immune to your action! They are either higher or identical rank.");
            return;
        }
        
        String unreason = String.join(" ", Arrays.copyOfRange(arg3, options+1, arg3.length));
        
        targetPDO.unbanPlayer(unreason, adminPDO.getUniqueID(), console);
        
        commandSender.sendMessage(ChatColor.GREEN + "> " + targetPDO.getFriendlyName() + ChatColor.RESET + ChatColor.GREEN + " has been unbanned for: " + ChatColor.GRAY + unreason);
    }
}
