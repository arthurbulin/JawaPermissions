/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.utils.ArgumentParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

/**
 *
 * @author Arthur Bulin
 */
public class setRank implements CommandExecutor {
    private String targetRank;
    private PlayerDataObject pdObject;
    private String adminRank;
    private PlayerDataObject targetData;

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {

        String usage = "/setrank -p <player> -r <rank>";
        HashMap<String, String> parsedArguments = ArgumentParser.getArgumentValues(arg3);
        HashSet<String> acceptedFlags = new HashSet(Arrays.asList("p", "r", "flags", "o", "b"));

//###############################################################################
// Validate command input
//###############################################################################

        if (!parsedArguments.containsKey("p")) {
            commandSender.sendMessage(ChatColor.RED + " > Error: No player flag found! Usage: " + usage);
            return true;
        } else {
            targetData = PlayerManager.getPlayerDataObject(parsedArguments.get("p"));
        }
        if (!parsedArguments.containsKey("r")) {
            commandSender.sendMessage(ChatColor.RED + " > Error: No rank flag found! Usage: " + usage);
            return true;
        } else {
            targetRank = parsedArguments.get("r").toLowerCase();
        }
        if (parsedArguments.containsKey("b") && (commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + " > Error: The by(-b) flag is only used when unbanning from the console!");
            return true;
        } else if (!(commandSender instanceof Player) && !parsedArguments.containsKey("b")) {
            System.out.print("You must specify a username for yourself with the -b flag! Make sure to use your minecraft name not your nick!");
            return true;
        }

        //Assess flags
        parsedArguments.keySet().forEach((key) -> {
            if (!acceptedFlags.contains(key)) {
                commandSender.sendMessage(ChatColor.RED + " > Error: Unknown flag found: " + key + "Usage: " + usage);
            } else if (key.equals("flags")) {
                for (char ch : parsedArguments.get("flags").toCharArray()) {
                    if (!acceptedFlags.contains(ch)) {
                        commandSender.sendMessage(ChatColor.RED + " > Error: Unknown flag found: " + String.valueOf(ch) + "Usage: " + usage);
                    }
                }
            }
        });

//###############################################################################
//# Is the rank valid
//###############################################################################

        //Check if the rank is a valid rank
        if (!PermissionsHandler.rankExists(targetRank)) {
            commandSender.sendMessage(ChatColor.RED + " > Error! " + targetRank + " is not a valid rank!");
            commandSender.sendMessage(ChatColor.YELLOW + " > Your choices are: " + ChatColor.WHITE + PermissionsHandler.rankList());
            return true;
        }

//###############################################################################
//# Assess offline status for target
//###############################################################################
        //Check if target is offline
         
        if (targetData.getRank().toLowerCase().equals(targetRank)) {
            commandSender.sendMessage(ChatColor.RED + " > "  + parsedArguments.get("p") + " already has that rank!");
            return true;
        }       
        
        PlayerDataObject adminData;
        //If admin is in the console or not
        if (parsedArguments.containsKey("b") && (commandSender instanceof ConsoleCommandSender)){
            adminData = PlayerManager.getPlayerDataObject(parsedArguments.get("b"));
        } else {
            adminData = PlayerManager.getPlayerDataObject((Player) commandSender);
        }

        if (!PermissionsHandler.isImmune(adminData.getRank(), targetRank)){
            commandSender.sendMessage(ChatColor.RED + " > " + ChatColor.BLUE + targetData.getName() + ChatColor.RED + "Has immunity to your specified command.");
            return true;
        }

        if (targetRank.equals("owner")){
            commandSender.sendMessage(ChatColor.RED + " > Owner rank cannot be set by command and must be manually entered in the ElasticSearch index.");
            return true;
        }

        targetData.setRank(targetRank, adminData.getUniqueID(), PermissionsHandler.getRankColor(targetRank.toLowerCase()));
        
        commandSender.sendMessage(ChatColor.GREEN + " > " + targetData.getFriendlyName() + ChatColor.GREEN + "'s rank has been changed to " + PermissionsHandler.getRankColor(targetRank.toLowerCase()) + targetRank );

        if (targetData.isOnline()){
            targetData.getPlayer().sendMessage(ChatColor.GREEN + " > Your rank has been changed to " + PermissionsHandler.getRankColor(targetRank.toLowerCase()) + targetRank);
            //Hopefully this will allow the clients to see new commands
            targetData.getPlayer().updateCommands(); 
        }
        return true;
    }


}
