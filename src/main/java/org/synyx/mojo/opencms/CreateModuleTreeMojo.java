package org.synyx.mojo.opencms;

import java.io.File;
import java.io.IOException;

import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.util.FileUtils;

import org.apache.maven.plugin.MojoFailureException;


/**
 * This Mojo creates a module-tree containing all files to be included in the module. It does so by copying all files
 * that dont match the excludes2 to the distdir.
 *
 * @goal    createtree
 * @phase   package
 * @author  Marc Kannegiesser, kannegiesser@synyx.de
 */
public class CreateModuleTreeMojo extends AbstractOpenCmsMojo {

    /**
     * If you provide targetLibPath the file found under jarFile is copied to it. This defaults to
     * ${project.build.directory}/${project.build.finalName}.jar
     *
     * @parameter  expression="${project.build.directory}/${project.build.finalName}.jar"
     */
    private String jarFile = null;

    /**
     * Directories in sourceDir to exclude (ant-style excludes). You may provide excludes in ant-style patterns. Files
     * matching that pattern will not be included to the module (basically they will not be copied to distDir).
     *
     * @parameter
     */
    private String excludes = null;

    /**
     * Defines a set of resources that are included in the module. Currently only directory and targetPath are
     * evaluated. The targetPath configured is relative to the distFolder. A possible configuration might look like
     * this:
     *
     * <pre>
    <srcResources>
    <resource>
    <directory>${project.basedir}/web/</directory>
    <targetPath></targetPath>
    </resource>
    <resource>
    <directory>${project.basedir}/../web-module/src/main/webapp/WEB-INF/</directory>
    <targetPath>system/modules/my.module/WEB-INF/</targetPath>
    </resource>
    </srcResources>
     * </pre>
     *
     * @parameter
     * @required
     */
    private Resource[] srcResources;

    /**
     * Executes this task and creates the module-tree within the distDir.
     *
     * @throws  org.apache.maven.plugin.MojoExecutionException
     * @throws  org.apache.maven.plugin.MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        File target = new File(getDistfolder());

        // only create the target directoty when it doesn't exist
        // no deletion
        if (!target.exists()) {
            target.mkdir();
        } else {
            getLog().warn("Using existing target directory " + target.getAbsolutePath());
        }

        getLog().info("Target is " + target);

        String excludes2 = getExcludes();

        if (excludes2 == null) {
            excludes2 = "**/.svn/,**/.cvs/";

            getLog().info("Configuration for excludes not set. Defaulting to: \"" + excludes2
                + "\". Excludes are ant-fileset-style.");
        } else {
            if (excludes2.contains("__acl") || excludes2.contains("__properties")) {
                getLog().warn("Your excludes \"" + excludes2
                    + "\" contains __acl or __properties. You do not want these to be excluded in order to have permissoins and properties work.");
            }
        }

        try {
            String targetLibPath = getTargetLibPath();

            if ((null != targetLibPath) && !"".equals(targetLibPath)) {
                File targetLibDir = new File(target, targetLibPath);

                if (!targetLibDir.exists()) {
                    targetLibDir.mkdirs();
                }

                if (!targetLibDir.isDirectory()) {
                    throw new MojoExecutionException("Could not create targetLibdir: " + targetLibDir.toString());
                }

                File jar = new File(jarFile);
                FileUtils.copyFileToDirectory(jar, targetLibDir);
            }

            for (Resource resource : srcResources) {
                File source = new File(resource.getDirectory());
                File currentTarget = null;

                if (resource.getTargetPath() != null) {
                    currentTarget = new File(target, resource.getTargetPath());
                } else {
                    currentTarget = target;
                }

                String normalized = source.getAbsolutePath();
                int offsetLength = normalized.length();

                getLog().debug("Normalzed sourcefolder is " + normalized + " (Offset is: " + offsetLength + ")");

                List<String> results = FileUtils.getFileAndDirectoryNames(source, null, excludes2, true, true, true,
                        true);

                for (String fileName : results) {
                    File file = new File(fileName);

                    String normalizedParent = new File(file.getParent()).getAbsolutePath();

                    if (offsetLength > normalizedParent.length()) {
                        getLog().info("Skipping file (its the sourcefolder or a parent of it): "
                            + file.getAbsolutePath());
                        getLog().debug("Parent: " + normalizedParent + " with length " + normalizedParent.length());

                        continue;
                    }

                    String relative = file.getParent();
                    relative = relative.substring(offsetLength); // cut the target-dir away

                    getLog().debug("Copy " + file + " to " + currentTarget.getPath() + File.separator + relative);

                    if (file.isFile()) {
                        File parent = new File(currentTarget.getPath() + File.separator + relative);

                        if (!parent.exists()) {
                            // create dir if it does not exist
                            parent.mkdirs();
                        }

                        FileUtils.copyFileToDirectory(file, parent);
                    } else if (file.isDirectory()) {
                        // take care about directories (so that empty dirs get created)

                        File tocreate = new File(currentTarget.getPath() + File.separator + relative + File.separator
                                + file.getName());
                        getLog().debug("Creating dir" + tocreate.toString());
                        tocreate.mkdirs();
                    }
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Could copy file(s) to directory", ex);
        }
    }


    public Resource[] getSrcResources() {

        return srcResources;
    }


    public void setSrcResources(Resource[] resources) {

        this.srcResources = resources;
    }


    public String getExcludes() {

        return excludes;
    }


    public void setExcludes(String excludes) {

        this.excludes = excludes;
    }


    public String getJarFile() {

        return jarFile;
    }


    public void setJarFile(String jarFile) {

        this.jarFile = jarFile;
    }
}
