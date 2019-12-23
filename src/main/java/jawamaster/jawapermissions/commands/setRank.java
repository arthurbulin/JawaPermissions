/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.PlayerDataObject;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.handlers.PlayerDataHandler;
import jawamaster.jawapermissions.utils.ArgumentParser;
import org.bukkit.command.ConsoleCommandSender;

/**
 *
 * @author Arthur Bulin
 */
public class setRank implements CommandExecutor {
    private String targetUUID;
    private String adminUUID;
    private String targetRank;
    private PlayerDataObject pdObject;
    private String adminRank;

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {

        String usage = "/setrank <-[o]> -p <player> -r <rank>";
        HashMap<String, String> parsedArguments = ArgumentParser.getArgumentValues(arg3);
        HashSet<String> acceptedFlags = new HashSet(Arrays.asList("p", "r", "flags", "o", "b"));

//###############################################################################
// Validate command input
//###############################################################################

        if (!parsedArguments.containsKey("p")) {
            commandSender.sendMessage("Error: No player flag found! Usage: " + usage);
            return true;
        }
        if (!parsedArguments.containsKey("r")) {
            commandSender.sendMessage("Error: No rank flag found! Usage: " + usage);
            return true;
        }
        if (parsedArguments.containsKey("b") && (commandSender instanceof Player)) {
            commandSender.sendMessage("Error: The by(-b) flag is only used when unbanning from the console!");
            return true;
        } else if (!(commandSender instanceof Player) && !parsedArguments.containsKey("b")) {
            System.out.print("You must specify a username for yourself with the -b flag! Make sure to use your minecraft name not your nick!");
            return true;
        }

        //Assess flags
        parsedArguments.keySet().forEach((key) -> {
            if (!acceptedFlags.contains(key)) {
                commandSender.sendMessage("Error: Unknown flag found: " + key + "Usage: " + usage);
            } else if (key.equals("flags")) {
                for (char ch : parsedArguments.get("flags").toCharArray()) {
                    if (!acceptedFlags.contains(ch)) {
                        commandSender.sendMessage("Error: Unknown flag found: " + String.valueOf(ch) + "Usage: " + usage);

                    }
                }
            }
        });

//###############################################################################
//# Is the rank valid
//###############################################################################

        //Check if the rank is a valid rank
        //TODO I think there is a better way to check this
        if (!JawaPermissions.immunityLevels.containsKey(parsedArguments.get("r").toLowerCase())) {
            commandSender.sendMessage("Error! " + parsedArguments.get("r") + " is not a valid rank!");
            commandSender.sendMessage("Your choices are: " + JawaPermissions.immunityLevels.keySet());
            return true;
        }

//###############################################################################
//# Assess offline status for target
//###############################################################################
        //Check if target is offline
        Player target = JawaPermissions.plugin.getServer().getPlayer(parsedArguments.get("p"));

        //If player is online
        if (target != null) {
            targetUUID = target.getUniqueId().toString();
            targetRank = JawaPermissions.playerRank.get(target.getUniqueId());
        } else { //if player is offline
            pdObject = ESHandler.findOfflinePlayer(parsedArguments.get("p"), true);
            if ( pdObject == null) {
                return true;//Short circuit in the event the player is not found
            }
            targetRank = pdObject.getRank();
            targetUUID = pdObject.getPlayerUUID();
            //Short circuit if the user already has that rank
            if (targetRank.toLowerCase().equals(parsedArguments.get("r").toLowerCase())){
                System.out.println(" > " + parsedArguments.get("p") + " already has that rank!");
                return true;
            }
        }
        
        //If admin is in the console or not
        if (parsedArguments.containsKey("b") && (commandSender instanceof ConsoleCommandSender)){
            PlayerDataObject adminData = ESHandler.findOfflinePlayer(parsedArguments.get("b"), true);
            adminUUID = adminData.getPlayerUUID();
            adminRank = adminData.getRank();
        } else {
            adminUUID = ((Player) commandSender).getUniqueId().toString();
            adminRank = JawaPermissions.playerRank.get(UUID.fromString(adminUUID));
        }

        if (!JawaPermissions.permissionsHandler.isImmune(adminRank, targetRank)){
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage(target.getName() + "Has immunity to your specified command.");
            return true;
        }

        if (parsedArguments.get("r").toLowerCase().equals("owner")){
            commandSender.sendMessage("Owner rank cannot be set by command and must be manually entered in the ElasticSearch index.");
            return true;
        }

        if (target != null){
            ESHandler.asyncUpdateData(target, PlayerDataHandler.createPlayerRankChangeData(targetRank, parsedArguments.get("r"), adminUUID));
        } else {
            
            ESHandler.asyncUpdateData(targetUUID, PlayerDataHandler.createPlayerRankChangeData(targetRank, parsedArguments.get("r"), adminUUID));
        }

        return true;
    }


}
