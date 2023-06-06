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

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;
import sun.jvm.hotspot.types.AddressField;

/**
 *
 * @author Jawamaster (Arthur Bulin)
 */
public class PlayerInfoHandler {

    private static final Logger LOGGER = Logger.getLogger("JawaPermissions][PlayerInfoHandler");
    private static Queue<JSONObject> requests = new LinkedBlockingQueue<>();
    private static AltSearch altSearchThread;
    private static DatabaseReader dbReader;
    
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
        
//        JSONObject requestObj = new JSONObject();
//        requestObj.put("sender", target);
//        requestObj.put("pdo", target);
//        
//        requests.add(requestObj);
//        
//        synchronized (requests) {
//            altSearchThread.notify();
//        }
        
        Bukkit.getServer().getScheduler().runTaskAsynchronously(JawaPermissions.getPlugin(), (BukkitTask t) -> {
            String ip = target.getIP();
            SearchHit[] hits = ESHandler.runAltSearch(ip);
            if (hits.length == 1) {
                sender.sendMessage(ChatColor.GREEN + "> No alts were found for " + ChatColor.BLUE + target.getPlainNick());
            } else {

                BaseComponent[] header = new ComponentBuilder(ChatColor.GREEN + "> Possible alts for: " + ChatColor.BLUE + target.getName()).create();
                sender.spigot().sendMessage(header);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(JawaPermissions.getPlugin(), () -> {
                    for (SearchHit hit : hits) {
                        if (!hit.getId().equals(target.getUniqueID().toString())) {
                            JSONObject hitMap = new JSONObject(hit.getSourceAsMap());
                            BaseComponent[] altInfo = new ComponentBuilder(ChatColor.GREEN + " > ")
                                    .append(hitMap.getString("name")).color(PermissionsHandler.getRankColor(hitMap.getString("rank")))
                                    .append(", with rank: ").color(ChatColor.GREEN)
                                    .append(hitMap.getString("rank")).color(PermissionsHandler.getRankColor(hitMap.getString("rank")))
                                    .reset()
                                    .append(" [Who is]")
                                    .color(ChatColor.BLUE)
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Who lookup")))
                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/who " + hitMap.getString("name")))
                                    .create();

                            sender.spigot().sendMessage(altInfo);
                        }
                    }
                });

            }

        });
    }
    
    public static void autoIPAltSearch(Player target) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(JawaPermissions.getPlugin(), (BukkitTask t) -> {
            String ip = target.getAddress().toString();
            SearchHit[] hits = ESHandler.runAltSearch(ip);
            if (hits.length > 1) {
                BaseComponent[] baseComp = new ComponentBuilder("[JawaBot] ").color(ChatColor.GRAY)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/who ".concat(target.getName())))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/who".concat(target.getName()))))
                        .italic(true)
                        .append("Player ").color(ChatColor.GREEN).italic(false)
                        .append(target.getName())
                        .append(" has ").color(ChatColor.GREEN)
                        .append(String.valueOf(hits.length)).color(ChatColor.RED)
                        .append(" likely alts").color(ChatColor.GREEN)
                        .create();
                Bukkit.getServer().getScheduler().runTask(JawaPermissions.plugin, () -> {
                    Bukkit.getServer().getOnlinePlayers().forEach(((player) -> {
                        if (player.hasPermission("jawachat.opchat")) {
                            player.spigot().sendMessage(baseComp);
                        }
                    }));
                });

            }
        });
    }
    
    /** Load the maxmind geoip database
     * @param databaseName The database name present in the plugin's data folder
     * @return True if the database exists and is readable. False if not.
     */
    public static boolean getIPDatabase(String databaseName){
        
        File database = new File(JawaPermissions.getPlugin().getDataFolder() + "/" + databaseName);
        
        if (database.exists()) {
            try {
                dbReader = new DatabaseReader.Builder(database).build();
                return true;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "{0} could not be read. GeoIP checking will not be possible.", databaseName);
                return false;
            }
        } else {
            LOGGER.log(Level.SEVERE, "{0} does not exist. GeoIP checking will not be possible.", databaseName);
            return false;
        }
    }
    
    /** Query the geoIP database and get string as city, state, country back. if not found
     * or some error occurs then an empty string is returned.
     * @param ip The ip address in question
     * @return 
     */
    public static String getIPGeoLocation(String ip){
        CityResponse response;
        try {
            response = dbReader.city(InetAddress.getByName(ip));
            return response.getCity().getName() + ", " + response.getLeastSpecificSubdivision().getName() + ", " + response.getCountry().getName() ;
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.WARNING, "ip: {0} could not be found do to an UnknownHostException", ip);
            if (JawaPermissions.debug) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return "Unknown";
        } catch (IOException | GeoIp2Exception ex) {
            LOGGER.log(Level.WARNING, "ip: {0} could not be found do to an IOException ot GeoIp2Exception", ip);
            if (JawaPermissions.debug) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return "Unknown";
        } 
        
    }


}
