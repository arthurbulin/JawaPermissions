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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.utils.ListHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
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
    
    private static final Logger LOGGER = Logger.getLogger("PlayerCommentCommand");
    private static final HashMap<Integer, Integer> REQUESTTOTASKMAP = new HashMap();
    private static final HashMap<Integer, Integer> REQUESTITERATIONCOUNT = new HashMap();
    
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
        
        if (PlayerManager.isPlayerInCache(args[0])){
            playerComment(PlayerManager.getPlayerDataObject(args[0]), sender, command, args);
        } else {
            int requestID = new Random().nextInt();
            PlayerManager.requestCaching(args[0], requestID);
            REQUESTITERATIONCOUNT.put(requestID, 0);
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(JawaPermissions.getPlugin(), () -> {
                Boolean status = PlayerManager.getRequestStatus(requestID);
                if (status != null) {
                    if (status) {
                        PlayerDataObject target = PlayerManager.getOfflinePlayerFromCache(args[0]);
                        playerComment(target, sender, command, args);
                        PlayerManager.completeRequest(requestID);
                        REQUESTITERATIONCOUNT.remove(requestID);
                        Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(requestID));
                        REQUESTTOTASKMAP.remove(requestID);

                    } else {
                        sender.sendMessage(ChatColor.RED + " > Error: That player is not found! Try their actual minecraft name instead of nickname.");
                        REQUESTITERATIONCOUNT.remove(requestID);
                        Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(requestID));
                        REQUESTTOTASKMAP.remove(requestID);
                    }
                } else {
                    int count = REQUESTITERATIONCOUNT.get(requestID) + 1;
                    REQUESTITERATIONCOUNT.put(requestID, count);
                    if (count > 27) {
                        LOGGER.log(Level.SEVERE, "A cache request was not answered");
                        sender.sendMessage(ChatColor.RED + " > Error: The database never responded to the request. Contact your server administrator.");
                        REQUESTITERATIONCOUNT.remove(requestID);
                        Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(requestID));
                        REQUESTTOTASKMAP.remove(requestID);
                    }
                }
            }, 5, 2);
            REQUESTTOTASKMAP.put(requestID, task);
        }
        
        return true;
        
        
    }
    
    private void playerComment(PlayerDataObject target, CommandSender sender, Command command, String[] args){
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
                return;
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
                return;
            case "list":
                JSONArray list = target.getComments();
                if (list == null){
                    sender.sendMessage(ChatColor.RED + "> Error: That player has no admin comments");
                    return;
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
                        return;
                    }
                } else {
                    page = 1;
                }
                
                List<String> msgs = new ArrayList();

                msgs.add(ChatColor.GREEN + "> Admin Comments, page " + page + " of " + pages);
                //int end = (page * 7) - mod;
                //1:8, 2:15, 3:22, 4:29
                //page size: 7
                //last page size: l%7
                int end = (pages * 7 ) - list.length() -1;
//                if (page == 1){
//                    end = 8;
//                } else {
//                    //end = (page * 7) - mod;
//                    end = ;
//                }
//                end = (mod == 0) ? (page * 7) + 1 : (page * 7) - (7 -mod);
//                if (list.length() < 7) {
//                    end = list.length();
//                }
//                LOGGER.log(Level.INFO, "listlen:"+list.length() + " page:" + page + " end:" + end + " pages:"+pages+" mod:"+mod);
//                for (int x = (page * 7) - 7; x < end; x++) {
//                    JSONObject comment = list.getJSONObject(x);
//                    String adminName = PlayerManager.getPlayerDataObject(UUID.fromString(comment.getString("admin"))).getFriendlyName();
//                    LocalDateTime date = LocalDateTime.parse(comment.getString("date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//                    String datePrefix = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "@" + date.getHour() +":"+date.getMinute();
//                    msgs.add(ChatColor.GREEN + String.valueOf(x) + ". " + adminName + ChatColor.BLUE + " " + datePrefix + ": " + ChatColor.DARK_AQUA + comment.getString("comment"));
//                }
                
                JSONArray assembledPages = ListHandler.buildPages(list, 7);
//               for (Object aPages : assembledPages){
//                System.out.println(aPages);
//               }
                int iteration = 0;
//                for (Object assembledPage : assembledPages) {
                JSONArray assembledPage = assembledPages.getJSONArray(page-1);
                for (Object commentOBJ : (JSONArray) assembledPage) {
                    JSONObject comment = (JSONObject) commentOBJ;
                    String adminName = PlayerManager.getPlayerDataObject(UUID.fromString(comment.getString("admin"))).getFriendlyName();
                    LocalDateTime date = LocalDateTime.parse(comment.getString("date"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String datePrefix = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "@" + date.getHour() + ":" + date.getMinute();
                    int entry = iteration + (7 * (page-1));
                    msgs.add(ChatColor.GREEN +" " + String.valueOf(entry) + ". " + adminName + ChatColor.BLUE + " " + datePrefix + ": " + ChatColor.DARK_AQUA + comment.getString("comment"));
                    iteration++;
                }
//                }
                
                msgs.forEach(msg -> {
                    sender.sendMessage(msg);
                });
                
                if (assembledPages.length() > 1){
                    ComponentBuilder builder = new ComponentBuilder();
                    builder.append(" > ").color(ChatColor.GREEN);
                    if (page != 1) {
                        builder.append("[Prev Page]").color(ChatColor.DARK_AQUA)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playercomment ".concat(target.getName()).concat(" list ").concat(String.valueOf(page - 1))))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Next Page")));
                    }

                    if (page != assembledPages.length()) {
                        builder.append(" [Next Page]").color(ChatColor.YELLOW)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playercomment ".concat(target.getName()).concat(" list ").concat(String.valueOf(page + 1))))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Next Page")));
                    }
                    BaseComponent[] msgLine = builder.create();
                    sender.spigot().sendMessage(msgLine);
                }
                
                

                return;
//
//            case "detail":
//                
//                return true;
            default:
                sender.sendMessage(USAGE);
                return;
        }
    }
    
}
