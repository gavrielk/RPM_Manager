/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager.resources;

/**
 * Description: 
 * Every project entry that RPM_Manager returns regarding a project consists of 3 part:
 * 1) project name
 * 2) project path
 * 3) product path
 * We also store the containing rpm name to display to the user to help him know where to put the new project he wishes to add:
 * 4) containing RPM
 * This class will parse the entry and contain those details
 * this.resource = project name
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
