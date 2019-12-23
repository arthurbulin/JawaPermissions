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
public class unbanPlayer implements CommandExecutor {
    private String target;
    private HashMap<String, Object> playerData;
    private PlayerDataObject pdObject;
    private MultiSearchRequest multiSearchRequest;
    private JSONObject topLevelBanObject;

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        
        JawaPermissions.plugin.getServer().getScheduler().scheduleSyncDelayedTask(JawaPermissions.plugin, new Runnable() {
            @Override
            public void run() {
                UnbanPlayer(commandSender, arg3);
            }
        });
        
        return true;

    }
    
    public boolean UnbanPlayer(CommandSender commandSender, String[] arg3){
        String usage = "/unban -p <playername> -r <reason for unban>";
        
        HashSet<String> acceptedFlags = new HashSet(Arrays.asList("p","r","b"));

//###############################################################################
// Validate command input
//###############################################################################        

        //Parse the command arguments or if no arguments are sent out
        if (arg3 == null) {
            return true; //TODO see what happens when the command is run without arguments
        }
        HashMap<String, String> parsedArguments = ArgumentParser.getArgumentValues(arg3);
        if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[UnBanPlayer] parsedArguments: " + parsedArguments );
        
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
        }

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
//# Assess offline status for target of unban
//###############################################################################
            //find this mofos UUID. If it fails then target=null
            target = ESHandler.findOfflinePlayer(parsedArguments.get("p")).getId();
        
        if (target == null) {
            commandSender.sendMessage("Elastic Search failed to find that player's UUID! Please make sure you are using the correct username for when they were banned!");
            commandSender.sendMessage("It is also possible that multipe players were found! Be exact!");
            return true;
        }
        
//###############################################################################
//# Get player and ban data
//###############################################################################
        multiSearchRequest = new MultiSearchRequest();
        multiSearchRequest.add(ESRequestBuilder.buildSearchRequest("players", "_id", target));

        pdObject = ESHandler.runMultiIndexSearch(multiSearchRequest, new PlayerDataObject(UUID.fromString(target)));
        
        if (!pdObject.isBanned()){
            commandSender.sendMessage("That player isn't banned! Can't unban.");
            return true;
        }
        
        String unbanDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        topLevelBanObject = PlayerDataHandler.assembleUnBanData(commandSender, parsedArguments, unbanDateTime, pdObject.getLatestBanDate());
        
        JSONObject playerBanData = new JSONObject();
        playerBanData.put("banned", false);
        
        //Build the bulk index request
        BulkRequest bulkRequest = new BulkRequest();
        
        
        UpdateRequest banIndexUpdate = ESRequestBuilder.updateRequestBuilder(topLevelBanObject, "bans", target, true);
        UpdateRequest playerUpdate = ESRequestBuilder.updateRequestBuilder(playerBanData, "players", target, true);
        bulkRequest.add(playerUpdate);
        bulkRequest.add(banIndexUpdate);
        
        ESHandler.runAsyncBulkRequest(bulkRequest, commandSender);
        
        return true;
    }
    
}
