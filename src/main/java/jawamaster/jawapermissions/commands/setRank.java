/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.ESHandler;

/**
 *
 * @author Arthur Bulin
 */
public class setRank implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {

        
        if (arg3.length < 2){
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage("Missing arguments! /setrank <player> <rank>");
            else System.out.println("Missing arguments! /setrank <player> <rank>");
            return true;
        }
        
        //Get the target       
        Player target = JawaPermissions.plugin.getServer().getPlayer(arg3[0]);
        String rank = arg3[1].toLowerCase();
        String notValid;
        
        //Check if the player is a valid player
        if (target == null) {
            notValid = arg3[0] + " is is not a valid player";
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage(notValid);
            else System.out.println(notValid);
            return true;
        }
        
        //Check if the rank is a valid rank
        if (!JawaPermissions.immunityLevels.containsKey(rank)) {
            notValid = arg3[1] + " is is not a valid rank.";
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage(notValid);
            else System.out.println(notValid);
            return true;
        }
        
        //Check if player already has that rank
        if (JawaPermissions.playerRank.get(target.getUniqueId()).equals(rank)){
            notValid = target.getName() + " already has rank " + rank;
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage(notValid);
            else System.out.println(notValid);
            return true;
        }
        
        if (!JawaPermissions.handler.immunityCheck(commandSender, target)){
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage(target.getName() + "Has immunity to your specified command.");
            return true;
        }
        
        if (rank.equals("owner")){
            notValid = "Owner rank cannot be set by command and must be manually entered in the ElasticSearch index.";
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage(notValid);
            else System.out.println(notValid);
            return true;
        }
        
        ESHandler.setPlayerRank(commandSender, target, rank);
        
        return true;
    }
    
    
}
