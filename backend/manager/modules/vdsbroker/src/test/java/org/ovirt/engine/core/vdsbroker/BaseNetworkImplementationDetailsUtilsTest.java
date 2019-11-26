package org.ovirt.engine.core.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.network.cluster.DefaultRouteUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith({MockConfigExtension.class, RandomUtilsSeedingExtension.class})
public abstract class BaseNetworkImplementationDetailsUtilsTest {
    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.DefaultMTU, 1500));
    }


    @Mock
    private HostNetworkQosDao hostNetworkQosDaoMock;

    @Mock
    private NetworkAttachmentDao networkAttachmentDaoMock;

    @Mock
    private ClusterDao clusterDaoMock;

    @Mock
    private InterfaceDao interfaceDaoMock;

    @Mock
    private NetworkDao networkDaoMock;

    @Mock
    private DnsResolverConfigurationDao dnsResolverConfigurationDaoMock;

    @Mock
    private VdsDynamicDao vdsDynamicDaoMock;

    @Mock
    private VdsStaticDao vdsStaticDaoMock;

    @Mock
    protected CalculateBaseNic calculateBaseNic;

    @Mock
    private DefaultRouteUtil defaultRouteUtil;

    private Guid VDS_ID = Guid.newGuid();
    private Guid CLUSTER_ID = Guid.newGuid();
    private Cluster cluster;


    private VdsNetworkInterface testIface;

    protected HostNetworkQos qosA;
    protected HostNetworkQos qosB;
    protected HostNetworkQos unlimitedHostNetworkQos;
    protected final String networkName = RandomUtils.instance().nextString(10);


    @BeforeEach
    public void setUpBefore() throws Exception {
        qosA = createAndMockQos(30, 30, 30);
        qosB = createAndMockQos(60, 60, 60);
        unlimitedHostNetworkQos = createQos(null, null, null);

        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setId(VDS_ID);
        vdsStatic.setClusterId(CLUSTER_ID);

        VdsDynamic vdsDynamic = new VdsDynamic();

        cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_2);
        cluster.setId(CLUSTER_ID);

        when(vdsStaticDaoMock.get(eq(VDS_ID))).thenReturn(vdsStatic);
        when(vdsDynamicDaoMock.get(eq(VDS_ID))).thenReturn(vdsDynamic);
        when(clusterDaoMock.get(eq(CLUSTER_ID))).thenReturn(cluster);

        EffectiveHostNetworkQos effectiveHostNetworkQos = new EffectiveHostNetworkQos(hostNetworkQosDaoMock);
        networkImplementationDetailsUtils = new NetworkImplementationDetailsUtils(effectiveHostNetworkQos,
                networkAttachmentDaoMock,
                vdsStaticDaoMock,
                clusterDaoMock,
                interfaceDaoMock,
                networkDaoMock,
                dnsResolverConfigurationDaoMock,
                calculateBaseNic,
                this.defaultRouteUtil);
    }

    @Test
    public void calculateNetworkImplementationDetailsUnmanagedNetwork() {
        calculateNetworkImplementationDetailsAndAssertManaged(testIface, false, null);
    }

    @Test
    public void calculateNetworkImplementationDetailsManagedNetwork() {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertManaged(testIface, true, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkIsSync() {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, true, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkDefaultMtuAndVmNetworkOutOfSync() {
        Network network = createNetwork(!testIface.isBridged(), 0, RandomUtils.instance().nextInt());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void caluculateNetworkImplementationDetailsNetworkInSyncWithoutQos() {
        testIface.setQos(null);
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, true, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkMtuOutOfSync() {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu() + 1, testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVmNetworkOutOfSync() {
        Network network = createNetwork(!testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsInterfaceQosMissing() {
        testIface.setQos(null);
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosMissing() {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOutOfSync() {
        HostNetworkQos qos = qosB;

        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());

        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qos, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsOverriddenQosOutOfSync() {
        HostNetworkQos qos = qosB;

        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        network.setQosId(qos.getId());
        when(hostNetworkQosDaoMock.get(qos.getId())).thenReturn(qos);

        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOverridden() {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, unlimitedHostNetworkQos, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNoNetworkName() {
        testIface.setNetworkName(null);

        assertNull(initMocksForNetworkImplementationDetailsUtils(null, null, testIface)
                .calculateNetworkImplementationDetails(testIface, null, clusterDaoMock.get(CLUSTER_ID)),
                "Network implementation details should not be filled.");
        assertEquals(1, Mockito.mockingDetails(clusterDaoMock).getInvocations().size());
    }

    @Test
    public void calculateNetworkImplementationDetailsEmptyNetworkName() {
        testIface.setNetworkName("");

        assertNull(initMocksForNetworkImplementationDetailsUtils(null, null, testIface)
                .calculateNetworkImplementationDetails(testIface, null, clusterDaoMock.get(CLUSTER_ID)),
                "Network implementation details should not be filled.");
        assertEquals(1, Mockito.mockingDetails(clusterDaoMock).getInvocations().size());
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkDefaultMtuOutOfSync() {
        Network network = createNetwork(testIface.isBridged(), 0, testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOverriddenBackToUnlimited() {
        testIface.setQos(unlimitedHostNetworkQos);
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        network.setQosId(qosA.getId());

        calculateNetworkImplementationDetailsAndAssertSync(testIface, true, unlimitedHostNetworkQos,
            network);
    }

    private void calculateNetworkImplementationDetailsAndAssertManaged(VdsNetworkInterface iface,
        boolean expectManaged,
        Network network) {

        VdsNetworkInterface.NetworkImplementationDetails networkImplementationDetails =
            initMocksForNetworkImplementationDetailsUtils(network, null, testIface)
                .calculateNetworkImplementationDetails(iface, network, clusterDaoMock.get(CLUSTER_ID));

        assertNotNull(networkImplementationDetails, "Network implementation details should be filled.");
        assertEquals(expectManaged, networkImplementationDetails.isManaged(),
                "Network implementation details should be " + (expectManaged ? "" : "un") + "managed.");
        assertEquals(1, Mockito.mockingDetails(clusterDaoMock).getInvocations().size());
    }

    protected void calculateNetworkImplementationDetailsAndAssertSync(VdsNetworkInterface iface,
        boolean expectSync,
        HostNetworkQos qos,
        Network network) {

        VdsNetworkInterface.NetworkImplementationDetails networkImplementationDetails =
            initMocksForNetworkImplementationDetailsUtils(network, qos, testIface)
                .calculateNetworkImplementationDetails(iface, network, clusterDaoMock.get(CLUSTER_ID));



        assertNotNull(networkImplementationDetails, "Network implementation details should be filled.");
        assertEquals(expectSync, networkImplementationDetails.isInSync(),
                "Network implementation details should be " + (expectSync ? "in" : "out of") + " sync.");
        assertEquals(1, Mockito.mockingDetails(clusterDaoMock).getInvocations().size());
    }

    protected Network createNetwork(boolean vmNetwork,
        int mtu,
        Integer vlanId) {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setVmNetwork(vmNetwork);
        network.setMtu(mtu);
        network.setVlanId(vlanId);

        return network;
    }

    private HostNetworkQos createAndMockQos(int outAverageLinkshare, int outAverageUpperlimit, int outAverageRealtime) {
        HostNetworkQos qos = createQos(outAverageLinkshare, outAverageUpperlimit, outAverageRealtime);
        when(hostNetworkQosDaoMock.get(qos.getId())).thenReturn(qos);
        return qos;
    }

    private HostNetworkQos createQos(Integer outAverageLinkshare,
        Integer outAverageUpperlimit,
        Integer outAverageRealtime) {

        HostNetworkQos qos = new HostNetworkQos();
        qos.setId(Guid.newGuid());
        qos.setOutAverageLinkshare(outAverageLinkshare);
        qos.setOutAverageUpperlimit(outAverageUpperlimit);
        qos.setOutAverageRealtime(outAverageRealtime);
        return qos;
    }

    private NetworkImplementationDetailsUtils initMocksForNetworkImplementationDetailsUtils(Network network,
        HostNetworkQos qos, VdsNetworkInterface testIface) {

        final VdsNetworkInterface baseIface = this.calculateBaseNic.getBaseNic(testIface);
        Guid baseIfaceId = baseIface.getId();
        Guid networkId = network == null ? null : network.getId();

        if (baseIfaceId != null && networkId != null) {
            when(networkAttachmentDaoMock.getNetworkAttachmentByNicIdAndNetworkId(eq(baseIfaceId), eq(networkId)))
                .thenReturn(createNetworkAttachment(qos, baseIface));
        }

        return this.networkImplementationDetailsUtils;
    }

    private NetworkAttachment createNetworkAttachment(HostNetworkQos qos, VdsNetworkInterface baseIface) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setNicId(baseIface.getId());
        networkAttachment.setNicName(baseIface.getName());
        networkAttachment.setHostNetworkQos(AnonymousHostNetworkQos.fromHostNetworkQos(qos));
        return networkAttachment;
    }

    protected VdsNetworkInterface createBaseInterface(HostNetworkQos qos, String networkName) {
        VdsNetworkInterface baseInterface = new VdsNetworkInterface();
        baseInterface.setId(Guid.newGuid());
        baseInterface.setNetworkName(networkName);
        baseInterface.setName("eth");
        baseInterface.setQos(qos);
        baseInterface.setVdsId(VDS_ID);
        return baseInterface;
    }

    protected VdsNetworkInterface createVlanInterface(VdsNetworkInterface baseIface, String networkName,
        HostNetworkQos qos) {

        VdsNetworkInterface vlanIface = new VdsNetworkInterface();
        vlanIface.setNetworkName(networkName);
        vlanIface.setBridged(RandomUtils.instance().nextBoolean());
        vlanIface.setMtu(100);
        vlanIface.setVlanId(100);
        vlanIface.setBaseInterface(baseIface.getName());
        vlanIface.setName(vlanIface.getBaseInterface() + "_" + vlanIface.getVlanId());
        vlanIface.setQos(qos);
        vlanIface.setVdsId(VDS_ID);
        return vlanIface;
    }

    public VdsNetworkInterface getTestIface() {
        return testIface;
    }

    public void setTestIface(VdsNetworkInterface testIface) {
        this.testIface = testIface;
        this.testIface.setReportedSwitchType(SwitchType.LEGACY);
    }
}
