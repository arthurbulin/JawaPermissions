/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Arthur Bulin
 */
public class rankUpgrade implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        //Check to make sure that files are setup
        
        //IF yes. Warn user that database conversion will start on next command run
        //IF no. Warn the user that stuff is missing
        
        return true;
    }
    
    
}
