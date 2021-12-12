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

import net.jawasystems.jawacore.PlayerManager;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Jawamaster (Arthur Bulin)
 */
public class WorldZone {
    
//    private final JSONObject LOCATION;
//    private final JSONObject DIMENSIONS;
//    private final JSONObject CONFIGURATION;
//    private final Location CENTER;
    
    private final String NAME;
    private final String CREATOR;
    
    //Dimensions
    /** The shape of the zone. This is circle or square. **/
    private String shape;
    /** The x-radius from the center. **/
    private int xradius;
    /** The z-radius from the center. **/
    private int zradius;
    /** The x-coord of the center. **/
    private int xcenter;
    /** The z-coord of the center. **/
    private int zcenter;
    /** The world the zone exists in. **/
    private final World WORLD;
    
    //Settings
//    /** If the zone is inclusive. If inclusive the type will keep those defined by zone type
//     * within the zone. */
//    private boolean inclusive;
    /** The type defines who is kept in or out depending on the setting of inclusive.
     * This is rank, list, or permission */
    private String type;
    /** This will either teleport the user to the other side of the zone, or bonk them.
     * This can be a rank. */
    private String method;
    /** This is a JSONArray of ranks. */
    private JSONObject rankSettings;
    /** This is a JSONArray of ranks or user UUIDs. */
    private JSONArray userArray;
    /** Determines if the zone wall will damage the user on bonk. **/
    private boolean damage;
    
    //Messages
    /** A description message containing legacy color coding. */
    private String description = "";
    /** An enter message containing legacy color coding. */
    private String enterMessage = "";
    /** An exit message containing legacy color coding. */
    private String exitMessage = "";
    /** An enter deny message containing legacy color coding. */
    private String enterDeny = "";
    /** An exit deny message containing legacy color coding. */
    private String exitDeny = "";

    /** Creates a world zone based on the input data.
     * @param zoneName The name of the zone
     * @param shape The shape, must be either circle or square
     * @param xradius The x-radius
     * @param zradius The y-radius
     * @param center The location of the zone's center
     * @param inclusive True if the zone is set to inclusive, false if the zone is not
     * @param type The test criteria of the zone
     * @param method Whether the zone bonks or teleports the user
     * @param creater The user who created the 
     */
    public WorldZone(String zoneName, String shape, int xradius, int zradius, Location center, boolean inclusive, String type, String method, String creater){
        this.NAME = zoneName;
        this.CREATOR = creater;
        
        this.shape = shape;
        this.xradius = xradius;
        this.zradius = zradius;
        this.xcenter = center.getBlockX();
        this.zcenter = center.getBlockZ();
        this.WORLD = center.getWorld();
        
//        this.inclusive = inclusive;
        this.damage = false;
        this.type = type;
        this.method = method;
        this.rankSettings = new JSONObject();
        createRankDefaults();
        this.userArray = new JSONArray();
    }
    
//    public WorldZone(String wbName, JSONObject location, String shape, int x, int z) {
//        this.NAME = wbName;
//        this.LOCATION = location;
//        
//        this.DIMENSIONS = new JSONObject();
//        this.DIMENSIONS.put("XSIZE", x);
//        this.DIMENSIONS.put("ZSIZE", z);
//        this.DIMENSIONS.put("SHAPE", shape);
//        
//        
//        this.CONFIGURATION = new JSONObject();
//        this.CONFIGURATION.put("MODE1", "INCLUSIVE"); //or EXCLUSIVE
//            //Inclusive should keep people defined by MODE2 inside but allow those not defined in mode to free passage
//            //Exclusive should keep people out who are NOT defined by MODE2
//        this.CONFIGURATION.put("MODE2", "RANK"); //or PERMISSION or LIST
//            //Defines the persons that the boarder applies too
//        //this.CONFIGURATION.put("MODE3", "");
//        //Mode 3 will be either a string rank, string permission, or jsonarray of user uuids
//        this.CONFIGURATION.put("MODE4", "BONK");
//            //Defines what happens when a player is denied enter or exit
//            //Bonk will "bonk" them back
//            //"TELEPORT" will send them to spawn
//        this.CONFIGURATION.put("DAMAGE", false);
//            //Defines if a player takes damage when hitting the barrier
//        this.CONFIGURATION.put("PORTALS", true); //or false to suppress portal creation
//            //Defines whether portals are allowed or not
//        this.CONFIGURATION.put("EXITMESSAGE", "&eExiting " + wbName + " boarder zone"); //or EXCLUSIVE
//        this.CONFIGURATION.put("ENTERMESSAGE", "&bEntering " + wbName + " boarder zone");
//        this.CONFIGURATION.put("ENTERDENY", "&cYou may not enter the " + wbName + " boarder zone");
//        this.CONFIGURATION.put("EXITDENY", "&cYou may not exit the " + wbName + " boarder zone");
//        //wb.put("CONFIGURATION", CONFIGURATION);
//        
//        this.CENTER = LocationDataHandler.unpackLocation(location);
//        
//        //WBS.put(wbName, wb);
//        //saveWorldBoarders();
//    }
    
    public WorldZone(JSONObject worldZoneObj){
        this.NAME = worldZoneObj.getString("name");
        this.CREATOR = worldZoneObj.getString("creator");
        
        //Shape data
        this.shape = worldZoneObj.getJSONObject("shape-data").getString("shape");
        this.xradius = worldZoneObj.getJSONObject("shape-data").getInt("xradius");
        this.zradius = worldZoneObj.getJSONObject("shape-data").getInt("zradius");
        this.xcenter = worldZoneObj.getJSONObject("shape-data").getInt("xcenter");
        this.zcenter = worldZoneObj.getJSONObject("shape-data").getInt("zcenter");
        this.WORLD = Bukkit.getWorld(worldZoneObj.getJSONObject("shape-data").getString("world"));
        
        //Settings
//        this.inclusive = worldZoneObj.getJSONObject("settings").getBoolean("inclusive");
        this.damage = worldZoneObj.getJSONObject("settings").getBoolean("damage");
        this.type = worldZoneObj.getJSONObject("settings").getString("type");
        this.method = worldZoneObj.getJSONObject("settings").getString("method");
        this.rankSettings = worldZoneObj.getJSONObject("settings").getJSONObject("rank-settings");
        this.userArray = worldZoneObj.getJSONObject("settings").getJSONArray("user-array");
        
        //Messages
        this.description = worldZoneObj.getJSONObject("messages").getString("description");
        this.enterMessage = worldZoneObj.getJSONObject("messages").getString("enter-message");
        this.exitMessage = worldZoneObj.getJSONObject("messages").getString("exit-message");
        this.enterDeny = worldZoneObj.getJSONObject("messages").getString("enter-deny");
        this.exitDeny = worldZoneObj.getJSONObject("messages").getString("exit-deny");
    }
    
    /** Export a world zone to a JSON Object
     * @return The zone settings in JSON format
     */
    private JSONObject exportZone(){
        JSONObject worldZone = new JSONObject();
        worldZone.put("name", this.NAME);
        worldZone.put("creator", this.CREATOR);
        
        JSONObject shapeData = new JSONObject();
        shapeData.put("shape", this.shape);
        shapeData.put("xradius", this.xradius);
        shapeData.put("zradius", this.zradius);
        shapeData.put("xcenter", this.xcenter);
        shapeData.put("zcenter", this.zcenter);
        shapeData.put("world", this.WORLD.getName());
        worldZone.put("shape-data", shapeData);
        
        JSONObject settings = new JSONObject();
//        settings.put("inclusive", this.inclusive);
        settings.put("damage", this.damage);
        settings.put("type", this.type);
        settings.put("method", this.method);
        settings.put("rank-settings", this.rankSettings);
        worldZone.put("settings", settings);
        
        JSONObject messages = new JSONObject();
        messages.put("description", this.description);
        messages.put("enter-message", this.enterMessage);
        messages.put("exit-message", this.exitMessage);
        messages.put("enter-deny", this.enterDeny);
        messages.put("exit-deny", this.exitDeny);
        worldZone.put("messages", messages);

        return worldZone;
        
    }
    
    public void setRankSetting(String rank, String setting, String mode){
        if (!rankSettings.has("rank")) {
            rankSettings.put(rank,rankSettings.getJSONObject("defaults"));
        }
        rankSettings.getJSONObject(rank).put(setting, mode);
    }
    
    /** Generates an new default setting for ranks and installs into the settings object.
     */
    private void createRankDefaults(){
        JSONObject rankDefaults = new JSONObject();
        rankDefaults.put("mode", "passthrough");
        rankDefaults.put("build", true);
        rankSettings.put("default", rankDefaults);
    }
    
    public void handlePlayer(Player player){
        Vector vel = player.getVelocity();
        Vector newVel = new Vector((-2.0)*vel.getX(), vel.getBlockY()+2, (-2.0)*vel.getBlockZ());
        player.setVelocity(newVel);
        
    }
    
    /** Evaluates if a player is within the zone. This checks the super zone first, and if they are not within the super zone
     * this just returns false. It checks the super zone first to try to avoid the costly squaring and comparative calculations.
     * No variables are mutated, this is threadsafe.
     * @threadsafe
     * @param X The x location of the player
     * @param Z The z location of the player
     * @return True if the player is within the zone, false if the player is not in the zone.
     */
    public boolean isPlayerInZone(int X, int Z) {
        if (this.type.equals("square")) {
            return between(X, xcenter-xradius, xcenter+xradius) && between(Z, zcenter-zradius, zcenter+zcenter);
        } else if (between(X, xcenter-xradius, xcenter+xradius) && between(Z, zcenter-zradius, zcenter+zcenter)) {
            return (((X-xcenter)^2 / (xradius)^2) + ((Z-zcenter)^2 / (zradius)^2)) <= 1;
        } else {
            return false;
        }
    }
    
    /** Is a player allowed in the zone. If the player is not excluded from the zone.
     * @param player The player to test
     * @return True if the player is allowed, false otherwise.
     */
    public boolean isPlayerAllowedInZone(Player player){
        if (type.equals("rank")) {
            if (rankSettings.has(PlayerManager.getPlayerDataObject(player).getRank())) {
                return rankSettings.getString("mode").matches("passthrough|contain");
            } else {
                return rankSettings.getString("default").matches("passthrough|contain");
            }
        } else if (type.equals("list")) {
            //TODO user type is not done
            return false;
        } else if (type.equals("permission")) {
            //TODO permission type is not done
            return false;
        } else {
            return false;
        }
    }
    
    /** Is a player required to be within the zone. If the player is required in the zone.
     * @param player The player being checked
     * @return true if the player is required to be in the zone, false if otherwise
     */
    public boolean isPlayerRequiredInZone(Player player){
        if (type.equals("rank")){
            if (rankSettings.has(PlayerManager.getPlayerDataObject(player).getRank())) {
                return rankSettings.getString("mode").matches("contain");
            } else {
                return rankSettings.getString("default").matches("contain");
            }
        } else if (type.equals("list")) {
            //TODO user type is not done
            return false;
        } else if (type.equals("permission")) {
            //TODO permission type is not done
            return false;
        } else {
            return false;
        }
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
    
    /** Returns the maximum +X coordinate. 
     * @return The maximum +X coordinate
     */
    public int getMaxX(){
        return xcenter+xradius;
    }
    
    /** Returns the minimum X coordinate. 
     * @return The minimum X coordinate
     */
    public int getMinX(){
        return xcenter-xradius;
    }
    
    /** Returns the maximum Z coordinate. 
     * @return The maximum Z coordinate
     */
    public int getMaxZ(){
        return zcenter+zradius;
    }
    
    /** Returns the minimum Z coordinate. 
     * @return The minimum Z coordinate
     */
    public int getMinZ(){
        return zcenter-zradius;
    }
    
    /** Returns the X-center. 
     * @return The X-center
     */
    public int getXCenter(){
        return xcenter;
    }
    
    /** Returns the Z-center. 
     * @return The Z-center
     */
    public int getZCenter(){
        return zcenter;
    }
    
    /** Returns the shape. 
     * @return The shape
     */
    public String getShape(){
        return shape;
    }
    
    /** Returns the world. 
     * @return The world
     */
    public World getWorld(){
        return WORLD;
    }
    
//    /** Return inclusive state
//     * @return true if inclusive, false if not
//     */
//    public boolean getInclusiveness(){
//        return inclusive;
//    }
    
    /** Return damage state
     * @return true if the border does damage, false if not
     */
    public boolean getDamange(){
        return damage;
    }
    
    /** Return the type
     * @return the type
     */
    public String getType(){
        return type;
    }
    
    /** Return the method of border protection
     * @return The method: bonk, or teleport
     */
    public String getMethod(){
        return method;
    }
    
//    /** Return the method applicability array
//     * @return the JSONArray containing the applicability array
//     */
//    public JSONArray getMethodArray(){
//        return typeArray;
//    }
    
    public void setShape(String shape){
        this.shape = shape;
    }
    
    public void setRadius(Integer X, Integer Z){
        if (X != null){
            this.xradius = X;
        }
        if (Z != null){
            this.zradius = Z;
        }
    }
    
    public void setCenter(Integer X, Integer Z){
        if (X != null){
            this.xcenter = X;
        }
        if (Z != null){
            this.zcenter = Z;
        }
    }
    
//    public void setInclusive(boolean inclusive) {
//        this.inclusive = inclusive;
//    }
    
    public void setDamage(boolean damage){
        this.damage = damage;
    }
    
    public void setType(String type){
        this.type = type;
//        this.typeArray = new JSONArray();
    }
    
    public void setMethod(String method){
        this.method = method;
        
    }
    
//    public void addToTypeArray(String item){
//        this.typeArray.put(item);
//    }
//    
//    public void addToTypeArray(String[] items){
//        this.typeArray.putAll(items);
//    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public void setEnterMessage(String enterMessage){
        this.enterMessage = enterMessage;
    }
    
    public void setExitMessage(String exitMessage){
        this.exitMessage = exitMessage;
    }
    
    public void setEnterDeny(String enterDeny){
        this.enterDeny = enterDeny;
    }
    
    public void setExitDeny(String exitDeny){
        this.exitDeny = exitDeny;
    }

}
