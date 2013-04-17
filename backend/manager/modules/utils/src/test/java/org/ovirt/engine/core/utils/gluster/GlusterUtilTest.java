package org.ovirt.engine.core.utils.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.utils.ssh.SSHClient;

@RunWith(MockitoJUnitRunner.class)
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
    private SSHClient client;

    @Spy
    private GlusterUtil glusterUtil;

    @Before
    public void setUp() throws Exception {
        setupMock();
    }

    private void setupMock() throws AuthenticationException {
        doReturn(client).when(glusterUtil).connect(SERVER_NAME1);
        doReturn(FINGER_PRINT1).when(glusterUtil).getFingerprint(client);
        doReturn(FINGER_PRINT1).when(glusterUtil).getFingerprint(SERVER_NAME1);
        doReturn(FINGER_PRINT2).when(glusterUtil).getFingerprint(SERVER_NAME2);
        doReturn(OUTPUT_XML).when(glusterUtil).executePeerStatusCommand(client);
        doNothing().when(glusterUtil).authenticate(client, USER, PASSWORD);
        doThrow(AuthenticationException.class).when(glusterUtil).authenticate(client, USER, WRONG_PASSWORD);
    }

    @Test
    public void testGetPeersWithFingerprint() throws AuthenticationException {
        Map<String, String> peers = glusterUtil.getPeers(SERVER_NAME1, PASSWORD, FINGER_PRINT1);
        assertNotNull(peers);
        peers.containsKey(SERVER_NAME1);
        assertEquals(FINGER_PRINT1, peers.get(SERVER_NAME1));
        peers.containsKey(SERVER_NAME2);
        assertEquals(FINGER_PRINT2, peers.get(SERVER_NAME2));
    }

    @Test
    public void testGetPeers() throws AuthenticationException {
        Set<String> peers = glusterUtil.getPeers(SERVER_NAME1, PASSWORD);
        assertNotNull(peers);
        assertTrue(peers.contains(SERVER_NAME1));
        assertTrue(peers.contains(SERVER_NAME2));
    }

    @Test(expected = AuthenticationException.class)
    public void testGetPeersWithWrongPassword() throws AuthenticationException {
        glusterUtil.getPeers(SERVER_NAME1, WRONG_PASSWORD);
    }

    @Test
    public void testHasPeersTrue() {
        assertTrue(glusterUtil.hasPeers(client));
    }

    @Test
    public void testHasPeersFalse() {
        doReturn(OUTPUT_XML_NO_PEERS).when(glusterUtil).executePeerStatusCommand(client);
        assertFalse(glusterUtil.hasPeers(client));
    }
}
