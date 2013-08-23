package com.example.client;

import com.example.service.ExternalService;
import com.example.service.InternalService;
import java.text.MessageFormat;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;

public class StandaloneClient {
      
    public static final String DEFAULT_USER = "test"; //Assume "guest" role for JMS testing (default settings)
    public static final String DEFAULT_PASS = "1qaz2wsx!";
    
    public static void main(String[] args) throws Exception {
        System.out.println(MessageFormat.format("Login: user={0} pass={1}", DEFAULT_USER, DEFAULT_PASS));
        
        final Properties props = new Properties();
        props.put("remote.connections", "default");
        props.put("remote.connection.default.username", DEFAULT_USER);
        props.put("remote.connection.default.password", DEFAULT_PASS);
        props.put("remote.connection.default.host", "127.0.0.1");
        
        //EAP-6.1.0.Alpha
        //props.put("remote.connection.default.port", "4447");
        //WildFly 8.0.0.Alpha4 / EJB over HTTP
        props.put("remote.connection.default.port", "8080");        
        
        //Implies "org.jboss.xnio" module dependency
        props.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false"); 
        props.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false");	
        props.put("remote.connection.default.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", "JBOSS-LOCAL-USER");
        props.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
        
        //https://docs.jboss.org/author/display/WFLY8/Scoped+EJB+client+contexts
        props.put("org.jboss.ejb.client.scoped.context","true");
        
        props.put("javax.security.sasl.policy.noplaintext", "false");
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");                      
        
        Context ejbRootNamingContext = null;
        try {
            ejbRootNamingContext = (Context) new InitialContext(props).lookup("ejb:");
                        
            //Direct call works as expected (no additional EJB Context is created)
            InternalService internalService = (InternalService) ejbRootNamingContext.lookup("/test//InternalServiceBean!com.example.service.InternalService");
            internalService.internalBusinessMethod();
            
            //Fails on transaction commit (ARJUNA016039), but no exception is thrown (JMS will reattemp delivery tough).
            ExternalService externalService = (ExternalService) ejbRootNamingContext.lookup("/test//ExternalServiceBean!com.example.service.ExternalService");
            externalService.delegateToInternalMethod();                        
            System.out.println("Done.");
        } catch (Exception ex) { 
            System.err.println("Remote invocation failed: " + ex.getMessage());
        } finally { 
            System.out.println("Closing EJB context...");
            try {
                if (ejbRootNamingContext != null) {
                    ejbRootNamingContext.close();
                }
                System.out.println("Closed.");
            } catch (Throwable t) {
                System.err.println("Unable to close context" + t.getMessage());
            }
        }
    }
       
}