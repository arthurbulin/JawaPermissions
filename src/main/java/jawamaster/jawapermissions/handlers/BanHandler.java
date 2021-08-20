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
package jawamaster.jawapermissions.handlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.utils.TimeParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author alexander
 */
public class BanHandler {
    
    /** Return a ban info for a specific ban.
     * @param commandSender
     * @param ban
     * @param pdObject 
     */
    public static void getBanInfo(CommandSender commandSender, String ban, PlayerDataObject pdObject){
        JSONObject banData = pdObject.getBanEntry(ban);
        JSONArray message = new JSONArray();
        message.put(ChatColor.GREEN + " > Ban information for " + pdObject.getFriendlyName() + ChatColor.GREEN + " on ban: " + ChatColor.BLUE + ban);
        
        BaseComponent[] reason = new ComponentBuilder("  > Reason: ").color(ChatColor.GREEN)
                        .append(banData.optString("reason", "WARNING: MISSING DATA"))
                            .color(ChatColor.WHITE)
                        .create();
        message.put(reason);
        
        //Ban Active
        ComponentBuilder active = new ComponentBuilder("  > Active: ").color(ChatColor.GREEN);
        if (banData.getBoolean("active")) {
            active.append("True")
                .color(ChatColor.RED);
        } else {
            active.append("False")
                .color(ChatColor.YELLOW);
        }
        message.put(active.create());
       
        //Banned by
        String adminUUID = banData.getString("banned-by");
        String adminFriendlyName;
        if ("00000000-0000-0000-0000-000000000000".equalsIgnoreCase(adminUUID)){
            adminFriendlyName = "JawaCore System";
        } else {
            PlayerDataObject admin = PlayerManager.getPlayerDataObject(UUID.fromString(adminUUID));
            if (admin != null) {
                adminFriendlyName = admin.getFriendlyName();
            } else {
                adminFriendlyName = "WARNING: MISSING PLAYER ENTRY for " + adminUUID;
            }
        }
        
        ComponentBuilder by = new ComponentBuilder("  > Banned-by: ").color(ChatColor.GREEN)
                .append(adminFriendlyName);
        
        
        if (banData.keySet().contains("unbanned-by")) {
            String unadminUUID = banData.getString("unbanned-by");
            String unadminFriendlyName;
            if ("00000000-0000-0000-0000-000000000000".equalsIgnoreCase(unadminUUID)){
                unadminFriendlyName = "JawaCore System";
            } else {
                PlayerDataObject unadmin = PlayerManager.getPlayerDataObject(UUID.fromString(unadminUUID));
                if (unadmin != null) {
                    unadminFriendlyName = unadmin.getFriendlyName();
                } else {
                    unadminFriendlyName = "WARNING: MISSING PLAYER ENTRY for " + unadminUUID;
                }
            }
//            String unbannedby;
//            if (!banData.getString("banned-by").equals(banData.getString("unbanned-by"))) {
//                unbannedby = PlayerManager.getPlayerDataObject(UUID.fromString(banData.getString("banned-by"))).getFriendlyName();
//            } else {
//                unbannedby = bannedBy.getFriendlyName();
//            }
            by.append(" Unbanned-by: ").color(ChatColor.GREEN)
                    .append(unadminFriendlyName);
        }
        message.put(by.create());
        
        //Banned on
        ComponentBuilder bannedOn = new ComponentBuilder("  > Banned On: ").color(ChatColor.GREEN);
        bannedOn.append(TimeParser.getHumanReadableDateTime(ban,1))
                    .color(ChatColor.BLUE);
        message.put(bannedOn.create());
        
        //Banned until
        ComponentBuilder bannedUntil = new ComponentBuilder("  > Banned Until: ").color(ChatColor.GREEN);
        if (LocalDateTime.parse(banData.getString("banned-until"), DateTimeFormatter.ISO_LOCAL_DATE_TIME).isAfter(LocalDateTime.now().plusYears(10L))){
            bannedUntil.append("forever")
                    .color(ChatColor.RED);
        } else {
            bannedUntil.append(TimeParser.getHumanReadableDateTime(banData.getString("banned-until"),1))
                    .color(ChatColor.BLUE);
        }
        message.put(bannedUntil.create());
        
        //Console ban/unban
        ComponentBuilder console = new ComponentBuilder("  > Console Ban: ").color(ChatColor.GREEN)
                .append(String.valueOf(banData.getBoolean("via-console"))).color(ChatColor.BLUE);
        
        if (banData.keySet().contains("unbanned-via-console")) {
            console.append(" Unbanned via console: ")
                        .color(ChatColor.GREEN)
                   .append(String.valueOf(banData.getBoolean("unbanned-via-console")))
                        .color(ChatColor.BLUE);
        }
        message.put(console.create());
        
        //Unban Date
        if (banData.keySet().contains("unbanned-on")) {
            ComponentBuilder unban = new ComponentBuilder("  > Unbanned on: ").color(ChatColor.GREEN)
                    .append(TimeParser.getHumanReadableDateTime(banData.getString("unbanned-on"),1))
                    .color(ChatColor.BLUE);
            message.put(unban.create());
        }
        
        ComponentBuilder options = new ComponentBuilder("  > Options: ").color(ChatColor.GREEN);
        options.append("[Update Ban Reason] ")
                .color(ChatColor.BLUE)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Update Ban Reason")))
                //.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Update Ban Reason").create()))
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "Not Yet Implimented"));
                

        PlayerDataObject sender = PlayerManager.getPlayerDataObject((Player) commandSender);
        sender.sendMessage(message);
    }
    
    /** List bans for a specific player
     * @param commandSender
     * @param player
     * @param pdObject 
     */
    public static void listBans(CommandSender commandSender, String player, PlayerDataObject pdObject){
                
                JSONArray message = new JSONArray();
                JSONArray bans = pdObject.getBanData();
                String latestBanDate = pdObject.getLatestBanDate();
                //String[] message = new String[1 + bans.length()];
                message.put(ChatColor.GREEN + "> Ban information for " + pdObject.getFriendlyName());
                
                BaseComponent[] latestBanData = new ComponentBuilder(" > ").color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/baninfo info " + player + " " + latestBanDate))
                        .append(latestBanDate)
                            .color(ChatColor.RED)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Latest Ban")))
                        .append(": ")
                            .color(ChatColor.GREEN)
                        .append(pdObject.getLatestBanEntry().getString("reason")).color(ChatColor.WHITE)
                            
                        .create();
                message.put(latestBanData);
                
                
                for (Object ban : bans){
                    if (!((JSONObject) ban).getString("date").equals(latestBanDate)) {
                        ComponentBuilder banData = new ComponentBuilder(" > ")
                                .color(ChatColor.GREEN)
                        .append(((JSONObject) ban).getString("date"))
                                .color(ChatColor.YELLOW)
                        .append(": ")
                            .color(ChatColor.GREEN)
                        .append(((JSONObject) ban).getString("reason")).color(ChatColor.WHITE)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/baninfo info " + player + " " + ban));
                        BaseComponent[] bc = banData.create();
                        message.put(bc);
                    }
                            
                }
                
                PlayerDataObject sender = PlayerManager.getPlayerDataObject((Player) commandSender);
                sender.sendMessage(message);
                
    }
}
