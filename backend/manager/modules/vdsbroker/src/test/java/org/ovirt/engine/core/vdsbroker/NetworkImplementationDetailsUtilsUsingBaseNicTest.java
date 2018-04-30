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
public class NetworkImplementationDetailsUtilsUsingBaseNicTest extends BaseNetworkImplementationDetailsUtilsTest {

    @Override
    @BeforeEach
    public void setUpBefore() throws Exception {
        super.setUpBefore();
        VdsNetworkInterface baseIface = createBaseInterface(qosA, networkName);
        baseIface.setMtu(100);

        setTestIface(baseIface);

        when(calculateBaseNic.getBaseNic(baseIface)).thenReturn(baseIface);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSyncNetworkAndNicHasNoVlanId() {
        Network network = createNetwork(getTestIface().isBridged(), getTestIface().getMtu(), getTestIface().getVlanId());

        calculateNetworkImplementationDetailsAndAssertSync(getTestIface(), true, qosA, network);
    }
}
