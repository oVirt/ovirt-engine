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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlusterUtilTest {
    private static final String SERVER_NAME1 = "testserver1";
    private static final String SERVER_NAME2 = "testserver2";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    private static final String WRONG_PASSWORD = "wrong_password";
    private static final String SSH_PK_1 =
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDCyjmpkot5oNiMOElWAXUTwiItYDegil5efQHp4fTPuGsm3BBJbfVXMyCVXR8aVV+/B2keDvCUaClXq18cYzMFLbMschSBqmQfnveDdFg59hGxIOV4VzAAK7p2az/jnPWKqtNgZvxTe7PNsJ/2bCAIvlpCH5/GlXiuDjWJNBrOaO9RyeHz79KYEggq2LdDmMepioCdzo3xObVXO5DLRYFz2J7zRyqJbshLvtsq/fmdBSmQEjUqu5gEmoqyajgBpxpkCdLza/uP1bmVwmCmYGH14xybfY8ocmODx52LUY2BYjFNGTQJyU+QmpDB3PlU8HJJs/n6VlpL7agpCEqVEX+XXc3i1qp5Wte2EGP4/U3r73onkl2UkxW0oMm/Fgi9G7dhJTfDVTbsm6caTUpx+l2+nkrIY/DS4g/srFcCF2UEv7xgTw5BWgR2KASIE9yYcgM1Q1AMB9u5MAcB28T+dCr3zPF903y9CeNsAbm9edG/+gFIx/0A15EvX4ld4rS1qnE=";
    private static final String SSH_PK_2 =
            "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMGxcFCjUP16rF7Ovnxx0uBvO0jo0MHzaw3worb9pd1uIW6ZFhadQ/SKrzowTwIuWcmWH4uE0DTBh1//9GPUeHo=";
    private static final String OUTPUT_XML =
            "<cliOutput><peerStatus><peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver1</hostname><connected>1</connected><state>3</state></peer>"
                    +
                    "<peer><uuid>85c42b0d-c2b7-424a-ae72-5174c25da40b</uuid><hostname>testserver2</hostname><connected>1</connected><state>3</state></peer></peerStatus></cliOutput>";
    private static final String OUTPUT_XML_NO_PEERS = "<cliOutput><peerStatus/></cliOutput>";
    private static final String expectedIp = "10.70.42.91";
    private static final String expectedIp2 = "10.70.42.92";
    private static final String expectedIp3 = "10.70.42.93";
    private static final String expectedIpv6 = " 0:0:0:0:0:ffff:a46:2a5b";
    private static final String expectedIpv62 = "0:0:0:0:0:ffff:a46:2a5c";
    private static final String expectedIpv63 = "0:0:0:0:0:ffff:a46:2a5d";

    private static final String networkName = "gluster_net";

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
    public void testGetPeersWithPublicKey() throws AuthenticationException, IOException {
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put(SERVER_NAME1, SSH_PK_1);
        expectedMap.put(SERVER_NAME2, SSH_PK_2);
        doReturn(client).when(glusterUtil).getSSHClient();
        doNothing().when(glusterUtil).connect(client, SERVER_NAME1, USER, PASSWORD);
        doNothing().when(glusterUtil).authenticate(client);
        doReturn(expectedMap).when(glusterUtil).getPublicKeys(any());
        Map<String, String> peers = glusterUtil.getPeersWithSshPublicKeys(SERVER_NAME1, USER, PASSWORD);
        assertNotNull(peers);
        peers.containsKey(SERVER_NAME1);
        assertEquals(SSH_PK_1, peers.get(SERVER_NAME1));
        peers.containsKey(SERVER_NAME2);
        assertEquals(SSH_PK_2, peers.get(SERVER_NAME2));
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

    @Test
    public void testGetGlusterIpv4address() {

        Map<String, Network> map = new HashMap<>();

        VdsNetworkInterface vdsNetworkInterfaceOldHost = new VdsNetworkInterface();
        VdsNetworkInterface vdsNetworkInterfaceClusterHost = new VdsNetworkInterface();
        VdsNetworkInterface vdsNetworkInterfaceClusterHost2 = new VdsNetworkInterface();
        List<VdsNetworkInterface> vdsNetworkInterfaceOldHostList = new ArrayList<>();
        List<VdsNetworkInterface> vdsNetworkInterfaceClusterHostList= new ArrayList<>();
        List<VdsNetworkInterface> vdsNetworkInterfaceClusterHost2List = new ArrayList<>();

        VDS vds1 = new VDS();
        VDS vds2 = new VDS();
        VDS vds3 = new VDS();

        Map<VDS, String> expectedMap = new HashMap<>();

        List<VDS> hostlist = new ArrayList<>();
        vds1.setId(new Guid("00000000-0000-0000-0000-000000000000"));
        vds2.setId(new Guid("11111111-1111-1111-1111-111111111111"));
        vds3.setId(new Guid("22222222-2222-2222-2222-222222222222"));
        expectedMap.put(vds1, expectedIp);
        expectedMap.put(vds2, expectedIp2);
        expectedMap.put(vds3, expectedIp3);
        hostlist.add(vds2);
        hostlist.add(vds3);
        vdsNetworkInterfaceOldHost.setNetworkName(networkName);
        vdsNetworkInterfaceClusterHost.setNetworkName(networkName);
        vdsNetworkInterfaceClusterHost2.setNetworkName(networkName);
        vdsNetworkInterfaceOldHost.setIpv4Address(expectedIp);
        vdsNetworkInterfaceClusterHost.setIpv4Address(expectedIp2);
        vdsNetworkInterfaceClusterHost2.setIpv4Address(expectedIp3);
        vdsNetworkInterfaceOldHostList.add(vdsNetworkInterfaceOldHost);
        vdsNetworkInterfaceClusterHostList.add(vdsNetworkInterfaceClusterHost);
        vdsNetworkInterfaceClusterHost2List.add(vdsNetworkInterfaceClusterHost2);
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setManagement(false);
        Network network = new Network();
        network.setName("network_name");
        network.setCluster(networkCluster);
        network.getCluster().setGluster(true);
        map.put(networkName, network);

        doReturn(vdsNetworkInterfaceOldHostList).when(glusterUtil).getGlusterIpaddressUtil(vds1.getId());
        doReturn(vdsNetworkInterfaceClusterHostList).when(glusterUtil).getGlusterIpaddressUtil(vds2.getId());
        doReturn(vdsNetworkInterfaceClusterHost2List).when(glusterUtil).getGlusterIpaddressUtil(vds3.getId());
        Map<VDS, String> actualMap = glusterUtil.getGlusterIpaddressAsMap(map, vds1, hostlist);
        assertEquals(expectedMap, actualMap);
    }

    @Test
    public void testGetGlusterIpv6address() {

        Map<String, Network> map = new HashMap<>();


        VdsNetworkInterface vdsNetworkInterfaceOldHostIpv6 = new VdsNetworkInterface();
        VdsNetworkInterface vdsNetworkInterfaceClusterHostIpv6 = new VdsNetworkInterface();
        VdsNetworkInterface vdsNetworkInterfaceClusterHost2Ipv6 = new VdsNetworkInterface();
        List<VdsNetworkInterface> vdsNetworkInterfaceOldHostListIpv6 = new ArrayList<>();
        List<VdsNetworkInterface> vdsNetworkInterfaceClusterHostListIpv6= new ArrayList<>();
        List<VdsNetworkInterface> vdsNetworkInterfaceClusterHost2ListIpv6 = new ArrayList<>();

        VDS vds1 = new VDS();
        VDS vds2 = new VDS();
        VDS vds3 = new VDS();

        Map<VDS, String> expectedMap = new HashMap<>();

        List<VDS> hostlist = new ArrayList<>();
        vds1.setId(new Guid("00000000-0000-0000-0000-000000000000"));
        vds2.setId(new Guid("11111111-1111-1111-1111-111111111111"));
        vds3.setId(new Guid("22222222-2222-2222-2222-222222222222"));
        expectedMap.put(vds1, expectedIpv6);
        expectedMap.put(vds2, expectedIpv62);
        expectedMap.put(vds3, expectedIpv63);
        hostlist.add(vds2);
        hostlist.add(vds3);

        NetworkCluster networkCluster = new NetworkCluster();

        vdsNetworkInterfaceOldHostIpv6.setNetworkName(networkName);
        vdsNetworkInterfaceClusterHostIpv6.setNetworkName(networkName);
        vdsNetworkInterfaceClusterHost2Ipv6.setNetworkName(networkName);
        vdsNetworkInterfaceOldHostIpv6.setIpv6Address(expectedIpv6);
        vdsNetworkInterfaceClusterHostIpv6.setIpv6Address(expectedIpv62);
        vdsNetworkInterfaceClusterHost2Ipv6.setIpv6Address(expectedIpv63);
        vdsNetworkInterfaceOldHostListIpv6.add(vdsNetworkInterfaceOldHostIpv6);
        vdsNetworkInterfaceClusterHostListIpv6.add(vdsNetworkInterfaceClusterHostIpv6);
        vdsNetworkInterfaceClusterHost2ListIpv6.add(vdsNetworkInterfaceClusterHost2Ipv6);

        networkCluster.setManagement(false);
        Network network = new Network();
        network.setName("network_name");
        network.setCluster(networkCluster);
        network.getCluster().setGluster(true);
        map.put(networkName, network);

        doReturn(vdsNetworkInterfaceOldHostListIpv6).when(glusterUtil).getGlusterIpaddressUtil(vds1.getId());
        doReturn(vdsNetworkInterfaceClusterHostListIpv6).when(glusterUtil).getGlusterIpaddressUtil(vds2.getId());
        doReturn(vdsNetworkInterfaceClusterHost2ListIpv6).when(glusterUtil).getGlusterIpaddressUtil(vds3.getId());
        Map<VDS, String> actualMap = glusterUtil.getGlusterIpaddressAsMap(map, vds1, hostlist);
        assertEquals(expectedMap, actualMap);

    }
}
