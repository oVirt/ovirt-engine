package org.ovirt.engine.core.config;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import org.ovirt.engine.core.tools.common.db.JbossConnectionFactory;

public class RhevConnectionFactoryTest {

    public static final Logger log = Logger.getLogger(RhevConnectionFactoryTest.class);
    private static Configuration appConfig = null;

    @BeforeClass
    public static void initConfigurationFile() throws ConfigurationException {
        appConfig = new PropertiesConfiguration("target/test-classes/engine-tools.conf");
    }

    /**
     * Test tries to obtain a database connection by connectivity credentials which were extracted from JBoss
     * configuration files in non-encrypted form
     * @throws Exception
     */
    @Test
    public void getConnectionByOpenPassword() throws Exception {
        log.info("getConnectionByOpenPassword: Testing obtaining a connection by non-encrypted password");
        String jbossDataSourceFile = appConfig.getString("jbossDataSourceFile");
        JbossConnectionFactory connectionFactory =
                new JbossConnectionFactory(jbossDataSourceFile, null);
        Connection connection = connectionFactory.getConnection();
        connection.close();
        assertTrue(connection.isClosed());
    }

    /**
     * Test tries to obtain a database connection by connectivity credentials which were extracted from JBoss
     * configuration files in an encrypted form
     * @throws Exception
     */
    @Test
    public void getConnectionBySecuredPassword() throws Exception {
        log.info("getConnectionBySecuredPassword: Testing obtaining a connection by an encrypted password");
        String jbossDataSourceFile = appConfig.getString("jbossEncodedDataSourceFile");
        String jbossLoginConfigFile = appConfig.getString("jbossEncodedLoginConfigFile");
        JbossConnectionFactory connectionFactory =
                new JbossConnectionFactory(jbossDataSourceFile, jbossLoginConfigFile);
        Connection connection = connectionFactory.getConnection();
        connection.close();
        assertTrue(connection.isClosed());
    }

}
