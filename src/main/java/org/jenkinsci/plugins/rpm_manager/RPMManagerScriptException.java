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
public class RPMManagerScriptException extends Exception {
    
    public String shortMessage = "";
    public String fullMessage = "";

    public RPMManagerScriptException(String message) {
        super(message);
    }
    
    public RPMManagerScriptException(ArrayList<String> message) {
        int i;
        for (i = 0; i < message.size(); i++)
        {
            this.fullMessage += message.get(i) + "\n";
            if (message.get(i).startsWith("[ERROR]") == true)
            {
                if (this.shortMessage.isEmpty() == false)
                {
                    this.shortMessage += "\n";
                }
                this.shortMessage += message.get(i);
            }
        }
        if (this.shortMessage.isEmpty() == true && this.fullMessage.isEmpty() == false)
        {
            this.shortMessage = this.fullMessage;
        }
    }
    
}
