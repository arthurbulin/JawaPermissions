/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import jawamaster.jawapermissions.handlers.PermissionsHandler;

/**
 *
 * @author Arthur Bulin
 */
public class JawaPermissibleBase extends PermissibleBase {

    private final Player player;
    private final PermissionsHandler handler;
    
    //Testing something here
    private static String rank;
    
    public JawaPermissibleBase (Player player, JawaPermissions plugin) {
        super(player);
        this.player = player;
        this.handler = JawaPermissions.handler;
    }
    
    @Override
    public void recalculatePermissions() {
        
    }
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        recalculatePermissions();
        return null;
    }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        recalculatePermissions();
        return null;
    }
    
    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        recalculatePermissions();
    }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        recalculatePermissions();
        return null;
    }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        recalculatePermissions();
        return null;
    }
        
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return new HashSet<>();
    }
    
    @Override
    public boolean hasPermission(String arg0) {
        //This will give op all permissions
        return handler.has(player, arg0.toLowerCase());
        //return isOp() || handler.has(player, arg0.toLowerCase());
    }
    
    @Override
    public boolean hasPermission(Permission arg0) {
        return hasPermission(arg0.getName());
    }
    
    @Override
    public boolean isPermissionSet(String arg0) {
        return true;
    }
    
    @Override
    public boolean isPermissionSet(Permission arg0) {
        return true;
    }

    @Override
    public boolean isOp() {
        return hasPermission("bukkit.base.op");
    }

    @Override
    public void setOp(boolean value) {
        player.setOp(value);
        recalculatePermissions();
    }
    
    private boolean isBaseOp(){
        return player.isOp();
    }

}