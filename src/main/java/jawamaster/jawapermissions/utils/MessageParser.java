/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jawamaster.jawapermissions.utils;

import java.util.HashMap;

/**
 *
 * @author alexander
 */
public class MessageParser {
    
    private static HashMap<String,String> messages = new HashMap();
    
    
    public MessageParser () {
        
    }
    
    public static String getMessages(String key){
        return messages.get(key);
    }
    
}
