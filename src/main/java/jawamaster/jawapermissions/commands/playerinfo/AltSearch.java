/*
 * Copyright (C) 2020 Jawamaster (Arthur Bulin)
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
package jawamaster.jawapermissions.commands.playerinfo;

import java.util.logging.Level;
import java.util.logging.Logger;
import jawamaster.jawapermissions.handlers.PlayerInfoHandler;
import net.jawasystems.jawacore.dataobjects.PlayerDataObject;
import net.jawasystems.jawacore.handlers.ESHandler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Jawamaster (Arthur Bulin)
 */
public class AltSearch extends Thread {
    public AltSearch() {
        
    }
    
    @Override
    public void run() {
        while (true){
            JSONObject requestObj;
            synchronized (this) {
                requestObj = PlayerInfoHandler.pollAltSearchQueue();
            }
            
            if (requestObj != null){
                PlayerDataObject pdo = (PlayerDataObject) requestObj.get("pdo");
                JSONArray ar = pdo.getIPArray();
//                ESHandler.runAltSearch(ar);
            }
            else { //Wait
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
//                        Logger.getLogger(CrossLinkOutput.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
    }
}
