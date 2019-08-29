/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.utils.ArgumentParser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;

/**
 *
 * @author Arthur Bulin
 */
public class unbanPlayer implements CommandExecutor {
    private String target;
    private HashMap<String, Object> playerData;

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        
        String usage = "/unban -p <playername> -r <reason for unban>";
        
        HashSet<String> acceptedFlags = new HashSet(Arrays.asList("p","r","b"));
        
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
        
        //Unban will always take place with an offline user
        try {
            //find this mofos UUID. If it fails then target=null
            target = ESHandler.findOfflinePlayer(parsedArguments.get("p")).getId();
        } catch (IOException ex) {
            Logger.getLogger(unbanPlayer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Something went wrong trying to find the offline player's UUID. Make sure the ElasticSearch database is running");
            return true;
        }
        
        if (target == null) {
            commandSender.sendMessage("Elastic Search failed to find that player's UUID! Please make sure you are using the correct username for when they were banned!");
            commandSender.sendMessage("It is also possible that multipe players were found! Be exact!");
            return true;
        }
        
        //Now try to find the existing ban information, we need this to update the ban index
        try { 
            playerData = (HashMap) ESHandler.checkBanStatus(UUID.fromString(target));
            if (playerData == null){
                commandSender.sendMessage(ChatColor.RED + "> Error! Player: " + target + " was not found in the bans or players index!!");
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(unbanPlayer.class.getName()).log(Level.SEVERE, null, ex);
            commandSender.sendMessage(ChatColor.RED + "> Error! Something went wrong pulling the player data!! Check that ElasticSearch is running!!");
            return true;
        }
        
        String unbanDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        //Assemble unban information for the ban index
        HashMap<String, Object> banData = new HashMap();
        banData.put("unreason", parsedArguments.get("r"));
        banData.put("active", false);
        
        HashMap<String, Object> playerBanData = new HashMap();
        playerBanData.put("banned", false);
        
        if (commandSender instanceof Player) {
            banData.put("unbanned-by", ((Player) commandSender).getUniqueId().toString());
            banData.put("unbanned-via-console", false);
        }
        else if (commandSender instanceof ConsoleCommandSender) {
            //Make sure they gave a valid name and get the UUID
            String commandSenderUUID;
            try {
                commandSenderUUID = ESHandler.findOfflinePlayer(parsedArguments.get("b")).getId();
            } catch (IOException ex) {
                Logger.getLogger(unbanPlayer.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Something went wrong trying to find the administrative user's UUID! Make sure the ElasticSearch database is running");
                return true;
            }
            
            if (commandSenderUUID == null) {
                commandSender.sendMessage("Elastic Search failed to find that administrative user's UUID! Please make sure you are using the correct username and not your nickname!");
                commandSender.sendMessage("It is also possible that multipe players were found! Be exact!");
                return true;
            }
            
            //Plug the uuid into the ban index
            banData.put("unbanned-via-console", true);
            banData.put("unbanned-by", commandSenderUUID);
            banData.put("unbanned-on", unbanDateTime);
        }
        HashMap<String, Object> banIndexData = new HashMap();
        banIndexData.put((String) ((HashMap<String,Object>) playerData.get("player")).get("latest-ban"), banData);
        
        //Build the bulk index request
        BulkRequest bulkRequest = new BulkRequest();
        UpdateRequest banIndexUpdate = (new UpdateRequest()).index("bans").id(target).doc(banIndexData).docAsUpsert(true);
        UpdateRequest playerUpdate = (new UpdateRequest()).index("players").id(target).doc(playerBanData).docAsUpsert(true);
        bulkRequest.add(playerUpdate);
        bulkRequest.add(banIndexUpdate);
        
        ESHandler.runAsyncBulkRequest(bulkRequest, commandSender);
        
        return true;
    }
    
}
