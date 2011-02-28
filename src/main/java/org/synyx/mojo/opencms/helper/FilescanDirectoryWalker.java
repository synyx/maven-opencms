package org.synyx.mojo.opencms.helper;

import org.apache.commons.io.DirectoryWalker;

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.List;


/**
 * This DirectoryWalker collects all Files and Directories of a given Start-Directory. You can exclude certain
 * directories by giving their exact names as a List.
 *
 * @author  macmac
 */
public class FilescanDirectoryWalker extends DirectoryWalker {

    /** List of Strings containing names of directories to be excluded */
    List<String> excludedirectories = null;


    /**
     * Creates a new instance of FilescanDirectoryWorker
     *
     * @param  excludedirectories  List<String> containing directories to be excluded
     */
    public FilescanDirectoryWalker(List<String> excludedirectories) {

        this();
        this.excludedirectories = excludedirectories;

    }


    /**
     * Creates a new instance of FilescanDirectoryWorker
     */
    public FilescanDirectoryWalker() {

        super();
    }


    /**
     * Decides wether to step into a directory or not and adds the directory to the results if it does not match any
     * excludes.
     *
     * @param   directory  File containing the dir
     * @param   depth      depth (ignored)
     * @param   results    Collection holding the results of the scan
     *
     * @return  true if the name does not equal a given excludedirectory
     */
    @Override
    protected boolean handleDirectory(File directory, int depth, Collection results) {

        if (excludedirectories != null) {

            for (String exclude : excludedirectories) {

                if (exclude.equals(directory.getName())) {

                    return false;
                }
            }
        }


        results.add(directory);

        return true;
    }


    /**
     * Adds the file to the results (always)
     *
     * @param  file     File to be added
     * @param  depth    depth (ignored)
     * @param  results  Collection containing the results
     */
    @Override
    protected void handleFile(File file, int depth, Collection results) {

        results.add(file);
    }


    /**
     * Starts the scan
     *
     * @param   startDirectory
     * @param   results
     *
     * @throws  java.io.IOException
     */
    public void scan(File startDirectory, Collection results) throws IOException {

        walk(startDirectory, results);
    }

}
