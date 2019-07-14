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
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.handlers.PlayerDataHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class banPlayer implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        //Check argument length
        if (arg3.length < 2) {
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage("Missing arguments! /ban <player> <reason> [time]");
            else System.out.println("Missing arguments! /ban <player> <reason> [time]");
            return true;
        }
        
        //Declare variables
        String name = arg3[0];
        Player target;
        UUID uuid;
        OfflinePlayer targetOP;
        
        targetOP = JawaPermissions.plugin.getServer().getOfflinePlayer(name);
        target = targetOP.getPlayer();
        
        //Check if the player is a valid player
        if (target == null) {
            if (targetOP == null) {
                if (commandSender instanceof Player) ((Player) commandSender).sendMessage(arg3[0] + " is is not a valid player.");
                else System.out.println(arg3[0] + " is is not a valid player.");
                return true;
            }
            else uuid = targetOP.getUniqueId();
        } else uuid = target.getUniqueId();
        
        //TODO perform immunity check
        
        //Extract the reason from the command
        int i = 0;
        int a = 0, b = 0;
        while ( i < arg3.length) {
            if (arg3[i].startsWith("\"")) a = i;
            else if (arg3[i].endsWith("\"")) b = i+1;
            i++;
        }
        
        if (a == 0 || b == 0) {
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage("You have a malformed reason please enclose the reason in \"double quotes\"");
            else System.out.println("You have a malformed reason please enclose the reason in \"double quotes\"");
            return true;
        }
        
        String[] reason = Arrays.copyOfRange(arg3, a, b);
        String reasonString = "";
        for (String s: reason) reasonString = reasonString + s + " ";

        //Extract the time
        String time;
        if (arg3.length == 3) time = arg3[2];
        else time = "forever";     
        
        //Perform ban functions
        try {
            Map<String, Object> banData = ESHandler.checkBanStatus(uuid);
            
//            //If no existing ban data create new ban data for the player, create the ban, kick the player if they are online
//            if (banData == null) {
//                Map<String, Object> newBanData = PlayerDataHandler.assembleBanData(commandSender, name, reasonString, time);
//                ESHandler.createBan(commandSender, uuid, banData);
//                if (target != null) { //If the player is online
//                    target.kickPlayer("You have been banned for: " + ((String) newBanData.get("reason")).trim() + ". This ban will end on: " + ((LocalDateTime) newBanData.get("end-of-ban")).format(DateTimeFormatter.ISO_DATE_TIME));
//                }
//            } 
//            //If existing ban data perform an update
//            else {
                //Perform ban update with new data
                Map<String, Object> updateData = ESHandler.updateBan(commandSender, uuid, reasonString, time);
                //If player is online kick the player
                if (target != null) target.kickPlayer("You have been banned for: " + ((String) updateData.get("reason")).trim() + ". This ban will end on: " + ((LocalDateTime) updateData.get("end-of-ban")).format(DateTimeFormatter.ISO_DATE_TIME));
            
        } catch (IOException ex) {
            System.out.println(JawaPermissions.pluginSlug + " Something went wrong on ban command.");
            Logger.getLogger(banPlayer.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        
        if (commandSender instanceof Player) ((Player) commandSender).sendMessage(name + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat to update ban.");
        else System.out.println(name + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat to update ban.");
        return true;
    }
}
