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
public abstract class IResource {
    
    protected String resource = "Missing";
    
    protected IResource()
    {
    }
    
    public IResource(String resource)
    {
        this.resource = resource;
    }
    
    public String getResource()
    {
        return this.resource;
    }
}
