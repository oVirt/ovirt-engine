package org.ovirt.engine.core.vdsbroker;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NetworkImplementationDetailsUtilsUsingVlanNicTest extends BaseNetworkImplementationDetailsUtilsTest {

    private VdsNetworkInterface baseIface;

    @Override
    @BeforeEach
    public void setUpBefore() throws Exception {
        super.setUpBefore();

        baseIface = createBaseInterface(null, null);
        VdsNetworkInterface vlanIface = createVlanInterface(baseIface, networkName, qosA);

        setTestIface(vlanIface);
        when(calculateBaseNic.getBaseNic(vlanIface)).thenReturn(baseIface);
    }

    /**
     * Cover a case when MTU is unset & other network parameters out of sync, which is not covered by other tests.
     */
    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNicAndNetworkHasDifferentVlanId() {
        Network network = createNetwork(getTestIface().isBridged(), getTestIface().getMtu(), getTestIface().getVlanId() + 1);
        calculateNetworkImplementationDetailsAndAssertSync(getTestIface(), false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNicAndNetworkHasNoVlanId() {
        Network network = createNetwork(getTestIface().isBridged(), getTestIface().getMtu(), null);
        calculateNetworkImplementationDetailsAndAssertSync(getTestIface(), false, qosA, network);
    }
}
