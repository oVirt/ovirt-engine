package org.ovirt.engine.core.notifier.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Class tests two methods of obtaining database connectivity:<br>
 * <li>By retrieving connectivity credentials from JBoss configuration files <li>By retrieving connectivity properties
 * directly from service configuration file (e.g. for debug purpose)<br>
 * The use of application server properties precede the use of explicit connectivity properties
 */
public class ConnectionHelperTest {
    /**
     * Expect obtain database connectivity using JBoss configuration files.<br>
     * The tests uses inside the engine-config tool project
     */
    @Test
    public void testConnectivityWithDataSourceProperties() {
        ConnectionHelper helper = null;
        try {
            helper =
                    new ConnectionHelper(NotificationProperties.readPropertiesFile("src/test/resources/conf/notifier.conf"));
        } catch (Exception e) {
        }

        assertNotNull(helper);
        helper.closeConnection();

    }

    /**
     * Expect obtain database connectivity using explicit properties in service configuration file
     */
    @Test
    public void testConnectivityWithProperties() {
        ConnectionHelper helper = null;
        try {
            helper =
                    new ConnectionHelper(NotificationProperties.readPropertiesFile("src/test/resources/conf/connection-test-notifier.conf"));
        } catch (Exception e) {
        }
        assertNotNull(helper);
        helper.closeConnection();
    }

}
