package com.example.mdb;

import static com.example.client.StandaloneClient.DEFAULT_PASS;
import static com.example.client.StandaloneClient.DEFAULT_USER;
import com.example.service.ExternalService;
import java.util.Properties;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
  
@MessageDriven(name = "TestMDB", activationConfig = { 
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/TestQueue"),
	@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
        @ActivationConfigProperty(propertyName = "transactionTimeout", propertyValue = "5000") 
})
public class TestMDB implements MessageListener {

    private static Logger logger = LoggerFactory.getLogger(TestMDB.class);
        
    @Resource
    MessageDrivenContext messageDrivenContext;
        
    @Override
    public void onMessage(Message msg) {        
        logger.info(">>> ON MESSAGE >>>");
        logger.info("MDB: principal="+ messageDrivenContext.getCallerPrincipal().getName());
            
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

            logger.info("Calling external service.");

            ExternalService externalService = (ExternalService) ejbRootNamingContext.lookup("/test//ExternalServiceBean!com.example.service.ExternalService");
            externalService.delegateToInternalMethod();
            
            logger.info("Done.");            
        } catch (Exception ex) { 
            logger.error("Remote invocation failed", ex);
        } finally { 
            logger.info("Closing EJB context...");
            try {
                if (ejbRootNamingContext != null)
                    ejbRootNamingContext.close();
                logger.info("Closed.");
            } catch (Throwable t) {
                logger.warn("Unable to close context: {}", t.getMessage());
            }
        }
        
        logger.info("<<< ON MESSAGE <<<");        
    }
                       
}
