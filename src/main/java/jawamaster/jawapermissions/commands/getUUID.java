/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.commands;

import jawamaster.jawapermissions.JawaPermissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class getUUID implements CommandExecutor {
    
    /** This should only run on the /uuid command. This will return a player's UUID if they have the permission, it will also allow a player to
     * get the UUID of another player. Or of all players online.
     * @param commandSender
     * @param arg1
     * @param arg2
     * @param arg3
     * @return  **/
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String player, String[] arg3) {
        String usage = "/uuid <player>";
        JawaPermissions.plugin.getServer().getPlayer(player);
        if (commandSender instanceof Player) commandSender.sendMessage(((Player) commandSender).getUniqueId().toString());
        else if (commandSender instanceof ConsoleCommandSender) commandSender.sendMessage("Console does not have a UUID.");        
        return true;
    }
    
}
