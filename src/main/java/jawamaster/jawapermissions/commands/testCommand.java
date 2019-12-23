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

/**
 *
 * @author Arthur Bulin
 */
public class testCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {

        
        System.out.println("playerRanks: " + JawaPermissions.playerRank);
        System.out.println("immunitylevels; " + JawaPermissions.immunityLevels);
        
//        JawaPermissions.plugin.getServer().getScheduler().runTaskAsynchronously(JawaPermissions.plugin, new Runnable() {
//            @Override
//            public void run() {
//                commandSender.sendMessage(ChatColor.RED + " test async messages!");
//            }
//        });
        
        return true;
    }
    
    
}
