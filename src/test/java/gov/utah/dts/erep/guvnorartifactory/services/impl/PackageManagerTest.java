package gov.utah.dts.erep.guvnorartifactory.services.impl;

import java.io.File;

import gov.utah.dts.erep.guvnorartifactory.services.PackageManager;
import gov.utah.dts.erep.guvnorartifactory.services.impl.PackageManagerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath:test-guvnorArtifactory-context.xml"})
public class PackageManagerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    PackageManager packageManager;

    @Test
    public void getPackageNamesToUpdateShouldReturnTheListOfDirectoriesInGivenResourcesFolder() {
        Assert.assertNotNull(((PackageManagerImpl)packageManager).getPackageNamesToUpdate(new File(System.getProperty("java.io.tmpdir"))));
    }

}
