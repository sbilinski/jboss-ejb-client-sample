jboss-ejb-client-sample
=======================

Related discussion:
https://community.jboss.org/message/833041

Instructions:
1. Launch WildFly 8.0.0.Alpha4 (http://www.wildfly.org/download/) in standalone mode using standalone-full.xml.
2. Create a new user account for testing purposes: login=test password=1qaz2wsx! roles=guest  
3. Run "mvn clean install" (uses jboss-as-maven-plugin for app redeployment)
4. Run com.example.client.StandaloneClient; check server log.

