/*
 * Copyright (C) 2021 alexander
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
package jawamaster.jawapermissions.worldzone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author alexander
 */
public class WorldZoneScanner implements Runnable{

    @Override
    public void run() {
        Collection<Player> players = ImmutableList.copyOf(Bukkit.getServer().getOnlinePlayers());
        ImmutableMap<String, WorldZone> zones = ImmutableMap.copyOf(ZoneHandler.getWorldZones());
        
        for (Player player : players){
            if (zones.containsKey(player.getWorld())) {
                zones.get(player.getWorld()).handlePlayer(player);
            }
        }
    }
    
}
