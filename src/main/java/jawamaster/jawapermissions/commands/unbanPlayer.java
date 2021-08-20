/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.StandardMessages;
import net.md_5.bungee.api.ChatColor;
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
        
        int options = 0;
        boolean console = false;
        PlayerDataObject admin = null;
        
        if (!(commandSender instanceof Player)) {
            if (arg3[0].matches("^b:.*")) {
                options++;
                console = true;
                admin = PlayerManager.getPlayerDataObject(arg3[0].replaceFirst("b:", ""));
                //LOGGER.log(Level.INFO, arg3[0]);
                if (admin == null) { 
                    commandSender.sendMessage(ChatColor.RED + "> Error: That admin is not found! Try your actual minecraft name instead of nickname.");
                    return true;
                }
            } else {
                LOGGER.log(Level.INFO, "ERROR: You must specify the b:<your username> to unban from the console");
                return true;
            }
        } 
        
        if (commandSender instanceof Player) {
            admin = PlayerManager.getPlayerDataObject((Player) commandSender);
        }
        
        PlayerDataObject target = PlayerManager.getPlayerDataObject(arg3[options]);
        if (target == null) { 
            commandSender.sendMessage(StandardMessages.getMessage(StandardMessages.Message.PLAYERNOTFOUND));
            return true;
        }
        
        //Immunity check
        if ((commandSender instanceof Player) && PermissionsHandler.isImmune(admin.getRank(), target.getRank())){
            commandSender.sendMessage(ChatColor.RED + "> Error: That player is immune to your action! They are either higher or identical rank.");
            return true;
        }
        
        String unreason = String.join(" ", Arrays.copyOfRange(arg3, options+1, arg3.length-1));
        
        target.unbanPlayer(unreason, admin.getUniqueID(), console);
        
        
//        
//        PlayerDataObject admin;
//        //Parse the command arguments or if no arguments are sent out
//        if (arg3 == null || arg3.length == 0) {
//            commandSender.sendMessage(USAGE);
//            return true;
//        }
//
//        HashMap<String, String> parsedArguments = ArgumentParser.getArgumentValues(arg3);
//        if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[UnBanPlayer] parsedArguments: " + parsedArguments );
//        
//        // Validate that the flags are ones that this commands accepts
//        if (!ArgumentParser.validateArguments(commandSender, parsedArguments, acceptedFlags)) return true;
//        
//        admin = PlayerManager.getAdmin(commandSender, parsedArguments);
//        if (admin == null) {
//            return true;
//        }
//
//        // Validate that all the required flags are found
//        acceptedFlags.remove("b");
//        if (!ArgumentParser.validateCommandInput(commandSender, acceptedFlags, parsedArguments, USAGE)) return true;
//        
//        //Validate Player
//        PlayerDataObject target = PlayerManager.getPlayerDataObject(parsedArguments.get("p"));
//        if (target == null) { 
//            commandSender.sendMessage(ChatColor.RED + " > Error: That player wasn't found! Try their actual minecraft name instead of nickname.");
//            return true;
//        }
//        if (!target.isBanned()){
//            commandSender.sendMessage(ChatColor.RED + " > Error: That player isn't banned!");
//            return true;
//        }
//        if (target.isBanLocked()){
//            commandSender.sendMessage(ChatColor.RED + "> That player cannot be unbanned until the ban lock has been cleared.");
//            commandSender.sendMessage(ChatColor.GREEN + "> " + target.getFriendlyName() + ChatColor.GREEN + " has been ban locked by " + target.getBanLockAdmin() + ChatColor.GREEN + " for " + ChatColor.GOLD + target.getBanLockReason());
//            return true;
//        }
//        
//        target.unbanPlayer(admin, parsedArguments, LocalDateTime.now());
        
        commandSender.sendMessage(ChatColor.GREEN + "> " + target.getFriendlyName() + ChatColor.RESET + ChatColor.GREEN + " has been unbanned for: " + ChatColor.GRAY + unreason);
        
        return true;
    }
    
}
