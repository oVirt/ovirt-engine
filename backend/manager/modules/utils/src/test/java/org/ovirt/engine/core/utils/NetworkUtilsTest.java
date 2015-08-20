package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
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

}
