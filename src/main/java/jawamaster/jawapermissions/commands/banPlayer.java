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
import jawamaster.jawapermissions.PlayerDataObject;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.handlers.PlayerDataHandler;
import jawamaster.jawapermissions.utils.ArgumentParser;
import jawamaster.jawapermissions.utils.ESRequestBuilder;
import jawamaster.jawapermissions.utils.TimeParser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.json.JSONObject;

/**
 *
 * @author Arthur Bulin
 */
public class banPlayer implements CommandExecutor {

    private UUID targetUUID;
    private JSONObject playerData;
    private HashSet<String> acceptedFlags;
    private Player targetPlayer;
    private String usage;
    
    private BulkRequest bulkRequest;
    private UpdateRequest playerIndexRequest;
    private UpdateRequest banIndexRequest;
    private LocalDateTime banDate;


    //ban <-[u:o]> <player> <reason> <-[d:h:m]>
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {

        JawaPermissions.plugin.getServer().getScheduler().scheduleSyncDelayedTask(JawaPermissions.plugin, new Runnable() {
            @Override
            public void run() {
                banPlayer(commandSender, arg1, arg2, arg3);
            }
        }
                
                );

        return true;
    }

    
    private boolean banPlayer(CommandSender commandSender, Command arg1, String arg2, String[] arg3){
                //Declare the needed variables for the assessment
        usage = "/ban <-[u|s]> -p <playername> -r <reason for ban> [<-d|h|m> <integer>] [<-b> <your username>]";
        
        //TODO add options for silent kick or ban
        acceptedFlags = new HashSet(Arrays.asList("p", "r", "h", "flags", "d", "m", "o", "u", "b","s"));

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

        if (parsedArguments.containsKey("flags") && parsedArguments.get("flags").contains("o")) {
                //Assume player is offline and get UUID
                targetUUID = UUID.fromString(ESHandler.findOfflinePlayer(parsedArguments.get("p")).getId());

            
            PlayerDataObject pdObject = new PlayerDataObject(targetUUID);
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest(); 
            ESRequestBuilder.addToMultiSearchRequest(multiSearchRequest, "players", "_id", targetUUID.toString());
            pdObject = ESHandler.runMultiIndexSearch(multiSearchRequest, pdObject);
            
            if (!JawaPermissions.permissionsHandler.offlineImmunityCheck(commandSender, targetUUID,pdObject.getRank())) {
                    commandSender.sendMessage("Based on rank " + parsedArguments.get("p") + " is immune to your command!");
                    commandSender.sendMessage("Please escalate this ban to a higher level for approval and execution!");
                    return true;
            }
            
        } else {
            targetPlayer = JawaPermissions.plugin.getServer().getPlayer(parsedArguments.get("p"));
            targetUUID = targetPlayer.getUniqueId();
            if (targetPlayer == null) {
                commandSender.sendMessage("Error: Targer Player returned null! This is likely because the player is offline. Simply repeat your command with the -o flag.");
                return true;
            }
            if (!JawaPermissions.permissionsHandler.immunityCheck(commandSender, targetUUID)) {
                commandSender.sendMessage("Based on rank " + parsedArguments.get("p") + " is immune to your command!");
                commandSender.sendMessage("Please escalate this ban to a higher level for approval and execution!");
                return true;
            }
            

        }

//###############################################################################
//# Check if update
//###############################################################################        
        //TODO If this is a ban update adjust for updating
        if (parsedArguments.containsKey("flags") && parsedArguments.get("flags").contains("u")) {
            //get current ban information
            //create update data
            //update data in ES
            //return true;
        } else {//New ban
            //Get the current time for the ban, this will also be the ban's unique id
            banDate = LocalDateTime.now();

//###############################################################################
//# Assemble ban information
//###############################################################################   
            
            JSONObject banIndexTopLevel = PlayerDataHandler.assembleBanData(commandSender, parsedArguments, banDate);
            if (banIndexTopLevel == null) { //to accomidate for failures on the other side.
                commandSender.sendMessage("Ban Command Failed.");
                return true;
            }
            
            //Assemble the playerData ban
            playerData = new JSONObject();
            playerData.put("banned", true);
            playerData.put("latest-ban", banDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        
            //Assemble bulk request and update requests
            bulkRequest = new BulkRequest();
            System.out.println("playerData: " + playerData);
            System.out.println("targetUUID: "+ targetUUID.toString());
            playerIndexRequest = ESRequestBuilder.updateRequestBuilder(playerData, "players", targetUUID.toString(), true);
            banIndexRequest = ESRequestBuilder.updateRequestBuilder(banIndexTopLevel, "bans", targetUUID.toString(), true);
            
            bulkRequest.add(banIndexRequest);
            bulkRequest.add(playerIndexRequest);
            
            ESHandler.runAsyncBulkRequest(bulkRequest, commandSender);
            
            //Build ban string 
            if (targetPlayer != null) {
                String banString = "You have been banned for: " + parsedArguments.get("r").trim() + ".";
                if (!PlayerDataHandler.assessBanTime(parsedArguments, banDate).equals("forever")) banString += " This ban will end on: " + TimeParser.getHumanReadableDateTime(parsedArguments.get("banned-until"));
                targetPlayer.kickPlayer(banString);

            }
            
            if (!parsedArguments.containsKey("s")){
                JawaPermissions.plugin.getServer().broadcastMessage(ChatColor.DARK_RED + "[Server] " + parsedArguments.get("p") + " has been banned for " + parsedArguments.get("r"));
            }

        }

        if (commandSender instanceof Player) {
            ((Player) commandSender).sendMessage(parsedArguments.get("p") + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat with -u to update ban.");
        } else {
            System.out.println(parsedArguments.get("p") + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat with -u to update ban.");
        }
        return true;
    }
}
