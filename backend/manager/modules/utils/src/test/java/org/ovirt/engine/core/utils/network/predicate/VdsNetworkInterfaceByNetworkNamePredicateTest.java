package org.ovirt.engine.core.utils.network.predicate;

import org.junit.Before;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class VdsNetworkInterfaceByNetworkNamePredicateTest extends AbstractVdsNetworkInterfacePredicateTest {

    @Before
    public void setup() {
        setUnderTest(new InterfaceByNetworkNamePredicate(getVALID()));
    }

    @Override
    public VdsNetworkInterface generateVdsNetworkInterface(String networkAddress) {
        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setNetworkName(networkAddress);
        return iface;
    }

}
