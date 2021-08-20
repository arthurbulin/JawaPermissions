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

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.LocationDataHandler;
import org.bukkit.Location;
import org.json.JSONObject;

/**
 *
 * @author Jawamaster (Arthur Bulin)
 */
public class WorldZone {
    
    private final String NAME;
    private final JSONObject LOCATION;
    private final JSONObject DIMENSIONS;
    private final JSONObject CONFIGURATION;
    private final Location CENTER;
    
    
    public WorldZone(String wbName, JSONObject location, String shape, int x, int z) {
        this.NAME = wbName;
        this.LOCATION = location;
        
        this.DIMENSIONS = new JSONObject();
        this.DIMENSIONS.put("XSIZE", x);
        this.DIMENSIONS.put("ZSIZE", z);
        this.DIMENSIONS.put("SHAPE", shape);
        
        
        this.CONFIGURATION = new JSONObject();
        this.CONFIGURATION.put("MODE1", "INCLUSIVE"); //or EXCLUSIVE
            //Inclusive should keep people defined by MODE2 inside but allow those not defined in mode to free passage
            //Exclusive should keep people out who are NOT defined by MODE2
        this.CONFIGURATION.put("MODE2", "RANK"); //or PERMISSION or LIST
            //Defines the persons that the boarder applies too
        //this.CONFIGURATION.put("MODE3", "");
        //Mode 3 will be either a string rank, string permission, or jsonarray of user uuids
        this.CONFIGURATION.put("MODE4", "BONK");
            //Defines what happens when a player is denied enter or exit
            //Bonk will "bonk" them back
            //"TELEPORT" will send them to spawn
        this.CONFIGURATION.put("DAMAGE", false);
            //Defines if a player takes damage when hitting the barrier
        this.CONFIGURATION.put("PORTALS", true); //or false to suppress portal creation
            //Defines whether portals are allowed or not
        this.CONFIGURATION.put("EXITMESSAGE", "&eExiting " + wbName + " boarder zone"); //or EXCLUSIVE
        this.CONFIGURATION.put("ENTERMESSAGE", "&bEntering " + wbName + " boarder zone");
        this.CONFIGURATION.put("ENTERDENY", "&cYou may not enter the " + wbName + " boarder zone");
        this.CONFIGURATION.put("EXITDENY", "&cYou may not exit the " + wbName + " boarder zone");
        //wb.put("CONFIGURATION", CONFIGURATION);
        
        this.CENTER = LocationDataHandler.unpackLocation(location);
        
        //WBS.put(wbName, wb);
        //saveWorldBoarders();
    }
    
    public WorldZone(JSONObject wbObj){
        this.NAME = wbObj.getString("NAME");
        this.LOCATION = wbObj.getJSONObject("LOCATION");
        this.DIMENSIONS = wbObj.getJSONObject("DIMENSIONS");
        this.CONFIGURATION = wbObj.getJSONObject("CONFIGURATION");
        
        this.CENTER = LocationDataHandler.unpackLocation(this.LOCATION);
    }
    
    public void handlePlayer(PlayerDataObject pdo){
        //Are they in the right world
        if (pdo.getPlayer().getLocation().getWorld().equals(CENTER.getWorld())){
            boolean inclusive = this.CONFIGURATION.getString("MODE1").equals("INCLUSIVE");
            if (CONFIGURATION.getString("MODE2").equals("RANK")) {
                if (inclusive && pdo.getRank().equalsIgnoreCase(this.CONFIGURATION.getString("MODE3"))){
                    
                }
            }
        }
    }
    
//    public boolean playerInZone(String worldName, int X, int Z, JSONObject location, JSONObject dimension) {
//        
//    }
    
    public boolean playerInZone(String worldName, int X, int Z) {
        if (worldName.equals(this.LOCATION.getString("world"))){
            if (this.DIMENSIONS.getString("SHAPE").equals("CIRCLE")){
                return round(sqrt((CENTER.getBlockX() + X)^2 + (CENTER.getBlockZ() + Z)^2)) < this.DIMENSIONS.getInt("XSIZE");
            } else {
                return (between(X, this.DIMENSIONS.getInt("XSIZE"), CENTER.getBlockX()) && between(Z, this.DIMENSIONS.getInt("ZSIZE"), CENTER.getBlockZ()));
            }
        } else
            return false;
    }
    
    /** Given a value to check this will return true if between value1 and value2
     * value1 and value2 do not have to be in order as this method will detect which
     * is the upper and lower value.
     * @param valueToCheck The value to be evaluated
     * @param value1 One value of the bound, which does not matter
     * @param value2 The other value
     * @return True if valueToCheck falls between value1 and value2
     */
    private boolean between(int valueToCheck, int value1, int value2){
        if (value1 > value2){
            return (valueToCheck < value1 ) && (valueToCheck > value2);
        } else {
            return (valueToCheck > value1 ) && (valueToCheck < value2);
        }
        
    }
    
}
