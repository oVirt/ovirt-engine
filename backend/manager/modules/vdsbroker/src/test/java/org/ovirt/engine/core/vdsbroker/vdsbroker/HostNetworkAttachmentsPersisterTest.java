package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.CustomPropertiesForVdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.utils.NetworkUtils;

@ExtendWith(MockitoExtension.class)
public class HostNetworkAttachmentsPersisterTest {

    private static final String IPV4_ADDRESS = "192.168.1.10";
    private static final String IPV4_NETMASK = "255.255.255.0";
    private static final String IPV4_GATEWAY = "192.168.1.1";

    private static final String IPV6_ADDRESS = "ipv6 address";
    private static final Integer IPV6_PREFIX = 666;
    private static final String IPV6_GATEWAY = "ipv6 gateway";

    @Mock
    private NetworkAttachmentDao networkAttachmentDao;

    @Captor
    private ArgumentCaptor<NetworkAttachment> networkAttachmentCaptor;

    private Guid hostId = Guid.newGuid();
    private Network clusterNetworkA;
    private Network clusterNetworkB;
    private List<Network> clusterNetworks;
    private VdsNetworkInterface interfaceWithoutAttachedNetwork;
    private VdsNetworkInterface interfaceWithAttachedClusterNetworkA;

    private CustomPropertiesForVdsNetworkInterface customPropertiesForNics = new CustomPropertiesForVdsNetworkInterface();

    @BeforeEach
    public void setUp() {

        clusterNetworkA = createNetworkWithName("clusterNetworkA");

        // cluster network, not attached to nic.
        clusterNetworkB = createNetworkWithName("clusterNetworkB");

        // clusterNetworks.
        clusterNetworks = new ArrayList<>(Arrays.asList(clusterNetworkA, clusterNetworkB));

        interfaceWithAttachedClusterNetworkA = createVdsNetworkInterfaceWithId("interfaceWithAttachedClusterNetworkA");
        interfaceWithAttachedClusterNetworkA.setNetworkName(clusterNetworkA.getName());

        customPropertiesForNics.add(interfaceWithAttachedClusterNetworkA, createCustomProperties());

        interfaceWithAttachedClusterNetworkA.setIpv4BootProtocol(Ipv4BootProtocol.STATIC_IP);
        interfaceWithAttachedClusterNetworkA.setIpv4Address(IPV4_ADDRESS);
        interfaceWithAttachedClusterNetworkA.setIpv4Subnet(IPV4_NETMASK);
        interfaceWithAttachedClusterNetworkA.setIpv4Gateway(IPV4_GATEWAY);

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

    private VdsNetworkInterface createVdsNetworkInterface() {
        return createVdsNetworkInterface(null, "interfaceWithUnreportedNetwork");
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
            VdsNetworkInterface... hostInterfaces) {
        return createPersister(userNetworkAttachments, Collections.emptySet(), hostInterfaces);
    }

    private HostNetworkAttachmentsPersister createPersister(List<NetworkAttachment> userNetworkAttachments,
            Set<Guid> removedNetworkAttachments,
            VdsNetworkInterface... hostInterfaces) {
        return new HostNetworkAttachmentsPersister(
                networkAttachmentDao,
                hostId,
                new ArrayList<>(Arrays.asList(hostInterfaces)),
                userNetworkAttachments,
                removedNetworkAttachments,
                clusterNetworks);
    }

    @Test
    public void testPersistNetworkAttachmentsDeleteInvalidNetworkAttachments() {

        // network attachments.
        NetworkAttachment networkAttachmentForClusterNetworkA = createNetworkAttachment(clusterNetworkA);
        networkAttachmentForClusterNetworkA.setNicId(interfaceWithAttachedClusterNetworkA.getId());
        networkAttachmentForClusterNetworkA.setProperties(
                customPropertiesForNics.getCustomPropertiesFor(interfaceWithAttachedClusterNetworkA));
        networkAttachmentForClusterNetworkA.setIpConfiguration(
                NetworkUtils.createIpConfigurationFromVdsNetworkInterface(interfaceWithAttachedClusterNetworkA));

        NetworkAttachment networkAttachmentForClusterNetworkB = createNetworkAttachment(clusterNetworkB);
        NetworkAttachment networkAttachmentWithoutNetworkAssigned = createNetworkAttachment(null);

        when(networkAttachmentDao.getAllForHost(eq(hostId))).thenReturn(new ArrayList<>(Arrays.asList(
                networkAttachmentForClusterNetworkA,
                networkAttachmentForClusterNetworkB,
                networkAttachmentWithoutNetworkAssigned
        )));

        createPersister(Collections.emptyList()).persistNetworkAttachments();
        verify(networkAttachmentDao).getAllForHost(any());
        verify(networkAttachmentDao).remove(eq(networkAttachmentForClusterNetworkB.getId()));
        verify(networkAttachmentDao).remove(eq(networkAttachmentWithoutNetworkAssigned.getId()));

        // verify that nothing else gets removed.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenNetworkMovedToDifferentNic() {

        NetworkAttachment networkAttachmentForClusterNetworkA = createNetworkAttachment(clusterNetworkA);

        //make network attachment out of sync, by setting different nicId and nicName
        Guid notUpToDateNicId = Guid.newGuid();
        networkAttachmentForClusterNetworkA.setNicId(notUpToDateNicId);
        networkAttachmentForClusterNetworkA.setNicName("nonsense");

        IpConfiguration ipConfiguration =
                NetworkUtils.createIpConfigurationFromVdsNetworkInterface(interfaceWithAttachedClusterNetworkA);
        networkAttachmentForClusterNetworkA.setIpConfiguration(ipConfiguration);
        networkAttachmentForClusterNetworkA.setProperties(customPropertiesForNics
                .getCustomPropertiesFor(interfaceWithAttachedClusterNetworkA));

        callPersistNetworkAttachmentsAndVerifyThatNetworkAttachmentIsSynced(networkAttachmentForClusterNetworkA,
                createPersister(Collections.emptyList()));
    }

    @Test
    public void testPersistNetworkAttachmentsWhenNothingToUpdate() {
        NetworkAttachment upToDateNetworkAttachment = createNetworkAttachment(clusterNetworkA);
        upToDateNetworkAttachment.setNicId(interfaceWithAttachedClusterNetworkA.getId());

        IpConfiguration ipConfiguration =
                NetworkUtils.createIpConfigurationFromVdsNetworkInterface(interfaceWithAttachedClusterNetworkA);
        upToDateNetworkAttachment.setIpConfiguration(ipConfiguration);
        upToDateNetworkAttachment.setProperties(customPropertiesForNics
                .getCustomPropertiesFor(interfaceWithAttachedClusterNetworkA));

        when(networkAttachmentDao.getAllForHost(eq(hostId)))
                .thenReturn(Collections.singletonList(upToDateNetworkAttachment));

        createPersister(Collections.emptyList()).persistNetworkAttachments();
        verify(networkAttachmentDao).getAllForHost(any());
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    private void callPersistNetworkAttachmentsAndVerifyThatNetworkAttachmentIsSynced(NetworkAttachment attachment,
            HostNetworkAttachmentsPersister persister) {
        when(networkAttachmentDao.getAllForHost(eq(hostId))).thenReturn(Collections.singletonList( attachment));

        persister.persistNetworkAttachments();
        verify(networkAttachmentDao).getAllForHost(any());
        verify(networkAttachmentDao).update(argThat(networkAttachment -> {
            IpConfiguration ipConfiguration =
                    NetworkUtils.createIpConfigurationFromVdsNetworkInterface(interfaceWithAttachedClusterNetworkA);

            return networkAttachment.getId() != null
                && networkAttachment.getId().equals(attachment.getId())
                && networkAttachment.getNicId() != null
                && networkAttachment.getNicId().equals(interfaceWithAttachedClusterNetworkA.getId())
                && Objects.equals(networkAttachment.getIpConfiguration(), ipConfiguration)
                && Objects.equals(networkAttachment.getProperties(),
                    customPropertiesForNics.getCustomPropertiesFor(interfaceWithAttachedClusterNetworkA));
        }));

        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenPersistingUserNetworkAttachmentWithoutNetworkDoNotPersist() {
        createPersister(Collections.singletonList(createNetworkAttachment(null)),
            new VdsNetworkInterface[] {}).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenPersistingUserNetworkAttachmentWithNetworkNotAttachedToNicDoNotPersist() {
        // user attachments references network, which is not assigned to NIC.
        createPersister(Collections.singletonList(createNetworkAttachment(clusterNetworkB)),
            new VdsNetworkInterface[] {}).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsWhenCalledWithNewUserAttachments() {
        Guid userNetworkAttachmentNicId = interfaceWithAttachedClusterNetworkA.getId();
        NetworkAttachment userNetworkAttachment = createNetworkAttachment(clusterNetworkA);
        userNetworkAttachment.setNicId(userNetworkAttachmentNicId);

        userNetworkAttachment.setProperties(customPropertiesForNics.getCustomPropertiesFor(interfaceWithAttachedClusterNetworkA));
        userNetworkAttachment.setIpConfiguration(NetworkUtils.createIpConfigurationFromVdsNetworkInterface(interfaceWithAttachedClusterNetworkA));

        // when persisting new record user provided will be replaced.
        Guid userProvidedNetworkAttachmentId = userNetworkAttachment.getId();

        // user attachments references network, which is not assigned to NIC.
        createPersister(Collections.singletonList(userNetworkAttachment)).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

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
    public void testPersistNetworkAttachmentsWhenCalledWithAlreadyExistingAttachmentItWillUpdated() {
        NetworkAttachment userNetworkAttachment = createNetworkAttachment(clusterNetworkA);
        userNetworkAttachment.setNicId(interfaceWithAttachedClusterNetworkA.getId());
        userNetworkAttachment.setProperties(createCustomProperties());
        userNetworkAttachment.setIpConfiguration(createIpConfiguration());

        when(networkAttachmentDao.getAllForHost(eq(hostId)))
                .thenReturn(new ArrayList<>(Collections.singletonList(userNetworkAttachment)));


        // user attachments references network, which is not assigned to NIC.
        createPersister(Collections.singletonList(userNetworkAttachment)).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

        ArgumentCaptor<NetworkAttachment> networkAttachmentCaptor = ArgumentCaptor.forClass(NetworkAttachment.class);

        verify(networkAttachmentDao).update(networkAttachmentCaptor.capture());
        // nicId will be updated to calculated value
        NetworkAttachment attachmentBeingPersisted = networkAttachmentCaptor.getValue();
        assertThat(attachmentBeingPersisted.getNicId(), is(interfaceWithAttachedClusterNetworkA.getId()));
        // new id will be generated for persisted record
        assertThat(attachmentBeingPersisted.getId(), equalTo(userNetworkAttachment.getId()));
        assertThat(attachmentBeingPersisted.getNetworkId(), is(userNetworkAttachment.getNetworkId()));


        Map<String, String> propertiesBeingPersisted = attachmentBeingPersisted.getProperties();

        assertCustomProperties(propertiesBeingPersisted, createCustomProperties());

        assertIpConfigurationsEqual(attachmentBeingPersisted.getIpConfiguration(), createIpConfiguration());

        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    private IpConfiguration createIpConfiguration() {
        IpConfiguration result = new IpConfiguration();

        IPv4Address ipv4Address = createIpv4Address();
        IpV6Address ipv6Address = createIpv6Address();

        result.setIPv4Addresses(Collections.singletonList(ipv4Address));
        result.setIpV6Addresses(Collections.singletonList(ipv6Address));

        return result;
    }

    private IPv4Address createIpv4Address() {
        IPv4Address address = new IPv4Address();
        address.setAddress(IPV4_ADDRESS);
        address.setNetmask(IPV4_NETMASK);
        address.setGateway(IPV4_GATEWAY);
        address.setBootProtocol(Ipv4BootProtocol.STATIC_IP);
        return address;
    }

    private IpV6Address createIpv6Address() {
        IpV6Address address = new IpV6Address();
        address.setAddress(IPV6_ADDRESS);
        address.setPrefix(IPV6_PREFIX);
        address.setGateway(IPV6_GATEWAY);
        address.setBootProtocol(Ipv6BootProtocol.AUTOCONF);
        return address;
    }

    @Test
    public void testPersistNetworkAttachmentsForInterfaceWithoutNetworkNothingIsPersisted() {
        HostNetworkAttachmentsPersister persister = createPersister(
            Collections.emptyList(),
            interfaceWithoutAttachedNetwork);

        persister.persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

        // verify that nothing else happens, namely, interfaceWithoutAttachedNetwork will not trigger persisting any data.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsForNotReportedNetworkAttachmentIsNotPersisted() {
        VdsNetworkInterface interfaceWithUnreportedNetwork = createVdsNetworkInterface();
        interfaceWithUnreportedNetwork.setNetworkName("unreportedNetwork");

        HostNetworkAttachmentsPersister persister = createPersister(
            Collections.emptyList(),
            interfaceWithUnreportedNetwork);

        persister.persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

        // verify that nothing else happens, namely, interfaceWithoutAttachedNetwork will not trigger persisting any data.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testPersistNetworkAttachmentsCreateNetworkAttachmentWhichWasntYetCreatedForEachNetworkOnReportedNic() {
        createPersister(Collections.emptyList(), interfaceWithAttachedClusterNetworkA).persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

        verify(networkAttachmentDao).save(networkAttachmentCaptor.capture());

        NetworkAttachment attachmentBeingPersisted = networkAttachmentCaptor.getValue();
        assertThat(attachmentBeingPersisted.getNetworkId(), is(clusterNetworkA.getId()));
        assertThat(attachmentBeingPersisted.getNicId(), is(interfaceWithAttachedClusterNetworkA.getId()));
        assertThat(attachmentBeingPersisted.getId(), notNullValue());

        assertNicIpConfiguration(attachmentBeingPersisted.getIpConfiguration(), interfaceWithAttachedClusterNetworkA);

        // verify that nothing else happens, namely, interfaceWithoutAttachedNetwork will not trigger persisting any data.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    @Test
    public void testNotPersistingAttachmentWhichIsReportedOnDifferentNic() {
        VdsNetworkInterface nic = createVdsNetworkInterface(Guid.newGuid(), "nic");

        NetworkAttachment networkAttachment = createNetworkAttachment(clusterNetworkA);
        networkAttachment.setNicId(nic.getId());

        HostNetworkAttachmentsPersister persister = createPersister(
            Collections.singletonList(networkAttachment),
            interfaceWithAttachedClusterNetworkA, nic);

        IllegalStateException e = assertThrows(IllegalStateException.class, persister::persistNetworkAttachments);
        assertEquals(HostNetworkAttachmentsPersister.INCONSISTENCY_NETWORK_IS_REPORTED_ON_DIFFERENT_NIC_THAN_WAS_SPECIFIED, e.getMessage());
    }

    @Test
    public void testPersistNetworkAttachmentsRemovedAndNewAttachmentReferToTheSameNetwork() {
        NetworkAttachment removedNetworkAttachment = createNetworkAttachment(clusterNetworkA);
        removedNetworkAttachment.setNicId(interfaceWithAttachedClusterNetworkA.getId());
        Guid removedAttachmentId = removedNetworkAttachment.getId();

        when(networkAttachmentDao.getAllForHost(eq(hostId)))
                .thenReturn(new ArrayList<>(Collections.singletonList(removedNetworkAttachment)));

        NetworkAttachment userNetworkAttachment = new NetworkAttachment(removedNetworkAttachment);
        userNetworkAttachment.setId(Guid.newGuid());

        // user attachments references network, which is not assigned to NIC.
        createPersister(Collections.singletonList(userNetworkAttachment),
                Collections.singleton(removedAttachmentId),
                interfaceWithAttachedClusterNetworkA)
                        .persistNetworkAttachments();

        verify(networkAttachmentDao).getAllForHost(any());

        verify(networkAttachmentDao).remove(eq(removedAttachmentId));

        ArgumentCaptor<NetworkAttachment> networkAttachmentCaptor = ArgumentCaptor.forClass(NetworkAttachment.class);

        verify(networkAttachmentDao).save(networkAttachmentCaptor.capture());
        // nicId will be updated to calculated value
        NetworkAttachment attachmentBeingPersisted = networkAttachmentCaptor.getValue();
        assertThat(attachmentBeingPersisted.getNicId(), is(interfaceWithAttachedClusterNetworkA.getId()));
        // new id will be generated for persisted record
        assertThat(attachmentBeingPersisted.getId(), equalTo(userNetworkAttachment.getId()));
        assertThat(attachmentBeingPersisted.getNetworkId(), is(userNetworkAttachment.getNetworkId()));

        // verify that nothing else happens, no removals, no creations.
        verifyNoMoreInteractions(networkAttachmentDao);
    }

    private void assertNicIpConfiguration(IpConfiguration ipConfiguration, VdsNetworkInterface nic) {
        assertNicIpv4Configuration(ipConfiguration.getIpv4PrimaryAddress(), nic);
        assertNicIpv6Configuration(ipConfiguration.getIpv6PrimaryAddress(), nic);
    }

    private void assertNicIpv4Configuration(IPv4Address ipv4PrimaryAddress, VdsNetworkInterface nic) {
        assertThat(ipv4PrimaryAddress.getBootProtocol(), is(nic.getIpv4BootProtocol()));
        assertThat(ipv4PrimaryAddress.getAddress(), is(nic.getIpv4Address()));
        assertThat(ipv4PrimaryAddress.getNetmask(), is(nic.getIpv4Subnet()));
        assertThat(ipv4PrimaryAddress.getGateway(), is(nic.getIpv4Gateway()));
    }

    private void assertNicIpv6Configuration(IpV6Address ipv6PrimaryAddress, VdsNetworkInterface nic) {
        assertThat(ipv6PrimaryAddress.getBootProtocol(), is(nic.getIpv6BootProtocol()));
        assertThat(ipv6PrimaryAddress.getAddress(), is(nic.getIpv6Address()));
        assertThat(ipv6PrimaryAddress.getPrefix(), is(nic.getIpv6Prefix()));
        assertThat(ipv6PrimaryAddress.getGateway(), is(nic.getIpv6Gateway()));
    }

    private void assertIpConfigurationsEqual(IpConfiguration ipConfiguration1, IpConfiguration ipConfiguration2) {
        assertIpv4AddressesEqual(ipConfiguration1.getIpv4PrimaryAddress(), ipConfiguration2.getIpv4PrimaryAddress());
        assertIpv6AddressesEqual(ipConfiguration1.getIpv6PrimaryAddress(), ipConfiguration2.getIpv6PrimaryAddress());
    }

    private void assertIpv4AddressesEqual(IPv4Address ipv4Address1, IPv4Address ipv4Address2) {
        assertThat(ipv4Address1.getBootProtocol(), is(ipv4Address2.getBootProtocol()));
        assertThat(ipv4Address1.getAddress(), is(ipv4Address2.getAddress()));
        assertThat(ipv4Address1.getNetmask(), is(ipv4Address2.getNetmask()));
        assertThat(ipv4Address1.getGateway(), is(ipv4Address2.getGateway()));
    }

    private void assertIpv6AddressesEqual(IpV6Address ipv6Address1, IpV6Address ipv6Address2) {
        assertThat(ipv6Address1.getBootProtocol(), is(ipv6Address2.getBootProtocol()));
        assertThat(ipv6Address1.getAddress(), is(ipv6Address2.getAddress()));
        assertThat(ipv6Address1.getPrefix(), is(ipv6Address2.getPrefix()));
        assertThat(ipv6Address1.getGateway(), is(ipv6Address2.getGateway()));
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
