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

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import jawamaster.jawapermissions.handlers.BanHandler;
import net.jawasystems.jawacore.JawaCore;
import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author alexander
 */
public class BanInfo implements CommandExecutor{
    
    private static final Logger LOGGER = Logger.getLogger("BanInfo");
    private final HashMap<Integer, Integer> REQUESTTOTASKMAP = new HashMap();
    private final HashMap<Integer, Integer> REQUESTITERATIONCOUNT = new HashMap();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        
        String[] usage = new String[]{
            "List a player's ban info - /baninfo list <player>",
            "Get specific ban info - /baninfo info <player> <ban id>",
            "Get help - /baninfo help"};
        
        
        if (args.length > 1){
            if (PlayerManager.isPlayerInCache(args[1])){
                returnPlayerDataObject(args, commandSender, PlayerManager.getPlayerDataObject(args[1]));
            } else {
                int requestID = new Random().nextInt();
                PlayerManager.requestCaching(args[1], requestID);
                REQUESTITERATIONCOUNT.put(requestID, 0);
                
                int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(JawaPermissions.getPlugin(), () -> {
                    Boolean status = PlayerManager.getRequestStatus(requestID);
                        if ( status != null){
                            if (status) {
                                if (JawaPermissions.debug) LOGGER.log(Level.INFO, "offline player request status true for {0}for player {1}", new Object[]{requestID, args[1]});
                                PlayerDataObject target = PlayerManager.getOfflinePlayerFromCache(args[1]);
                                returnPlayerDataObject(args, commandSender, target);
                                PlayerManager.completeRequest(requestID);
                                REQUESTITERATIONCOUNT.remove(requestID);
                                Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(requestID));
                                REQUESTTOTASKMAP.remove(requestID);
                                
                            } else {
                                commandSender.sendMessage(ChatColor.RED + " > Error: That player is not found in the offline cache! Try their actual minecraft name instead of nickname.");
                                REQUESTITERATIONCOUNT.remove(requestID);
                                Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(requestID));
                                REQUESTTOTASKMAP.remove(requestID);
                           }
                        } else {
                            int count = REQUESTITERATIONCOUNT.get(requestID) + 1;
                            REQUESTITERATIONCOUNT.put(requestID, count);
                            if (count > 27) {
                                LOGGER.log(Level.SEVERE, "A cache request was not answered");
                                commandSender.sendMessage(ChatColor.RED + " > Error: The database never responded to the request. Contact your server administrator.");
                                REQUESTITERATIONCOUNT.remove(requestID);
                                Bukkit.getServer().getScheduler().cancelTask(REQUESTTOTASKMAP.get(requestID));
                                REQUESTTOTASKMAP.remove(requestID);
                            }
                        }
                }, 5, 2);
                REQUESTTOTASKMAP.put(requestID, task);

            }
        } else {
            commandSender.sendMessage(usage);
        }
        return true;
    }
    
    private void returnPlayerDataObject(String[] args, CommandSender commandSender, PlayerDataObject pdObject){
        if (pdObject == null) {
                commandSender.sendMessage(ChatColor.RED + " > Error: That player wasn't found! Try their actual minecraft name instead of nickname.");
                return;
            }

            if (!pdObject.containsBanData()) {
                commandSender.sendMessage(ChatColor.RED + " > Error: That player doesn't seem to contain any ban data. This means they have never been banned.");
                return;
            }
            if (args[0].equalsIgnoreCase("list")) {
                BanHandler.listBans(commandSender, args[1], pdObject);
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!pdObject.isBanIDValid(args[2])) {
                    commandSender.sendMessage(ChatColor.RED + " > Error: " + args[2] + " does not appear to be a valid ban ID. Remember ban IDs are case sensative.");
                    return;
                }
                
                Bukkit.getServer().getScheduler().runTaskAsynchronously(JawaCore.plugin, new Runnable() {
                    @Override
                    public void run() {
                        BanHandler.getBanInfo(commandSender, args[2], pdObject);
                    }
                });
            } else {
                commandSender.sendMessage(ChatColor.RED + " > Error: " + args[0] + " is not understood");
                return;
            }
    }
    
    
    
    
    
}
