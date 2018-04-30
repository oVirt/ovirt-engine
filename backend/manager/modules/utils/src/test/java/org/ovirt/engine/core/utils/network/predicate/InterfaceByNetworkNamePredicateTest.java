package org.ovirt.engine.core.utils.network.predicate;

import org.junit.jupiter.api.BeforeEach;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class InterfaceByNetworkNamePredicateTest extends AbstractVdsNetworkInterfacePredicateTest {

    @BeforeEach
    public void setup() {
        setUnderTest(new InterfaceByNetworkNamePredicate(VALID));
    }

    @Override
    protected void setIfaceProperty(VdsNetworkInterface iface, String value) {
        iface.setNetworkName(value);
    }

}
