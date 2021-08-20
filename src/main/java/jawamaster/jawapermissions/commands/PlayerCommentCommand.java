/*
 * Copyright (C) 2021 Jawamaster (Arthur Bulin)
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
package jawamaster.jawapermissions.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Jawamaster (Arthur Bulin)
 */
public class PlayerCommentCommand implements CommandExecutor {
    private static final String[] USAGE = {
        ChatColor.GREEN + "> Usage of the /playercomment command",
        ChatColor.GREEN + " > /playercomment <player> add <comment>",
        ChatColor.GREEN + " > /playercomment <player> remove <comment number>",
        ChatColor.GREEN + " > /playercomment <player> list [page#]"};
        //"/playercomment <player> detail <comment number>"};
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1){
            sender.sendMessage(USAGE);
            return true;
        }
        
        PlayerDataObject target = PlayerManager.getPlayerDataObject(args[0]);
        if (target == null) {
            sender.sendMessage(PlayerManager.NOPLAYERERROR);
            return true;
        }
        
        switch (args[1]) {
            case "add":
                if (sender.hasPermission("jawapermissions.playercomment.add")){
                    if (args.length >= 7) {
                        target.addComment(String.join(" ", Arrays.copyOfRange(args, 2, args.length)), ((Player) sender).getUniqueId());
                        sender.sendMessage(ChatColor.GREEN + "> Adding a comment to " + target.getFriendlyName() + ChatColor.GREEN + "'s data");
                    } else {
                        sender.sendMessage(ChatColor.RED + "> Error: Comments must be at least 5 words long");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "No permission");
                }
                return true;
            case "remove":
                if (sender.hasPermission("jawapermissions.playercomment.remove")){
                    try {
                        int ind = Integer.valueOf(args[2]);
                        if (target.hasCommentAtIndex(ind)) {
                            target.removeComment(ind);
                            sender.sendMessage(ChatColor.GREEN + "> Removing comment");
                        } else {
                            sender.sendMessage(ChatColor.RED + "> Error: There is no comment at that index");
                        }
                    } catch (NumberFormatException e){
                        sender.sendMessage(ChatColor.RED + "> Error: The comment index must be an integer number");
                    }
                    
                } else {
                    sender.sendMessage(command.getPermissionMessage());
                }
                return true;
            case "list":
                JSONArray list = target.getComments();
                if (list == null){
                    sender.sendMessage(ChatColor.RED + "> Error: That player has no admin comments");
                    return true;
                }
                
                // TODO this is clumsy and can be done better
                
                //ComponentBuilder pgmsg = new ComponentBuilder();
                int page = 1;
                int pages = list.length() / 7;
                int mod = list.length() % 7;
                if (mod != 0) pages+=1;
                
                if (args.length >= 3){
                    try {
                        page = Integer.valueOf(args[2]);
                    } catch (NumberFormatException e){
                        sender.sendMessage(ChatColor.RED + "> Error: The page number must be an integer number");
                        return true;
                    }
                } else {
                    page = 1;
                }
                
                List<String> msgs = new ArrayList();

                msgs.add(ChatColor.GREEN + "> Admin Comments, page " + page + " of " + pages);
                int end = (page * 7) - mod;
                if (list.length() < 7) {
                    end = list.length();
                }
                
                for (int x = (page * 7) - 7; x < end; x++) {
                    JSONObject comment = list.getJSONObject(x);
                    String adminName = PlayerManager.getPlayerDataObject(UUID.fromString(comment.getString("ADMIN"))).getFriendlyName();
                    LocalDateTime date = LocalDateTime.parse(comment.getString("DATE-TIME"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String datePrefix = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "@" + date.getHour() +":"+date.getMinute();
                    msgs.add(ChatColor.GREEN + String.valueOf(x) + ". " + adminName + ChatColor.BLUE + datePrefix + ": " + ChatColor.WHITE + comment.getString("COMMENT"));
                }
                
                msgs.forEach(msg -> {
                    sender.sendMessage(msg);
                });

                return true;
//
//            case "detail":
//                
//                return true;
            default:
                sender.sendMessage(USAGE);
                return true;
        }
        
    }
    
}
