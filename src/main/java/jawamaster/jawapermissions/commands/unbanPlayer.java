/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class unbanPlayer implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        
        //Check to make sure a player name has been entered
        if (arg3.length < 1) {
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage("Missing arguments! /unban <player>");
            else System.out.println("Missing arguments! /unban <player>");
            return true;
        }
       
        OfflinePlayer target = JawaPermissions.plugin.getServer().getOfflinePlayer(arg3[0]);
        
        //Check if the player is a valid player
        if (target == null) {
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage(arg3[0] + " is is not a valid player");
            else System.out.println(arg3[0] + " is is not a valid player");
            return true;
        }
        
        UUID uuid = target.getUniqueId();
     
        try {
            Map<String, Object> banData = ESHandler.checkBanStatus(uuid);
            if (!(banData == null)) {//If ban data exists
                
//                if ((boolean) banData.get("ban-lock")) { //If there is a ban lock
//                    if (commandSender instanceof Player) {
//                        String rank = JawaPermissions.playerRank.get(((Player) commandSender).getUniqueId());
//                        if (!rank.contains(rank)) {
//                            ((Player) commandSender).sendMessage(target.getName() + " has an active ban-lock. This player can only be unbanned by an owner rank player.");
//                            return true;
//                        }
//                    } else {
//                        System.out.println(target.getName() + " has an active ban-lock. This player can only be unbanned by an owner rank player.");
//                        return true;
//                    }
//                }
                
                //If ban is active perform unban
                if ((boolean) banData.get("banned")) ESHandler.unbanPlayer(uuid, commandSender, banData, 0);
                
            } else { //If ban data doesn't exist
                if (commandSender instanceof Player) ((Player) commandSender).sendMessage(target.getName() + " does not have an existing ban.");
                else System.out.println(target.getName() + " does not have an existing ban.");
            }
        } catch (IOException ex) {
            Logger.getLogger(unbanPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
}
