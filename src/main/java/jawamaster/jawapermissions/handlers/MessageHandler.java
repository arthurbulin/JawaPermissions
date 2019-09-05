/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.handlers;

import java.util.HashMap;
import jawamaster.jawapermissions.JawaPermissions;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/** The message handler will accept HashMaps to create an handle all responses to 
 * players and the console (not debug).
 *
 * @author alexander
 */
public class MessageHandler {
    private JawaPermissions plugin;
    private static HashMap<String, String[]> messages;
    
    public MessageHandler(JawaPermissions plugin){
        this.plugin = plugin;
    }
    
    public static void loadMessages(){
        
    
    }
    
    public static void sendTo(CommandSender commandSender, String messageAction){
        if (commandSender instanceof Player) sendTo((Player) commandSender, messageAction);
        else if (commandSender instanceof ConsoleCommandSender) sendTo((ConsoleCommandSender) commandSender, messageAction);
        else if (commandSender instanceof BlockCommandSender) sendTo((BlockCommandSender) commandSender, messageAction);
    }
    
    public static void sendTo(Player player, String messageAction){
       for (String message:messages.get(messageAction)){
           player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
       }
    }
    
    public static void sendTo(ConsoleCommandSender console, String messageAction){
       for (String message:messages.get(messageAction)){
           console.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
       }
    }
    
    public static void sendTo(BlockCommandSender block, String messageAction){
        for (String message:messages.get(messageAction)){
           block.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
       }

    }
}
