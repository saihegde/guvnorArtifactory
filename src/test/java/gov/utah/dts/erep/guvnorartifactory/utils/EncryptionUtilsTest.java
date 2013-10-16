package gov.utah.dts.erep.guvnorartifactory.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(locations = {"classpath*:test-guvnorArtifactory-context.xml"})
public class EncryptionUtilsTest extends AbstractTestNGSpringContextTests {

    @Autowired
    EncryptionUtils encryptionUtils;

    @Test
    public void testDecryptionShouldReturnThePlainText() {
        Assert.assertEquals(encryptionUtils.decrypt("8jDzsmNCjNtG3+JwgSTa+lKw+QHPQuKt"), "plainText");
    }

}
