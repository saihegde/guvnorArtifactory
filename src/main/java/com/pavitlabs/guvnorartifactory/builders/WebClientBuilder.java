package gov.utah.dts.erep.guvnorartifactory.builders;


public class WebClientBuilder {
    
    private String host = "localhost";
    private int port = 8080;
    private String contextRoot = "drools-guvnor";
    private String userName = "admin";
    private String encryptedPassword = "";
    
    public WebClientBuilder(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public WebClientBuilder contextRoot(String contextRoot){
        this.contextRoot = contextRoot;
        return this;
    }
    
    public WebClientBuilder username(String userName){
        this.userName = userName;
        return this;
    }
    
    public WebClientBuilder encryptedPassword(String encryptedPassword){
        this.encryptedPassword = encryptedPassword;
        return this;
    }

}
