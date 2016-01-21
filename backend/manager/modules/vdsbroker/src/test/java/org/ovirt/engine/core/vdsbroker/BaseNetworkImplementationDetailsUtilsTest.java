package org.ovirt.engine.core.vdsbroker;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
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
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

public abstract class BaseNetworkImplementationDetailsUtilsTest {
    private static final int DEFAULT_MTU = 1500;

    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;


    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Rule
    public MockConfigRule mcr = new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.DefaultMTU, DEFAULT_MTU));


    @Mock
    private HostNetworkQosDao hostNetworkQosDaoMock;

    @Mock
    private NetworkAttachmentDao networkAttachmentDaoMock;

    @Mock
    private ClusterDao clusterDaoMock;

    @Mock
    private VdsDao vdsDaoMock;

    @Mock CalculateBaseNic calculateBaseNic;

    private Guid VDS_ID = Guid.newGuid();
    private Guid CLUSTER_ID = Guid.newGuid();
    private VDS vds;
    private Cluster cluster;


    protected VdsNetworkInterface testIface;

    protected HostNetworkQos qosA;
    protected HostNetworkQos qosB;
    protected HostNetworkQos unlimitedHostNetworkQos;
    protected final String networkName = RandomUtils.instance().nextString(10);


    @Before
    public void setUpBefore() throws Exception {
        qosA = createAndMockQos(30, 30, 30);
        qosB = createAndMockQos(60, 60, 60);
        unlimitedHostNetworkQos = createQos(null, null, null);

        vds = new VDS();
        vds.setId(VDS_ID);
        vds.setClusterId(CLUSTER_ID);

        cluster = new Cluster();
        cluster.setId(CLUSTER_ID);

        when(vdsDaoMock.get(eq(VDS_ID))).thenReturn(vds);
        when(clusterDaoMock.get(eq(CLUSTER_ID))).thenReturn(cluster);

        EffectiveHostNetworkQos effectiveHostNetworkQos = new EffectiveHostNetworkQos(hostNetworkQosDaoMock);
        networkImplementationDetailsUtils = new NetworkImplementationDetailsUtils(effectiveHostNetworkQos,
                        networkAttachmentDaoMock,
                        vdsDaoMock,
                        clusterDaoMock,
                        calculateBaseNic);

    }

    @Test
    public void calculateNetworkImplementationDetailsUnmanagedNetwork() throws Exception {
        calculateNetworkImplementationDetailsAndAssertManaged(testIface, false, null);
    }

    @Test
    public void calculateNetworkImplementationDetailsManagedNetwork() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertManaged(testIface, true, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkIsSync() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, true, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkDefaultMtuAndVmNetworkOutOfSync() throws Exception {
        Network network = createNetwork(!testIface.isBridged(), 0, RandomUtils.instance().nextInt());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void caluculateNetworkImplementationDetailsNetworkInSyncWithoutQos() throws Exception {
        testIface.setQos(null);
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, true, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkMtuOutOfSync() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu() + 1, testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVmNetworkOutOfSync() throws Exception {
        Network network = createNetwork(!testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsInterfaceQosMissing() throws Exception {
        testIface.setQos(null);
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosMissing() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOutOfSync() throws Exception {
        HostNetworkQos qos = qosB;
        when(hostNetworkQosDaoMock.get(qos.getId())).thenReturn(qos);


        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());

        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qos, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsOverriddenQosOutOfSync() throws Exception {
        HostNetworkQos qos = qosB;

        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        network.setQosId(qos.getId());
        when(hostNetworkQosDaoMock.get(qos.getId())).thenReturn(qos);

        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOverridden() throws Exception {
        Network network = createNetwork(testIface.isBridged(), testIface.getMtu(), testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, unlimitedHostNetworkQos, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNoNetworkName() throws Exception {
        testIface.setNetworkName(null);

        assertNull("Network implementation details should not be filled.",
            initMocksForNetworkImplementationDetailsUtils(null, null, testIface).calculateNetworkImplementationDetails(
                testIface, null));
    }

    @Test
    public void calculateNetworkImplementationDetailsEmptyNetworkName() throws Exception {
        testIface.setNetworkName("");

        assertNull("Network implementation details should not be filled.",
            initMocksForNetworkImplementationDetailsUtils(null, null, testIface).calculateNetworkImplementationDetails(
                testIface,
                null));
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkDefaultMtuOutOfSync() throws Exception {
        Network network = createNetwork(testIface.isBridged(), 0, testIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(testIface, false, qosA, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOverriddenBackToUnlimited() throws Exception {
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
                .calculateNetworkImplementationDetails(iface, network);

        Assert.assertNotNull("Network implementation details should be filled.", networkImplementationDetails);
        Assert.assertEquals("Network implementation details should be " + (expectManaged ? "" : "un") + "managed.",
            expectManaged,
            networkImplementationDetails.isManaged());
    }

    protected void calculateNetworkImplementationDetailsAndAssertSync(VdsNetworkInterface iface,
        boolean expectSync,
        HostNetworkQos qos,
        Network network) {

        VdsNetworkInterface.NetworkImplementationDetails networkImplementationDetails =
            initMocksForNetworkImplementationDetailsUtils(network, qos, testIface)
                .calculateNetworkImplementationDetails(iface, network);

        Assert.assertNotNull("Network implementation details should be filled.", networkImplementationDetails);
        Assert.assertEquals("Network implementation details should be " + (expectSync ? "in" : "out of") + " sync.",
            expectSync,
            networkImplementationDetails.isInSync());
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
}
