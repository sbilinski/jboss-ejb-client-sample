package com.example.service;

import static com.example.client.StandaloneClient.DEFAULT_PASS;
import static com.example.client.StandaloneClient.DEFAULT_USER;
import java.util.Properties;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Remote(ExternalService.class)
@Stateless
public class ExternalServiceBean implements ExternalService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceBean.class);

    @Resource
    private SessionContext sessionContext;
              
    @PermitAll
    @Override
    public void delegateToInternalMethod() {
        logger.info(">>> EXTERNAL BUSINESS METHOD >>>");
        logger.info("SLSB: principal="+ sessionContext.getCallerPrincipal().getName());
            
        final Properties props = new Properties();
        props.put("remote.connections", "default");
        props.put("remote.connection.default.username", DEFAULT_USER);
        props.put("remote.connection.default.password", DEFAULT_PASS);
        props.put("remote.connection.default.host", "127.0.0.1");
        
        //EAP-6.1.0.Alpha
        //props.put("remote.connection.default.port", "4447");
        //WildFly / EJB over HTTP
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
                    
            InternalService internalService = (InternalService) ejbRootNamingContext.lookup("/test//InternalServiceBean!com.example.service.InternalService");
            internalService.internalBusinessMethod();
            
        } catch (Exception ex) { 
            logger.error("Remote invocation failed", ex);
        } finally { 
            logger.info("Closing EJB context...");
            try {
                if (ejbRootNamingContext != null)
                    ejbRootNamingContext.close();
                logger.info("Done.");
            } catch (Throwable t) {
                logger.warn("Unable to close context: {}", t.getMessage());
            }
        }
        
        logger.info("<<< EXTERNAL BUSINESS METHOD <<<");
    }
                         
}
