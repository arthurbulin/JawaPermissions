/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Arthur Bulin
 */
public class PlayerRankChange extends Event{
    private static final HandlerList handlers = new HandlerList();
    static Player player;
    private static String rank;
    
    public PlayerRankChange(Player who, String newRank) {
        PlayerRankChange.player = who;
        PlayerRankChange.rank = newRank;
        System.out.println("PlayerRankChangeCalled for " + who.getDisplayName() + " with rank " + newRank);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getRank() {
        return rank;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    } 
}
