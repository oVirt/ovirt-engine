package org.ovirt.engine.core.bll.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlusterUtilTest {
    private static final String SERVER_NAME1 = "testserver1";
    private static final String SERVER_NAME2 = "testserver2";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private static final String WRONG_PASSWORD = "wrong_password";
    private static final String FINGER_PRINT1 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:31";
    private static final String FINGER_PRINT2 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:32";
    private static final String OUTPUT_XML =
            "<cliOutput><peerStatus><peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver1</hostname><connected>1</connected><state>3</state></peer>"
                    +
                    "<peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver2</hostname><connected>1</connected><state>3</state></peer></peerStatus></cliOutput>";
    private static final String OUTPUT_XML_NO_PEERS = "<cliOutput><peerStatus/></cliOutput>";

    @Mock
    private EngineSSHClient client;

    @Spy
    private GlusterUtil glusterUtil;

    @BeforeEach
    public void setUp() throws Exception {
        doNothing().when(glusterUtil).connect(client, SERVER_NAME1, USER, WRONG_PASSWORD);
        doReturn(OUTPUT_XML).when(glusterUtil).executePeerStatusCommand(client);
        doThrow(AuthenticationException.class).when(glusterUtil).authenticate(client);
    }

    @Test
    public void testGetPeersWithFingerprint() throws AuthenticationException, IOException {
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put(SERVER_NAME1, FINGER_PRINT1);
        expectedMap.put(SERVER_NAME2, FINGER_PRINT2);
        doReturn(client).when(glusterUtil).getSSHClient();
        doNothing().when(glusterUtil).connect(client, SERVER_NAME1, USER, PASSWORD);
        doNothing().when(glusterUtil).authenticate(client);
        doReturn(expectedMap).when(glusterUtil).getFingerprints(any());
        Map<String, String> peers = glusterUtil.getPeers(SERVER_NAME1, USER, PASSWORD, FINGER_PRINT1);
        assertNotNull(peers);
        peers.containsKey(SERVER_NAME1);
        assertEquals(FINGER_PRINT1, peers.get(SERVER_NAME1));
        peers.containsKey(SERVER_NAME2);
        assertEquals(FINGER_PRINT2, peers.get(SERVER_NAME2));
    }

    @Test
    public void testGetPeers() throws AuthenticationException, IOException {
        doReturn(client).when(glusterUtil).getSSHClient();
        doNothing().when(glusterUtil).connect(client, SERVER_NAME1, USER, PASSWORD);
        doNothing().when(glusterUtil).authenticate(client);
        Set<String> peers = glusterUtil.getPeers(SERVER_NAME1, USER, PASSWORD);
        assertNotNull(peers);
        assertTrue(peers.contains(SERVER_NAME1));
        assertTrue(peers.contains(SERVER_NAME2));
    }

    @Test
    public void testGetPeersWithWrongPassword() {
        doReturn(client).when(glusterUtil).getSSHClient();
        assertThrows(AuthenticationException.class, () -> glusterUtil.getPeers(SERVER_NAME1, USER, WRONG_PASSWORD));
    }

    @Test
    public void testHasPeersTrue() {
        assertNotNull(glusterUtil.getPeers(client));
        assertEquals(2, glusterUtil.getPeers(client).size());
    }

    @Test
    public void testHasPeersFalse() {
        doReturn(OUTPUT_XML_NO_PEERS).when(glusterUtil).executePeerStatusCommand(client);
        assertTrue(glusterUtil.getPeers(client).isEmpty());
    }
}
