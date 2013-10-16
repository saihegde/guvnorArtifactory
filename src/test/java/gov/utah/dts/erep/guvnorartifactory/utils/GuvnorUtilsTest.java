package gov.utah.dts.erep.guvnorartifactory.utils;

import gov.utah.dts.erep.guvnorartifactory.exceptions.GuvnorArtifactoryException;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath*:test-guvnorArtifactory-context.xml"})
public class GuvnorUtilsTest extends AbstractTestNGSpringContextTests {

    @Autowired
    GuvnorUtils guvnorUtils;
    private static final Logger logger = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());

    @Test
    public void listDirectoriesShouldListAllDirectoriesForTheFolderName() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        logger.debug("Temp Directory evaluates to : " + tmpDir);
        if((tmpDir != null) && !(tmpDir.isEmpty())){
            System.out.println(guvnorUtils.listDirectories(new File(System.getProperty("java.io.tmpdir"))));
            Assert.assertNotNull(guvnorUtils.listDirectories(new File(System.getProperty("java.io.tmpdir"))));
        }
        
    }
    
    @Test(expectedExceptions = GuvnorArtifactoryException.class)  
    public void listDirectoriesForNonexistentDirectoryShouldThrowException() {
        guvnorUtils.listDirectories(new File("NED://SHOULDFAIL"));
    }

}
