package org.ovirt.engine.core.vdsbroker.jsonrpc;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.vdsm.jsonrpc.client.ClientConnectionException;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcClient;
import org.ovirt.vdsm.jsonrpc.client.JsonRpcRequest;
import org.ovirt.vdsm.jsonrpc.client.RequestBuilder;


/**
 * This class can be used to test communication between vdsm and java code without running heavy engine.
 *
 */
@Ignore
public class JsonRpcIntegrationTest {

    // Please customize HOST_ADDRESS for your vdsm location
    private final static String HOST_ADDRESS = "192.168.1.10";
    private final static int PORT = 4044;
    private final static int TIMEOUT = 5000;
    private final static String DEFAULT_REQUEST_QUEUE = "jms.queue.requests";
    private final static String DEFAULT_RESPONSE_QUEUE = "jms.queue.reponses";

    @Test
    public void testGetVdsCapabilities() throws InterruptedException, ExecutionException, ClientConnectionException {
        JsonRpcClient client = JsonRpcUtils.createStompClient(HOST_ADDRESS, PORT, TIMEOUT, 0, TIMEOUT, TIMEOUT, true, "TLSv1",
                DEFAULT_REQUEST_QUEUE, DEFAULT_RESPONSE_QUEUE);
        final JsonRpcRequest request = new RequestBuilder("Host.getCapabilities").build();
        Map<String, Object> map = new FutureMap(client, request);
        assertTrue(map.isEmpty());
    }
}
