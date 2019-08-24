/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.utils.ArgumentParser;
import jawamaster.jawapermissions.utils.TimeParser;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class banPlayer implements CommandExecutor {

    private String reason;
    private String playerName;
    private UUID targetUUID;
    private CommandSender sender;
    private HashMap<String, Object> banIndex;
    private HashMap<String, Object> targetPlayerData;
    private Player targetPlayer;

    //ban <-[u:o]> <player> <reason> <-[d:h:m]>
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {

        //Declare the needed variables for the assessment
        String usage = "/ban <-[o|u|f]> -p <playername> -r <reason for ban> [<-d|h|m> <integer>]";
        
        //TODO add options for silent kick or ban
        HashSet<String> acceptedFlags = new HashSet(Arrays.asList("p", "r", "h", "flags", "d", "m", "o", "u", "f", "b"));
        banIndex = new HashMap();

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
        }

        for (String key : parsedArguments.keySet()) {
            if (!acceptedFlags.contains(key)) {
                commandSender.sendMessage("Error: Unknown flag found: " + key + "Usage: " + usage);
            } else if (key.equals("flags")) {
                for (char ch : parsedArguments.get("flags").toCharArray()) {
                    if (!acceptedFlags.contains(ch)) {
                        commandSender.sendMessage("Error: Unknown flag found: " + String.valueOf(ch) + "Usage: " + usage);

                    }
                }
            }
        }

        if (parsedArguments.containsKey("flags") && parsedArguments.get("flags").contains("o")) { //Assume player is offline
            OfflinePlayer[] targetPlayer = JawaPermissions.plugin.getServer().getOfflinePlayers();
            //TODO search ES for player match
            //targetPlayerData should contain the new data
            //Search ES for name data
            //return uuid            
        } else {
            targetPlayer = JawaPermissions.plugin.getServer().getPlayer(parsedArguments.get("p"));
            if (targetPlayer == null) {
                commandSender.sendMessage("Error: Targer Player returned null! This is likely because the player is offline. Simply repeat your command with the -o flag.");
                return true;
            }
            targetUUID = targetPlayer.getUniqueId();

        }

        //TODO clean up immunity messages
        if (!JawaPermissions.permissionsHandler.immunityCheck(commandSender, targetUUID)) {
            commandSender.sendMessage("Based on rank " + parsedArguments.get("p") + " is immune to your command!");
            commandSender.sendMessage("Please escalate this ban to a higher level for approval and execution!");
            return true;
        }

        //TODO If this is a ban update adjust for updating
        if (parsedArguments.get("flags").contains("u")) {
            //get current ban information
            //create update data
            //update data in ES
            //return true;
        } else {
            //New ban
            //Create map object for ban index

            banIndex.put("reason", parsedArguments.get("r"));
            if (commandSender instanceof Player) {
                banIndex.put("banned-by", ((Player) commandSender).getUniqueId());
            } else {
                banIndex.put("via-console", true);
                //TODO allow lookup of banning admin by name so their UUID can be put in place.
                if (parsedArguments.containsKey("b")) {
                    banIndex.put("banned-by", parsedArguments.get("b"));
                } else {
                    commandSender.sendMessage("Error! When banning from console please include the -b argument with your name or UUID(prefered)!");
                    return true;
                }
            }

            //Evaluate ban times. If temp ban evaluate arguments else assume max ban time, which is LocalDateTime.MAX
            LocalDateTime banDate;
            if (parsedArguments.containsKey("d") || parsedArguments.containsKey("h") || parsedArguments.containsKey("m")) {
                banDate = LocalDateTime.now();
                if (parsedArguments.containsKey("d")) {
                    banDate.plusDays(Integer.valueOf(parsedArguments.get("d")));
                }
                if (parsedArguments.containsKey("h")) {
                    banDate.plusDays(Integer.valueOf(parsedArguments.get("h")));
                }
                if (parsedArguments.containsKey("m")) {
                    banDate.plusDays(Integer.valueOf(parsedArguments.get("m")));
                }
            } else {
                banDate = LocalDateTime.MAX;
            }
            banIndex.put("banned-until", banDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            banIndex.put("active", true);
            //ES Async put entry in ban index
            //ES Async update player index

            ESHandler.banIndexUpdate(targetUUID, banIndex);
            commandSender.sendMessage("Player ban in progress...");
            //Remove and inform player
            
            
            //Build ban string
            if (targetPlayer != null) {
                String banString = "You have been banned for: " + ((String) parsedArguments.get("r")).trim() + ".";
                if (!banDate.isEqual(LocalDateTime.MAX)) banString += " This ban will end on: " + TimeParser.getHumanReadableDateTime(parsedArguments.get("banned-until"));
                targetPlayer.kickPlayer(banString);
            }

        }

        if (commandSender instanceof Player) {
            ((Player) commandSender).sendMessage(parsedArguments.get("p") + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat to update ban.");
        } else {
            System.out.println(parsedArguments.get("p") + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat to update ban.");
        }
        return true;
    }

}
