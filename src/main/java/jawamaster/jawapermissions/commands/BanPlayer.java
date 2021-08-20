/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.StandardMessages;
import net.jawasystems.jawacore.utils.TimeParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class BanPlayer implements CommandExecutor {
    private final Logger LOGGER = Logger.getLogger("BanPlayer");
    //private HashSet<String> acceptedFlags;
    //private Player targetPlayer;
    private int reasonLength = JawaPermissions.getPlugin().getConfig().getInt("ban-reason-length", 3);
    private final String[] USAGE = {ChatColor.GREEN + "> Usage of the /ban command:",
                                    ChatColor.GREEN + " > " + ChatColor.BLUE + "/ban [options] <player name> <reason>",
                                    ChatColor.GREEN + " > Available Options:",
                                    ChatColor.GREEN + "  > update -"+ ChatColor.YELLOW +" this will update the users current ban reason",
                                    ChatColor.GREEN + "  > silent -"+ ChatColor.YELLOW +" this will perform a silent ban. Only staff will be notified",
                                    ChatColor.GREEN + "  > d#h#m# -"+ ChatColor.YELLOW +" time to ban for. Can be used with one or all options of d, h, and m (days, hours, minutes). Replace # with number",
                                    ChatColor.GREEN + "  > b:<your username> -"+ ChatColor.YELLOW +" Only used if banning from console",
                                    ChatColor.GREEN + " > Example:"+ ChatColor.YELLOW +" /ban update silent d20h1m5 Jawamaster being a twit",
                                    ChatColor.GREEN + "> Note: Reason length must be, at a minimum, "+ ChatColor.BLUE + "THREE" + ChatColor.GREEN + " words long"};
    //private LocalDateTime banDate;


    //ban <-[u:o]> <player> <reason> <-[d:h:m]>
    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        //Declare the needed variables for the assessment
        //USAGE = {"/ban <-[u|s]> -p <playername> -r <reason for ban> [<-d|h|m> <integer>] [<-b> <your username>]"};
        //ban <update|silent|d#h#m#> <playername> <reason>
        //up?d?a?t?e? si?l?e?n?t? d?[0-9]+?h?[0-9]+?m?[0-9]+? 
        //TODO add options for silent kick or ban
        //acceptedFlags = new HashSet(Arrays.asList("p", "r", "h", "flags", "d", "m", "u", "b","s"));

//###############################################################################
// Validate command input
//###############################################################################
        if (reasonLength < 3) reasonLength = 3;
        
        if (arg3 == null || arg3.length == 0) {
            for (String msg : USAGE) {
                commandSender.sendMessage(msg);
            }
            return true;
        }
        
        if (arg3.length < reasonLength + 1) {
            commandSender.sendMessage(ChatColor.RED + "> Error: Invalid number of arguments. Ensure that you have at least " + ChatColor.BOLD + reasonLength + ChatColor.RESET + ChatColor.RED + " words in your reason.");
            return true;
        }
        
        //To track how far into the array the options go
        int options = 0;
        boolean update = false;
        boolean silent = false;
        boolean console = false;
        PlayerDataObject admin = null;
        LocalDateTime banLength = LocalDateTime.of(3000, 1, 1, 0, 0);

        for (String option : Arrays.copyOfRange(arg3, 0, 3)) {
            if (option.matches("^up?d?a?t?e?$")) {
                options++;
                update = true;
            } else if (option.matches("^si?l?e?n?t?$")){
                options++;
                silent = true;
            } else if (option.matches("([dhm][0-9]+)+")){
                options++;
                int days;
                int minutes;
                int hours;
                
                try {
                    days = Integer.valueOf(option.replaceAll("[Hh][0-9]+", "").replaceAll("[Mm][0-9]+", "").replaceAll("[Dd]", ""));
                } catch (NumberFormatException e){
                    days = 0;
                }
                try {
                    minutes = Integer.valueOf(option.replaceAll("[Dd][0-9]+", "").replaceAll("[Hh][0-9]+", "").replaceAll("[Mm]", ""));
                } catch (NumberFormatException e){
                    minutes = 0;
                }
                try {
                    hours = Integer.valueOf(option.replaceAll("[Dd][0-9]+", "").replaceAll("[Mm][0-9]+", "").replaceAll("[Hh]", ""));
                } catch (NumberFormatException e){
                    hours = 0;
                }
                LOGGER.log(Level.INFO, "{0}:{1}:{2}", new Object[]{days, minutes, hours});
                banLength = LocalDateTime.now().plusDays(days).plusHours(hours).plusMinutes(minutes);
            } else if (option.matches("^b:.*")){
                System.out.println("option matches: " + option);
                options++;
                if (!(commandSender instanceof Player)) {
                    console = true;
                    //System.out.println("option matches: " + option.replace("b:", ""));
                    admin = PlayerManager.getPlayerDataObject(option.replaceFirst("b:", ""));
                    
                    if (admin == null) {
                        commandSender.sendMessage(ChatColor.RED + "> Error: That admin is not found! Try your actual minecraft name instead of nickname.");
                        return true;
                    }
                }
            }
        }
        
        //Always assume the player running the command is the admin
        if (commandSender instanceof Player) {
            admin = PlayerManager.getPlayerDataObject((Player) commandSender);
        } else if (!console) {
            LOGGER.log(Level.INFO, "ERROR: You must specify the b:<your username> to ban from the console");
            return true;
        }
        
        PlayerDataObject target = PlayerManager.getPlayerDataObject(arg3[options]);
        if (target == null) { 
            commandSender.sendMessage(StandardMessages.getMessage(StandardMessages.Message.PLAYERNOTFOUND));
            return true;
        }
        
        if (target.isBanned()){
            commandSender.sendMessage(ChatColor.RED +"> ERROR: That player is already banned, please run the ban command with 'update' to update the ban reason. Or unban and reban.");
            return true;
        }
        //Deal with someone trying to ban an owner rank
        if (target.getRank().equalsIgnoreCase("owner")){
            commandSender.sendMessage(ChatColor.RED + "> Error: You cannot ban a player with owner rank! This incident will be reported.");
            for (Player ply :Bukkit.getServer().getOnlinePlayers()){
                if (ply.hasPermission("jawachat.opchat")) ply.sendMessage(ChatColor.RED + "> WARNING: " + ChatColor.GRAY + admin.getName() + " has attempted to ban a player designated as owner. Console: " + console);
            }
            LOGGER.log(Level.SEVERE, "{0} has attempted to ban a player designated as owner. Console: {1}", new Object[]{admin.getName(), console});
            return true;
        }
        
        //Immunity check
        if (PermissionsHandler.isImmune(admin.getRank(), target.getRank())){
            commandSender.sendMessage(ChatColor.RED + "> Error: That player is immune to your action! They are either higher or identical rank.");
            return true;
        }
        
        String reason = String.join(" ", Arrays.copyOfRange(arg3, options+1, arg3.length));
        
        

//        //Parse the command arguments or if no arguments are sent out
//        if (arg3 == null) {
//            return true; //TODO see what happens when the command is run without arguments
//        }
//        HashMap<String, String> parsedArguments = ArgumentParser.getArgumentValues(arg3);
//        if (JawaPermissions.debug) System.out.println(JawaPermissions.pluginSlug + "[BanPlayer] parsedArguments: " + parsedArguments );
//        
//        if (!parsedArguments.containsKey("p")) {
//            commandSender.sendMessage("Error: No player flag found! Usage: " + USAGE);
//            return true;
//        }
//        if (!parsedArguments.containsKey("r")) {
//            commandSender.sendMessage("Error: No reason flag found! Usage: " + USAGE);
//            return true;
//        }
//        if (parsedArguments.containsKey("b") && (commandSender instanceof Player)) {
//            commandSender.sendMessage("Error: The by(-b) flag is only used when unbanning from the console!");
//            return true;
//        } else if (!(commandSender instanceof Player) && !parsedArguments.containsKey("b")) {
//            System.out.print("You must specify a username for yourself with the -b flag! Make sure to use your minecraft name not your nick!");
//            return true;
//        }
//
//        // TODO use the new argument parser validateArguments for this
//        //Assess flags
//        parsedArguments.keySet().forEach((key) -> {
//            if (!acceptedFlags.contains(key)) {
//                commandSender.sendMessage("Error: Unknown flag found: " + key + "Usage: " + USAGE);
//            } else if (key.equals("flags")) {
//                for (char ch : parsedArguments.get("flags").toCharArray()) {
//                    if (!acceptedFlags.contains(ch)) {
//                        commandSender.sendMessage("Error: Unknown flag found: " + String.valueOf(ch) + "Usage: " + USAGE);
//
//                    }
//                }
//            }
//        });
//        
////###############################################################################
////# Assess offline status for target of ban
////###############################################################################
//        PlayerDataObject target = PlayerManager.getPlayerDataObject(parsedArguments.get("p"));
//        if (target == null) { 
//            commandSender.sendMessage(ChatColor.RED + " > Error: That player is not found! Try their actual minecraft name instead of nickname.");
//            return true;
//        }
//
////###############################################################################
////# Check if update
////###############################################################################        
//        //TODO If this is a ban update adjust for updating
//        if (parsedArguments.containsKey("flags") && parsedArguments.get("flags").contains("u")) {
//            target.updateBan(parsedArguments.get("r"));
//            commandSender.sendMessage(ChatColor.GREEN + "Ban data for " + target.getDisplayName() + ChatColor.GREEN + " has been updated.");
//            return true;
//        } else {//New ban
//            //Get the current time for the ban, this will also be the ban's unique id
//            banDate = LocalDateTime.now();
        if (update) {
            target.updateBan(reason);
            commandSender.sendMessage(ChatColor.GREEN + target.getDisplayName() + ChatColor.RESET + ChatColor.GREEN + "'s ban information has been updated.");
            LOGGER.log(Level.INFO, "{0}''s ban data has been updated by {1}", new Object[]{target.getName(), admin.getName()});
            return true;
        } else {
            target.banPlayer(reason, admin.getUniqueID(), console, banLength);
        }

////###############################################################################
////# Assemble ban information
////###############################################################################   
//            target.banPlayer(commandSender, parsedArguments, banDate);
//            
        //Build ban string 
        if (target.isOnline()) {
            String banString = "You have been banned for: " + reason + ".";
            if (!banLength.isAfter(LocalDateTime.now().plusYears(10L))) {
                banString += " This ban will end on: " + TimeParser.getHumanReadableDateTime(banLength.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            target.getPlayer().kickPlayer(banString);
        }

        //Tell who needs to be told
        for (Player ply : JawaPermissions.plugin.getServer().getOnlinePlayers()) {
            if (!silent || ply.hasPermission("jawachat.opchat")) {
                ply.sendMessage(ChatColor.RED + "[Server] " + admin.getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " has banned " + target.getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " for: " + ChatColor.RED + reason);
            }
        }

        if (commandSender instanceof Player) {
            ((Player) commandSender).sendMessage(ChatColor.GREEN + "> " + target.getDisplayName() + ChatColor.GREEN + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat with the update option and detailed reason.");
        } else {
            System.out.println(target.getDisplayName() + " has been banned. Please ensure that you have entered a detailed reason. If not please repeat with the update option to update ban.");
        }
        return true;
    }
}
