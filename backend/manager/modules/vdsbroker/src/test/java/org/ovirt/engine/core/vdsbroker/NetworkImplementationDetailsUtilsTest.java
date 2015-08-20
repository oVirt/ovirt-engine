package org.ovirt.engine.core.vdsbroker;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

//TODO MM: You should run all the sync test also on non-vlan nic.
@RunWith(MockitoJUnitRunner.class)
public class NetworkImplementationDetailsUtilsTest {
    private static final int DEFAULT_MTU = 1500;

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Rule
    public MockConfigRule mcr = new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.DefaultMTU, DEFAULT_MTU));


    @Mock
    private HostNetworkQosDao hostNetworkQosDaoMock;

    @Mock
    private NetworkAttachmentDao networkAttachmentDaoMock;

    @Mock CalculateBaseNic calculateBaseNic;

    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

    private VdsNetworkInterface vlanIface;
    private VdsNetworkInterface baseIface;

    @Before
    public void setUp() throws Exception {
        vlanIface = createVlanInterface(createAndMockQos());

        baseIface = new VdsNetworkInterface();
        baseIface.setId(Guid.newGuid());
        baseIface.setName("eth");

        when(calculateBaseNic.getBaseNic(vlanIface)).thenReturn(baseIface);

        EffectiveHostNetworkQos effectiveHostNetworkQos = new EffectiveHostNetworkQos(hostNetworkQosDaoMock);
        networkImplementationDetailsUtils =
            new NetworkImplementationDetailsUtils(effectiveHostNetworkQos, networkAttachmentDaoMock, calculateBaseNic);
    }

    private VdsNetworkInterface createVlanInterface(HostNetworkQos qos) {
        VdsNetworkInterface vlanIface = new VdsNetworkInterface();
        vlanIface.setNetworkName(RandomUtils.instance().nextString(10));
        vlanIface.setBridged(RandomUtils.instance().nextBoolean());
        vlanIface.setMtu(100);
        vlanIface.setVlanId(100);
        vlanIface.setBaseInterface("eth");
        vlanIface.setName("eth_100");
        vlanIface.setQos(qos);
        return vlanIface;
    }


    @Test
    public void calculateNetworkImplementationDetailsUnmanagedNetwork() throws Exception {
        calculateNetworkImplementationDetailsAndAssertManaged(vlanIface, false, null);
    }

    @Test
    public void calculateNetworkImplementationDetailsManagedNetwork() throws Exception {
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertManaged(vlanIface, true, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkIsSync() throws Exception {
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, true, createAndMockQos(), network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkDefaultMtuOutOfSync() throws Exception {
        Network network = createNetwork(vlanIface.isBridged(), 0, vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, createAndMockQos(), network);
    }

    /**
     * Cover a case when MTU is unset & other network parameters out of sync, which is not covered by other tests.
     */
    @Test
    public void calculateNetworkImplementationDetailsNetworkDefaultMtuAndVmNetworkOutOfSync() throws Exception {
        Network network = createNetwork(!vlanIface.isBridged(), 0, RandomUtils.instance().nextInt());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, createAndMockQos(), network);
    }

    @Test
    public void caluculateNetworkImplementationDetailsNetworkInSyncWithoutQos() throws Exception {
        vlanIface.setQos(null);
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, true, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkMtuOutOfSync() throws Exception {
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu() + 1, vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, createAndMockQos(), network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVmNetworkOutOfSync() throws Exception {
        Network network = createNetwork(!vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, createAndMockQos(), network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkVlanOutOfSync() throws Exception {
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId() + 1);
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, createAndMockQos(), network);
    }

    @Test
    public void calculateNetworkImplementationDetailsInterfaceQosMissing() throws Exception {
        vlanIface.setQos(null);
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, createAndMockQos(), network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosMissing() throws Exception {
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOutOfSync() throws Exception {
        HostNetworkQos qos = createAndMockQos(60, 60, 60);
        when(hostNetworkQosDaoMock.get(qos.getId())).thenReturn(qos);


        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());

        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, qos, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsOverriddenQosOutOfSync() throws Exception {
        HostNetworkQos qos = createAndMockQos(60, 60, 60);

        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        network.setQosId(qos.getId());
        when(hostNetworkQosDaoMock.get(qos.getId())).thenReturn(qos);

        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, null, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOverridden() throws Exception {
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        HostNetworkQos hostNetworkQos = new HostNetworkQos();
        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, false, hostNetworkQos, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNetworkQosOverriddenBackToUnlimited() throws Exception {
        HostNetworkQos unlimitedHostNetworkQos = new HostNetworkQos();
        unlimitedHostNetworkQos.setId(Guid.newGuid());

        VdsNetworkInterface vlanIface = createVlanInterface(unlimitedHostNetworkQos);
        Network network = createNetwork(vlanIface.isBridged(), vlanIface.getMtu(), vlanIface.getVlanId());
        network.setQosId(createAndMockQos().getId());

        when(calculateBaseNic.getBaseNic(vlanIface)).thenReturn(baseIface);


        calculateNetworkImplementationDetailsAndAssertSync(vlanIface, true, unlimitedHostNetworkQos, network);
    }

    @Test
    public void calculateNetworkImplementationDetailsNoNetworkName() throws Exception {
        vlanIface.setNetworkName(null);

        assertNull("Network implementation details should not be filled.",
            initMocksForNetworkImplementationDetailsUtils(null, null).calculateNetworkImplementationDetails(
                vlanIface, null));
    }

    @Test
    public void calculateNetworkImplementationDetailsEmptyNetworkName() throws Exception {
        vlanIface.setNetworkName("");

        assertNull("Network implementation details should not be filled.",
            initMocksForNetworkImplementationDetailsUtils(null, null).calculateNetworkImplementationDetails(vlanIface,
                null));
    }

    private void calculateNetworkImplementationDetailsAndAssertManaged(VdsNetworkInterface iface,
        boolean expectManaged,
        Network network) {

        NetworkImplementationDetails networkImplementationDetails =
            initMocksForNetworkImplementationDetailsUtils(network, null)
                .calculateNetworkImplementationDetails(iface, network);

        Assert.assertNotNull("Network implementation details should be filled.", networkImplementationDetails);
        Assert.assertEquals("Network implementation details should be " + (expectManaged ? "" : "un") + "managed.",
            expectManaged,
            networkImplementationDetails.isManaged());
    }

    private void calculateNetworkImplementationDetailsAndAssertSync(VdsNetworkInterface iface,
        boolean expectSync,
        HostNetworkQos qos, Network network) {

        NetworkImplementationDetails networkImplementationDetails =
            initMocksForNetworkImplementationDetailsUtils(network, qos)
                .calculateNetworkImplementationDetails(iface, network);

        Assert.assertNotNull("Network implementation details should be filled.", networkImplementationDetails);
        Assert.assertEquals("Network implementation details should be " + (expectSync ? "in" : "out of") + " sync.",
            expectSync,
            networkImplementationDetails.isInSync());
    }

    private Network createNetwork(boolean vmNetwork,
        int mtu,
        Integer vlanId) {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setVmNetwork(vmNetwork);
        network.setMtu(mtu);
        network.setVlanId(vlanId);

        return network;
    }

    private HostNetworkQos createAndMockQos() {
        return createAndMockQos(30, 30, 30);
    }

    private HostNetworkQos createAndMockQos(int outAverageLinkshare, int outAverageUpperlimit, int outAverageRealtime) {
        HostNetworkQos qos = new HostNetworkQos();
        qos.setId(Guid.newGuid());
        qos.setOutAverageLinkshare(outAverageLinkshare);
        qos.setOutAverageUpperlimit(outAverageUpperlimit);
        qos.setOutAverageRealtime(outAverageRealtime);

        when(hostNetworkQosDaoMock.get(qos.getId())).thenReturn(qos);
        return qos;
    }

    private NetworkImplementationDetailsUtils initMocksForNetworkImplementationDetailsUtils(Network network,
        HostNetworkQos qos) {

        Guid baseIfaceId = baseIface.getId();
        Guid networkId = network == null ? null : network.getId();

        if (baseIfaceId != null && networkId != null) {
            when(networkAttachmentDaoMock.getNetworkAttachmentByNicIdAndNetworkId(eq(baseIfaceId), eq(networkId)))
                .thenReturn(createNetworkAttachment(qos, baseIfaceId));
        }

        return this.networkImplementationDetailsUtils;
    }

    private NetworkAttachment createNetworkAttachment(HostNetworkQos qos, Guid baseIfaceId) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setNicId(baseIfaceId);
        networkAttachment.setNicName(baseIface.getName());
        networkAttachment.setHostNetworkQos(qos);
        return networkAttachment;
    }
}
