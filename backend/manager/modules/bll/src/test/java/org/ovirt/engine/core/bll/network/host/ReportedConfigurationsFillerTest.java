package org.ovirt.engine.core.bll.network.host;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;

@RunWith(MockitoJUnitRunner.class)
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
    private VdsDao vdsDao;

    @Mock
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

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

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
        mockConfig(ConfigValues.DefaultMTU, 1500));

    @Before
    public void setUp() {
        hostId = Guid.newGuid();
        clusterId = Guid.newGuid();

        VDS vds = new VDS();
        vds.setId(hostId);
        vds.setClusterId(clusterId);

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

        when(vdsDao.get(hostId)).thenReturn(vds);
        when(hostNetworkQosDao.get(eq(baseNicNetwork.getQosId()))).thenReturn(baseNicNetworkQos);
        when(hostNetworkQosDao.get(eq(vlanNetwork.getQosId()))).thenReturn(vlanNetworkQos);

        cluster = new Cluster();
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
    public void testFillReportedConfigurationOnBaseNic() throws Exception {

        baseNic.setNetworkName(baseNicNetwork.getName());
        when(interfaceDao.getAllInterfacesForVds(eq(hostId))).thenReturn(Arrays.asList(baseNic, vlanNic));
        when(networkDao.getAllForCluster(eq(clusterId))).thenReturn(Collections.singletonList(baseNicNetwork));


        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNicId(baseNic.getId());
        networkAttachment.setNicName(baseNic.getName());
        networkAttachment.setNetworkId(baseNicNetwork.getId());

        when(effectiveHostNetworkQos.getQos(networkAttachment, baseNicNetwork)).thenReturn(baseNicNetworkQos);
        filler.fillReportedConfiguration(networkAttachment, hostId);

        verify(filler).createNetworkInSyncWithVdsNetworkInterface(networkAttachment, baseNic, baseNicNetwork, cluster);
    }

    @Test
    public void testFillReportedConfigurationOnVlanNic() throws Exception {

        vlanNic.setNetworkName(vlanNetwork.getName());
        when(interfaceDao.getAllInterfacesForVds(eq(hostId))).thenReturn(Arrays.asList(baseNic, vlanNic));
        when(networkDao.getAllForCluster(eq(clusterId))).thenReturn(Collections.singletonList(vlanNetwork));


        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNicId(baseNic.getId());
        networkAttachment.setNicName(baseNic.getName());
        networkAttachment.setNetworkId(vlanNetwork.getId());

        when(effectiveHostNetworkQos.getQos(networkAttachment, vlanNetwork)).thenReturn(vlanNetworkQos);
        filler.fillReportedConfiguration(networkAttachment, hostId);

        verify(filler).createNetworkInSyncWithVdsNetworkInterface(networkAttachment, vlanNic, vlanNetwork, cluster);
    }
}
