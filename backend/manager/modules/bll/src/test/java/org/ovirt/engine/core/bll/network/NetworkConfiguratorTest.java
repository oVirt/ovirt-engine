package org.ovirt.engine.core.bll.network;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.function.Function;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

@RunWith(MockitoJUnitRunner.class)
public class NetworkConfiguratorTest {

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    private ManagementNetworkUtil managementNetworkUtil;

    @Test
    public void getIpv4AddressOfNetworkReturnsNullWhenThereIsNoNetworkOfGivenName() {
        String networkName = "networkName";
        testResolveHostNetworkAddress(e -> e.getIpv4AddressOfNetwork(networkName), networkName);
    }

    @Test
    public void getIpv6AddressOfNetwork() {
        String networkName = "networkName";
        testResolveHostNetworkAddress(e -> e.getIpv6AddressOfNetwork(networkName), networkName);
    }

    private void testResolveHostNetworkAddress(Function<NetworkConfigurator, String> function, String networkName) {
        injectorRule.bind(ManagementNetworkUtil.class, managementNetworkUtil);

        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setNetworkName(networkName);

        VDS vds = new VDS();
        vds.getInterfaces().add(nic);

        assertThat(function.apply(new NetworkConfigurator(vds, null)), nullValue());
    }

}
