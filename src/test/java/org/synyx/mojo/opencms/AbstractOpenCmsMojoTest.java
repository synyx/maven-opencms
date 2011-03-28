package org.synyx.mojo.opencms;

import junit.framework.TestCase;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author Florian Hopf, Synyx GmbH & Co. KG
 */
public class AbstractOpenCmsMojoTest extends TestCase {

    private AbstractOpenCmsMojo mojo;

    public AbstractOpenCmsMojoTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        mojo = new AbstractOpenCmsMojo() {

            @Override
            public void execute() throws MojoExecutionException, MojoFailureException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }



    public void testVersionWithOnlyDigits() {

        mojo.setVersion("1.2.3");
        assertEquals("1.2.3", mojo.getVersion());

    }

    public void testSnapshotVersion() {
        mojo.setVersion("0.1-SNAPSHOT");
        assertEquals("0.1", mojo.getVersion());
    }

}
