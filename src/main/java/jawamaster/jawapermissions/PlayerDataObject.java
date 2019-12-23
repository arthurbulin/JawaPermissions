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
import jawamaster.jawapermissions.handlers.ESHandler;
import jawamaster.jawapermissions.handlers.PlayerDataHandler;
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
    private JSONObject homeData;
    private JSONObject updateData;

    public PlayerDataObject(UUID player) {
        this.player = player;
    }

    public void addBanData(Map banData) {
        this.banData = new JSONObject(banData);
    }

    public void addPlayerData(Map playerData) {
        this.playerData = new JSONObject(playerData);
    }

    public void addHomeData(Map homeData) {
        this.homeData = new JSONObject(homeData);
    }

    public String getPlayerUUID() {
        return player.toString();
    }

    public void addSearchData(String index, Map data) {
        switch (index) {
            case "players": {
                this.playerData = new JSONObject(data);
            }
            case "bans": {
                this.banData = new JSONObject(data);
            }
            case "homes": {
                this.homeData = new JSONObject(data);
            }
        }
    }

    public JSONObject getPlayerData() {
        return playerData;
    }

    public boolean containsBanData() {
        return banData != null;
    }

    public boolean containsPlayerData() {
        return playerData != null;
    }

    public boolean containsHomeData() {
        return homeData != null;
    }

    //##########################################################################
    //#   Player ban gets
    //##########################################################################
    public String getLatestBanDate() {
        return (String) playerData.get("latest-ban");
    }

    private JSONObject getBanEntry(String banDateTime) {
        return (JSONObject) banData.get(banDateTime);
    }

    public String getBanReason(String banDateTime) {
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

    public String unbannedOn(String banDateTime) {
        return (String) getBanEntry(banDateTime).get("unbanned-on");
    }

    public boolean getBanState(String banDateTime) {
        return Boolean.valueOf((String) getBanEntry(banDateTime).get("active"));
    }

    public boolean isConsoleBan(String banDateTime) {
        return Boolean.valueOf((String) getBanEntry(banDateTime).get("via-console"));
    }

    public boolean isConsoleUnban(String banDateTime) {
        return Boolean.valueOf((String) getBanEntry(banDateTime).get("unbanned-via-console"));
    }

    public Set getListOfBans() {
        return banData.keySet();
    }

    //##########################################################################
    //#   Player data gets
    //##########################################################################
    public String getRank() {
        return (String) playerData.get("rank");
    }

    public String getName() {
        return (String) playerData.get("name");
    }

    public int getPlayTime() {
        return (int) playerData.get("play-time");
    }

    public LocalDateTime getLastLogin() {
        return LocalDateTime.parse((String) playerData.get("last-login"));
    }

    public LocalDateTime getLastLogout() {
        return LocalDateTime.parse((String) playerData.get("last-logout"));
    }

    public JSONArray getIPArray() {
        return new JSONArray(String.valueOf(playerData.get("ips")));
    }

    public boolean isBanned() {
        return Boolean.valueOf(String.valueOf(playerData.get("banned")));
    }

    public JSONArray getNameArray() {
        return new JSONArray(String.valueOf(playerData.get("name-data")));

    }

    public JSONObject getLatestBan() {
        return getBanEntry(getLatestBanDate());
    }

    public String getIP() {
        return (String) playerData.get("ip");
    }

    //##########################################################################
    //#   Player home gets
    //##########################################################################
    public boolean homeExists(String homeName) {
        return homeData.keySet().contains(homeName);
    }

    /**
     * This will return the home entry in for that name. It will need to be
     * location processed to be usable.
     *
     * @param homeName
     * @return
     */
    public JSONObject getHome(String homeName) {
        return homeData.getJSONObject(homeName);
    }

    public Set getHomeEntries() {
        return homeData.keySet();
    }

    public boolean containsHome(String homeName) {
        if (containsHomeData()) {
            return homeData.keySet().contains(homeName);
        } else {
            return false;
        }
    }

    //##########################################################################
    //#   Player name gets
    //########################################################################## 
    public String getStar() {
        return playerData.getString("star");
    }

    public String getNickName() {
        return playerData.getString("nick");
    }

    public String getTag() {
        return playerData.getString("tag");
    }

    public JSONArray getNickData() {
        return playerData.getJSONArray("nick-data");
    }

    //##########################################################################
    //#   Data updates
    //##########################################################################
    public void addUpdateData() {
        updateData = new JSONObject();
    }

    /**
     * adds a nick and resolves the nick-data attribute for update. Should only
     * be used with PlayerDataObjects that contain a player's full data.
     *
     * @param nick
     */
    public void updateNick(String nick) {
        if (!nick.equals("")) {
            JSONArray nickData = PlayerDataHandler.nickData(nick, getNickData());
            if (nickData != null) {
                updateData.put("nick-data", nickData);
            }
        }
        updateData.put("nick", nick);
    }

    public void updateTag(String tag) {
        updateData.put("tag", tag);
    }

    public void updateStarData(String obj) {
        updateData.put("star", obj);
    }

    public void updateRank(String newRank, UUID adminUUID) {
        updateData.put("rank-data", PlayerDataHandler.createPlayerRankChangeData(getRank(), newRank, adminUUID.toString()));
        updateData.put("rank", newRank);
    }

    public void triggerAsyncUpdate() {
        ESHandler.asyncUpdateData(player.toString(), updateData);
    }
    //##########################################################################
    //#   Debug
    //##########################################################################

    public void spillData() {
        System.out.println("PlayerDataObject Spilling data for player: " + player.toString());
        System.out.println(playerData);

    }

}
