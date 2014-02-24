package org.jenkinsci.plugins.rpm_manager;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.security.Permission;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.ui_samples.UISample;
import jenkins.plugins.ui_samples.UISampleDescriptor;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link RPM_Manager} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class RPM_Manager extends UISample implements Action {

    private AbstractProject<?, ?> project;
    private String rpmManagersScriptPath;
    
    public RPM_Manager() 
    {
    }
    
    public RPM_Manager(AbstractProject<?, ?> project) 
    {
    	this.project = project;
        String username = System.getProperty("user.name");
        rpmManagersScriptPath = "/home/" + username + "/BuildSystem/cc-views/" + username + "_" + project.getName()
                + "_int/vobs/linux/CI_Build_Scripts/src/RPM_Manager/RPM_Manager.sh";
    }
    
    @Override
    public String getIconFileName() {
        if (CheckBuildPermissions() == true){
            return "/plugin/RPM_Manager/rpm-icon.png";
        }
        else{
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        if (CheckBuildPermissions() == true){
            return "RPM Manager";
        }
        else{
            return null;
        }
    }

    @Override
    public String getUrlName() {
        if (CheckBuildPermissions() == true){
            return "RPM_Manager";
        }
        else{
            return null;
        }
    }
    
    private boolean CheckBuildPermissions(){
        for ( Permission permission : Permission.getAll())
        {
            if (permission.name.equals("Build") == true)
            {
                if (Jenkins.getInstance().hasPermission(permission) == true)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void doRemoveRPM(StaplerRequest req, StaplerResponse rsp) throws IOException    
    {
        String selectedRPM = req.getParameter("selectedRPM");
        try {
            executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "rpm", selectedRPM}));
        } catch (RPMManagerScriptException ex) {
            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/RPM_Manager");
    }
    
    public void doAddRPM(StaplerRequest req, StaplerResponse rsp) throws IOException   
    {
        String rpmName = req.getParameter("rpm-name");
        try {
            executeRPMManagerCommand(Arrays.asList(new String[]{"add", "rpm", rpmName}));
        } catch (RPMManagerScriptException ex) {
            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/RPM_Manager");
    }
    
    public String getRpm()
    {
//        ListBoxModel m = new ListBoxModel();
//        ArrayList<String> filesArr = null;
//        try {
//            filesArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "all"}));
//        } catch (RPMManagerScriptException ex) {
//            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        for (String s : filesArr)
//        {
//            m.add(s);
//        }
//        return m;
        return "3";
    }

//    @Override
//    public DescriptorImpl getDescriptor() {
//        System.out.println("In getDescriptor()");
//        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
//    }

    @Override
    public String getDescription() {
        return "This plugin gives management over Ceragon's RPM mechanism";
    }
    
    @Extension
    public static final class DescriptorImpl extends UISampleDescriptor
    {
        
        public ListBoxModel doFillRpmItems()
        {
            System.out.println("doFillRpmItems");
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> filesArr = null;
            try {
                filesArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "all"}));
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (String s : filesArr)
            {
                m.add(s);
            }
            return m;
        }

    
        public ListBoxModel doFillFileItems(@QueryParameter String rpm)
        {
            System.out.println("doFillFileItems");
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> filesArr = null;
            try {
                filesArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "files", rpm}));
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (String s : filesArr)
            {
                m.add(s);
            }
            return m;
        }

        @Override
        public String getDisplayName() {
            System.out.println("In getDisplayName()");
            return clazz.getSimpleName();
        }
    }
    
    
    public String getJobName()
    {
        return project.getName();
    }
    
    private static ArrayList<String> executeRPMManagerCommand(List<String> options) throws RPMManagerScriptException
    {
        ArrayList<String> outputArr = new ArrayList<String>();
        
        // Initializing the output file
        File scriptOutputFile = new File("/var/log/jenkins/RPM_Manager.out");
        if (scriptOutputFile.exists() == true)
        {
            scriptOutputFile.delete();
        }
        
        // Initializing the RPM_Manager.sh command in an array that will be passed to ProcessBuilder
        ArrayList<String> rpmManagerCMD = new ArrayList<String>(options);
        rpmManagerCMD.add(0, "/home/builder-testing/BuildSystem/cc-views/builder-testing_Genesis-7.7"
                + "_int/vobs/linux/CI_Build_Scripts/src/RPM_Manager/RPM_Manager.sh");
        
        // Initializing Proccess builder, redirecting the output to the script output file
        ProcessBuilder pb = new ProcessBuilder().inheritIO();
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(scriptOutputFile));
        
        try {        
            String line;
            
            // Executing the script command
            pb.command(rpmManagerCMD).start().waitFor();
            
            // Reading the output file
            InputStream fis = new FileInputStream(scriptOutputFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) 
            {
                outputArr.add(line);
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String returnMessage = outputArr.remove(outputArr.size() - 1);
        if (returnMessage.equals("[OK]"))
        {
            return outputArr;
        }
        else
        {
            throw new RPMManagerScriptException("Error occured while running " + rpmManagerCMD.toString() + "\nError message: " + returnMessage);
        }
    }

}