/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.commands.playerinfo;

import java.util.Arrays;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Arthur Bulin
 */
public class WhoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command arg1, String arg2, String[] arg3) {
        
        if (arg3.length == 0){
            Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers().toArray(new Player[Bukkit.getServer().getOnlinePlayers().size()]);
            String[] stringPlayers = new String[onlinePlayers.length];
            
            for (int i = 0; i < onlinePlayers.length; i++) {
                stringPlayers[i] = onlinePlayers[i].getName();
            }

            commandSender.sendMessage(ChatColor.GREEN + "> Currently Online Players: " + ChatColor.WHITE + String.join(",", Arrays.toString(stringPlayers)));

        } else if (arg3.length >= 1){
            if (commandSender.hasPermission("jawapermissions.who.detail")) {
                PlayerDataObject target = PlayerManager.getPlayerDataObject(arg3[0]);
                if (target == null) {
                    commandSender.sendMessage(ChatColor.RED + " > Error: That player is not found! Try their actual minecraft name instead of nickname.");
                    return true;
                }
                
                commandSender.sendMessage(ChatColor.GREEN + "> " + arg3[0] + "'s current player data");
                
                String names = ChatColor.GREEN + " > User name: " + target.getRankColor() + target.getName();
                if (!target.getNickName().equals("")){ //if user already has a nick then their friendly name will be the nick
                    names += ChatColor.GREEN + " Nickname: " + ChatColor.RESET + target.getFriendlyName();
                }
                if (!target.getTag().equals("")) {
                    names += ChatColor.GREEN + " Tag: " + ChatColor.RESET + target.getFriendlyTag();
                }
                if (!target.getStar().equals("")) {
                    names += ChatColor.GREEN + " Star: " + ChatColor.RESET + target.getStar();
                }
                commandSender.sendMessage(names);
                
                commandSender.sendMessage(ChatColor.GREEN + " > Rank: " + target.getRankColor() + target.getRank() +ChatColor.GREEN + " Current IP: " + ChatColor.WHITE + target.getIP());
                
                //TODO build this to be a clickable request for ban data
                String banInfo = ChatColor.GREEN + " > Banned: ";
                if (target.isBanned()) banInfo += ChatColor.RED + "True";
                else banInfo += ChatColor.AQUA + "False";
                commandSender.sendMessage(banInfo);
                
                if (target.isOnline()) {
                    Player player = target.getPlayer();
                    //commandSender.sendMessage(ChatColor.GREEN +  + ChatColor.RESET + player.getGameMode().toString().toLowerCase());
                    BaseComponent[] locationInfo = new ComponentBuilder(" > Gamemode: ").color(ChatColor.GREEN)
                            .append(player.getGameMode().toString().toLowerCase()).color(ChatColor.WHITE)
                            .append("[s]").color(ChatColor.GREEN)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gamemode survival " + target.getName()))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Change to Survival").create()))
                            .append("[a]").color(ChatColor.GREEN)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gamemode adventure " + target.getName()))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Change to adventure").create()))
                            .append("[c]").color(ChatColor.GREEN)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gamemode creative " + target.getName()))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Change to creative").create()))
                            .append("[sp]").color(ChatColor.GREEN)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gamemode spectator " + target.getName()))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Change to spectator").create()))
                            .append(" Location: ")
                            .color(net.md_5.bungee.api.ChatColor.GREEN)
                            .append(player.getWorld().getName())
                                .color(net.md_5.bungee.api.ChatColor.BLUE)
                            .append(" " + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ())
                                .color(net.md_5.bungee.api.ChatColor.GOLD)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + target.getName()))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to player").create()))
                            .create();
                    commandSender.spigot().sendMessage(locationInfo);
                }
                
                BaseComponent[] nameOptions = new ComponentBuilder(" > Name Options: ")
                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append(" [Set Nick]")
                            .color(net.md_5.bungee.api.ChatColor.BLUE)
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setnick " + target.getName() + " "))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set Nick").create()))
                        .append(" [Set Tag]")
                            .color(net.md_5.bungee.api.ChatColor.AQUA)
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/settag " + target.getName() + " "))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set Tag").create()))
                        .append(" [Set Star]")
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setstar " + target.getName() + " "))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set Star").create()))
                        .create();
                commandSender.spigot().sendMessage(nameOptions);
                
                BaseComponent[] adminOptions = new ComponentBuilder(" > Admin Options: ")
                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append(" [Rank]")
                            .color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE)
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setrank -p " + target.getName() + " -r "))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set rank").create()))
                        .append(" [OInv]")
                            .color(net.md_5.bungee.api.ChatColor.GOLD)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openinv " + target.getName() + " -r "))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("OpenInv").create()))
                        .append(" [Alts]")
                            .color(net.md_5.bungee.api.ChatColor.DARK_GREEN)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playerinfo " + target.getName() + " alts"))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Alt Search").create()))
                        .append(" [Ban]")
                            .color(net.md_5.bungee.api.ChatColor.RED)
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ban -p " + target.getName() + " -r "))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Ban Player").create()))
                        .append(" [Mute]")
                            .color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mute " + target.getName() ))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Mute").create()))
                        .append(" [Freeze]")
                            .color(net.md_5.bungee.api.ChatColor.AQUA)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/freeze " + target.getName() ))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Freeze").create()))
                        .create();
                commandSender.spigot().sendMessage(adminOptions);
                
                BaseComponent[] infoOptions = new ComponentBuilder(" > Info Options: ")
                        .color(net.md_5.bungee.api.ChatColor.GREEN)
                        .append(" [IP Info]")
                            .color(net.md_5.bungee.api.ChatColor.DARK_PURPLE)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playerinfo " + target.getName() + " ips"))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("IP Info").create()))
                        .append(" [Ranking Info]")
                            .color(net.md_5.bungee.api.ChatColor.DARK_AQUA)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playerinfo " + target.getName() + " ranks"))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Nick Info").create()))
                        .append(" [Ban Info]")
                            .color(net.md_5.bungee.api.ChatColor.DARK_AQUA)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/baninfo list " + target.getName()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Ban Info").create()))
                        .create();
                commandSender.spigot().sendMessage(infoOptions);

            } else {
                commandSender.sendMessage(ChatColor.RED + "> You do not have permission to perform a detailed player lookup.");
            }

        }
        
        return true;
    }
}
