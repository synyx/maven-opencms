package org.synyx.mojo.opencms;

import org.synyx.mojo.opencms.helper.MavenPlexusLoggerWrapper;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.io.File;
import java.io.IOException;

/**
 * This Mojo creates the zip file of a previously created module tree.
 *
 * @goal    createzip
 * @phase   package
 * @author  Marc Kannegiesser, kannegiesser@synyx.de
 */
public class ZipModuleMojo extends AbstractOpenCmsMojo {

    /**
     * Executes this task
     *
     * @throws  org.apache.maven.plugin.MojoExecutionException
     * @throws  org.apache.maven.plugin.MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        File source = new File(getDistfolder());

        try {

            ZipArchiver archiver = new ZipArchiver();


            archiver.enableLogging(new MavenPlexusLoggerWrapper(0, "", getLog()));

            String[] excludes = new String[]{"**/sites", "**/__properties", "**/__properties/**", "**/__acl", "**/__acl/**"};
            // no includes, exludes st mal diee above
            archiver.addDirectory(source, null, excludes);

            File destination = new File(getModuleDir());


            if (!destination.exists()) {

                destination.mkdirs();
            }


            if (!destination.isDirectory()) {

                throw new MojoExecutionException("moduledir is not a directory");
            }


            archiver.setDestFile(new File(destination, getName() + "_" + getVersion() + ".zip"));
            archiver.createArchive();

        } catch (ArchiverException ex) {

            throw new MojoExecutionException("Could not zip the targetdirectory", ex);
        } catch (IOException ex) {

            throw new MojoExecutionException("Could not zip the targetdirectory", ex);
        }

    }
    // ##################################################
    // Getters and Setters below
}
