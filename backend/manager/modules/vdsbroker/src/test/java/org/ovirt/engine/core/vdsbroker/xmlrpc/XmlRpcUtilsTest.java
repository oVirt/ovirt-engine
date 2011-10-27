package org.ovirt.engine.core.vdsbroker.xmlrpc;

import java.util.Map;

import org.junit.Test;

public class XmlRpcUtilsTest {

    @Test
    public void testGetHttpConnection() {
        // String hostName = "10.35.16.31";
        // int port = 54321;
        // int clientTimeOut = 10*1000;
        // VdsServerConnector connector =
        // XmlRpcUtils.getHttpConnection(hostName, port, clientTimeOut,
        // VdsServerConnector.class);
        // Map<String,Object> result = connector.list();
        // System.out.println("the result size"+result.size());
    }

    private interface VdsServerConnector {
        public Map<String, Object> list();
    }
}
