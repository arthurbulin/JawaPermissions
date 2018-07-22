/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.handlers;

import java.io.File;
import jawamaster.jawapermissions.JawaPermissions;

/**
 *
 * @author Arthur Bulin
 */
public class MassRankChange {
    static File rankChangeFile;
    
    public static boolean CheckForMassChange() {
        rankChangeFile = new File(JawaPermissions.getPlugin().getDataFolder() + "/rankchange/ranks.yml");
        
        return true;
    }
    
}
