/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class reloadPermissions implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        try {
            PermissionsHandler.reload();
            if (commandSender instanceof Player) ((Player) commandSender).sendMessage("Permissions reload called.");
            else System.out.println("Permissions reload called.");
        } catch (IOException ex) {
            Logger.getLogger(reloadPermissions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
}
