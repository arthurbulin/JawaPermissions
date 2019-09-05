/*
 * Copyright (C) 2019 alexander
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
package jawamaster.jawapermissions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author alexander
 */
public class PlayerDataObject {
    private UUID player;
    private JSONObject banData;
    private JSONObject playerData;
    
    public PlayerDataObject (UUID player){
        this.player = player;
    }
    
    public void addBanData(Map banData){
        this.banData = new JSONObject(banData);
    }
    
    public void addPlayerData(Map playerData){
        this.playerData = new JSONObject(playerData);
    }
    
    public String getPlayerUUID(){
        return player.toString();
    }
    
    public void addSearchData(String index, Map data){
        switch (index){
            case "players":{
                this.playerData = new JSONObject(data);
            }
            case "bans":{
                this.banData = new JSONObject(data);
            }
        }
    }
    
    public JSONObject getPlayerData(){
        return playerData;
    }
    
    public boolean containsBanData(){
        if (banData == null) return false;
        else return true;
    }
    
    public boolean containsPlayerData(){
        if (playerData == null) return false;
        else return true;
    }
    
    
    
    //Ban elements
    public String getLatestBanDate(){
        return (String) playerData.get("latest-ban");
    }
    
    private JSONObject getBanEntry(String banDateTime){
        return (JSONObject) banData.get(banDateTime);
    }
    
    public String getBanReason(String banDateTime){
        return (String) getBanEntry(banDateTime).get("reason");
    }

    public String getBannedBy(String banDateTime) {
        return (String) getBanEntry(banDateTime).get("banned-by");
    }

    public String getBannedUntil(String banDateTime) {
        return (String) getBanEntry(banDateTime).get("banned-until");
    }

    public String getBannedUnreason(String banDateTime) {
        return (String) getBanEntry(banDateTime).get("unreason");
    }

    public String getBannedUnBy(String banDateTime) {
        return (String) getBanEntry(banDateTime).get("unbanned-by");
    }
    
    public String unbannedOn(String banDateTime){
        return (String) getBanEntry(banDateTime).get("unbanned-on");
    }
    
    public boolean getBanState(String banDateTime) {
        return Boolean.valueOf((String) getBanEntry(banDateTime).get("active"));
    }
    
    public boolean isConsoleBan(String banDateTime){
        return Boolean.valueOf((String) getBanEntry(banDateTime).get("via-console"));
    }
    
     public boolean isConsoleUnban(String banDateTime){
        return Boolean.valueOf((String) getBanEntry(banDateTime).get("unbanned-via-console"));
    }
     
     public Set getListOfBans(){
         return banData.keySet();
     }
    
    
    
    //##########################################################################
    //#   Player data gets
    //##########################################################################
    public String getRank(){
        return (String) playerData.get("rank");
    }
    
    public String getName(){
        return (String) playerData.get("name");
    }
    
    public int getPlayTime() {
        return (int) playerData.get("play-time");
    }
    
    public LocalDateTime getLastLogin(){
        return LocalDateTime.parse((String) playerData.get("last-login"));
    }
    
    public LocalDateTime getLastLogout(){
        return LocalDateTime.parse((String) playerData.get("last-logout"));
    }
    
    public JSONArray getStarData(){
        return new JSONArray(String.valueOf(playerData.get("star")));
    }
    
    public JSONArray getIPArray(){
        return new JSONArray(String.valueOf(playerData.get("ips")));
    }
    
    public boolean isBanned(){
        return Boolean.valueOf(String.valueOf(playerData.get("banned")));
    }
    
    public JSONArray getNameArray(){
        return new JSONArray(String.valueOf(playerData.get("name-data")));
               
    }
    
    public JSONObject getLatestBan(){
        return getBanEntry(getLatestBanDate());
    }
    
    public String getIP(){
        return (String) playerData.get("ip");
    }
    
   
}
