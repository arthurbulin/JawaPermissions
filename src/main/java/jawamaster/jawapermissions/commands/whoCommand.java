/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.PlayerDataObject;
import jawamaster.jawapermissions.handlers.ESHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class whoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        
        if (arg3.length == 0){
            Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers().toArray(new Player[Bukkit.getServer().getOnlinePlayers().size()]);
            String[] stringPlayers = new String[onlinePlayers.length];
            
            for (int i = 0; i < onlinePlayers.length; i++) {
                stringPlayers[i] = onlinePlayers[i].getName();
            }

            commandSender.sendMessage(ChatColor.DARK_GREEN + "> Currently Online Players: " + ChatColor.WHITE + String.join(",", Arrays.toString(stringPlayers)));

        } else if (arg3.length >= 1){
            if (commandSender.hasPermission("jawapermissions.who.detail")) {
                Player target = Bukkit.getServer().getPlayer(arg3[0]);

                if (target == null) {
                    PlayerDataObject pdObject = ESHandler.findOfflinePlayer(arg3[0], true);
                    if (pdObject != null) {
                        displayerUserData(pdObject);
                    }
                } else {
                    try {
                        ESHandler.whoLookUp(commandSender, target);
                    } catch (IOException ex) {
                        Logger.getLogger(whoCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }   

                //Is user offline
                    //get uuid and player data via search
                //if online
            } else {
                commandSender.sendMessage(ChatColor.DARK_RED + "You do not have permission to perform a detailed lookup.");
            }

        }
        
        return true;
    }
    
    private static void displayerUserData(PlayerDataObject pdObject){
        
    }
    
}
