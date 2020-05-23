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
package jawamaster.jawapermissions.listeners;

import jawamaster.jawapermissions.handlers.PermissionsHandler;
import net.jawasystems.jawacore.events.PlayerInfoLoaded;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author alexander
 */
public class OnPlayerInfoLoaded implements Listener {

    @EventHandler
    public static void playerInfoLoaded(PlayerInfoLoaded event) {
        //Once the player is indexed we don't need the elevation list entry anymore
//        if (JawaPermissions.autoElevate.containsKey(event.getPlayerDataObject().getUniqueID())) {
//            JawaPermissions.autoElevate.remove(event.getPlayerDataObject().getUniqueID());
//            FileHandler.saveAutoElevateList();
//        }
       // System.out.println("Rank color: " + PermissionsHandler.getRankColor(event.getPlayerDataObject().getRank()) + "Test");
        event.getPlayerDataObject().setRankColor(PermissionsHandler.getRankColor(event.getPlayerDataObject().getRank()));
        //event.getPlayer().updateCommands();
    }

}
