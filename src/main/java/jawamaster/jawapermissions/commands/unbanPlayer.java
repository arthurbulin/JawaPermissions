/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import jawamaster.jawapermissions.JawaPermissions;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.utils.ArgumentParser;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class unbanPlayer implements CommandExecutor {
    public final String[] USAGE = new String[]{ChatColor.GREEN + " > " + ChatColor.YELLOW + "/unban -p <playername> -r <reason for unban> [-b] <Your user name>",
        ChatColor.GREEN + " > " + ChatColor.YELLOW + "p: Player Minecraft Name, or Nickname",
        ChatColor.GREEN + " > " + ChatColor.YELLOW + "r: Reason for the player's unban",
        ChatColor.GREEN + " > " + ChatColor.YELLOW + "b: Used from the console to specify the name of who undid the ban"};
    public HashSet<String> acceptedFlags = new HashSet(Arrays.asList("p","r","b"));

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        PlayerDataObject admin;
        //Parse the command arguments or if no arguments are sent out
        if (arg3 == null || arg3.length == 0) {
            commandSender.sendMessage(USAGE);
            return true;
        }

        HashMap<String, String> parsedArguments = ArgumentParser.getArgumentValues(arg3);
        if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[UnBanPlayer] parsedArguments: " + parsedArguments );
        
        // Validate that the flags are ones that this commands accepts
        if (!ArgumentParser.validateArguments(commandSender, parsedArguments, acceptedFlags)) return true;
        
        admin = PlayerManager.getAdmin(commandSender, parsedArguments);
        if (admin == null) {
            return true;
        }

        // Validate that all the required flags are found
        acceptedFlags.remove("b");
        if (!ArgumentParser.validateCommandInput(commandSender, acceptedFlags, parsedArguments, USAGE)) return true;
        
        //Validate Player
        PlayerDataObject target = PlayerManager.getPlayerDataObject(parsedArguments.get("p"));
        if (target == null) { 
            commandSender.sendMessage(ChatColor.RED + " > Error: That player wasn't found! Try their actual minecraft name instead of nickname.");
            return true;
        }
        if (!target.isBanned()){
            commandSender.sendMessage(ChatColor.RED + " > Error: That player isn't banned!");
            return true;
        }
        
        target.unbanPlayer(admin, parsedArguments, LocalDateTime.now());
        
        commandSender.sendMessage(ChatColor.GREEN + " > " + target.getFriendlyName() + ChatColor.GREEN + " has been unbanned for: " + ChatColor.GRAY + parsedArguments.get("r"));
        
        return true;
    }
    
}
