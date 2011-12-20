package org.ovirt.engine.core.tools.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class JbossConnectionFactory implements ConnectionFactory {

    private ConnectivityCredentials credentials;
    private Logger log = Logger.getLogger(JbossConnectionFactory.class);
    private static final String DATASOURCES_NAMESPACE = "urn:jboss:domain:datasources:1.0:";
    private static final String SECURITY_NAMESPACE = "urn:jboss:domain:security:1.1:";
    private static final String DATASOURCE_PATH = "//" + DATASOURCES_NAMESPACE + "datasource[@pool-name='ENGINEDataSource']" + "/" + DATASOURCES_NAMESPACE;
    private static final String DRIVER_PATH = DATASOURCE_PATH + "driver";
    private static final String CONNECTION_URL_PATH = DATASOURCE_PATH + "connection-url";
    private static final String SECURITY_PATH = DATASOURCE_PATH + "security";
    private static final String USERNAME_PATH = SECURITY_PATH + "/" + DATASOURCES_NAMESPACE + "user-name";
    private static final String PASSWORD_PATH = SECURITY_PATH + "/" + DATASOURCES_NAMESPACE + "password";
    private static final String DRIVER_CLASS_PATH = "//" + DATASOURCES_NAMESPACE + "driver[@name='%1$s']" + "/" + DATASOURCES_NAMESPACE + "xa-datasource-class";
    private static final String SECURITY_DOMAIN_NAME_PATH = SECURITY_PATH + "/" + DATASOURCES_NAMESPACE + "security-domain";
    private static final String SECURITY_DOMAIN_PATH = "//" + SECURITY_NAMESPACE + "security-domain[@name='%1$s']" + "/" + SECURITY_NAMESPACE + "authentication" + "/" + SECURITY_NAMESPACE + "login-module";
    private static final String SECURITY_DOMAIN_USER_NAME_PATH = SECURITY_DOMAIN_PATH + "/" + SECURITY_NAMESPACE + "module-option[@name='username']";
    private static final String SECURITY_DOMAIN_PASSWORD_PATH = SECURITY_DOMAIN_PATH + "/" + SECURITY_NAMESPACE + "module-option[@name='password']";
    private static final String VALUE_ATTRIBUTE = "value";

    public JbossConnectionFactory(String jbossDataSourceFile, String jbossLoginConfigFile)
            throws ClassNotFoundException, XPathExpressionException {
        this.credentials =
                getConnectivityCredentials(jbossDataSourceFile, jbossLoginConfigFile);

        log.debug("getConnection: driver class name=" + credentials.getDriverClassName());

        try {
            Class.forName(credentials.getDriverClassName());
        } catch (ClassNotFoundException e) {
            log.error("faild to load db driver.");
            throw e;
        }
    }

    @Override
    public Connection getConnection() throws SQLException{

        log.debug("getConnection: URL=" + credentials.getConnectionUrl());

        if (StringUtils.isNotEmpty(credentials.getSecurityDomain())){
            log.debug("getConnection: Considering encrypted passord. secDomain=" + credentials.getSecurityDomain());
        }

        return DriverManager.getConnection(credentials.getConnectionUrl(),
                credentials.getUserName(),
                credentials.getPassword());
    }

    private static String getStringFromXml(XPath xpath, String path, InputSource inputSource) throws XPathExpressionException {
        String retVal = (String) xpath.evaluate(path, inputSource, XPathConstants.STRING);

        if ( retVal != null ) {
            retVal = retVal.trim();
        }

        return retVal;
    }

    private static String getAttributeStringFromXml(XPath xpath, String path, InputSource inputSource, String attrName) throws XPathExpressionException {
        Node node = (Node) xpath.evaluate(path, inputSource, XPathConstants.NODE);
        String retVal = null;

        if ( node != null ) {
            Node attr = node.getAttributes().getNamedItem(attrName);
            if ( attr != null ) {
                retVal = attr.getTextContent().trim();
            }
        }

        return retVal;
    }

    public static ConnectivityCredentials getConnectivityCredentials(String jbossConfigurationFile,
            String jbossLoginConfigFile)
            throws XPathExpressionException {
        ConnectivityCredentials credentials = new ConnectivityCredentials();

        XPath xpath = XPathFactory.newInstance().newXPath();
	    InputSource inputSource = new InputSource(jbossConfigurationFile);

        credentials.setConnectionUrl(getStringFromXml(xpath, CONNECTION_URL_PATH, inputSource));
        String driver = getStringFromXml(xpath, DRIVER_PATH, inputSource);
        credentials.setDriverClassName(getStringFromXml(xpath, String.format(DRIVER_CLASS_PATH, driver), inputSource));

        String securityDomain = getStringFromXml(xpath, SECURITY_DOMAIN_NAME_PATH, inputSource);

	    if ( securityDomain != null ) {
            credentials.setSecurityDomain(securityDomain);
            credentials.setUserName(getAttributeStringFromXml(xpath, String.format(SECURITY_DOMAIN_USER_NAME_PATH, securityDomain), inputSource, VALUE_ATTRIBUTE));
            String password = getAttributeStringFromXml(xpath, String.format(SECURITY_DOMAIN_PASSWORD_PATH, securityDomain), inputSource, VALUE_ATTRIBUTE);
            credentials.setPassword(EncryptionUtils.decode(password, "", ""));
	    } else {
            credentials.setUserName(getStringFromXml(xpath, USERNAME_PATH, inputSource));
            credentials.setPassword(getStringFromXml(xpath, PASSWORD_PATH, inputSource));
	    }

        return credentials;
    }

    public static class ConnectivityCredentials {
        private String userName;
        private String password;
        private String connectionUrl;
        private String driverClassName;
        private String securityDomain = null;

        public String getSecurityDomain() {
            return securityDomain;
        }

        public void setSecurityDomain(String securityDomain) {
            this.securityDomain = securityDomain;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConnectionUrl() {
            return connectionUrl;
        }

        public void setConnectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }
    }
}
