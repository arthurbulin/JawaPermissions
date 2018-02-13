/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.util.Set;
import org.bukkit.Bukkit;
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
            Set<Player> onlinePlayers =  (Set<Player>) Bukkit.getServer().getOnlinePlayers();
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("JawaChat")){
                //TODO colored rank input
            }
            
            for (Player p: onlinePlayers){
                                
            }
        }
        
        return true;
    }
    
}
