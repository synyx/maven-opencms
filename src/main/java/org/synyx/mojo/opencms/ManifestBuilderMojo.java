package org.synyx.mojo.opencms;

import com.eurelis.opencms.ant.task.ManifestBuilderTask;
import com.eurelis.opencms.ant.task.ManifestBuilderTask.ExportPoint;
import com.eurelis.opencms.ant.task.ManifestBuilderTask.Parameter;
import com.eurelis.opencms.ant.task.ManifestBuilderTask.Resource;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/**
 * This Mojo creates a manifest.xml-File by delegating to ManifestBuilderTask created by Eurelis.
 *
 * @goal    manifest
 * @phase   package
 * @author  Marc Kannegiesser, kannegiesser@synyx.de
 * @author  Florian Hopf, hopf@synyx.de
 */
public class ManifestBuilderMojo extends AbstractOpenCmsMojo {

    /**
     * The Export-Version (try 4 or 6) of the module.
     *
     * @parameter  expression="6"
     * @required
     */
    private String exportversion;

   
    /**
     * The Group of the Module. Defaults to the Group of the Project
     *
     * @parameter  expression="${project.groupId}"
     */
    private String group = "";

  
    
    private String authorname = "";
    /**
     * The Email of the Author of the Module.
     *
     * @parameter
     */
    private String authoremail = "";

    
    /**
     * The Nice-Name of the Module. Defaults to the Name of the Project
     *
     * @parameter  expression="${project.name}"
     */
    private String nicename = "";


    
    /**
     * The Project of the Module. Defaults to Offline
     *
     * @parameter  expression="Offline"
     */
    private String project = "";


    
    /**
     * The OpenCms-Version to create the module for
     *
     * @parameter
     * @required
     */
    private String opencmsversion = "";


    /**
     * @parameter  expression="${project.basedir}"
     */
    private File projectBaseDir = null;
    /**
     * Extra Resourcetypes. Path to XML
     *
     * @parameter
     */
    private String resourcetypes = null;
    /**
     * Extra Explorertypes. Path to XML
     *
     * @parameter
     */
    private String explorertypes = null;
    /**
     * Flag wether to create UUIDs. Defaults to false
     *
     * @parameter  expression="false"
     */
    private Boolean generateuuids = null;
    /**
     * ExportPoints of the Module as Properties.
     *
     * @parameter
     */
    private Properties exportpoints = new Properties();
    /**
     * Resources of the Module as List.
     *
     * @parameter
     */
    private List<String> resources = new ArrayList<String>();
    /**
     * Dependencies of the Module as Properties.
     *
     * @parameter
     */
    private Properties dependencies = new Properties();
    /**
     * Parameters of the Module as Properties.
     *
     * @parameter
     */
    private Properties parameters = new Properties();
    private Vector<FileSet> filesets = new Vector<FileSet>();
    private Map<String, String> extensionDefaultTypes = new HashMap<String, String>();
    /**
     * The creator of the module. Defaults to Admin
     *
     * @parameter  expression="Admin"
     */
    private String creator = "";
    /**
     * The ActionClass of the Module.
     *
     * @parameter
     */
    private String moduleclass = "";
    /**
     * The Description of the Module. Defaults to the Description of the Project
     *
     * @parameter  expression="${project.description}"
     */
    private String moduledescription = "";
    /**
     * The User that has installed the Module. Defaults to Admin
     *
     * @parameter  expression="Admin"
     */
    private String userinstalled = "";
    /**
     * The Date this module was installed.
     *
     * @parameter
     */
    private String dateinstalled = "";

    public File getProjectBaseDir() {

        return projectBaseDir;
    }

    public void setProjectBaseDir(File projectBaseDir) {

        this.projectBaseDir = projectBaseDir;
    }

    /**
     * This method creates Parameters the way the ant-task likes them
     *
     * @param  anttask  ManifestBuilderTask
     */
    private void convertParameters(ManifestBuilderTask anttask) {

        // convert parameters into anttask-parameters
        if (parameters != null) {

            for (Object okey : parameters.keySet()) {

                String key = (String) okey;
                Parameter antparameter = anttask.createParameter();


                antparameter.setName(key);
                antparameter.setValue(parameters.getProperty(key));
            }
        }
    }

    /**
     * This method creates Parameters the way the ant-task likes them
     *
     * @param  anttask  ManifestBuilderTask
     */
    private void convertExportPoints(ManifestBuilderTask anttask) {

        // convert parameters into anttask-parameters
        if (exportpoints != null) {

            for (Object okey : exportpoints.keySet()) {

                String key = (String) okey;
                ExportPoint antparameter = anttask.createExportPoint();


                antparameter.setSrc(key);
                antparameter.setDst(exportpoints.getProperty(key));
            }
        }


        String targetLibPath = getTargetLibPath();


        if ((null != targetLibPath) && !"".equals(targetLibPath)) {

            // eventually add leading slash /
            if (!targetLibPath.startsWith(File.separator)) {

                targetLibPath = File.separator + targetLibPath;
            }


            File targetLibDir = new File(new File(getDistfolder()), targetLibPath);


            if (targetLibDir.exists() && !exportpoints.containsKey(targetLibPath)) {

                ExportPoint antparameter = anttask.createExportPoint();
                getLog().info("Adding additional exportpoint: " + targetLibPath);
                antparameter.setSrc(targetLibPath);
                antparameter.setDst("WEB-INF/lib/");
            }
        }
    }

    /**
     * This method creates Parameters the way the ant-task likes them
     *
     * @param  anttask  ManifestBuilderTask
     */
    private void convertResources(ManifestBuilderTask anttask) {

        // convert parameters into anttask-parameters
        if (resources != null) {

            for (String resource : resources) {

                Resource antparameter = anttask.createResource();


                antparameter.setUri(resource);

            }
        }
    }

    /**
     * Executes this task
     *
     * @throws  org.apache.maven.plugin.MojoExecutionException
     * @throws  org.apache.maven.plugin.MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        // creating ant-task and fill in all the values we got.

        ManifestBuilderTask anttask = new ManifestBuilderTask();


        anttask.setAuthoremail(getAuthoremail());
        anttask.setAuthorname(getAuthorname());
        anttask.setCreator(getCreator());
        anttask.setDateinstalled(getDateinstalled());
        anttask.setDistfolder(getDistfolder());
        anttask.setExplorertypes(getExplorertypes());
        anttask.setExportversion(getExportversion());
        anttask.setGenerateuuids(getGenerateuuids().toString());
        anttask.setGroup(getGroup());
        anttask.setModuleclass(getModuleclass());
        anttask.setModuledescription(getModuledescription());
        anttask.setName(getName());
        anttask.setNicename(getNicename());
        anttask.setOpencmsversion(getOpencmsversion());
        anttask.setProject(getProject());
        anttask.setResourcetypes(getResourcetypes());
        anttask.setSrcfolder(getDistfolder());
        anttask.setUserinstalled(getUserinstalled());
        anttask.setVersion(getVersion());

        convertParameters(anttask);
        convertExportPoints(anttask);
        convertResources(anttask);

        File target = new File(getDistfolder());


        if (!target.exists()) {

            target.mkdirs();
        }


        if (!target.isDirectory()) {

            throw new MojoExecutionException("distfolder " + target + " is not a directory.");
        }


        FileSet fileset = new FileSet();

        File sourceDir = new File(getDistfolder());


        if (!sourceDir.exists()) {

            throw new MojoExecutionException("Sourcefolder does not exist:" + getDistfolder());
        }


        fileset.setDir(sourceDir);
        fileset.setExcludes("**/__properties,**/__properties/**,**/__acl,**/__acl/**, **/manifest.xml");

        // we have to create an ant-projet to avoid nullpointers...
        // @todo fix this
        Project antproject = new Project();


        antproject.setBaseDir(projectBaseDir);
        anttask.setProject(antproject);
        fileset.setProject(antproject);
        anttask.addFileset(fileset);

        // tell the task that .jsp-files always have type: jsp
        extensionDefaultTypes.put("jsp", "jsp");

        extensionDefaultTypes.put("png", "image");
        extensionDefaultTypes.put("jpg", "image");
        extensionDefaultTypes.put("gif", "image");

        extensionDefaultTypes.put("jar", "binary");
        extensionDefaultTypes.put("zip", "binary");

        anttask.setExtensionDefaultTypes(extensionDefaultTypes);

        try {

            // execute the task
            anttask.execute();
        } catch (BuildException ex) {

            getLog().error("Exception while running Task.");
            throw new MojoExecutionException("Exception while running Task: ", ex);
        }

    }


    // ##################################################
    // Getters and Setters below
    public Properties getDependencies() {

        return dependencies;
    }

    public void setDependencies(Properties dependencies) {

        this.dependencies = dependencies;
    }

    public Properties getExportpoints() {

        return exportpoints;
    }

    public void setExportpoints(Properties exportpoints) {

        this.exportpoints = exportpoints;
    }

    public List<String> getResources() {

        return resources;
    }

    public void setResources(List<String> resources) {

        this.resources = resources;
    }

    public Properties getParameters() {

        return parameters;
    }

    public void setParameters(Properties parameters) {

        this.parameters = parameters;
    }

    public String getCreator() {

        return creator;
    }

    public void setCreator(String creator) {

        this.creator = creator;
    }

    public String getDateinstalled() {

        return dateinstalled;
    }

    public void setDateinstalled(String dateinstalled) {

        this.dateinstalled = dateinstalled;
    }

    public String getExplorertypes() {

        return explorertypes;
    }

    public void setExplorertypes(String explorertypes) {

        this.explorertypes = explorertypes;
    }

    public Map<String, String> getExtensionDefaultTypes() {

        return extensionDefaultTypes;
    }

    public void setExtensionDefaultTypes(Map<String, String> extensionDefaultTypes) {

        this.extensionDefaultTypes = extensionDefaultTypes;
    }

    public Vector<FileSet> getFilesets() {

        return filesets;
    }

    public void setFilesets(Vector<FileSet> filesets) {

        this.filesets = filesets;
    }

    public Boolean getGenerateuuids() {

        return generateuuids;
    }

    public void setGenerateuuids(Boolean generateuuids) {

        this.generateuuids = generateuuids;
    }

    public String getModuleclass() {

        return moduleclass;
    }

    public void setModuleclass(String moduleclass) {

        this.moduleclass = moduleclass;
    }

    public String getModuledescription() {

        return moduledescription;
    }

    public void setModuledescription(String moduledescription) {

        this.moduledescription = moduledescription;
    }

    public String getResourcetypes() {

        return resourcetypes;
    }

    public void setResourcetypes(String resourcetypes) {

        this.resourcetypes = resourcetypes;
    }

    public String getUserinstalled() {

        return userinstalled;
    }

    public void setUserinstalled(String userinstalled) {

        this.userinstalled = userinstalled;
    }
    
        public String getOpencmsversion() {

        return opencmsversion;
    }

    public void setOpencmsversion(String opencmsversion) {

        this.opencmsversion = opencmsversion;
    }
    
        public String getProject() {

        return project;
    }

    public void setProject(String project) {

        this.project = project;
    }
    
        public String getNicename() {

        return nicename;
    }

    public void setNicename(String nicename) {

        this.nicename = nicename;
    }
    
      public String getGroup() {

        return group;
    }

    public void setGroup(String group) {

        this.group = group;
    }
    
     public String getExportversion() {

        return exportversion;
    }

    public void setExportversion(String exportversion) {

        this.exportversion = exportversion;
    }
    
    public String getAuthoremail() {

        return authoremail;
    }

    public void setAuthoremail(String authoremail) {

        this.authoremail = authoremail;
    }

    public String getAuthorname() {

        return authorname;
    }

    public void setAuthorname(String authorname) {

        this.authorname = authorname;
    }
}
