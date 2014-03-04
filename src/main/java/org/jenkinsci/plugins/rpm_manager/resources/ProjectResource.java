/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager.resources;

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
public class ProjectResource extends IResource {
    
    // this.resource (declared in the super class) = project name
    private String projectPath = "";
    private String productPath = "";
    private ProjectContainingRPMEnum containingRPM;
    
    public ProjectResource(String entry, ProjectContainingRPMEnum containingRPM)
    {
        String[] parts = entry.split("\\s+");
        if (parts[0] != null)
        {
            this.resource = parts[0];
        }
        if (parts[1] != null)
        {
            this.projectPath = parts[1];
        }
        if (parts.length == 3 && parts[2] != null)
        {
            this.productPath = parts[2];
        }
        this.containingRPM = containingRPM;
    }
    
    public String getProjectPath()
    {
        return this.projectPath;
    }
    
    public String getProductPath()
    {
        return this.productPath;
    }
    
    public ProjectContainingRPMEnum getContainingRPM()
    {
        return this.containingRPM;
    }
    
    
    public enum ProjectContainingRPMEnum
    {
        HOST, MANAGEMENT, MCTL;
    }
}
