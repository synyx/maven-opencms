package org.synyx.mojo.opencms;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * This is the abstract baseclass for all Mojos within Maven-Opencms.
 *
 * @author  Marc Kannegiesser, kannegiesser@synyx.de
 */
public abstract class AbstractOpenCmsMojo extends AbstractMojo {

    /**
     * If targetLibPath is given the package that is created during the package-phase of the lifecycle. The file that
     * can be found unter <code>${project.build.directory}/${project.build.finalName}.jar</code> will be copied to the
     * given targetLibPath. Also there is an extra export-point added to the manifest for this directory.
     *
     * @parameter
     */
    private String targetLibPath = null;

    /**
     * Where the modules zip should be created to. Defaults to ${project.basedir}/target/
     *
     * @parameter  expression="${project.basedir}/target/"
     */
    private String moduleDir = null;

    /**
     * This is the temporary folder where output is generated to. All mojos somehow interact using this folder: The
     * createtree goal copies its data to there, the manifest goal creates its manifest there and the zipmodule goal
     * zips this folder. WARNING: This folder gets deleted if it already exists when you execute the createtree goal.
     * Defaults to ${project.basedir}/target/opencms
     *
     * @parameter  expression="${project.basedir}/target/opencms"
     */
    private String distfolder = null;

    /**
     * The Name of the Module. Defaults to the projects artifactid.
     *
     * @parameter  expression="${project.artifactId}"
     */
    private String name = "";

    /**
     * The Version of the Module. Defaults to the Version of the Project
     *
     * @parameter  expression="${project.version}"
     */
    private String version = "";


    /**
     * The Name of the Author of the Module. Defaults to Admin
     *
     * @parameter  expression="Admin"
     */
    private String author = null;

    /**
     * Executes this task (abstract),
     *
     * @throws  org.apache.maven.plugin.MojoExecutionException
     * @throws  org.apache.maven.plugin.MojoFailureException
     */
    public abstract void execute() throws MojoExecutionException, MojoFailureException;


    // ##################################################
    // Getters and Setters below
    public String getDistfolder() {

        return distfolder;
    }


    public void setDistfolder(String distfolder) {

        this.distfolder = distfolder;
    }


    public String getName() {

        return name;
    }


    public void setName(String name) {

        this.name = name;
    }


    public String getVersion() {

        return version;

    }


    public void setVersion(String version) {

        this.version = version;
    }


    public String getModuleDir() {

        return moduleDir;
    }


    public void setModuleDir(String moduleDir) {

        this.moduleDir = moduleDir;
    }


    public String getTargetLibPath() {

        return targetLibPath;
    }


    public void setTargetLibPath(String targetLibPath) {

        this.targetLibPath = targetLibPath;
    }
}
