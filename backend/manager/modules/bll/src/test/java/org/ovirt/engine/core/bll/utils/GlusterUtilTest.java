package org.ovirt.engine.core.bll.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GlusterUtilTest {
    private static final String SERVER_NAME1 = "testserver1";
    private static final String SERVER_NAME2 = "testserver2";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private static final String WRONG_PASSWORD = "wrong_password";
    private static final String FINGER_PRINT1 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:31";
    private static final String FINGER_PRINT2 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:32";
    private static final Map<String, String> EXPECTED_MAP = new HashMap<>();
    private static final String OUTPUT_XML =
            "<cliOutput><peerStatus><peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver1</hostname><connected>1</connected><state>3</state></peer>"
                    +
                    "<peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver2</hostname><connected>1</connected><state>3</state></peer></peerStatus></cliOutput>";
    private static final String OUTPUT_XML_NO_PEERS = "<cliOutput><peerStatus/></cliOutput>";

    @Mock
    private EngineSSHClient client;

    @Spy
    private GlusterUtil glusterUtil;

    @Before
    public void setUp() throws Exception {
        setupMock();
    }

    private void setupMock() throws AuthenticationException, Exception {
        doReturn(client).when(glusterUtil).getSSHClient();
        doNothing().when(glusterUtil).connect(client, SERVER_NAME1, USER, WRONG_PASSWORD);
        doReturn(FINGER_PRINT1).when(client).getHostFingerprint();
        doReturn(OUTPUT_XML).when(glusterUtil).executePeerStatusCommand(client);
        doThrow(AuthenticationException.class).when(glusterUtil).authenticate(client);
    }

    @Test
    public void testGetPeersWithFingerprint() throws AuthenticationException, IOException {
        EXPECTED_MAP.put(SERVER_NAME1, FINGER_PRINT1);
        EXPECTED_MAP.put(SERVER_NAME2, FINGER_PRINT2);
        doReturn(client).when(glusterUtil).getSSHClient();
        doNothing().when(glusterUtil).connect(client, SERVER_NAME1, USER, PASSWORD);
        doNothing().when(glusterUtil).authenticate(client);
        doReturn(EXPECTED_MAP).when(glusterUtil).getFingerprints(anySetOf(String.class));
        Map<String, String> peers = glusterUtil.getPeers(SERVER_NAME1, USER, PASSWORD, FINGER_PRINT1);
        assertNotNull(peers);
        peers.containsKey(SERVER_NAME1);
        assertEquals(FINGER_PRINT1, peers.get(SERVER_NAME1));
        peers.containsKey(SERVER_NAME2);
        assertEquals(FINGER_PRINT2, peers.get(SERVER_NAME2));
    }

    @Test
    public void testGetPeers() throws AuthenticationException, IOException {
        EXPECTED_MAP.put(SERVER_NAME1, FINGER_PRINT1);
        EXPECTED_MAP.put(SERVER_NAME2, FINGER_PRINT2);
        doReturn(client).when(glusterUtil).getSSHClient();
        doNothing().when(glusterUtil).connect(client, SERVER_NAME1, USER, PASSWORD);
        doNothing().when(glusterUtil).authenticate(client);
        doReturn(EXPECTED_MAP).when(glusterUtil).getFingerprints(anySetOf(String.class));
        Set<String> peers = glusterUtil.getPeers(SERVER_NAME1, USER, PASSWORD);
        assertNotNull(peers);
        assertTrue(peers.contains(SERVER_NAME1));
        assertTrue(peers.contains(SERVER_NAME2));
    }

    @Test(expected = AuthenticationException.class)
    public void testGetPeersWithWrongPassword() throws AuthenticationException, IOException {
        EXPECTED_MAP.put(SERVER_NAME1, FINGER_PRINT1);
        doReturn(client).when(glusterUtil).getSSHClient();
        doReturn(EXPECTED_MAP).when(glusterUtil).getFingerprints(anySetOf(String.class));
        glusterUtil.getPeers(SERVER_NAME1, USER, WRONG_PASSWORD);
    }

    @Test
    public void testHasPeersTrue() {
        EXPECTED_MAP.put(SERVER_NAME1, FINGER_PRINT1);
        doReturn(EXPECTED_MAP).when(glusterUtil).getFingerprints(anySetOf(String.class));
        assertNotNull(glusterUtil.getPeers(client));
        assertEquals(2, glusterUtil.getPeers(client).size());
    }

    @Test
    public void testHasPeersFalse() {
        EXPECTED_MAP.put(SERVER_NAME1, FINGER_PRINT1);
        doReturn(EXPECTED_MAP).when(glusterUtil).getFingerprints(anySetOf(String.class));
        doReturn(OUTPUT_XML_NO_PEERS).when(glusterUtil).executePeerStatusCommand(client);
        assertTrue(glusterUtil.getPeers(client).isEmpty());
    }
}
