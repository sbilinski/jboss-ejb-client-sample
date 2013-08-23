package com.example.client;

import static com.example.client.StandaloneClient.DEFAULT_PASS;
import static com.example.client.StandaloneClient.DEFAULT_USER;
import java.text.MessageFormat;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

public class StandaloneJMSClient {
          
    public static void main(String[] args) throws Exception {
        System.out.println(MessageFormat.format("Login: user={0} pass={1}", DEFAULT_USER, DEFAULT_PASS));
        
        Context context = null;
        Connection connection = null;
        try {
            final Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
            props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080"); //WildFly 8.0.0.Alpha4, use 4447 for EAP-6.1.0.Alpha
            props.put(Context.SECURITY_PRINCIPAL, DEFAULT_USER);
            props.put(Context.SECURITY_CREDENTIALS, DEFAULT_PASS);

            context = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("jms/RemoteConnectionFactory");
            Queue queue = (Queue) context.lookup("jms/TestQueue");

            connection = connectionFactory.createConnection(DEFAULT_USER, DEFAULT_PASS);
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            connection.start();


            Message message = session.createTextMessage("Please invoke ExternalService...");
            producer.send(message);

            System.out.println("Message sent.");
        } finally {
            if (context != null) {
                context.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }
       
}