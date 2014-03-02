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
import hudson.util.ComboBoxModel;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
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
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.ui_samples.UISample;
import jenkins.plugins.ui_samples.UISampleDescriptor;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

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
public class RPM_Manager implements Action, Describable<RPM_Manager> {

    private AbstractProject<?, ?> project;
    private static String rpmManagersScriptPath;
    
    public RPM_Manager() 
    {
    }
    
    public RPM_Manager(AbstractProject<?, ?> project) 
    {
    	this.project = project;
        String username = System.getProperty("user.name");
        RPM_Manager.rpmManagersScriptPath = "/home/" + username + "/BuildSystem/cc-views/" + username + "_" + project.getName()
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
    
    /**
     * This function purpose is to route the user's input from the index.jelly(client side) file
     * Note: parameters which start with _.<parameter_name> are defined in the field attribute inside tags in the jelly file
     * @param req
     * @param rsp
     * @throws IOException
     */
    public void doSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException    
    {
        String rpmRemove = req.getParameter("rpm-remove");
        String rpmAdd = req.getParameter("rpm-add");
        String fileRemove = req.getParameter("file-remove");
        
        if (rpmRemove != null)
        {
            String rpmRemoveName = req.getParameter("_.rpm");
            System.out.println("rpmRemoveName=" + rpmRemoveName);
//            removeRPM(rpmRemoveName);
        }
        else if(rpmAdd != null)
        {
            String rpmAddName = req.getParameter("rpm-add-name");
            System.out.println("rpmAddName=" + rpmAddName);
//            adRPM(rpmAddName);
            
        }
        else if(fileRemove != null)
        {
            String fileRemoveName = req.getParameter("file-remove-name");
            System.out.println("fileRemoveName=" + fileRemoveName);
//            removeFile(rpmRemoveName);
        }
        
        Iterator keySetIt = req.getParameterMap().keySet().iterator();
        Object key;
        while (keySetIt.hasNext())
        {
            key = keySetIt.next();
            System.out.println("key: " + key.toString() + ", value: " + req.getParameter(key.toString()));
        }
//        
        
        
        
        
        
//        String selectedRPM = req.getParameter("selectedRPM");
//        try {
//            executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "rpm", selectedRPM}));
//        } catch (RPMManagerScriptException ex) {
//            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        //Find a better way to redirect the response so it won't be hard coded.
        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/RPM_Manager");
    }
    
//    public void doRemoveRPM(StaplerRequest req, StaplerResponse rsp) throws IOException    
//    {
//        String selectedRPM = req.getParameter("selectedRPM");
//        try {
//            executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "rpm", selectedRPM}));
//        } catch (RPMManagerScriptException ex) {
//            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        //Find a better way to redirect the response so it won't be hard coded.
//        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/RPM_Manager");
//    }
//    
//    public void doAddRPM(StaplerRequest req, StaplerResponse rsp) throws IOException   
//    {
//        String rpmName = req.getParameter("rpm-name");
//        try {
//            executeRPMManagerCommand(Arrays.asList(new String[]{"add", "rpm", rpmName}));
//        } catch (RPMManagerScriptException ex) {
//            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        //Find a better way to redirect the response so it won't be hard coded.
//        rsp.sendRedirect2(req.getRootPath() + "/job/" + project.getName() + "/RPM_Manager");
//    }
    
//    public String getRpm()
//    {
//        ListBoxModel m = new ListBoxModel();
//        List<String> filesArr = null;
//        try {
//            filesArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "all"}));
//        } catch (RPMManagerScriptException ex) {
//            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        for (String s : asList("A", "B", "C"))
//        {
//            m.add(s, s);
//        }
//        System.out.println(m.toString());
//        return m;
//    }

    @Override
    public DescriptorImpl getDescriptor() {
        System.out.println("In getDescriptor()");
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public String getDescription() {
        return "This plugin gives management over Ceragon's RPM mechanism";
    }

    private void removeRPM(String rpmRemoveName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Extension
    public static final class DescriptorImpl extends RPM_ManagerDescriptor
    {
        private static ArrayList<FileEntry> fileEntriesArr = new ArrayList<FileEntry>();
        
        public ListBoxModel doFillRpmItems()
        {
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
            System.out.println("rpm:" + rpm);
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> filesArr = null;
            DescriptorImpl.fileEntriesArr = new ArrayList<FileEntry>();
            try {
                filesArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "files", rpm}));
                for (String entry : filesArr)
                {
                    System.out.println("entry: " + entry);
                    DescriptorImpl.fileEntriesArr.add(new FileEntry(entry));
                }
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (FileEntry file : DescriptorImpl.fileEntriesArr)
            {
                m.add(file.getDest());
            }
            return m;
        }
        
        @JavaScriptMethod
        public String getPermissions(String file)
        {
            System.out.println("file:" + file);
            ComboBoxModel m = new ComboBoxModel();
            int entryIndex = getFileIndexByDest(file);
            System.out.println("Found at: " + entryIndex + ", permissions: " + DescriptorImpl.fileEntriesArr.get(entryIndex).getPermissions());
            return DescriptorImpl.fileEntriesArr.get(entryIndex).getPermissions();
        }

        private int getFileIndexByDest(String file) {
            int i = 0;
            for (FileEntry entry :  fileEntriesArr)
            {
                if (entry.getDest().equals(file) == true)
                {
                    return i;
                }
                i++;
            }
            return -1;
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
        
        // Initializing the RPM_Manager.sh command in an array that will be passed to ProcessBuilder
        ArrayList<String> rpmManagerCMD = new ArrayList<String>(options);
        rpmManagerCMD.add(0, RPM_Manager.rpmManagersScriptPath);
        
        // Initializing Proccess builder, redirecting the output to the script output file
        ProcessBuilder pb = new ProcessBuilder().inheritIO();
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.to(scriptOutputFile));
        
        try {        
            String line;
            
            System.out.println("RPM manager command: " + Arrays.toString(rpmManagerCMD.toArray()));
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
        
        outputArr.remove(0);
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