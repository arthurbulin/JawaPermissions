/*
 * Copyright (C) 2020 Jawamaster (Arthur Bulin)
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

package jawamaster.jawapermissions.handlers;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.commands.playerinfo.AltSearch;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.ESHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

/**
 *
 * @author Jawamaster (Arthur Bulin)
 */
public class PlayerInfoHandler {

    private static Queue<JSONObject> requests = new LinkedBlockingQueue<>();
    private static AltSearch altSearchThread;
    
    public static void startupThreads() {
        altSearchThread = new AltSearch();
    }
    
    public static JSONObject pollAltSearchQueue(){
        return requests.poll();
    }
    
    public static void ipAltSearch(PlayerDataObject target){
        JSONObject requestObj = new JSONObject();
        requestObj.put("pdo", target);
        requests.add(requestObj);
        
        synchronized (requests) {
            altSearchThread.notify();
        }
    }
    
    public static void ipAltSearch(CommandSender sender, PlayerDataObject target) {
        
        JSONObject requestObj = new JSONObject();
        requestObj.put("sender", target);
        requestObj.put("pdo", target);
        
        requests.add(requestObj);
        
        synchronized (requests) {
            altSearchThread.notify();
        }
        
//        Bukkit.getServer().getScheduler().runTaskAsynchronously(JawaPermissions.getPlugin(), (BukkitTask t) -> {
//            String ip = target.getIP();
//            SearchHit[] hits = ESHandler.runAltSearch(ip);
//            if (hits.length == 1) {
//                sender.sendMessage(ChatColor.GREEN + "> No alts were found for " + ChatColor.BLUE + target.getPlainNick());
//            } else {
//
//                BaseComponent[] header = new ComponentBuilder(ChatColor.GREEN + "> Possible alts for: " + ChatColor.BLUE + target.getName()).create();
//                sender.spigot().sendMessage(header);
//                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(JawaPermissions.getPlugin(), () -> {
//                    for (SearchHit hit : hits) {
//                        if (!hit.getId().equals(target.getUniqueID().toString())) {
//                            JSONObject hitMap = new JSONObject(hit.getSourceAsMap());
//                            BaseComponent[] altInfo = new ComponentBuilder(ChatColor.GREEN + " > ")
//                                    .append(hitMap.getString("name")).color(PermissionsHandler.getRankColor(hitMap.getString("rank")))
//                                    .append(", with rank: ").color(ChatColor.GREEN)
//                                    .append(hitMap.getString("rank")).color(PermissionsHandler.getRankColor(hitMap.getString("rank")))
//                                    .reset()
//                                    .append(" [Who is]")
//                                    .color(ChatColor.BLUE)
//                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Who lookup")))
//                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/who " + hitMap.getString("name")))
//                                    .create();
//
//                            sender.spigot().sendMessage(altInfo);
//                        }
//                    }
//                });
//
//            }
//
//        });
    }


}
