/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jenkinsci.plugins.rpm_manager.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public class RpmResource extends IResource{
    
    // this.resource which is held in the super class (IResource) will be the RPM name
    private String version = "";
    private String release = "";
    private String dependencies = "";
    private String summary = "";
    private String description = "";
    private Map<String, String> variables;

    /**
     *
     * @param entries the output of RPM_Manager.sh when executed "./RPM_manager.sh show versions <rpm_name>", each entry represents a line in the output
     * example for an output: 
     * [gavrielk:~/cc-views/gavrielk_CI_int/vobs/linux/CI_Build_Scripts/src/RPM_Manager]$./RPM_Manager.sh show versions mctl
     * MCTL_name               %{MAIN_name}-mctl
     * MCTL_version            7.7.0.0.0.26
     * MCTL_release            3
     * MCTL_requires           pam, pam_passwdqc, iptables, libredblack
     */
    public RpmResource(ArrayList<String> versionsList)
    {
        init(versionsList);
    }

    public RpmResource(ArrayList<String> versionsList, String summary, String description) 
    {
        init(versionsList);
        this.summary = summary;
        this.description = description;
    }
    
    /**
     *
     * @return
     */
    public String getVersion()
    {
        return this.version;
    }
    
    public String getRelease()
    {
        return this.release;
    }
    
    public String getDependencies()
    {
        return this.dependencies;
    }
    
    public String getSummary()
    {
        return this.summary;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public Set<String> getVariables()
    {
        if (this.variables != null)
        {
            return this.variables.keySet();
        }
        return null;
    }
    
    public String getVarValue(String var)
    {
        if (this.variables != null)
        {
            return this.variables.get(var);
        }
        return null;
    }

    private String getKey(String entry) 
    {
        String[] entrySplit = entry.split("\\s+");
        return entrySplit.length > 0 ? entrySplit[0] : "";
    }

    private String getValue(String entry) 
    {
        // There is possibly spaces in the the value (the right column of each line of output) so we split the entry to get rid of the key (the left column) and
        // concat all the rest elements in the splitted array
        String value = "";
        String[] splitEntryArr = entry.split("\\s+");
        for (int i = 1; i < splitEntryArr.length; i++)
        {
            value += splitEntryArr[i] + " ";
        }
        return value;
    }
    
    private void init(ArrayList<String> versionsList) 
    {
        this.resource = getValue(versionsList.get(0));
        this.version = getValue(versionsList.get(1));
        this.release = getValue(versionsList.get(2));
        this.dependencies = getValue(versionsList.get(3));
        for (int i = 4; i < versionsList.size(); i++)
        {
            if (this.variables == null) // if this is the first time 
            {
                this.variables = new HashMap<String, String>();
            }
            this.variables.put(getKey(versionsList.get(i)), getValue(versionsList.get(i)));
        }
    }
}
