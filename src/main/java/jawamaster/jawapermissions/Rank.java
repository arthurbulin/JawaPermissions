/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Arthur Bulin
 */
public class Rank {
    private final String rankName;
    private final int immunity;
    
    //Each map will contain an entry named after the world and it's data will be a set containing either perms or prohibs
    private final Map<String, Set<String>> permissions;
    private final Map<String, Set<String>> prohibitions;
    
    
    public Rank (String rankName, int immunity) {
        this.rankName = rankName;
        this.immunity = immunity;
        this.permissions = new HashMap();
        this.prohibitions = new HashMap();
    }
   
    //Add data methods
    
    public void addWorld(String world, List permissions, List prohibitions){
        this.permissions.put(world, new HashSet(permissions));
        this.prohibitions.put(world, new HashSet(prohibitions));
    }
    
    public void addPermissions(String world, List permissions){
        this.permissions.get(world).addAll(permissions);
    }
    
    public void addPermission(String world, String perm){
        this.permissions.get(world).add(perm);
    }
    
    public void addProhibitions(String world, List prohibitions){
        this.prohibitions.get(world).addAll(prohibitions);
    }
    
    public void addProhibition(String world, String proh){
        this.prohibitions.get(world).add(proh);
    }
    
    //Get Data methods
    
    public boolean hasPermission(String world, String perm) {
        //System.out.println("Has check. World: " + world + " Perm: "+perm);
        if (this.permissions.get(world).contains(perm)) return !hasProhibition(world, perm);
        else if (this.permissions.get(world).contains("*") || this.permissions.get(world).contains("jawapermissions.all")) return !hasProhibition(world, perm);
        else {
            if ("*".equals(perm)) return false; //Because fucking perworld inventory checks if player has * for some damn reason
            
            String testPerm = perm.substring(0, perm.lastIndexOf("."))+".*";
            
            if (this.permissions.get(world).contains(testPerm)) {
                this.permissions.get(world).add(perm); //This will add the truncated wildcard perm to the perm set and speed up checks. Next call will not need to loop
                return !hasProhibition(world, perm);
            }
            
            for (int i = 0; i < perm.split("\\.").length-2; i++){
                testPerm = testPerm.substring(0, testPerm.substring(0,testPerm.lastIndexOf(".")).lastIndexOf("."))+".*";
                if (this.permissions.get(world).contains(testPerm)) {
                    this.permissions.get(world).add(perm); //This will add the truncated wildcard perm to the perm set and speed up checks. Next call will not need to loop
                    return !hasProhibition(world, perm);
                }
            }
        }
        return false;
    }
    
    public boolean hasProhibition(String world, String perm){
        if (this.prohibitions.get(world).contains(perm)) return true;
        else if (this.prohibitions.get(world).contains("*") || this.prohibitions.get(world).contains("jawapermissions.all")) return true; //This really should never happen but if it does i hope they know what they want
        else {
            if ("*".equals(perm)) return true; //Because fucking perworld inventory checks if player has * for some damn reason always return true if they are this stupid
            
            String testPerm = perm.substring(0, perm.lastIndexOf("."))+".*";
            
            if (this.prohibitions.get(world).contains(testPerm)) {
                this.prohibitions.get(world).add(perm); //This will add the truncated wildcard perm to the perm set and speed up checks. Next call will not need to loop
                return true;
            }
            
            for (int i = 0; i < perm.split("\\.").length-2; i++){
                testPerm = testPerm.substring(0, testPerm.substring(0,testPerm.lastIndexOf(".")).lastIndexOf("."))+".*";
                if (this.prohibitions.get(world).contains(testPerm)) {
                    this.prohibitions.get(world).add(perm); //This will add the truncated wildcard perm to the perm set and speed up checks. Next call will not need to loop
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public Set<String> getPermissions(String world){
        return this.permissions.get(world);
    }
    
    public Set<String> getProhibitions(String world){
        return this.prohibitions.get(world);
    }
    
    public Set<String> getWorlds(){
        return this.permissions.keySet();
    }
    
    public String getRankName(){
        return this.rankName;
    }
    
    public int getImmunity(){
        return this.immunity;
    }
    
    //Remove data methods
    
    public boolean purgeAll(boolean doPurge){
        //TODO clear everything but keep the objects to protect GC if possible
        return true;
    }
    
    public boolean removePermission(String world, String perm){
        return this.permissions.get(world).remove(perm);
    }
    
    public boolean removeProhibition(String world, String proh){
        return this.permissions.get(world).remove(proh);
    }
    
}
