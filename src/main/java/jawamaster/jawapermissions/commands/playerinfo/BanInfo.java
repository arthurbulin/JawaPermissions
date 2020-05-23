/*
 * Copyright (C) 2020 alexander
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jawamaster.jawapermissions.commands.playerinfo;

import jawamaster.jawapermissions.handlers.BanHandler;
import net.jawasystems.jawacore.JawaCore;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author alexander
 */
public class BanInfo implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        
        String[] usage = new String[]{
            "List a player's ban info - /baninfo list <player>",
            "Get specific ban info - /baninfo info <player> <ban>",
            "Get help - /baninfo help"};
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                PlayerDataObject pdObject = PlayerManager.getPlayerDataObject(args[1]);
                if (pdObject == null) { 
                    commandSender.sendMessage(ChatColor.RED + " > Error: That player wasn't found! Try their actual minecraft name instead of nickname.");
                    return true;
                }
                
                if (!pdObject.containsBanData()) {
                    commandSender.sendMessage(ChatColor.RED + " > Error: That player doesn't seem to contain any ban data. This means they have never been banned.");
                    return true;
                }
                
                Bukkit.getServer().getScheduler().runTaskAsynchronously(JawaCore.plugin, new Runnable() {
                    @Override
                    public void run() {
                        BanHandler.listBans(commandSender, args[1], pdObject);
                    }
                });
                
            } else {
                commandSender.sendMessage(ChatColor.RED + " > Error: " + args[0] + " is not understood");
                return true;
            }
            
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("info")) {
                PlayerDataObject pdObject = PlayerManager.getPlayerDataObject(args[1]);
                if (pdObject == null) { 
                    commandSender.sendMessage(ChatColor.RED + " > Error: That player wasn't found! Try their actual minecraft name instead of nickname.");
                    return true;
                }
                
                if (!pdObject.containsBanData()) {
                    commandSender.sendMessage(ChatColor.RED + " > Error: That player doesn't seem to contain any ban data. This means they have never been banned.");
                    return true;
                }
                
                if (!pdObject.getBanData().keySet().contains(args[2])) {
                    commandSender.sendMessage(ChatColor.RED + " > Error: " + args[2] + " does not appear to be a valid ban date.");
                    return true;
                }
                
                Bukkit.getServer().getScheduler().runTaskAsynchronously(JawaCore.plugin, new Runnable() {
                    @Override
                    public void run() {
                        BanHandler.getBanInfo(commandSender, args[2], pdObject);
                    }
                });
                
                
            } else {
                commandSender.sendMessage(ChatColor.RED + " > Error: " + args[0] + " is not understood");
                return true;
            }
        } else {
            commandSender.sendMessage(usage);
        }
        return true;
    }
    
    
    
    
    
}
