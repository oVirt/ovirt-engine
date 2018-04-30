package org.ovirt.engine.core.utils.network.predicate;

import org.junit.jupiter.api.BeforeEach;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class InterfaceByIpv4AddressPredicateTest extends AbstractVdsNetworkInterfacePredicateTest {

    @BeforeEach
    public void setup() {
        setUnderTest(new InterfaceByAddressPredicate(VALID));
    }

    @Override
    protected void setIfaceProperty(VdsNetworkInterface iface, String value) {
        iface.setIpv4Address(value);
    }
}
