/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager.resources;


/**
 * Description: 
 * Every dir entry that RPM_Manager returns regarding a dir consists of 2 part:
 * 1) permission
 * 2) dir location
 * This class will parse the entry and contain those details
 * this.resource = dir location
 * 
 * @author gavrielk
 */
public class DirResource extends IResource{
    
    private String permissions = "";
    
    public DirResource(String entry)
    {
        String[] parts = entry.split("\\s+");
        if (parts[0] != null)
        {
            this.permissions = parts[0];
        }
        if (parts[1] != null)
        {
            this.resource = parts[1];
        }
    }
    
    public String getPermissions()
    {
        return this.permissions;
    }
}
