package org.ovirt.engine.core.vdsbroker;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class NetworkImplementationDetailsUtilsTestForVlanNic extends BaseNetworkImplementationDetailsUtilsTest {

    private VdsNetworkInterface baseIface;

    @Override
    @Before
    public void setUpBefore() throws Exception {
        super.setUpBefore();

        baseIface = createBaseInterface(null, null);
        VdsNetworkInterface vlanIface = createVlanInterface(baseIface, networkName, qosA);

        testIface = vlanIface;

        when(calculateBaseNic.getBaseNic(vlanIface)).thenReturn(baseIface);
        when(calculateBaseNic.getBaseNic(baseIface)).thenReturn(baseIface);
    }

    /**
     * Cover a case when MTU is unset & other network parameters out of sync, which is not covered by other tests.
     */
    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNicAndNetworkHasDifferentVlanId() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId() + 1);
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNicAndNetworkHasNoVlanId() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), null);
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }
}
