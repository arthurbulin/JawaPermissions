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
package jawamaster.jawapermissions.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.JawaPermissions;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

/** Handle AutoElevation processes
 *
 * @author alexander
 */
public class AutoElevateHandler {

    /** The Logger Object for this class.
     */
    private static final Logger LOGGER = Logger.getLogger("FileHandler");
    //Declare HashMap Storage for the loaded Permissions
    public static final HashMap<UUID, String> AUTOELEVATELIST = new HashMap();
    
    /** Reads in and parses the autoelevate.txt file and returns a HashMap of the player's
     * UUID and the String representation of the rank assigned.
     * @return 
     */
    public static void getAutoElevateList() {
        File autoElevateList = new File(JawaPermissions.getPlugin().getDataFolder() + "/autoelevate.txt");

        if (autoElevateList.exists()) {
            try { //Proceed if the file exists
                BufferedReader reader;

                try { //Try to read the file
                    reader = new BufferedReader(new FileReader(autoElevateList));
                    String line = reader.readLine();
                    UUID uuid;
                    String rank;

                    while (line != null) { //iterate over the reader until there are no lines left
                        String[] splitLine = line.split(":");
                        if (splitLine.length != 2) { //verify incoming string data is is of format UUID:String
                            LOGGER.log(Level.SEVERE, "{0} is malformed in autoelevate.txt. Please fix it.", line);
                            continue;
                        }
                        try {
                            uuid = UUID.fromString(splitLine[0]);
                            rank = line.split(":")[1];
                        } catch (IllegalArgumentException ex) {
                            LOGGER.log(Level.SEVERE, "{0} is an invalid UUID. Please check your autoelevate.txt", line);
                            continue;
                        }

                        AUTOELEVATELIST.put(uuid, rank);
                        line = reader.readLine();
                    }

                    reader.close();
                    LOGGER.log(Level.INFO, "{0} UUIDs loaded into the autoelevate list.", AUTOELEVATELIST.size());

                } catch (FileNotFoundException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    LOGGER.log(Level.SEVERE, "A problem was encountered reading autoelevate.txt. No UUIDS loaded.");
                }

            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

        } else {
            LOGGER.log(Level.INFO, "No autoelevate.txt exists. No UUIDS loaded.");
        }
    }

    /** Saves the autoelevate file.
     */
    public static void saveAutoElevateList() {
        File maintenanceList = new File(JawaPermissions.getPlugin().getDataFolder() + "/autoelevate.txt");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(maintenanceList);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            for (UUID uuid : AUTOELEVATELIST.keySet()) {
                writer.write(uuid.toString() + ":" + AUTOELEVATELIST.get(uuid));
                writer.newLine();
            }

            writer.close();
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE,"Something went wrong and the autoelevate.txt file wasn't found.");
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,"Something went wrong and the autoelevate.txt file couldn't be written too.");
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /** Returns true if the player is in the AutoElevation list.
     * @param player Player to check
     * @return 
     */
    public static boolean doAutoElevate(Player player){
        return AUTOELEVATELIST.containsKey(player.getUniqueId());
    }
    
    /** Returns the rank the player should be elevated to.
     * @param player Player to rank
     * @return 
     */
    public static String getElevateRank(Player player){
        return AUTOELEVATELIST.get(player.getUniqueId());
    }
    
    /** Removes a player and saves the list.
     * @param uuid 
     */
    public static void removePlayer(UUID uuid){
        AUTOELEVATELIST.remove(uuid);
        saveAutoElevateList();
    }

    public static void evaluate(Player target, PlayerDataObject pdo){
        if (doAutoElevate(target)) {
            pdo.setRank(getElevateRank(target), UUID.fromString("00000000-0000-0000-0000-000000000000"), PermissionsHandler.getRankColor(AutoElevateHandler.AUTOELEVATELIST.get(target.getUniqueId())));
            target.sendMessage(ChatColor.GREEN + " > You have been tagged by the server's autoelevation protocol. You have been given the rank of " 
                    + ChatColor.BLUE + PermissionsHandler.getRankColor(getElevateRank(target)) + getElevateRank(target) + ChatColor.GREEN + ". Disconnect and rejoin to get your new rank.");
            removePlayer(target.getUniqueId());
            
        }
    }
}
