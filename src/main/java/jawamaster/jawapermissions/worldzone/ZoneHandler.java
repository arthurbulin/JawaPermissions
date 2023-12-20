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
package jawamaster.jawapermissions.worldzone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import net.jawasystems.jawacore.handlers.JSONHandler;
import net.jawasystems.jawacore.handlers.LocationDataHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.JSONObject;

/**
 *
 * @author Jawamaster (Arthur Bulin)
 */
public class ZoneHandler {

    private static final Logger LOGGER = Logger.getLogger("JawaPermission][WorldBorder");
    private static final HashMap<String,WorldZone> WORLDZONES = new HashMap();
    
    private static final HashMap<UUID,HashMap> ALLOWEDIN = new HashMap();
    private static final HashMap<UUID,HashMap> REQUIREDIN = new HashMap();
    
    
    public static HashMap<String,WorldZone> getWorldZones(){
        return WORLDZONES;
    }
    
    /** Load World Boarders from file.
     * 
     */
    public static void loadWorldBorders() {
        
        JSONObject worldborders = JSONHandler.LoadJSONConfig(JawaPermissions.getPlugin(), "/worldborders.json");
        if (!worldborders.isEmpty()) {
            for (String key : worldborders.keySet()) {
                //WORLDZONES.put(key, worldborders.getJSONObject(key));
            }
            LOGGER.log(Level.INFO, "{0} World Borders have been loaded.", WORLDZONES.size());
        } else {
            LOGGER.log(Level.INFO, "No World Borders were found to load");
        }
    }
    
    /** Save World Boarders to file.
     */
    public static void saveWorldBorders() {
        JSONObject wbs = new JSONObject();
        for (String kitname : WORLDZONES.keySet()){
            wbs.put(kitname, WORLDZONES.get(kitname));
        }
        JSONHandler.WriteJSONToFile(JawaPermissions.getPlugin(), "/worldborders.json", wbs);
        Logger.getLogger(JSONHandler.class.getName()).log(Level.INFO, "{0} World Borders saved to file", WORLDZONES.size());
    }
    
    
    
    public static void createWorldBorder(String wbName, Location location, String shape, int x, int z) {
        JSONObject wb = new JSONObject();
        wb.put("NAME", wbName);
        wb.put("LOCATION", LocationDataHandler.packLocation(location));
        
        JSONObject dimensions = new JSONObject();
            //X radi
            dimensions.put("XSIZE", x);
            //Z radi
            dimensions.put("ZSIZE", z);
            dimensions.put("SHAPE", shape);
        wb.put("DIMENSIONS", dimensions);
        
        JSONObject configuration = new JSONObject();
            configuration.put("MODE1", "INCLUSIVE"); //or EXCLUSIVE
            //Inclusive should keep people defined by MODE2 inside but allow those not defined in mode to free passage
            //Exclusive should keep people out who are NOT defined by MODE2
            configuration.put("MODE2", "RANK"); //or PERMISSION or LIST
            //Defines the persons that the boarder applies too
            configuration.put("MODE3", "BONK");
            //Defines what happens when a player is denied enter or exit
            //Bonk will "bonk" them back
            //"TELEPORT" will send them to spawn
            configuration.put("MODE4", false);
            //Mode4 defines if a player takes damage when hitting the barrier
            configuration.put("PORTALS", true); //or false to suppress portal creation
            //Defines whether portals are allowed or not
            configuration.put("EXITMESSAGE", "&eExiting " + wbName + " border zone"); //or EXCLUSIVE
            configuration.put("ENTERMESSAGE", "&bEntering " + wbName + " border zone");
            configuration.put("ENTERDENY", "&cYou may not enter the " + wbName + " border zone");
            configuration.put("EXITDENY", "&cYou may not exit the " + wbName + " border zone");
        wb.put("CONFIGURATION", configuration);
        
//        WORLDZONES.put(wbName, wb);
        saveWorldBorders();
    }
    
    public static void generateZoneMap(){
        
    }
    
    public static void checkPlayers(List<Player> players){
        //for each player
        players.forEach((player) -> {
            Set<String> superZones = new HashSet();
            //check if a player is in the super zone
            
                //check if a player falls in the zone
        });
            
    }
    
    public static void setPermissionsForPlayer(Player player){
        HashMap<String,Boolean> allowedIn = new HashMap();
        HashMap<String,Boolean> requiredIn = new HashMap();
        for(String zone : WORLDZONES.keySet()){
            allowedIn.put(zone, WORLDZONES.get(zone).isPlayerAllowedInZone(player));
            requiredIn.put(zone, WORLDZONES.get(zone).isPlayerRequiredInZone(player));
        }
        ALLOWEDIN.put(player.getUniqueId(), allowedIn);
        REQUIREDIN.put(player.getUniqueId(), requiredIn);
    }
    
    private static void setWorldZoneScan(WorldZone zone, Player player){
        //zone name, allowed in zone, required in zone, xradius, zradius, xcenter, zcenter
        Bukkit.getScheduler().runTaskLaterAsynchronously(JawaPermissions.getPlugin(), () -> {
            
        }, 5);
    }

}
