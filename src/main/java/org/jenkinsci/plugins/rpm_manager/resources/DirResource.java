/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager.resources;

/**
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
