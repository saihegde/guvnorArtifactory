package gov.utah.dts.erep.guvnorartifactory.services;

import gov.utah.dts.erep.guvnorartifactory.GuvnorArtifactoryConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath:test-guvnorArtifactory-context.xml"})
public class GuvnorArtifactoryConfigTest extends AbstractTestNGSpringContextTests{

    @Autowired
    GuvnorArtifactoryConfig guvnorArtifactoryConfig;
    
    @Test
    public void guvnorArtifactoryConfigShouldInitializeConfigProperties(){
        System.out.println(guvnorArtifactoryConfig.getHost());
        System.out.println(guvnorArtifactoryConfig.getContextRoot());
        Assert.assertNotNull(guvnorArtifactoryConfig.getHost());
    }
    
}
