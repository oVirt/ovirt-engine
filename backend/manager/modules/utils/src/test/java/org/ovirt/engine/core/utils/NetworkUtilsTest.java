package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;

@ExtendWith(RandomUtilsSeedingExtension.class)
public class NetworkUtilsTest {

    private static final String IFACE_NAME = "eth1";

    @Test
    public void interfaceBasedOn() {
        assertTrue(NetworkUtils.interfaceBasedOn(createVlan(IFACE_NAME), IFACE_NAME));
    }

    @Test
    public void interfaceBasedOnSameName() {
        assertTrue(NetworkUtils.interfaceBasedOn(createNic(IFACE_NAME), IFACE_NAME));
    }

    @Test
    public void interfaceBasedOnNotAVlanOfIface() {
        assertFalse(NetworkUtils.interfaceBasedOn(createVlan(IFACE_NAME + "1"), IFACE_NAME));
    }

    @Test
    public void interfaceBasedOnNotAVlanAtAll() {
        assertFalse(NetworkUtils.interfaceBasedOn(createNic(IFACE_NAME + "1"), IFACE_NAME));
    }

    @Test
    public void interfaceBasedOnNullIface() {
        assertFalse(NetworkUtils.interfaceBasedOn(createVlan(IFACE_NAME), null));
    }

    @Test
    public void interfaceBasedOnNullProposedVlan() {
        assertFalse(NetworkUtils.interfaceBasedOn(null, IFACE_NAME));
    }

    private VdsNetworkInterface createVlan(String baseIfaceName) {
        VdsNetworkInterface iface = new Vlan();
        iface.setVlanId(RandomUtils.instance().nextInt(100));
        iface.setBaseInterface(baseIfaceName);
        return iface;
    }

    private VdsNetworkInterface createNic(String ifaceName) {
        VdsNetworkInterface iface = new Nic();
        iface.setName(ifaceName);
        return iface;
    }

    @Test
    public void isRoleNetworkDisplay() {
        NetworkCluster networkCluster = createNetworkCluster(true, false, false);
        assertTrue(NetworkUtils.isRoleNetwork(networkCluster));
    }

    @Test
    public void isRoleNetworkMigration() {
        NetworkCluster networkCluster = createNetworkCluster(false, true, false);
        assertTrue(NetworkUtils.isRoleNetwork(networkCluster));
    }

    @Test
    public void isRoleNetworkGluster() {
        NetworkCluster networkCluster = createNetworkCluster(false, false, true);
        assertTrue(NetworkUtils.isRoleNetwork(networkCluster));
    }

    @Test
    public void isRoleNetworkAllRoles() {
        NetworkCluster networkCluster = createNetworkCluster(true, true, true);
        assertTrue(NetworkUtils.isRoleNetwork(networkCluster));
    }

    @Test
    public void isRoleNetworkNoRoles() {
        NetworkCluster networkCluster = createNetworkCluster(false, false, false);
        assertFalse(NetworkUtils.isRoleNetwork(networkCluster));
    }

    private NetworkCluster createNetworkCluster(boolean display, boolean migration, boolean gluster) {
        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDisplay(display);
        networkCluster.setMigration(migration);
        networkCluster.setGluster(gluster);
        return networkCluster;
    }

    @Test
    public void getIpAddressTest() {
        final String IP_ADDRESS = "192.0.2.1";
        final String URL = String.format("https://%s/someting", IP_ADDRESS);
        assertEquals(IP_ADDRESS, NetworkUtils.getIpAddress(URL));
    }

    @Test
    public void testStripIpv6ZoneId() {
        assertEquals("fe80::1",     NetworkUtils.stripIpv6ZoneIndex("fe80::1"));
        assertEquals("fe80::1/64",  NetworkUtils.stripIpv6ZoneIndex("fe80::1/64"));
        assertEquals("fe80::1",     NetworkUtils.stripIpv6ZoneIndex("fe80::1%"));
        assertEquals("fe80::1",     NetworkUtils.stripIpv6ZoneIndex("fe80::1%1"));
        assertEquals("fe80::1",     NetworkUtils.stripIpv6ZoneIndex("fe80::1%eth0"));
        assertEquals("fe80::1",     NetworkUtils.stripIpv6ZoneIndex("fe80::1%eth0/64"));
        assertEquals("",            NetworkUtils.stripIpv6ZoneIndex("%"));
        assertEquals("",            NetworkUtils.stripIpv6ZoneIndex(""));
        assertNull(NetworkUtils.stripIpv6ZoneIndex(null));
    }
}
