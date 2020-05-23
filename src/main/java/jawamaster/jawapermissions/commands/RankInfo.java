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
package jawamaster.jawapermissions.commands;

import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author alexander
 */
public class RankInfo implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        String usage = ChatColor.GREEN + "> To get a rank's description/requirements run /rankinfo [description|requirements] <rank>";

        // if the args are empty
        if (args == null || args.length == 0) {
            String rankList = ChatColor.GREEN + " > The available ranks are: ";
            for(String rank : PermissionsHandler.getRankList()){
                rankList += PermissionsHandler.getRankColor(rank) + rank + ChatColor.WHITE + ", ";
            }
            rankList = rankList.substring(0, rankList.length()-2);
            commandSender.sendMessage(usage);
            commandSender.sendMessage(rankList);
        } 
        //If the args contain 1 thing
        else if (args.length == 1) {
            //If the rank doesn't exit
            if (!PermissionsHandler.rankList().contains(args[0].toLowerCase())) {
                commandSender.sendMessage(ChatColor.RED + "> Error: That rank does not exist.");
                return true;
            }

            //Send all the values
            PermissionsHandler.sendDescription(commandSender, args[0]);
            PermissionsHandler.sendRequirements(commandSender, args[0]);
            
        } 
        //If the args are 2 or more
        else if (args.length >= 2) {
            //if the rank doesn't exit
            if (!PermissionsHandler.rankList().contains(args[1].toLowerCase())) {
                commandSender.sendMessage(ChatColor.RED + "> Error: That rank does not exist.");
                return true;
            }
            
            //For whichever option send the corresponding info
            if (args[0].equalsIgnoreCase("description")) {
                PermissionsHandler.sendDescription(commandSender, args[1]);
            } else if (args[0].equalsIgnoreCase("requirements")) {
                PermissionsHandler.sendRequirements(commandSender, args[1]);
            } else {
                commandSender.sendMessage(ChatColor.RED + "> Error: " + args[0] + " is not a valid argument.");
                commandSender.sendMessage(usage);
            }
        }

        return true;
    }



}
