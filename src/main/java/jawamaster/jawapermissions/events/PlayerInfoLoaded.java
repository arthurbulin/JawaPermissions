/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.events;

import java.util.Map;
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
    private static Map<String, Object> playerData;
    
    public PlayerInfoLoaded(Player who, Map<String, Object> data) {
        PlayerInfoLoaded.player = who;
        PlayerInfoLoaded.playerData = data;
        System.out.println(player.getName() + " has been loaded");
    }
    
    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }   
}
