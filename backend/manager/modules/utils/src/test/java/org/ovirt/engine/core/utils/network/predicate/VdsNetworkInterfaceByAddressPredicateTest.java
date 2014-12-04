package org.ovirt.engine.core.utils.network.predicate;

import org.junit.Before;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class VdsNetworkInterfaceByAddressPredicateTest extends AbstractVdsNetworkInterfacePredicateTest {

    @Before
    public void setup() {
        setUnderTest(new InterfaceByAddressPredicate(getVALID()));
    }

    @Override
    public VdsNetworkInterface generateVdsNetworkInterface(String address) {
        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setAddress(address);
        return iface;
    }

}
