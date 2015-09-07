package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;

public class NetworkUtilsTest {

    private static final String IFACE_NAME = "eth1";

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

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
        VdsNetworkInterface iface = new Vlan(RandomUtils.instance().nextInt(100), baseIfaceName);
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
}
