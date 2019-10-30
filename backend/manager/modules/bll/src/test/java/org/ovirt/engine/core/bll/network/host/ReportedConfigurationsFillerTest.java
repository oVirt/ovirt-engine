package org.ovirt.engine.core.bll.network.host;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.network.cluster.DefaultRouteUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
public class ReportedConfigurationsFillerTest {

    @Mock
    private HostNetworkQosDao hostNetworkQosDao;

    @Mock
    private InterfaceDao interfaceDao;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private VdsStaticDao vdsStaticDao;

    @Mock
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    @Mock
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    @Mock
    private DefaultRouteUtil defaultRouteUtil;

    @Spy
    @InjectMocks
    private ReportedConfigurationsFiller filler;


    private VdsNetworkInterface baseNic;
    private VdsNetworkInterface vlanNic;
    private Guid hostId;
    private Network vlanNetwork;
    private Network baseNicNetwork;
    private Guid clusterId;
    private HostNetworkQos baseNicNetworkQos;
    private HostNetworkQos vlanNetworkQos;
    private Cluster cluster;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.DefaultMTU, 1500));
    }
    private DnsResolverConfiguration reportedDnsResolverConfiguration;

    @BeforeEach
    public void setUp() {
        hostId = Guid.newGuid();
        clusterId = Guid.newGuid();

        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setId(hostId);
        vdsStatic.setClusterId(clusterId);

        reportedDnsResolverConfiguration = new DnsResolverConfiguration();

        baseNic = createNic("eth0");

        vlanNic = createNic("eth0.1");
        vlanNic.setBaseInterface(baseNic.getName());

        baseNicNetwork = createNetwork("baseNicNetwork");

        vlanNetwork = createNetwork("vlanNicNetwork");
        vlanNetwork.setVlanId(1);

        baseNicNetworkQos = new HostNetworkQos();
        baseNicNetworkQos.setId(baseNicNetwork.getQosId());

        vlanNetworkQos = new HostNetworkQos();
        vlanNetworkQos.setId(vlanNetwork.getQosId());

        when(vdsStaticDao.get(hostId)).thenReturn(vdsStatic);
        when(dnsResolverConfigurationDao.get(hostId)).thenReturn(reportedDnsResolverConfiguration);

        cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_2);
        when(clusterDao.get(any())).thenReturn(cluster);
    }

    private VdsNetworkInterface createNic(String name) {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setId(Guid.newGuid());
        nic.setName(name);
        return nic;
    }

    private Network createNetwork(String name) {
        Network network = new Network();
        network.setVmNetwork(false);
        network.setId(Guid.newGuid());
        network.setName(name);
        network.setQosId(Guid.newGuid());
        return network;
    }

    @Test
    public void testFillReportedConfigurationOnBaseNic() {
        baseNic.setNetworkName(baseNicNetwork.getName());
        testFillReportedConfiguration(this.baseNicNetwork, baseNic, baseNicNetworkQos);
    }

    @Test
    public void testFillReportedConfigurationOnVlanNic() {
        vlanNic.setNetworkName(vlanNetwork.getName());
        testFillReportedConfiguration(this.vlanNetwork, vlanNic, vlanNetworkQos);
    }

    /**
     * @param network network which will contain qos and will be attached to nic.
     * @param nic nic to which network should be attached
     * @param networkQos qos of network
     */
    private void testFillReportedConfiguration(Network network, VdsNetworkInterface nic, HostNetworkQos networkQos) {
        nic.setNetworkName(network.getName());
        when(interfaceDao.getAllInterfacesForVds(eq(hostId))).thenReturn(Arrays.asList(baseNic, vlanNic));
        when(networkDao.getAllForCluster(eq(clusterId))).thenReturn(Collections.singletonList(network));


        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNicId(baseNic.getId());
        networkAttachment.setNicName(baseNic.getName());
        networkAttachment.setNetworkId(network.getId());

        when(effectiveHostNetworkQos.getQos(networkAttachment, network)).thenReturn(networkQos);
        filler.fillReportedConfiguration(networkAttachment, hostId);

        verify(filler).createNetworkInSyncWithVdsNetworkInterface(networkAttachment,
                nic,
                network,
                reportedDnsResolverConfiguration,
                cluster);
    }
}
