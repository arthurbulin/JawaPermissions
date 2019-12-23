/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.events;

import java.util.Map;
import jawamaster.jawapermissions.PlayerDataObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerInfoLoaded extends Event{
    private static final HandlerList handlers = new HandlerList();
    private static Player player;
    private static PlayerDataObject playerData;
    
    
    public PlayerInfoLoaded(Player who, PlayerDataObject data) {
        super(true);
        PlayerInfoLoaded.player = who;
        PlayerInfoLoaded.playerData = data;
        System.out.println(player.getName() + " has been loaded");
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public PlayerDataObject getPlayerDataObject(){
        return playerData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }   
}
