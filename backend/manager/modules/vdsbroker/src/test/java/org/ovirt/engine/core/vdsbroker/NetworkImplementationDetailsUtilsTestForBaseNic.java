package org.ovirt.engine.core.vdsbroker;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class NetworkImplementationDetailsUtilsTestForBaseNic extends BaseNetworkImplementationDetailsUtilsTest {

    @Override
    @Before
    public void setUpBefore() throws Exception {
        super.setUpBefore();

        VdsNetworkInterface baseIface = createBaseInterface(qosA, networkName);

        testIface = baseIface;

        when(calculateBaseNic.getBaseNic(baseIface)).thenReturn(baseIface);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNicAndNicHasNoVlanId() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());

        testIface.setVlanId(null);
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }
}
