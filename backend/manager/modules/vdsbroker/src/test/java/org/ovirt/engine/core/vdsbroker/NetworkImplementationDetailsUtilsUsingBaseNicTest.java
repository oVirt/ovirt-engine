package org.ovirt.engine.core.vdsbroker;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class NetworkImplementationDetailsUtilsUsingBaseNicTest extends BaseNetworkImplementationDetailsUtilsTest {

    @Override
    @Before
    public void setUpBefore() throws Exception {
        super.setUpBefore();

        VdsNetworkInterface baseIface = createBaseInterface(qosA, networkName);
        baseIface.setMtu(100);

        setTestIface(baseIface);

        when(calculateBaseNic.getBaseNic(baseIface)).thenReturn(baseIface);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNetworkAndNicHasNoVlanId() throws Exception {
        Network network = createNetwork(getTestIface().isBridged(), getTestIface().getMtu(), getTestIface().getVlanId());

        calculateNetworkImplementationDetailsAndAssertSync(getTestIface(), true, qosA, network);
    }
}
