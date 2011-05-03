package org.synyx.mojo.opencms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Creates the module structure for an existing module.
 * Copies all included files, creates properties
 * as well as acl entries according to the manifest.
 * @author Florian Hopf, Synyx GmbH & Co. KG
 * @goal initFromModule
 */
public class CreateModuleStructure extends AbstractMojo {

    /**
     * The absolute path to the module zip to import from.
     * @parameter expression="${opencms.pathToModule}"
     * @required
     */
    private String pathToModule;
    /**
     * The path to create the folder structure to.
     * @parameter expression="${opencms.targetPath}" default-value="src/main/vfs"
     */
    private String targetPath;

    private String createModuleFile(Node currentFileNode, File targetFolder, File moduleFolder) throws IOException {
        Node destination = currentFileNode.selectSingleNode("destination");
        Node type = currentFileNode.selectSingleNode("type");
        if ("folder".equals(type.getText())) {
            createDirectory(targetFolder, destination.getText());
        } else {
            File src = new File(moduleFolder, destination.getText());
            File dest = new File(targetFolder, destination.getText());
            if (src.exists()) {
                FileUtils.copyFile(src, dest);
            } else {
                getLog().warn(src.getAbsolutePath() + " seems to be a sibling, copy manually");
            }
        }
        String relativePath = destination.getText();
        return relativePath;
    }

    private String unzipModule(String pathToModule) throws IOException {
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"));

        File moduleFolder = createDirectory(tmpFolder, "opencms-maven-" + String.valueOf(System.currentTimeMillis()));

        getLog().debug("Using temp folder " + moduleFolder.getAbsolutePath());

        int entries = 0;

        ZipFile zipfile = new ZipFile(pathToModule);
        for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            unzipEntry(zipfile, entry, moduleFolder);
            entries++;
        }

        getLog().info("Extracted " + entries + " entries");

        return moduleFolder.getAbsolutePath();
    }

    private File createDirectory(File parent, String name) {
        File dir = new File(parent, name);
        if (!dir.exists()) {
            getLog().debug("Creating directory " + dir.getAbsolutePath());
            dir.mkdirs();
        }
        return dir;
    }

    private void createParentFolders(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        getLog().debug("Extracting entry " + entry.getName());

        if (entry.isDirectory()) {
            createDirectory(outputDir, entry.getName());
            return;
        }

        File outputFile = new File(outputDir, entry.getName());

        createParentFolders(outputFile);

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private Document readManfestFromModuleFolder(String moduleFolder) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(moduleFolder, "manifest.xml"));
        return document;
    }

    private void processFiles(Document manifest, String moduleFolderPath, String targetPath) throws IOException {
        File moduleFolder = new File(moduleFolderPath);
        File targetFolder = new File(targetPath);
        List<Node> fileNodes = manifest.selectNodes("/export/files/file");
        for (Node currentFileNode : fileNodes) {
            String relativePath = createModuleFile(currentFileNode, targetFolder, moduleFolder);

            File currentFile = new File(targetFolder, relativePath);
            writeProperties(currentFile, currentFileNode);
            writeAcl(currentFile, currentFileNode);
        }
    }

    private void writeProperties(File currentFile, Node currentFileNode) throws IOException {
        File propertiesFolder = createDirectory(currentFile.getParentFile(), "__properties");
        File propertiesFile = null;
        if (currentFile.isFile()) {
            propertiesFile = new File(propertiesFolder, currentFile.getName() + ".properties");
        } else {
            propertiesFile = new File(propertiesFolder, "__" + currentFile.getName() + ".properties");
        }

        String content = createStructureInfo(currentFileNode) + createProperties(currentFileNode);

        FileUtils.writeStringToFile(propertiesFile, content);
    }

    private String createProperties(Node currentFileNode) {
        StringBuilder builder = new StringBuilder();
        List<Node> propertyNodes = currentFileNode.selectNodes("properties/property");
        for (Node currentPropNode : propertyNodes) {
            Attribute propType = (Attribute) currentPropNode.selectSingleNode("type");
            // skip shared properties
            if (propType == null) {
                builder.append(currentPropNode.selectSingleNode("name").getText());
                builder.append("=");
                builder.append(currentPropNode.selectSingleNode("value").getText());
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    //EurelisProperty.type=jsp
    //EurelisProperty.structureUUID=572431da-c135-11dd-9639-00c0f03d5b7d
    //EurelisProperty.resourceUUID=572431d9-c135-11dd-9639-00c0f03d5b7c
    private String createStructureInfo(Node currentFileNode) {
        StringBuilder builder = new StringBuilder();
        builder.append("EurelisProperty.type=");
        builder.append(currentFileNode.selectSingleNode("type").getText());
        builder.append("\nEurelisProperty.structureUUID=");
        builder.append(currentFileNode.selectSingleNode("uuidstructure").getText());
        Node resourceUUID = currentFileNode.selectSingleNode("uuidresource");
        if (resourceUUID != null) {
            builder.append("\nEurelisProperty.resourceUUID=");
            builder.append(resourceUUID.getText());
        }
        builder.append("\n");

        return builder.toString();
    }

    private void writeAcl(File currentFile, Node currentFileNode) throws IOException {
        Node accessControl = currentFileNode.selectSingleNode("accesscontrol");
        List<Node> accessControlEntries = accessControl.selectNodes("accessentry");
        if (accessControlEntries != null && !accessControlEntries.isEmpty()) {

            File aclFolder = createDirectory(currentFile.getParentFile(), "__acl");
            File xmlFile = null;
            if (currentFile.isFile()) {
                xmlFile = new File(aclFolder, currentFile.getName() + ".xml");
            } else {
                xmlFile = new File(aclFolder, "__" + currentFile.getName() + ".xml");
            }

            FileUtils.writeStringToFile(xmlFile, accessControl.asXML());
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            getLog().info("Importing module " + pathToModule + " to " + targetPath);

            // unzip to a temp folder
            String moduleFolder = unzipModule(pathToModule);

            // parse the manifest
            Document manifest = readManfestFromModuleFolder(moduleFolder);

            File targetFolder = new File(targetPath);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }

            // iterate all files configured in the manifest
            processFiles(manifest, moduleFolder, targetPath);

            getLog().info("Done importing module");

        } catch (DocumentException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }
}
