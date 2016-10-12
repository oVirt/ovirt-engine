package org.ovirt.engine.core.vdsbroker;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class NetworkImplementationDetailsUtilsUsingVlanNicTest extends BaseNetworkImplementationDetailsUtilsTest {

    private VdsNetworkInterface baseIface;

    @Override
    @Before
    public void setUpBefore() throws Exception {
        super.setUpBefore();

        baseIface = createBaseInterface(null, null);
        VdsNetworkInterface vlanIface = createVlanInterface(baseIface, networkName, qosA);

        setTestIface(vlanIface);

        when(calculateBaseNic.getBaseNic(vlanIface)).thenReturn(baseIface);
        when(calculateBaseNic.getBaseNic(baseIface)).thenReturn(baseIface);
    }

    /**
     * Cover a case when MTU is unset & other network parameters out of sync, which is not covered by other tests.
     */
    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNicAndNetworkHasDifferentVlanId() throws Exception {
        Network network = createNetwork(getTestIface().isBridged(), getTestIface().getMtu(), getTestIface().getVlanId() + 1);
        calculateNetworkImplementationDetailsAndAssertSync(getTestIface(), false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNicAndNetworkHasNoVlanId() throws Exception {
        Network network = createNetwork(getTestIface().isBridged(), getTestIface().getMtu(), null);
        calculateNetworkImplementationDetailsAndAssertSync(getTestIface(), false, qosA, network);
    }
}
