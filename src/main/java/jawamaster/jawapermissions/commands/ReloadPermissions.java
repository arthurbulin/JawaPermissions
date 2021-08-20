/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/** This command tells the PermissionsHandler to reload the permissions files and
 * rebuild the rank objects.
 * @author Arthur Bulin
 */
public class ReloadPermissions implements CommandExecutor {

    private static final Logger LOGGER = Logger.getLogger("ReloadPermissions");
    
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        
        PermissionsHandler.reload();
        commandSender.sendMessage(ChatColor.GREEN + "> Permissions reload called");
        LOGGER.log(Level.INFO, "Permissions reload called");
        
        return true;
    }
}
