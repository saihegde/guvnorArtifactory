package gov.utah.dts.erep.guvnorartifactory;

import gov.utah.dts.erep.guvnorartifactory.utils.EncryptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

@Configuration
@PropertySource(value = {"file:${ereproot}/ruleengine/guvnor/guvnor.properties"})
public class GuvnorArtifactoryConfig {

    @Autowired
    Environment environment;

    @Autowired
    EncryptionUtils encryptionUtils;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * @return the host
     */
    public String getHost() {
        return environment.getProperty("guvnor.host", "localhost");
    }

    /**
     * @return the port
     */
    public String getPort() {
        return environment.getProperty("guvnor.port", "8080");
    }

    /**
     * @return the contextRoot
     */
    public String getContextRoot() {
        return environment.getProperty("guvnor.contextRoot", "drools-guvnor");
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return environment.getProperty("guvnor.usr", "admin");
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return environment.getProperty("guvnor.pwd", "tE+VIgh8jy15U0a7ugcNow==");
    }

    /**
     * @return the AuthScope
     */
    public AuthScope getAuthScope() {
        return new AuthScope(getHost(), Integer.valueOf(getPort()), AuthScope.ANY_REALM);
    }

    /**
     * @return the credentials
     */
    public Credentials getCredentials() {
        return new UsernamePasswordCredentials(getUsername(), encryptionUtils.decrypt(getPassword()));
    }

    /**
     * @return the baseURL
     */
    public String getBaseURL() {
        return "http://" + getHost() + ":" + getPort() + "/" + getContextRoot();
    }



}
