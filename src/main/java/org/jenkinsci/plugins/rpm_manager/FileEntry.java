/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager;

/**
 * Description: 
 * Every file entry that RPM_Manager returns regarding a file consists of 3 part:
 * 1) permission
 * 2) source location
 * 3) dest location
 * This class will parse the entry and contain those details
 * 
 * @author gavrielk
 */
public class FileEntry {
    
    private String source = "Missing";
    private String dest = "Missing";
    private String permissions = "Missing";
    
    public FileEntry(String entry)
    {
        String[] parts = entry.split("\\s+");
        if (parts[0] != null)
        {
            this.permissions = parts[0];
        }
        if (parts[1] != null)
        {
            this.source = parts[1];
        }
        if (parts[2] != null)
        {
            this.dest = parts[2];
        }
    }
    
    public String getSource()
    {
        return this.source;
    }
    
    public String getDest()
    {
        return this.dest;
    }
    
    public String getPermissions()
    {
        return this.permissions;
    }
}
