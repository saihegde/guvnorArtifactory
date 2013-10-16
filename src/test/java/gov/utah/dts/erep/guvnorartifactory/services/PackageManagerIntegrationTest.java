package gov.utah.dts.erep.guvnorartifactory.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath:test-guvnorArtifactory-context.xml"})
public class PackageManagerIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    PackageManager packageManager;

    @Test
    public void listShouldReturnThePackageNamesInGuvnor() {
        System.out.println(packageManager.list());
    }

}
