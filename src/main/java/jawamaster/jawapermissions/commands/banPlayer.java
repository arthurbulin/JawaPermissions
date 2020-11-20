/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import jawamaster.jawapermissions.JawaPermissions;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.utils.ArgumentParser;
import net.jawasystems.jawacore.utils.TimeParser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class banPlayer implements CommandExecutor {
    private HashSet<String> acceptedFlags;
    private Player targetPlayer;
    private String usage;
    private LocalDateTime banDate;


    //ban <-[u:o]> <player> <reason> <-[d:h:m]>
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        //Declare the needed variables for the assessment
        usage = "/ban <-[u|s]> -p <playername> -r <reason for ban> [<-d|h|m> <integer>] [<-b> <your username>]";
        
        //TODO add options for silent kick or ban
        acceptedFlags = new HashSet(Arrays.asList("p", "r", "h", "flags", "d", "m", "u", "b","s"));

//###############################################################################
// Validate command input
//###############################################################################

        //Parse the command arguments or if no arguments are sent out
        if (arg3 == null) {
            return true; //TODO see what happens when the command is run without arguments
        }
        HashMap<String, String> parsedArguments = ArgumentParser.getArgumentValues(arg3);
        if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[BanPlayer] parsedArguments: " + parsedArguments );
        
        if (!parsedArguments.containsKey("p")) {
            commandSender.sendMessage("Error: No player flag found! Usage: " + usage);
            return true;
        }
        if (!parsedArguments.containsKey("r")) {
            commandSender.sendMessage("Error: No reason flag found! Usage: " + usage);
            return true;
        }
        if (parsedArguments.containsKey("b") && (commandSender instanceof Player)) {
            commandSender.sendMessage("Error: The by(-b) flag is only used when unbanning from the console!");
            return true;
        } else if (!(commandSender instanceof Player) && !parsedArguments.containsKey("b")) {
            System.out.print("You must specify a username for yourself with the -b flag! Make sure to use your minecraft name not your nick!");
            return true;
        }

        // TODO use the new argument parser validateArguments for this
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
//# Assess offline status for target of ban
//###############################################################################
        PlayerDataObject target = PlayerManager.getPlayerDataObject(parsedArguments.get("p"));
        if (target == null) { 
            commandSender.sendMessage(ChatColor.RED + " > Error: That player is not found! Try their actual minecraft name instead of nickname.");
            return true;
        }

//###############################################################################
//# Check if update
//###############################################################################        
        //TODO If this is a ban update adjust for updating
        if (parsedArguments.containsKey("flags") && parsedArguments.get("flags").contains("u")) {
            target.updateBan(parsedArguments.get("r"));
            commandSender.sendMessage(ChatColor.GREEN + "Ban data for " + target.getDisplayName() + ChatColor.GREEN + " has been updated.");
            return true;
        } else {//New ban
            //Get the current time for the ban, this will also be the ban's unique id
            banDate = LocalDateTime.now();

//###############################################################################
//# Assemble ban information
//###############################################################################   
            target.banPlayer(commandSender, parsedArguments, banDate);
            
            //Build ban string 
            if (target.isOnline()) {
                String banString = "You have been banned for: " + parsedArguments.get("r").trim() + ".";
                if (!target.isBannedUntil().equals("forever")) {
                    banString += " This ban will end on: " + TimeParser.getHumanReadableDateTime(target.isBannedUntil());
                }
                target.getPlayer().kickPlayer(banString);
            }
            if (!parsedArguments.containsKey("s")){
                JawaPermissions.plugin.getServer().broadcastMessage(ChatColor.RED + "[Server] " + target.getDisplayName() + ChatColor.GRAY + " has been banned for: " + ChatColor.RED + parsedArguments.get("r"));
            }
        }

        if (commandSender instanceof Player) {
            ((Player) commandSender).sendMessage(ChatColor.GREEN + " > " + target.getDisplayName() + ChatColor.GREEN + " has been banned. Please ensure that you have entered a detailed reason. If not please unban and repeat with the updated reason.");
        } else {
            System.out.println(target.getDisplayName() + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat with -u to update ban.");
        }
        return true;
    }
}
