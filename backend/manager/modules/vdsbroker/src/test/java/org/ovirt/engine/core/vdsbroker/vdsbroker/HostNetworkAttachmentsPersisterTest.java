package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;

@RunWith(MockitoJUnitRunner.class)
public class HostNetworkAttachmentsPersisterTest {

    private static final String IP_ADDRESS = "192.168.1.10";
    private static final String NETMASK = "255.255.255.0";
    private static final String GATEWAY = "192.168.1.1";

    @Mock
    private NetworkAttachmentDao networkAttachmentDao;

    private Guid hostId = Guid.newGuid();
    private Network clusterNetworkA;
    private Network clusterNetworkB;
    private List<Network> clusterNetworks;
    private VdsNetworkInterface interfaceWithoutAttachedNetwork;
    private VdsNetworkInterface interfaceWithAttachedClusterNetworkA;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        clusterNetworkA = createNetworkWithName("clusterNetworkA");

        // cluster network, not attached to nic.
        clusterNetworkB = createNetworkWithName("clusterNetworkB");

        // clusterNetworks.
        clusterNetworks = new ArrayList<>(Arrays.asList(clusterNetworkA, clusterNetworkB));

        interfaceWithAttachedClusterNetworkA = createVdsNetworkInterfaceWithId("interfaceWithAttachedClusterNetworkA");
        interfaceWithAttachedClusterNetworkA.setNetworkName(clusterNetworkA.getName());

        interfaceWithAttachedClusterNetworkA.setCustomProperties(createCustomProperties());

        interfaceWithAttachedClusterNetworkA.setBootProtocol(NetworkBootProtocol.STATIC_IP);
        interfaceWithAttachedClusterNetworkA.setAddress(IP_ADDRESS);
        interfaceWithAttachedClusterNetworkA.setSubnet(NETMASK);
        interfaceWithAttachedClusterNetworkA.setGateway(GATEWAY);

        // host interface not attached to any network.
        interfaceWithoutAttachedNetwork = createVdsNetworkInterfaceWithId("interfaceWithoutAttachedNetwork");
    }

    private Map<String, String> createCustomProperties() {
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("a", "b");
        customProperties.put("c", "d");
        return customProperties;
    }

    private VdsNetworkInterface createVdsNetworkInterface(Guid id, String name) {
        VdsNetworkInterface vdsNetworkInterface = new VdsNetworkInterface();
        vdsNetworkInterface.setName(name);
        vdsNetworkInterface.setId(id);
        return vdsNetworkInterface;
    }


    private VdsNetworkInterface createVdsNetworkInterfaceWithId(String name) {
        return createVdsNetworkInterface(Guid.newGuid(), name);
    }

    private VdsNetworkInterface createVdsNetworkInterface(String name) {
        return createVdsNetworkInterface(null, name);
    }

    private NetworkAttachment createNetworkAttachment(Network network) {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setId(Guid.newGuid());
        networkAttachment.setNetworkId(network == null ? null : network.getId());
        return networkAttachment;
    }

    private Network createNetworkWithName(String networkName) {
        Network network = new Network();
        network.setName(networkName);
        network.setId(Guid.newGuid());
        return network;
    }

    private HostNetworkAttachmentsPersister createPersister(List<NetworkAttachment> userNetworkAttachments) {
        return createPersister(userNetworkAttachments,
            interfaceWithAttachedClusterNetworkA,
            interfaceWithoutAttachedNetwork);
    }

    private HostNetworkAttachmentsPersister createPersister(List<NetworkAttachment> userNetworkAttachments,
            VdsNetworkInterface ... hostInterfaces) {

        return new HostNetworkAttachmentsPersister(
                networkAttachmentDao,
                hostId,
                new ArrayList<>(Arrays.asList(hostInterfaces)),
                userNetworkAttachments,
                clusterNetworks);
    }

    @Test
    public void testPersistNetworkAttachmentsDeleteInvalidNetworkAttachments() throws Exception {

        // network attachments.
        NetworkAttachment networkAttachmentForClusterNetworkA = createNetworkAttachment(clusterNetworkA);
        networkAttachmentForClusterNetworkA.setNicId(interfaceWithAttachedClusterNetworkA.getId());
        NetworkAttachment networkAttachmentForClusterNetworkB = createNetworkAttachment(clusterNetworkB);
        NetworkAttachment networkAttachmentWithoutNetworkAssigned = createNetworkAttachment(null);

        when(networkAttachmentDao.getAllForHost(eq(hostId))).thenReturn(new ArrayList<>(Arrays.asList(
                networkAttachmentForClusterNetworkA,
                networkAttachmentForClusterNetworkB,
                networkAttachmentWithoutNetworkAssigned
        )));

        createPersister(Collections.<NetworkAttachment> emptyList()).persistNetworkAttachments();
        verify(networkAttachmentDao).getAllForHost(any(Guid.class));
        verify(networkAttachmentDao).remove(eq(networkAttachmentForClusterNetworkB.getId()));
        verify(networkAttachmentDao).remove(eq(networkAttachmentWithoutNetworkAssigned.getId()));

        // verify that nothing else gets removed.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsUpdateNotUpToDateExistingNetworkAttachments() throws Exception {

        // network attachments.
        final NetworkAttachment networkAttachmentForClusterNetworkA = createNetworkAttachment(clusterNetworkA);
        Guid notUpToDateNicId = Guid.newGuid();
        networkAttachmentForClusterNetworkA.setNicId(notUpToDateNicId);
        networkAttachmentForClusterNetworkA.setNicName("nonsense");

        when(networkAttachmentDao.getAllForHost(eq(hostId)))
            .thenReturn(Arrays.asList(networkAttachmentForClusterNetworkA));

        createPersister(Collections.<NetworkAttachment> emptyList()).persistNetworkAttachments();
        verify(networkAttachmentDao).getAllForHost(any(Guid.class));
        verify(networkAttachmentDao).update(argThat(new ArgumentMatcher<NetworkAttachment>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof NetworkAttachment)) {
                    return false;
                }

                NetworkAttachment networkAttachment = (NetworkAttachment) o;
                return networkAttachment.getId() != null
                    && networkAttachment.getId().equals(networkAttachmentForClusterNetworkA.getId())
                    && networkAttachment.getNicId() != null
                    && networkAttachment.getNicId().equals(interfaceWithAttachedClusterNetworkA.getId())
                    && networkAttachment.getNicName() != null
                    && networkAttachment.getNicName().equals(interfaceWithAttachedClusterNetworkA.getName());
            }
        }));

        // verify that nothing else gets removed.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenPersistingUserNetworkAttachmentWithoutNetworkDoNotPersist() throws Exception {
        when(networkAttachmentDao.getAllForHost(eq(hostId)))
                .thenReturn(Collections.<NetworkAttachment> emptyList());

        createPersister(Collections.singletonList(createNetworkAttachment(null)),
            new VdsNetworkInterface[] {}).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any(Guid.class));

        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenPersistingUserNetworkAttachmentWithNetworkNotAttachedToNicDoNotPersist() throws Exception {
        when(networkAttachmentDao.getAllForHost(eq(hostId)))
                .thenReturn(Collections.<NetworkAttachment> emptyList());

        // user attachments references network, which is not assigned to NIC.
        createPersister(Collections.singletonList(createNetworkAttachment(clusterNetworkB)),
            new VdsNetworkInterface[] {}).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any(Guid.class));

        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenCalledWithNewUserAttachments() throws Exception {
        when(networkAttachmentDao.getAllForHost(eq(hostId))).thenReturn(new ArrayList<NetworkAttachment>());

        Guid userNetworkAttachmentNicId = interfaceWithAttachedClusterNetworkA.getId();
        NetworkAttachment userNetworkAttachment = createNetworkAttachment(clusterNetworkA);
        userNetworkAttachment.setNicId(userNetworkAttachmentNicId);

        // when persisting new record user provided will be replaced.
        Guid userProvidedNetworkAttachmentId = userNetworkAttachment.getId();

        // user attachments references network, which is not assigned to NIC.
        createPersister(Arrays.asList(userNetworkAttachment)).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any(Guid.class));

        ArgumentCaptor<NetworkAttachment> networkAttachmentCaptor = ArgumentCaptor.forClass(NetworkAttachment.class);
        verify(networkAttachmentDao).save(networkAttachmentCaptor.capture());

        // nicId won't be updated to calculated value
        assertThat(networkAttachmentCaptor.getValue().getNicId(), is(userNetworkAttachmentNicId));
        // new id will be generated for persisted record
        assertThat(networkAttachmentCaptor.getValue().getId(), not(equalTo(userProvidedNetworkAttachmentId)));
        assertThat(networkAttachmentCaptor.getValue().getIpConfiguration(),
            is(userNetworkAttachment.getIpConfiguration()));
        assertThat(networkAttachmentCaptor.getValue().getNetworkId(), is(userNetworkAttachment.getNetworkId()));

        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenCalledWithAlreadyExistingAttachmentItWillUpdated() throws Exception {
        NetworkAttachment userNetworkAttachment = createNetworkAttachment(clusterNetworkA);
        userNetworkAttachment.setNicId(interfaceWithAttachedClusterNetworkA.getId());
        userNetworkAttachment.setProperties(createCustomProperties());
        userNetworkAttachment.setIpConfiguration(createIpConfiguration());

        when(networkAttachmentDao.getAllForHost(eq(hostId)))
                .thenReturn(new ArrayList<>(Arrays.asList(userNetworkAttachment)));


        // user attachments references network, which is not assigned to NIC.
        createPersister(Arrays.asList(userNetworkAttachment)).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any(Guid.class));

        ArgumentCaptor<NetworkAttachment> networkAttachmentCaptor = ArgumentCaptor.forClass(NetworkAttachment.class);

        verify(networkAttachmentDao).update(networkAttachmentCaptor.capture());
        // nicId will be updated to calculated value
        NetworkAttachment attachmentBeingPersisted = networkAttachmentCaptor.getValue();
        assertThat(attachmentBeingPersisted.getNicId(), is(interfaceWithAttachedClusterNetworkA.getId()));
        // new id will be generated for persisted record
        assertThat(attachmentBeingPersisted.getId(), equalTo(userNetworkAttachment.getId()));
        assertThat(attachmentBeingPersisted.getNetworkId(), is(userNetworkAttachment.getNetworkId()));


        Map<String, String> propertiesBeingPersisted = attachmentBeingPersisted.getProperties();
        Map<String, String> interfaceCustomProperties = interfaceWithAttachedClusterNetworkA.getCustomProperties();

        assertCustomProperties(propertiesBeingPersisted, createCustomProperties());

        assertIpConfiguration(attachmentBeingPersisted.getIpConfiguration(), createIpConfiguration());


        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    private IpConfiguration createIpConfiguration() {
        IPv4Address address = new IPv4Address();
        address.setAddress(IP_ADDRESS);
        address.setNetmask(NETMASK);
        address.setGateway(GATEWAY);
        address.setBootProtocol(NetworkBootProtocol.STATIC_IP);

        IpConfiguration result = new IpConfiguration();
        result.setIPv4Addresses(Collections.singletonList(address));

        return result;
    }

    @Test
    public void testPersistNetworkAttachmentsForInterfaceWithoutNetworkNothingIsPersisted() {
        when(networkAttachmentDao.getAllForHost(eq(hostId))).thenReturn(new ArrayList<NetworkAttachment>());
        HostNetworkAttachmentsPersister persister = new HostNetworkAttachmentsPersister(
                networkAttachmentDao,
                hostId,
                new ArrayList<>(Arrays.asList(interfaceWithoutAttachedNetwork)),
                Collections.<NetworkAttachment> emptyList(),
                clusterNetworks);
        persister.persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any(Guid.class));

        // verify that nothing else happens, namely, interfaceWithoutAttachedNetwork will not trigger persisting any data.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsForNotReportedNetworkAttachmentIsNotPersisted() {
        when(networkAttachmentDao.getAllForHost(eq(hostId))).thenReturn(new ArrayList<NetworkAttachment>());

        VdsNetworkInterface interfaceWithUnreportedNetwork = createVdsNetworkInterface("interfaceWithUnreportedNetwork");
        interfaceWithUnreportedNetwork.setNetworkName("unreportedNetwork");

        HostNetworkAttachmentsPersister persister = new HostNetworkAttachmentsPersister(
                networkAttachmentDao,
                hostId,
                new ArrayList<>(Arrays.asList(interfaceWithUnreportedNetwork)),
                Collections.<NetworkAttachment> emptyList(),
                clusterNetworks);
        persister.persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any(Guid.class));

        // verify that nothing else happens, namely, interfaceWithoutAttachedNetwork will not trigger persisting any data.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsCreateNetworkAttachmentWhichWasntYetCreatedForEachNetworkOnReportedNic() {
        when(networkAttachmentDao.getAllForHost(eq(hostId))).thenReturn(new ArrayList<NetworkAttachment>());

        createPersister(Collections.<NetworkAttachment>emptyList(), interfaceWithAttachedClusterNetworkA)
                .persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any(Guid.class));

        ArgumentCaptor<NetworkAttachment> networkAttachmentCaptor = ArgumentCaptor.forClass(NetworkAttachment.class);
        verify(networkAttachmentDao).save(networkAttachmentCaptor.capture());

        NetworkAttachment attachmentBeingPersisted = networkAttachmentCaptor.getValue();
        assertThat(attachmentBeingPersisted.getNetworkId(), is(clusterNetworkA.getId()));
        assertThat(attachmentBeingPersisted.getNicId(), is(interfaceWithAttachedClusterNetworkA.getId()));
        assertThat(attachmentBeingPersisted.getId(), notNullValue());

        Map<String, String> propertiesBeingPersisted = attachmentBeingPersisted.getProperties();
        Map<String, String> interfaceCustomProperties = interfaceWithAttachedClusterNetworkA.getCustomProperties();

        assertCustomProperties(propertiesBeingPersisted, interfaceCustomProperties);
        assertIpConfiguration(attachmentBeingPersisted.getIpConfiguration());

        // verify that nothing else happens, namely, interfaceWithoutAttachedNetwork will not trigger persisting any data.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testNotPersistingAttachmentWhichIsReportedOnDifferentNic() {
        VdsNetworkInterface nic = createVdsNetworkInterface(Guid.newGuid(), "nic");

        NetworkAttachment networkAttachment = createNetworkAttachment(clusterNetworkA);
        networkAttachment.setNicId(nic.getId());

        HostNetworkAttachmentsPersister persister = new HostNetworkAttachmentsPersister(networkAttachmentDao,
            hostId,
            Arrays.asList(interfaceWithAttachedClusterNetworkA, nic),
            Collections.singletonList(networkAttachment),
            clusterNetworks);
        when(networkAttachmentDao.getAllForHost(hostId)).thenReturn(Collections.<NetworkAttachment> emptyList());

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(HostNetworkAttachmentsPersister.INCONSISTENCY_NETWORK_IS_REPORTED_ON_DIFFERENT_NIC_THAN_WAS_SPECIFIED);

        persister.persistNetworkAttachments();
    }

    private void assertIpConfiguration(IpConfiguration ipConfiguration) {
        IPv4Address primaryAddress = ipConfiguration.getPrimaryAddress();

        assertThat(primaryAddress.getBootProtocol(), is(interfaceWithAttachedClusterNetworkA.getBootProtocol()));
        assertThat(primaryAddress.getAddress(), is(interfaceWithAttachedClusterNetworkA.getAddress()));
        assertThat(primaryAddress.getNetmask(), is(interfaceWithAttachedClusterNetworkA.getSubnet()));
        assertThat(primaryAddress.getGateway(), is(interfaceWithAttachedClusterNetworkA.getGateway()));
    }

    private void assertIpConfiguration(IpConfiguration persistedIpConfiguration, IpConfiguration ipConfiguration) {
        IPv4Address primaryAddress = persistedIpConfiguration.getPrimaryAddress();

        assertThat(primaryAddress.getBootProtocol(), is(ipConfiguration.getPrimaryAddress().getBootProtocol()));
        assertThat(primaryAddress.getAddress(), is(ipConfiguration.getPrimaryAddress().getAddress()));
        assertThat(primaryAddress.getNetmask(), is(ipConfiguration.getPrimaryAddress().getNetmask()));
        assertThat(primaryAddress.getGateway(), is(ipConfiguration.getPrimaryAddress().getGateway()));
    }

    private void assertCustomProperties(Map<String, String> propertiesBeingPersisted,
        Map<String, String> interfaceCustomProperties) {
        assertThat(propertiesBeingPersisted.size(), is(interfaceCustomProperties.size()));
        for (Map.Entry<String, String> entry : interfaceCustomProperties.entrySet()) {
            String key = entry.getKey();
            assertThat(propertiesBeingPersisted.containsKey(key), is(true));
            assertThat(propertiesBeingPersisted.get(key), is(interfaceCustomProperties.get(key)));
        }
    }
}
