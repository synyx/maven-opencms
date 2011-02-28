package org.synyx.mojo.opencms;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.opencms.main.CmsShell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This MOJO starts a CmsShell (OpenCms) and executes any commands there.
 * 
 * There are several ways to give commands to the Shell: 
 * <pre>
 * 1st:
 * -----
 * Provide a File containing the commands and set configuration-node "commandFile" to that file. 
 * Alternatively provide the property -D cmsshell.commandfile=/path/to/file
 * 
 * In this case all other commands given are ignored (commands and additionalcommand).
 * 
 * 2nd:
 * -----
 * Enter the whole commands using configuration-node "commands".
 * Alternatively provide the property -D cmsshell.commands=myCommands
 * If the configuration-node additionalcommands (or the property cmsshell.additionalcommands) 
 * is also set these are appended at the end of the "regular" commands.
 * 
 * 3rd:
 * -----
 * Leave out commandFile and commands which results in defaulting to (delete module, import module). 
 * Additionalcommands can also be given by configuration-node additionalcommands (or the property 
 * cmsshell.additionalcommands). These are executed after the import.
 * </pre>
 *
 * @phase   install
 * @goal    cmsshell
 * @author  Marc Kannegiesser, kannegiesser@synyx.de
 * @author  Florian Hopf, hopf@synyx.de
 */
public class CmsShellMojo extends AbstractOpenCmsMojo {

    /**
     * The (absolute) path WEB-INF/ of to a (runnable) OpenCms installation. This option is required and can also be
     * given by the property cmsshell.webappfolder (using mvn ... -D)
     *
     * @parameter  expression="${cmsshell.webappfolder}"
     * @required
     */
    private String webappfolder = null;
    /**
     * Path to a file that contains the commands to be executed. This option can also be given by the property
     * cmsshell.commandfile (using mvn ... -D). If this is not given you may use "commands" to give the commands directly.
     *
     * @parameter  expression="${cmsshell.commandfile}"
     */
    private String commandFile = null;
    /**
     * String containing the commands to be executed. The commands (if there are more than one) have to be seperated by
     * semicolons ; or newlines ). This option can also be given by the property cmsshell.commands (using mvn ... -D). If neither
     * commands nor commandFile is set an default-commands will be used.
     *
     * @parameter  expression="${cmsshell.commands}"
     */
    private String commands = null;
    /**
     * String containing additional commands to be executed (additional to default or given commands). 
     * The commands (if there are more than one) have to be seperated by
     * semicolons or newlines ). This option can also be given by the property cmsshell.additionalcommand (using mvn ... -D). 
     *     
     * @parameter  expression="${cmsshell.additionalcommand}"
     */
    private String additionalcommands = null;
    
    
    /**
     * String containing the username to log into cmsshell with.
     * This defaults to "Admin" and/or can be given using the property cmsshell.username.
     * 
     * @parameter expression="${cmsshell.username} 
     *            default-value="Admin"
     */
    private String username = null;
    
    
    /**
     * String containing the password to log into cmsshell with.
     * This defaults to "admin" and/or can be given using the property cmsshell.password.
     * 
     * @parameter expression="${cmsshell.password} 
     *          default-value="admin"
     */
    private String password = null;

   
    
    /**
     * If this parameter is set the siteroot is changed to the given value after login.
     * This is accomplished by a call setSiteRoot( changetositeroot )

     * @parameter
     */
    private String changetositeroot = null;

   

    private String addLineBreaks(String input) {
        String[] parts = input.split(";");
        StringBuilder output = new StringBuilder();
        for (String part : parts) {
            output.append(part);
            output.append("\n");
        }

        return output.toString();
    }

    /**
     * Executes this task. 
     * This basically creates a CMSShell, writes any commands to a temporary file and
     * lets the shell execute the commands. 
     *
     * @throws  org.apache.maven.plugin.MojoExecutionException
     * @throws  org.apache.maven.plugin.MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        //File modulePackageDir = new File(wepappfolder + File.separator + "packages" + File.separator + "modules");

        File webinf = new File(webappfolder);


        if (!webinf.isDirectory()) {

            throw new MojoExecutionException("OpenCms-Webapp directory does not exist: " + webappfolder);
        }


        CmsShell shell = new CmsShell(webappfolder, "opencms", "opencms", "", null);


        File commandsToExecute = null;

        boolean deleteCommandsToExecute = true;

        if (commandFile != null && !"".equals(commandFile)) {
            commandsToExecute = new File(commandFile);
            if (!commandsToExecute.exists()) {
                throw new MojoExecutionException("commandFile does not exist: " + commandsToExecute.getAbsolutePath());
            }
            deleteCommandsToExecute = false;

        } else { // we need to create a tempfile since opencms wants a FileInputSource...

            String shellCommand = "";

            // if configuration has a commands we use it.
            if (commands != null && !"".equals(commands)) {

                shellCommand = addLineBreaks(commands);
            } else { // otherwise we default to import

                File zipFile = new File(getModuleDir(), getName() + "_" + getVersion() + ".zip");

                shellCommand = "login \"" + username + "\" \"" + password + "\"\n";
                if ( changetositeroot != null) {
                    shellCommand += "setSiteRoot \"" + changetositeroot + "\"\ngetSiteRoot\n";
                }
                shellCommand += "deleteModule " + getName() + "\nimportModule \"" + zipFile.getAbsolutePath() + "\"\n";
            }

            if (additionalcommands != null && !"".equals(additionalcommands)) {
                shellCommand += "\n" + addLineBreaks(additionalcommands);
            }

            try {
                shellCommand = escapeForCmsShell(shellCommand);
                getLog().info("I will execute the following commands:\n====\n" + shellCommand + "\n====\n" );
                commandsToExecute = File.createTempFile(this.getClass().getName(), null);

                FileWriter writer = new FileWriter(commandsToExecute);

                writer.write(shellCommand);
                writer.close();

            } catch (IOException ex) {

                throw new MojoExecutionException("Could not create or write temporary file for shellcommand", ex);
            }


        }

        // now read the stream again
        FileInputStream fis;
        try {

            fis = new FileInputStream(commandsToExecute);
        } catch (FileNotFoundException ex) {
            if (deleteCommandsToExecute) {
                commandsToExecute.delete();
            }
            throw new MojoExecutionException("Cannot create FileInputStream" + ex);
        }


        // give it to cmsshell
        try {

            shell.start(fis);

        } catch (Exception ex) {
            if (deleteCommandsToExecute) {
                commandsToExecute.delete();
            }
            throw new MojoExecutionException("Exceptoin while running commands in CmsShell", ex);
        }


        shell.exit();

        // if we created the file we will delete it afterwards
        if (deleteCommandsToExecute) {
            commandsToExecute.delete();
        }

    }


    // ##################################################
    // Getters and Setters below
    /**
     * Gets The (absolute) path WEB-INF/ of to a (runnable) OpenCms installation. 
     * @return String webapp-folder
     */
    public String getWebappfolder() {

        return webappfolder;
    }

    /**
     * Sets the (absolute) path WEB-INF/ of to a (runnable) OpenCms installation.
     * @param webappfolder String webapp-folder
     */
    public void setWebappfolder(String webappfolder) {

        this.webappfolder = webappfolder;
    }

    /**
     * Gets the String containing the commands to be executed. 
     * 
     * @return String
     */
    public String getCommands() {

        return commands;
    }

    /**
     * Sets the String containing the commands to be executed. 
     * 
     * @param commands String with commands(s)
     */
    public void setCommands(String command) {

        this.commands = command;
    }

    /**
     * Get additional (to command or default) commands to execute.
     * @return additional Commands.
     */
    public String getAdditionalcommands() {
        return additionalcommands;
    }

    /**
     * Sets additional (to command or default) commands to execute.
     * @param  additionalcommands Commands.
     */
    public void setAdditionalcommands(String additionalcommands) {
        this.additionalcommands = additionalcommands;
    }

    /**
     * Gets the Path to a file that contains the commands to be executed. 
     * @return path to the File containing the commands
     */
    public String getCommandFile() {

        return commandFile;
    }

    /**
     * Sets the path to a file that contains the commands to be executed. 
     * @param commandFile String path
     */
    public void setCommandFile(String commandFile) {

        this.commandFile = commandFile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
     public String getChangetositeroot() {
        return changetositeroot;
    }

    public void setChangetositeroot(String changetositeroot) {
        this.changetositeroot = changetositeroot;
    }

    private String escapeForCmsShell(String absolutePath) {
        // the filename seems to be evaluated as a java string in opencms
        // for windows file systems we have to replace any \ with \\
        return absolutePath.replaceAll("\\\\", "\\\\\\\\");
    }
}
