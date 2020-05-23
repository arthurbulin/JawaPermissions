/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package jawamaster.jawapermissions.commands.playerinfo;

import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Arthur Bulin
 */
public class GetUUID implements CommandExecutor {
    
    /** *  This should only run on the /uuid command.This will return a player's 
     * UID if they have the permission, it will also allow a player to
     * get the UUID of another player.Or of all players online.
     * @param arg1
     * @param arg2
     * @return  **/
    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        String usage = "/uuid <player>";
        PlayerDataObject target = PlayerManager.getPlayerDataObject(args[0]);
        
        BaseComponent[] baseComp = new ComponentBuilder(ChatColor.GREEN + "> UUID for " + target.getFriendlyName())
                .append(": ").color(ChatColor.WHITE)
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, target.getUniqueID().toString()))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Copy UUID to Clipboard").create()))
                .append(target.getUniqueID().toString()).color(ChatColor.WHITE)
                .create();
        
        sender.spigot().sendMessage(baseComp);
        return true;
    }
    
}
