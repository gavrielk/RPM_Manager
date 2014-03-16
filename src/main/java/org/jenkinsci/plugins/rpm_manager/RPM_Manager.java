package org.jenkinsci.plugins.rpm_manager;

import org.jenkinsci.plugins.rpm_manager.resources.LinkResource;
import org.jenkinsci.plugins.rpm_manager.resources.FileResource;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterValue;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Describable;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.security.Permission;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;
import org.jenkinsci.plugins.rpm_manager.resources.DirResource;
import org.jenkinsci.plugins.rpm_manager.resources.IResource;
import org.jenkinsci.plugins.rpm_manager.resources.ProjectResource;
import org.jenkinsci.plugins.rpm_manager.resources.ProjectResource.ProjectContainingRPMEnum;
import org.jenkinsci.plugins.rpm_manager.resources.RpmResource;
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
    private static File tempScriptOutputFile = new File("/var/log/jenkins/RPM_Manager.out");
    private static File logFile = new File("/var/log/jenkins/RPM_Manager.log");
    
    public RPM_Manager() 
    {
    }
    
    public RPM_Manager(AbstractProject<?, ?> project) 
    {
    	this.project = project;
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
     * This function purpose is to route the user's input from the index.jelly, versions.jelly and index.jelly(client side) file
     * Note: parameters which start with _.<parameter_name> are defined in the field attribute inside tags in the jelly file
     * @param req
     * @param rsp
     * @throws IOException
     */
    public void doSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException    
    {
        String projectRemove = req.getParameter("project-remove-submit");
        String projectAdd = req.getParameter("project-add-submit");
        String rpmRemove = req.getParameter("rpm-remove-submit");
        String rpmAdd = req.getParameter("rpm-add-submit");
        String dirRemove = req.getParameter("dir-remove-submit");
        String dirAdd = req.getParameter("dir-add-submit");
        String fileRemove = req.getParameter("file-remove-submit");
        String fileAdd = req.getParameter("file-add-submit");
        String linkRemove = req.getParameter("link-remove-submit");
        String linkAdd = req.getParameter("link-add-submit");
        
        
        String rpmName = req.getParameter("_.rpm");
        System.out.println("rpmName=" + rpmName);
        String versionRpmName = req.getParameter("_.versionRpm");
        System.out.println("versionRpmName=" + versionRpmName);
        String projectName = req.getParameter("_.project");
        System.out.println("projectName=" + projectName);
        
        Iterator keySetIt = req.getParameterMap().keySet().iterator();
        Object key;
        while (keySetIt.hasNext())
        {
            key = keySetIt.next();
            System.out.println("key: " + key.toString() + ", value: " + req.getParameter(key.toString()));
        }
        
        try
        {
            if (isBlank(new String[]{rpmName}) == false)
            {
                if (rpmRemove != null)
                {
                    executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "rpm", rpmName}));
                }
                else if(rpmAdd != null)
                {
                    String rpmAddName = req.getParameter("rpm-add-name");
                    System.out.println("rpmAddName=" + rpmAddName);
                    if (isBlank(new String[]{rpmAddName}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"add", "rpm", rpmAddName}));
                    }
                }
                else if(dirRemove != null)
                {
                    String dir = req.getParameter("_.dir");
                    System.out.println("dir=" + dir);
                    if (isBlank(new String[]{dir}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "dir", rpmName, dir}));
                    }
                }
                else if(dirAdd != null)
                {
                    String dir = req.getParameter("dir-add");
                    String dirPermissions = req.getParameter("dir-permissions-add");
                    System.out.println("dir=" + dir);
                    System.out.println("dirPermissions=" + dirPermissions);
                    if (isBlank(new String[]{dir, dirPermissions}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"add", "dir", rpmName, dir, dirPermissions}));
                    }
                }
                else if(fileRemove != null)
                {
                    String file = req.getParameter("_.file");
                    System.out.println("file=" + file);
                    if (isBlank(new String[]{file}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "file", rpmName, file}));
                    }
                }
                else if(fileAdd != null)
                {
                    String destFile = req.getParameter("file-add");
                    String filePermissions = req.getParameter("file-permissions-add");
                    String sourceFile = req.getParameter("source-file-add");
                    System.out.println("destFile=" + destFile);
                    System.out.println("filePermissions=" + filePermissions);
                    System.out.println("sourceFile=" + sourceFile);
                    if (isBlank(new String[]{destFile, filePermissions, sourceFile}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"add", "file", rpmName, sourceFile, destFile, filePermissions}));
                    }
                }
                else if(linkRemove != null)
                {
                    String destLink = req.getParameter("_.link");
                    System.out.println("linkRemoveName=" + destLink);
                    if (isBlank(new String[]{destLink}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "link", rpmName, destLink}));
                    }
                }
                else if(linkAdd != null)
                {
                    String sourceLink = req.getParameter("source-link-add");
                    String destLink = req.getParameter("dest-link-add");
                    System.out.println("sourceLink=" + sourceLink);
                    System.out.println("destLink=" + destLink);
                    if (isBlank(new String[]{sourceLink, destLink}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"add", "link", rpmName, sourceLink, destLink}));
                    }
                }
            }
            else if(isBlank(new String[]{projectName}) == false)
            {
                if(projectRemove != null)
                {
                    if (isBlank(new String[]{projectName}) == false)
                    {
                        executeRPMManagerCommand(Arrays.asList(new String[]{"remove", "project", projectName}));
                    }
                }
                else if(projectAdd != null)
                {
                    String newProjectName = req.getParameter("project-name-add");
                    String newProjectPath = req.getParameter("project-path-add");
                    String newProjectProductPath = req.getParameter("product-path-add");
                    String newProjectArtifactType = req.getParameter("artifact-type");
                    String newProjectContainingRPM = req.getParameter("projectContainingRpm");
                    System.out.println("newProjectName=" + newProjectName);
                    System.out.println("newProjectPath=" + newProjectPath);
                    System.out.println("newProjectProductPath=" + newProjectProductPath);
                    System.out.println("newProjectArtifactType=" + newProjectArtifactType);
                    System.out.println("newProjectContainingRPM=" + newProjectContainingRPM);
                    if (isBlank(new String[]{newProjectName, newProjectPath, newProjectArtifactType}) == false)
                    {
                        if (newProjectArtifactType.equals("static-library") == true)
                        {
                            executeRPMManagerCommand(Arrays.asList(new String[]{"add", "project", "host", newProjectName, newProjectPath}));
                        }
                        else if (newProjectArtifactType.equals("shared-library-exe") == true && isBlank(new String[]{newProjectProductPath, newProjectContainingRPM}) == false)
                        {
                            executeRPMManagerCommand(Arrays.asList(new String[]{"add", "project", newProjectContainingRPM, newProjectName, newProjectPath, newProjectProductPath}));
                        }
                    }
                }
            }
            else if(isBlank(new String[]{versionRpmName}) == false)
            {
                String savedVersion = req.getParameter("rpm-version");
                String savedRelease = req.getParameter("rpm-release");
                String savedDep = req.getParameter("rpm-dep");
                String savedSummary = req.getParameter("rpm-summary");
                String savedDescription = req.getParameter("rpm-description");
                String savedVarKey = req.getParameter("variables");
                String savedVarValue = req.getParameter("var-value");
                String newVarAdd = req.getParameter("var-add-block"); // this is the check box in which we add new variable
                System.out.println("savedVersion=" + savedVersion);
                System.out.println("savedRelease=" + savedRelease);
                System.out.println("savedDep=" + savedDep);
                System.out.println("savedSummary=" + savedSummary);
                System.out.println("savedDescription=" + savedDescription);
                System.out.println("savedVarKey=" + savedVarKey);
                System.out.println("savedVarValue=" + savedVarValue);
                System.out.println("newVarAdd=" + newVarAdd);

                RPM_Manager.rpmManagersScriptPath = getRPMManagerPath(this.project.getName());
                RpmResource currentRpm = DescriptorImpl.getRpmResource(versionRpmName);
                if (isBlank(new String[]{savedVersion}) == false && savedVersion.equals(currentRpm.getVersion()) == false)
                {
                    executeRPMManagerCommand(Arrays.asList(new String[]{"change", "version", versionRpmName, savedVersion}));
                }
                if (isBlank(new String[]{savedRelease}) == false && savedRelease.equals(currentRpm.getRelease()) == false)
                {
                    executeRPMManagerCommand(Arrays.asList(new String[]{"change", "release", versionRpmName, savedRelease}));
                }
                if (isBlank(new String[]{savedDep}) == false && savedDep.equals(currentRpm.getDependencies()) == false)
                {
                    executeRPMManagerCommand(Arrays.asList(new String[]{"change", "requires", versionRpmName, savedDep}));
                }
                if (isBlank(new String[]{savedSummary}) == false && savedSummary.equals(currentRpm.getSummary()) == false)
                {
                    executeRPMManagerCommand(Arrays.asList(new String[]{"change", "summary", versionRpmName, savedSummary}));
                }
                if (isBlank(new String[]{savedDescription}) == false && fixCrLf(savedDescription).equals(currentRpm.getDescription()) == false)
                {
                    executeRPMManagerCommand(Arrays.asList(new String[]{"change", "description", versionRpmName, fixCrLf(savedDescription)}));
                }
                if (isBlank(new String[]{savedVarKey, savedVarValue}) == false && savedVarValue.equals(currentRpm.getVarValue(savedVarKey)) == false)
                {
                    executeRPMManagerCommand(Arrays.asList(new String[]{"change", "variable", versionRpmName, savedVarKey, savedVarValue}));
                }
                if (isBlank(new String[]{newVarAdd}) == false)
                {
                    String newVarKey = req.getParameter("var-key-add");
                    String newVarValue = req.getParameter("var-value-add");
                    System.out.println("newVarKey=" + newVarKey);
                    System.out.println("newVarValue=" + newVarValue);
                    
                    executeRPMManagerCommand(Arrays.asList(new String[]{"add", "variable", versionRpmName, newVarKey, newVarValue}));
                }
            }
        }catch(RPMManagerScriptException ex){
            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        rsp.sendRedirect2(req.getReferer());
    }
    
    public void doValidate(StaplerRequest req, StaplerResponse rsp) throws IOException  
    {
        List<ParameterValue> values = new ArrayList<ParameterValue>();
        List<ParameterDefinition> definitions = new ArrayList<ParameterDefinition>();
        
        ParametersDefinitionProperty property = this.project.getProperty(ParametersDefinitionProperty.class);
        if (property != null && property.getParameterDefinitions() != null) 
        {
            definitions = property.getParameterDefinitions();
        }
        for (ParameterDefinition parameterDefinition : definitions) 
        {
            ParameterValue parameterValue;
            String parameterName = parameterDefinition.getName();
            if (parameterName.equals("Testing") == true || parameterName.equals("Update_view") == true)
            {
                parameterValue = new BooleanParameterValue(parameterName, false);
            }
            else
            {
                parameterValue = parameterDefinition.getDefaultParameterValue();
            }
            values.add(parameterValue);
        }
                
        Jenkins.getInstance().getQueue().schedule(project, 0, new ParametersAction(values), new CauseAction(new Cause.UserIdCause()));
            
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
        // http://10.10.12.164:8080/job/Genesis-1.8/5/console
        rsp.sendRedirect2(this.project.getLastBuild().getAbsoluteUrl() + "console");
    }
    
    public void doCheckin(StaplerRequest req, StaplerResponse rsp) throws IOException  
    {
//        try 
//        {
//            RPM_Manager.executeRPMManagerCommand(Arrays.asList(new String[]{"checkin"}));
//        } catch (RPMManagerScriptException ex) {
//            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
        rsp.sendRedirect2(req.getReferer());
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        System.out.println("In getDescriptor()");
        RPM_Manager.rpmManagersScriptPath = getRPMManagerPath(this.project.getName());
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

    public String getDescription() {
        return "This plugin gives management over Ceragon's RPM mechanism";
    }

    private static String getRPMManagerPath(String projectName) {
        String username = System.getProperty("user.name");
        return "/home/" + username + "/BuildSystem/cc-views/" + username + "_" + projectName
                + "_int/vobs/linux/CI_Build_Scripts/src/RPM_Manager/RPM_Manager.sh";
    }

    @Extension
    public static final class DescriptorImpl extends RPM_ManagerDescriptor
    {
        private static RpmResource userSelectedRpmResource = null;
        private static ArrayList<IResource> dirResourceArr = new ArrayList<IResource>();
        private static ArrayList<IResource> projectResourceArr = new ArrayList<IResource>();
        private static ArrayList<IResource> fileResourceArr = new ArrayList<IResource>();
        private static ArrayList<IResource> linkResourceArr = new ArrayList<IResource>();
        
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
        
        public ListBoxModel doFillVersionRpmItems()
        {
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> rpmArr = null;
            try {
                rpmArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "all"}));
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (String s : rpmArr)
            {
                m.add(s);
            }
            return m;
        }

        public ListBoxModel doFillVariableItems(@QueryParameter String versionRpm)
        {
            if (versionRpm == null || versionRpm.equals("") == true)
            {
                return null;
            }
            ListBoxModel m = new ListBoxModel();
            RpmResource rpmInfo = getRpmResource(versionRpm);
            
            if (rpmInfo.getVariables() != null)
            {
                for (String key : rpmInfo.getVariables())
                {
                    System.out.println(versionRpm + " var key: " + key);
                    m.add(key);
                }
            }
            return m;
        }

        public ListBoxModel doFillProjectItems()
        {
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> projectsArr = null;
            DescriptorImpl.projectResourceArr = new ArrayList<IResource>();
            try {
                for (ProjectContainingRPMEnum containgRpm : ProjectContainingRPMEnum.values())
                {
                    projectsArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "projects", containgRpm.name().toLowerCase()}));
                    System.out.println("Containing RPM name:" + containgRpm.name());
                    for (String entry : projectsArr)
                    {
                        System.out.println("Project entry: " + entry + ", Containing RPM: " + containgRpm.name());
                        DescriptorImpl.projectResourceArr.add(new ProjectResource(entry, containgRpm));
                    }
                }
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (IResource project : DescriptorImpl.projectResourceArr)
            {
                m.add(project.getResource());
            }
            return m;
        }                

        public ListBoxModel doFillProjectContainingRpmItems()
        {
            ListBoxModel m = new ListBoxModel();
            ProjectContainingRPMEnum[] possibleContainingRPMArr = ProjectContainingRPMEnum.values();
            // We start from one because we don't want to display HOST as this is the static-library option
            for (int i = 1; i < possibleContainingRPMArr.length; i++) 
            {
                m.add(possibleContainingRPMArr[i].name().toLowerCase());
            }
            return m;
        }

        public ListBoxModel doFillDirItems(@QueryParameter String rpm)
        {
            if (rpm == null || rpm.equals("") == true)
            {
                return null;
            }
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> dirsArr = null;
            DescriptorImpl.dirResourceArr = new ArrayList<IResource>();
            try {
                dirsArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "dirs", rpm}));
                for (String entry : dirsArr)
                {
                    System.out.println("Dir entry: " + entry);
                    DescriptorImpl.dirResourceArr.add(new DirResource(entry));
                }
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (IResource dir : DescriptorImpl.dirResourceArr)
            {
                m.add(dir.getResource());
            }
            return m;
        }
    
        public ListBoxModel doFillFileItems(@QueryParameter String rpm)
        {
            if (rpm == null || rpm.equals("") == true)
            {
                return null;
            }
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> filesArr = null;
            DescriptorImpl.fileResourceArr = new ArrayList<IResource>();
            try {
                filesArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "files", rpm}));
                for (String entry : filesArr)
                {
                    System.out.println("File entry: " + entry);
                    DescriptorImpl.fileResourceArr.add(new FileResource(entry));
                }
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (IResource file : DescriptorImpl.fileResourceArr)
            {
                System.out.println("Resource: " + file.getResource());
                m.add(file.getResource());
            }
            return m;
        }
    
        public ListBoxModel doFillLinkItems(@QueryParameter String rpm)
        {
            if (rpm == null || rpm.equals("") == true)
            {
                return null;
            }
            ListBoxModel m = new ListBoxModel();
            ArrayList<String> linksArr = null;
            DescriptorImpl.linkResourceArr = new ArrayList<IResource>();
            try {
                linksArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "links", rpm}));
                for (String entry : linksArr)
                {
                    System.out.println("Link entry: " + entry);
                    DescriptorImpl.linkResourceArr.add(new LinkResource(entry));
                }
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (IResource file : DescriptorImpl.linkResourceArr)
            {
                m.add(file.getResource());
            }
            return m;
        }
        
        @JavaScriptMethod
        public String doGetProjectPath(String project)
        {
            int resourceIndex = getResourceIndex(project, DescriptorImpl.projectResourceArr);
            System.out.println("Project found at: " + resourceIndex + ", project path: " + ((ProjectResource)DescriptorImpl.projectResourceArr.get(resourceIndex)).getProjectPath());
            return ((ProjectResource)DescriptorImpl.projectResourceArr.get(resourceIndex)).getProjectPath();
        }
        
        @JavaScriptMethod
        public String doGetProductPath(String project)
        {
            int resourceIndex = getResourceIndex(project, DescriptorImpl.projectResourceArr);
            System.out.println("Project found at: " + resourceIndex + ", product path: " + ((ProjectResource)DescriptorImpl.projectResourceArr.get(resourceIndex)).getProductPath());
            return ((ProjectResource)DescriptorImpl.projectResourceArr.get(resourceIndex)).getProductPath();
        }
        
        @JavaScriptMethod
        public String doGetContainingRPM(String project)
        {
            int resourceIndex = getResourceIndex(project, DescriptorImpl.projectResourceArr);
            System.out.println("Project found at: " + resourceIndex + ", product path: " + ((ProjectResource)DescriptorImpl.projectResourceArr.get(resourceIndex)).getContainingRPM().name());
            return ((ProjectResource)DescriptorImpl.projectResourceArr.get(resourceIndex)).getContainingRPM().name().toLowerCase();
        }
        
        @JavaScriptMethod
        public String doGetFilePermissions(String file)
        {
            int resourceIndex = getResourceIndex(file, fileResourceArr);
            System.out.println("File found at: " + resourceIndex + ", permissions: " + ((FileResource)DescriptorImpl.fileResourceArr.get(resourceIndex)).getPermissions());
            return ((FileResource)DescriptorImpl.fileResourceArr.get(resourceIndex)).getPermissions();
        }
        
        @JavaScriptMethod
        public String doGetSourceFile(String file)
        {
            int resourceIndex = getResourceIndex(file, DescriptorImpl.fileResourceArr);
            System.out.println("File found at: " + resourceIndex + ", source: " + ((FileResource)DescriptorImpl.fileResourceArr.get(resourceIndex)).getSource());
            return ((FileResource)DescriptorImpl.fileResourceArr.get(resourceIndex)).getSource();
        }
        
        @JavaScriptMethod
        public ListBoxModel doGetSWProjects(String rpm)
        {
            if (isBlank(new String[]{rpm}) == true)
            {
                return null;
            }
            ListBoxModel m = new ListBoxModel();
            System.out.println("rpm: " + rpm);
            try {
                ProjectContainingRPMEnum containingRPM = ProjectContainingRPMEnum.valueOf(rpm.toUpperCase());
                ArrayList<String> projectsArr = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "projects", rpm}));
                for (String entry : projectsArr)
                {
                    System.out.println("SW Project entry: " + entry + ", Containing RPM: " + rpm);
                    m.add(new ProjectResource(entry, containingRPM).getResource());
                }
            } catch (RPMManagerScriptException ex) {
                Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                System.out.println("[INFO] " + rpm + " doesn't contain project that need to be compiled");
            }
            
            return m;
        }
        
        @JavaScriptMethod
        public String doGetDirPermissions(String file)
        {
            int resourceIndex = getResourceIndex(file, dirResourceArr);
            System.out.println("Dir found at: " + resourceIndex + ", permissions: " + ((DirResource)DescriptorImpl.dirResourceArr.get(resourceIndex)).getPermissions());
            return ((DirResource)DescriptorImpl.dirResourceArr.get(resourceIndex)).getPermissions();
        }
        
        @JavaScriptMethod
        public String doGetSourceLink(String link)
        {
            int resourceIndex = getResourceIndex(link, DescriptorImpl.linkResourceArr);
            System.out.println("Link found at: " + resourceIndex + ", source link: " + ((LinkResource)DescriptorImpl.linkResourceArr.get(resourceIndex)).getSource());
            return ((LinkResource)DescriptorImpl.linkResourceArr.get(resourceIndex)).getSource();
        }
        
        @JavaScriptMethod
        public String doGetRPMVersion(String rpmName)
        {
            String version = "";
            System.out.println("doGetRPMVersion(), rpmName=" + rpmName);
            if (RPM_Manager.isBlank(new String[]{rpmName}) == false)
            {
                RpmResource rpmInfo = getRpmResource(rpmName);
                System.out.println(rpmName + " version: " + rpmInfo.getVersion());
                version = rpmInfo.getVersion();
            }
            return version;
        }
        
        @JavaScriptMethod
        public String doGetRPMRelease(String rpmName)
        {
            String release = "";
            if (RPM_Manager.isBlank(new String[]{rpmName}) == false)
            {
                RpmResource rpmInfo = getRpmResource(rpmName);
                System.out.println(rpmName + " release: " + rpmInfo.getRelease());
                release = rpmInfo.getRelease();
            }
            return release;
        }
        
        @JavaScriptMethod
        public String doGetRPMDep(String rpmName)
        {
            String dep = "";
            if (RPM_Manager.isBlank(new String[]{rpmName}) == false)
            {
                RpmResource rpmInfo = getRpmResource(rpmName);
                System.out.println(rpmName + " dependencies: " + rpmInfo.getDependencies());
                dep = rpmInfo.getDependencies();
            }
            return dep;
        }
        
        @JavaScriptMethod
        public String doGetRPMSummary(String rpmName)
        {
            String summary = "";
            if (RPM_Manager.isBlank(new String[]{rpmName}) == false)
            {
                RpmResource rpmInfo = getRpmResource(rpmName);
                System.out.println(rpmName + " summary: " + rpmInfo.getSummary());
                summary = rpmInfo.getSummary();
            }
            return summary;
        }
        
        @JavaScriptMethod
        public String doGetRPMDescription(String rpmName)
        {
            String description = "";
            if (RPM_Manager.isBlank(new String[]{rpmName}) == false)
            {
                RpmResource rpmInfo = getRpmResource(rpmName);
                System.out.println(rpmName + " description: " + rpmInfo.getDescription());
                description = rpmInfo.getDescription();
            }
            return description;
        }
        
        @JavaScriptMethod
        public String doGetRPMVaribleValue(String rpmName, String var)
        {
            String value = "";
            if (RPM_Manager.isBlank(new String[]{rpmName, var}) == false)
            {
                RpmResource rpmInfo = getRpmResource(rpmName);
                System.out.println(rpmName + " var: " + var + ", value: " + rpmInfo.getVarValue(var));
                value = rpmInfo.getVarValue(var);
            }
            return value;
        }
        
        private int getResourceIndex(String resourceName, ArrayList<IResource> resourceArr) {
            int i = 0;
            for (IResource resource : resourceArr)
            {
                if (resource.getResource().equals(resourceName) == true)
                {
                    return i;
                }
                i++;
            }
            return -1;
        }
        
        /**
         *
         * @param rpmName 
         * @return RpmSource
         * Note: there is a slight possibility that the when we check if to update the "userSelectedRpmResource" member with checking if rpmName is the same as the 
         *       current userSelectedRpmResource name(using getResource()) will output true but the object is accessed from 2 different projects
         *       so the versions might be taken from another projects RPM 
         *       if this case becomes more likely the if should be removed so we query the script for each request or another mechanism to check for the project name
         */
        public static RpmResource getRpmResource(String rpmName) {
            // Check if this rpmInfo member is null or if it is not up to date with the last RPM that was selected by the user then init it with the new rpm
            if (DescriptorImpl.userSelectedRpmResource == null || rpmName.equals(DescriptorImpl.userSelectedRpmResource.getResource()) == false)
            {
                ArrayList<String> versionsList;
                String summary;
                ArrayList<String> description;
                try {
                    versionsList = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "versions", rpmName}));
                    summary = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "summary", rpmName})).get(0);
                    description = executeRPMManagerCommand(Arrays.asList(new String[]{"show", "description", rpmName}));
                    DescriptorImpl.userSelectedRpmResource = new RpmResource(versionsList, summary, ArrayListToString(description));
                } catch (RPMManagerScriptException ex) {
                    Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return DescriptorImpl.userSelectedRpmResource;
        }

        private static String ArrayListToString(ArrayList<String> stringList) 
        {
            String s = "";
            for (int i = 0; i < stringList.size(); i++)
            {
                s += stringList.get(i) + "\n";
            }
            return s;
        }
    }
    
    
    public String getJobName()
    {
        return project.getName();
    }
    
    private static synchronized ArrayList<String> executeRPMManagerCommand(List<String> options) throws RPMManagerScriptException
    {
        ArrayList<String> outputArr = new ArrayList<String>();
        
        if (RPM_Manager.rpmManagersScriptPath == null)
        {
            return outputArr;
        }
        
        // Initializing the RPM_Manager.sh command in an array that will be passed to ProcessBuilder
        ArrayList<String> rpmManagerCMD = new ArrayList<String>(options);
        rpmManagerCMD.add(0, RPM_Manager.rpmManagersScriptPath);
        
        // Redirecting the output to the script output file
        // if we use the show command of RPM_Manager.sh we redirect the output to a temp output file and overwrite it so we can present it to the user later (no log)
        // if we run add/remove/change commands we don't need to present anything to the user we append the output to a log file (mainly for clearcase issues)
        Redirect redirect = Redirect.to(RPM_Manager.tempScriptOutputFile);
        
        try {        
            String line;
            
            System.out.println("RPM manager command: " + Arrays.toString(rpmManagerCMD.toArray()));
            // Executing the script command
            executeScript(rpmManagerCMD, redirect);
            
            // Reading the output file
            InputStream fis = new FileInputStream(RPM_Manager.tempScriptOutputFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            if (rpmManagerCMD.get(1).equals("show") == false)
            {
                BufferedWriter bw = new BufferedWriter(new FileWriter(RPM_Manager.logFile, true));        
                while ((line = br.readLine()) != null) 
                {
                    bw.write(line + "\n");
                    outputArr.add(line);
                }
                bw.close();
            }
            else
            {
                while ((line = br.readLine()) != null) 
                {
                    outputArr.add(line);
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(RPM_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (outputArr.size() > 0 && outputArr.get(0).equals("1_main") == true)
        {
                outputArr.set(0, "");
//                outputArr.remove(0);
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
    
    public static void executeScript(ArrayList<String> command, Redirect redirect) throws IOException, InterruptedException
    {
        ProcessBuilder pb = new ProcessBuilder().inheritIO();
        pb.redirectErrorStream(true);
        
        pb.redirectOutput(redirect);
        pb.command(command).start().waitFor();
    }
    
    private static boolean isBlank(String[] variables)
    {
        for (String variable : variables) 
        {
            if (variable == null || variable.isEmpty() == true) {
                return true;
            }
        }
        return false;
    }
    
    private  String fixCrLf(String s) {
        // eliminate CR
        int idx;
        while((idx=s.indexOf("\r\n"))!=-1)
        {
            s = s.substring(0,idx)+"\\n"+s.substring(idx+2);
        }

        return s;
    }

}