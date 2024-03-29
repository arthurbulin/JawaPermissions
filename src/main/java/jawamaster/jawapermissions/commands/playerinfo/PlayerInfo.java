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

import java.util.UUID;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.PermissionsHandler;
import jawamaster.jawapermissions.handlers.PlayerInfoHandler;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.utils.TimeParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author alexander
 */
public class PlayerInfo implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String usage = "/playerinfo <player> <alts|ips|ranks>";

        PlayerDataObject target = PlayerManager.getPlayerDataObject(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + " > Error: That player wasn't found! Try their actual minecraft name instead of nickname.");
        } else {
            //TODO allow historical searching of IPs using the recorded IP data
            //Execute the following async
            switch (args[1]) {
                case "alts":
                    PlayerInfoHandler.ipAltSearch(sender, target);
                    break;
                case "ips":
                    getIPHistory(sender, target);
                    break;
                case "ranks":
                    getRankHistory(sender, target);
                    break;
                case "location":
                    getIPGeoLocation(sender, target);
                    break;
                default:
                    sender.sendMessage(ChatColor.GREEN + "> Usage: " + usage);
                    break;
            }

        }
        return true;

    }

    /** Gets the geoIP location and returns the message to the admin
     * @param sender
     * @param target 
     */
    private void getIPGeoLocation(CommandSender sender, PlayerDataObject target){
        if (JawaPermissions.isGeoIPEnabled()){
            String ip = target.getIP().replace("/", "");
            String location = PlayerInfoHandler.getIPGeoLocation(ip);

            sender.sendMessage(ChatColor.GREEN + "> Approximate GeoLocation for " + ChatColor.BLUE + target.getName());
            sender.sendMessage(ChatColor.GREEN + " > " + ChatColor.GOLD + location);
        } else {
            sender.sendMessage(ChatColor.RED + "> Error: IP Geo Location is disabled");
        }
    }

    private void getIPHistory(CommandSender sender, PlayerDataObject target) {
        
        JSONArray ips = target.getIPData();
        //String ip = target.getIP();
        BaseComponent[] header = new ComponentBuilder(ChatColor.GREEN + "> IP history for: " + ChatColor.BLUE + target.getName()).create();
        sender.spigot().sendMessage(header);
        if (!JawaPermissions.isGeoIPEnabled()) {
            for (Object item : ips) {
                BaseComponent[] line = new ComponentBuilder(" > ").color(ChatColor.GREEN)
                        .append(((JSONObject) item).getString("ip")).color(ChatColor.YELLOW)
                        .append(":").color(ChatColor.WHITE)
                        .append(TimeParser.getHumanReadableDateTime(((JSONObject) item).getString("date"), 1)).color(ChatColor.YELLOW)
                        .create();
                sender.spigot().sendMessage(line);
            }
        } else {
            for (Object item : ips) {
                BaseComponent[] line = new ComponentBuilder(" > ").color(ChatColor.GREEN)
                        .append(((JSONObject) item).getString("ip")).color(ChatColor.YELLOW)
                        .append(":").color(ChatColor.WHITE)
                        .append(TimeParser.getHumanReadableDateTime(((JSONObject) item).getString("date"), 1)).color(ChatColor.YELLOW)
                        .append(" --> ").color(ChatColor.BLUE)
                        .append(PlayerInfoHandler.getIPGeoLocation(((JSONObject) item).getString("ip"))).color(ChatColor.GOLD)
                        .create();
                sender.spigot().sendMessage(line);
            }
        }
    }

    private void getRankHistory(CommandSender sender, PlayerDataObject target) {
        JSONArray rankData = target.getRankData();
        if (rankData == null || rankData.isEmpty()) {
            //Report no rank change data
            sender.sendMessage(ChatColor.GREEN + "> No rank history data was found.");
        } else {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(JawaPermissions.getPlugin(), (BukkitTask t) -> {
                JSONArray msg = new JSONArray();
                //BaseComponent[][] msgBC = new BaseComponent[][rankData.length() + 1];
                BaseComponent[] header = new ComponentBuilder("> Rank change history for ").color(ChatColor.GREEN)
                        .append(target.getFriendlyName()).create();

                msg.put(header);

                //header //FIXME there seems to be errors generating on lines 153 and 159 due to the lambda expression.
                for(Object banObj : rankData){
                    JSONObject ban = (JSONObject) banObj;
                    ComponentBuilder entry = new ComponentBuilder(" > ").color(ChatColor.GREEN)
                            .append(ban.getString("from-rank"))
                            .color(PermissionsHandler.getRankColor(ban.getString("from-rank")))
                            .append(" -> ")
                            .color(ChatColor.GREEN)
                            .append(ban.getString("to-rank"))
                            .color(PermissionsHandler.getRankColor(ban.getString("to-rank")))
                            .append(" by ")
                            .color(ChatColor.GREEN);
                    if (ban.getString("changed-by").equals("00000000-0000-0000-0000-000000000000")) {
                        entry.append("Autoelevation")
                                .color(ChatColor.BLUE);
                    } else {
                        PlayerDataObject admin = PlayerManager.getPlayerDataObject(UUID.fromString(ban.getString("changed-by")));
                        entry.append(admin.getFriendlyName());
                    }
                    entry.append(" on ").color(ChatColor.GREEN)
                            .append(TimeParser.getHumanReadableDateTime(ban.getString("date"), 1))
                            .color(ChatColor.BLUE);
                    
                    msg.put(entry.create());
                }
                
                
//                rankData.keySet().stream().map((date) -> {
//                    ComponentBuilder entry = new ComponentBuilder(" > ").color(ChatColor.GREEN)
//                            .append(rankData.getJSONObject(date).getString("from-rank"))
//                            .color(PermissionsHandler.getRankColor(rankData.getJSONObject(date).getString("from-rank")))
//                            .append(" -> ")
//                            .color(ChatColor.GREEN)
//                            .append(rankData.getJSONObject(date).getString("to-rank"))
//                            .color(PermissionsHandler.getRankColor(rankData.getJSONObject(date).getString("to-rank")))
//                            .append(" by ")
//                            .color(ChatColor.GREEN);
//                    if (rankData.getJSONObject(date).getString("changed-by").equals("00000000-0000-0000-0000-000000000000")) {
//                        entry.append("Autoelevation")
//                                .color(ChatColor.BLUE);
//                    } else {
//                        PlayerDataObject admin = PlayerManager.getPlayerDataObject(UUID.fromString(rankData.getJSONObject(date).getString("changed-by")));
//                        entry.append(admin.getFriendlyName());
//                    }
//                    entry.append(" on ").color(ChatColor.GREEN)
//                            .append(TimeParser.getHumanReadableDateTime(date, 1))
//                            .color(ChatColor.BLUE);
//                    return entry;
//                }).forEachOrdered((entry) -> {
//                    msg.put(entry.create());
//                });

                for (Object line : msg) {
                    sender.spigot().sendMessage((BaseComponent[]) line);
                }
            });
        }
    }
}
