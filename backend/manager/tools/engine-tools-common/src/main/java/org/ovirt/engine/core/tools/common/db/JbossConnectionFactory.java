package org.ovirt.engine.core.tools.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;

public class JbossConnectionFactory implements ConnectionFactory {

    private ConnectivityCredentials credentials;
    private Logger log = Logger.getLogger(JbossConnectionFactory.class);

    public JbossConnectionFactory(String jbossDataSourceFile, String jbossLoginConfigFile)
            throws ConfigurationException, ClassNotFoundException {
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

    public static ConnectivityCredentials getConnectivityCredentials(String jbossDataSourceFile,
            String jbossLoginConfigFile)
            throws ConfigurationException {
        ConnectivityCredentials credentials = new ConnectivityCredentials();
        XMLConfiguration dsFile = new XMLConfiguration(jbossDataSourceFile);
        credentials.setDriverClassName(dsFile.getString("local-tx-datasource.driver-class"));
        credentials.setConnectionUrl(dsFile.getString("local-tx-datasource.connection-url"));
        String secDomain = dsFile.getString("local-tx-datasource.security-domain");
        if (StringUtils.isNotEmpty(secDomain)) {
            credentials.setSecurityDomain(secDomain);
            XMLConfiguration loginConfigFile = new XMLConfiguration(jbossLoginConfigFile);
            loginConfigFile.setExpressionEngine(new XPathExpressionEngine());
            SubnodeConfiguration root =
                    loginConfigFile.configurationAt("application-policy[@name='" + secDomain + "']");
            credentials.setUserName(root.getString("authentication/login-module/module-option[@name='username']"));
            String password = root.getString("authentication/login-module/module-option[@name='password']");
            credentials.setPassword(EncryptionUtils.decode(password, "", ""));
        } else {
            credentials.setUserName(dsFile.getString("local-tx-datasource.user-name"));
            credentials.setPassword(dsFile.getString("local-tx-datasource.password"));
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
