/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager.resources;

/**
 * Description: 
 * Every link entry that RPM_Manager returns regarding a link consists of 2 part:
 * 1) source location
 * 2) dest location
 * This class will parse the entry and contain those details
 * this.resource = dest location
 * 
 * @author gavrielk
 */
public class LinkResource extends IResource {
    
    private String source = "";
    
    public LinkResource(String entry)
    {
        String[] parts = entry.split("\\s+");
        if (parts[0] != null)
        {
            this.source = parts[0];
        }
        if (parts[1] != null)
        {
            this.resource = parts[1];
        }
    }
    
    public String getSource()
    {
        return this.source;
    }
}
