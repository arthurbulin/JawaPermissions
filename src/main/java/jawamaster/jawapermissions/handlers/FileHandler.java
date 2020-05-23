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

/**
 *
 * @author alexander
 */
public class FileHandler {

    /** Reads in and parses the autoelevate.txt file and returns a HashMap of the player's
     * UUID and the String representation of the rank assigned.
     * @return 
     */
    public static HashMap<UUID, String> getAutoElevateList() {
        File autoElevateList = new File(JawaPermissions.getPlugin().getDataFolder() + "/autoelevate.txt");
        HashMap<UUID, String> autoElevMap = new HashMap(); //return this

        if (autoElevateList.exists()) {
            try { //Proceed if the file exists
                BufferedReader reader;

                try { //Try to read the file
                    reader = new BufferedReader(new FileReader(autoElevateList));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
                    Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, "A problem was encountered reading autoelevate.txt. No UUIDS loaded.");
                    //System.out.println(JawaPermissions.pluginSlug + "A problem was encountered reading autoelevate.txt. No UUIDS loaded.");
                    return autoElevMap; //short circuit it here
                }

                String line = reader.readLine();
                UUID uuid;
                String rank;

                while (line != null) { //iterate over the reader until there are no lines left
                    String[] splitLine = line.split(":");
                    if (splitLine.length != 2) { //verify incoming string data is is of format UUID:String
                        Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, "{0} is malformed in autoelevate.txt. Please fix it.", line);
//                    System.out.println(JawaPermissions.pluginSlug + line + " is malformed in autoelevate.txt. Please fix it.");
                        continue;
                    }
                    try {
                        uuid = UUID.fromString(splitLine[0]);
                        rank = line.split(":")[1];
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, "{0} is an invalid UUID. Please check your autoelevate.txt", line);
//                    System.out.println(JawaPermissions.pluginSlug + line + " is an invalid UUID. Please check your autoelevate.txt");
                        continue;
                    }

                    autoElevMap.put(uuid, rank);
                    line = reader.readLine();
                }

                reader.close();
                Logger.getLogger(FileHandler.class.getName()).log(Level.INFO, "{0} UUIDs loaded into the autoelevate list.", autoElevMap.size());
//            System.out.println(JawaPermissions.pluginSlug + autoElevMap.size() + " UUIDs loaded into the autoelevate list.");

            } catch (IOException ex) {
                Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
                return autoElevMap;
            }

        } else {
            System.out.println(JawaPermissions.pluginSlug + "No autoelevate.txt exists. No UUIDS loaded.");
        }
        return autoElevMap;
    }

    /** Saves the autoElevate file
     */
    public static void saveAutoElevateList() {
        File maintenanceList = new File(JawaPermissions.getPlugin().getDataFolder() + "/autoelevate.txt");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(maintenanceList);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            for (UUID uuid : JawaPermissions.autoElevate.keySet()) {
                writer.write(uuid.toString() + ":" + JawaPermissions.autoElevate.get(uuid));
                writer.newLine();
            }

            writer.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Something went wrong and the autoelevate.txt file wasn't found.");
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Something went wrong and the autoelevate.txt file couldn't be written too.");
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
