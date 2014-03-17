/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager;

import java.util.ArrayList;

/**
 *
 * @author gavrielk
 */
class RPMManagerScriptException extends Exception {
    
    String message = "";

    public RPMManagerScriptException(String message) {
        super(message);
    }
    
    public RPMManagerScriptException(ArrayList<String> message) {
        for (int i = 0; i < message.size(); i++)
        {
            this.message += message.get(i) + "\n";
        }
    }
    
}
