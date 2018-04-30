package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class NetworkAttachmentValidatorTest extends BaseCommandTest {

    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String HOST_NAME = "hostName";
    private static final String NETWORK_NAME = "networkName";

    @Mock
    private NetworkDao networkDaoMock;

    @Mock
    private NetworkClusterDao networkClusterDaoMock;

    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private NetworkValidator networkValidatorMock;

    private final VDS host;

    public NetworkAttachmentValidatorTest() {
        host = new VDS();
        host.getStaticData().setName(HOST_NAME);
        host.setId(Guid.newGuid());
        host.setClusterId(CLUSTER_ID);
    }

    private NetworkAttachmentValidator createNetworkAttachmentValidator(NetworkAttachment attachment) {
        return new NetworkAttachmentValidator(attachment, host, networkClusterDaoMock, networkDaoMock, vdsDaoMock);
    }

    @Test
    public void testNetworkAttachmentIsSetWhenAttachmentIsNull() {
        EngineMessage engineMessage = EngineMessage.NULL_PASSED_AS_NETWORK_ATTACHMENT;
        assertThat(createNetworkAttachmentValidator(null).networkAttachmentIsSet(),
                failsWith(engineMessage, ReplacementUtils.getVariableAssignmentString(engineMessage, null)));
    }

    @Test
    public void testNetworkAttachmentIsSetWhenAttachmentIsNotNull() {
        assertThat(createNetworkAttachmentValidator(new NetworkAttachment()).networkAttachmentIsSet(),
                isValid());
    }

    @Test
    public void testNetworkExistWhenNeitherIdNorNameIsSpecified() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(networkAttachment);
        assertThat(validator.networkExists(),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_NETWORK_ID_OR_NAME_IS_NOT_SET));
    }

    @Test
    public void testNetworkExistWhenOnlyNetworkIdIsSet() {
        Guid networkId = Guid.newGuid();

        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkId(networkId);
        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(networkAttachment);
        EngineMessage engineMessage = EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS;
        assertThat(validator.networkExists(),
                failsWith(engineMessage,
                        ReplacementUtils.getVariableAssignmentString(engineMessage, networkId.toString())));
    }

    @Test
    public void testNetworkExistWhenOnlyNetworkNameIsSet() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkName(NETWORK_NAME);
        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(networkAttachment);
        EngineMessage engineMessage = EngineMessage.NETWORK_HAVING_NAME_NOT_EXISTS;
        assertThat(validator.networkExists(),
                failsWith(engineMessage,
                        ReplacementUtils.getVariableAssignmentString(engineMessage, NETWORK_NAME)));
    }

    @Test
    public void testNetworkExistWhenBothNetworkNameAndNetworkIdAreSet() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkName(NETWORK_NAME);
        networkAttachment.setNetworkId(Guid.newGuid());
        assertThat(createNetworkAttachmentValidator(networkAttachment).networkExists(), isValid());
    }

    @Test
    public void testNotExternalNetworkWhenExternalNetworkIsProvided() {
        Network externalNetwork = new Network();
        externalNetwork.setId(Guid.newGuid());
        externalNetwork.setProvidedBy(new ProviderNetwork(Guid.newGuid(), ""));

        when(networkDaoMock.get(eq(externalNetwork.getId()))).thenReturn(externalNetwork);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(externalNetwork.getId());
        assertThat(createNetworkAttachmentValidator(attachment).notExternalNetwork(),
            failsWith(EngineMessage.EXTERNAL_NETWORK_HAVING_NAME_CANNOT_BE_PROVISIONED));
    }

    @Test
    public void testNotExternalNetwork() {
        Network notExternalNetwork = new Network();
        notExternalNetwork.setId(Guid.newGuid());
        notExternalNetwork.setProvidedBy(null);

        when(networkDaoMock.get(eq(notExternalNetwork.getId()))).thenReturn(notExternalNetwork);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(notExternalNetwork.getId());
        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);
        assertThat(validator.notExternalNetwork(), isValid());
    }

    @Test
    public void testNotRemovingManagementNetwork() {
        NetworkAttachmentValidator networkAttachmentValidatorSpy = spy(
            createNetworkAttachmentValidator(new NetworkAttachment()));

        doReturn(networkValidatorMock).when(networkAttachmentValidatorSpy).getNetworkValidator();

        ValidationResult propagatedResult =
            new ValidationResult(EngineMessage.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK, "a");
        when(networkValidatorMock.notRemovingManagementNetwork()).thenReturn(propagatedResult);

        assertThat("ValidationResult is not propagated correctly",
                networkAttachmentValidatorSpy.notRemovingManagementNetwork(), is(propagatedResult));
    }

    @Test
    public void testNetworkAttachedToClusterWhenAttached() {
        Network network = new Network();
        network.setId(Guid.newGuid());

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(network.getId());

        NetworkClusterId networkClusterId = new NetworkClusterId(host.getClusterId(), attachment.getNetworkId());
        when(networkClusterDaoMock.get(eq(networkClusterId))).thenReturn(new NetworkCluster());

        assertThat(createNetworkAttachmentValidator(attachment).networkAttachedToCluster(), isValid());
    }

    @Test
    public void testNetworkAttachedToClusterWhenNotAttached() {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName(NETWORK_NAME);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(network.getId());
        attachment.setNetworkName(network.getName());

        EngineMessage engineMessage = EngineMessage.NETWORK_OF_GIVEN_NAME_NOT_EXISTS_IN_CLUSTER;
        assertThat(createNetworkAttachmentValidator(attachment).networkAttachedToCluster(),
            failsWith(engineMessage, ReplacementUtils.getVariableAssignmentString(engineMessage, network.getName())));
    }

    private NetworkAttachment createNetworkAttachmentWithIpv4Configuration(Ipv4BootProtocol bootProtocol) {

        IpConfiguration ipConfiguration = new IpConfiguration();
        IPv4Address primaryAddress = new IPv4Address();
        primaryAddress.setAddress(null);
        primaryAddress.setNetmask(null);
        primaryAddress.setBootProtocol(bootProtocol);
        ipConfiguration.getIPv4Addresses().add(primaryAddress);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setIpConfiguration(ipConfiguration);

        return attachment;
    }

    @Test
    public void testBootProtocolSetForRoleNetworkWhenNullValuedIpConfigurationAndWhenNetworkHasNoRole() {
        doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(false,
                createNetwork(), isValid());
    }

    @Test
    public void testBootProtocolSetForRoleNetworkWhenIpConfigurationNullAndNotRoleNetwork() {
        Network network = createNetwork();

        doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(
                true,
                network,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentValidator.VAR_ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY,
                                network.getName())));
    }

    private void doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(
            boolean displayNetwork, Network network, Matcher<ValidationResult> matcher) {

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setIpConfiguration(null);
        attachment.setNetworkId(network.getId());

        doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(
                displayNetwork,
                false,
                false,
                matcher,
                network,
                attachment);
    }

    private void doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(
            boolean displayNetwork,
            boolean migrationNetwork,
            boolean glusterNetwork,
            Matcher<ValidationResult> matcher,
            Network network,
            NetworkAttachment attachment) {

        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDisplay(displayNetwork);
        networkCluster.setMigration(migrationNetwork);
        networkCluster.setGluster(glusterNetwork);

        NetworkClusterId networkClusterId = new NetworkClusterId(host.getClusterId(), attachment.getNetworkId());
        when(networkClusterDaoMock.get(eq(networkClusterId))).thenReturn(networkCluster);
        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);

        assertThat(createNetworkAttachmentValidator(attachment).bootProtocolSetForRoleNetwork(), matcher);
    }

    @Test
    public void testBootProtocolSetForRoleNetworkWhenIpConfigurationIsNull() {
        Network network = createNetwork();

        NetworkAttachment attachment = createNetworkAttachmentWithIpv4Configuration(Ipv4BootProtocol.NONE);
        attachment.setNetworkId(network.getId());

        doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(
                true,
                true,
                false,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentValidator.VAR_ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY,
                                network.getName())),
                network,
                attachment);
    }

    private Network createNetwork() {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName(NETWORK_NAME);
        return network;
    }

    @Test
    public void testBootProtocolSetForRoleNetworkWhenBootProtocolIsNone() {
        Network network = createNetwork();

        NetworkAttachment attachment = createNetworkAttachmentWithIpv4Configuration(Ipv4BootProtocol.NONE);
        attachment.setNetworkId(network.getId());


        doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(false,
                true,
                true,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentValidator.VAR_ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY,
                                network.getName())),
                network,
                attachment);
    }

    @Test
    public void testBootProtocolSetForRoleNetworkWhenBootProtocolIsDhcp() {
        Network network = createNetwork();

        NetworkAttachment attachment = createNetworkAttachmentWithIpv4Configuration(Ipv4BootProtocol.DHCP);
        attachment.setNetworkId(network.getId());

        doTestBootProtocolSetForRoleNetworkWhenNullValuedIpConfiguration(true,
                false,
                false,
                isValid(),
                network,
                attachment);
    }

    @Test
    public void testNicExistsWhenNicNameIsNull() {
        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNicName(null);
        assertThat(createNetworkAttachmentValidator(attachment).nicNameIsSet(),
                failsWith(EngineMessage.HOST_NETWORK_INTERFACE_DOES_NOT_HAVE_NAME_SET));
    }

    @Test
    public void testNicExistsWhenNicNameIsNotNull() {
        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNicId(null);
        attachment.setNicName("whatever");

        assertThat(createNetworkAttachmentValidator(attachment).nicNameIsSet(), isValid());
    }

    @Test
    public void testNetworkNotChangedWhenOldAttachmentIsNull() {
        assertThat(createNetworkAttachmentValidator(null).networkNotChanged(null), isValid());
    }

    @Test
    public void testNetworkNotChangedWhenDifferentNetworkIds() {
        NetworkAttachment oldAttachment = new NetworkAttachment();
        oldAttachment.setId(Guid.newGuid());
        oldAttachment.setNetworkId(Guid.newGuid());

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(Guid.newGuid());

        assertThat(createNetworkAttachmentValidator(attachment).networkNotChanged(oldAttachment),
            failsWith(EngineMessage.CANNOT_CHANGE_ATTACHED_NETWORK,
                    ReplacementUtils.createSetVariableString(NetworkAttachmentValidator.VAR_NETWORK_ATTACHMENT_ID,
                            oldAttachment.getId())));
    }

    @Test
    public void testNetworkNotChanged() {
        Guid networkId = Guid.newGuid();

        NetworkAttachment oldAttachment = new NetworkAttachment();
        oldAttachment.setNetworkId(networkId);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(networkId);

        assertThat(createNetworkAttachmentValidator(attachment).networkNotChanged(oldAttachment), isValid());
    }

    @Test
    public void testNetworkNotAttachedToHost() {
        Network network = createNetwork();

        when(networkDaoMock.get(network.getId())).thenReturn(network);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(network.getId());
        assertThat(createNetworkAttachmentValidator(attachment).networkNotAttachedToHost(),
            isValid());
    }

    @Test
    public void testNetworkNotAttachedToHostWhenAttached() {
        Network network = createNetwork();

        when(networkDaoMock.get(network.getId())).thenReturn(network);
        when(vdsDaoMock.getAllForNetwork(eq(network.getId()))).thenReturn(Collections.singletonList(host));

        String networkName = NETWORK_NAME;
        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(network.getId());
        attachment.setNetworkName(networkName);
        assertThat(createNetworkAttachmentValidator(attachment).networkNotAttachedToHost(),
            failsWith(EngineMessage.NETWORK_ALREADY_ATTACHED_TO_HOST,
                    ReplacementUtils.createSetVariableString(NETWORK_NAME, networkName),
                    ReplacementUtils.createSetVariableString(HOST_NAME, host.getName())));
    }

    @Test
    public void testExistingAttachmentIsReusedNotReused() {
        Guid networkId = Guid.newGuid();

        NetworkAttachment oldAttachment = new NetworkAttachment();
        oldAttachment.setNetworkId(networkId);
        oldAttachment.setNetworkName(NETWORK_NAME);
        oldAttachment.setId(Guid.newGuid());

        Map<Guid, NetworkAttachment> existingAttachmentsByNetworkId = new HashMap<>();
        existingAttachmentsByNetworkId.put(networkId, oldAttachment);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(networkId);

        assertThat(
                createNetworkAttachmentValidator(attachment).existingAttachmentIsReused(existingAttachmentsByNetworkId),
                failsWith(EngineMessage.ATTACHMENT_IS_NOT_REUSED,
                        ReplacementUtils.createSetVariableString(NetworkAttachmentValidator.VAR_NETWORK_ATTACHMENT_ID,
                                oldAttachment.getId()),
                        ReplacementUtils.createSetVariableString(NetworkAttachmentValidator.VAR_NETWORK_NAME,
                                oldAttachment.getNetworkName())));
    }

    @Test
    public void testExistingAttachmentIsReusedNoExisting() {
        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(Guid.newGuid());

        assertThat(
                createNetworkAttachmentValidator(attachment).existingAttachmentIsReused(Collections.emptyMap()),
                isValid());
    }

    @Test
    public void testExistingAttachmentExistingHasSameId() {
        Guid networkId = Guid.newGuid();
        Guid attachmentId = Guid.newGuid();

        NetworkAttachment oldAttachment = new NetworkAttachment();
        oldAttachment.setNetworkId(networkId);
        oldAttachment.setNetworkName(NETWORK_NAME);
        oldAttachment.setId(attachmentId);

        Map<Guid, NetworkAttachment> existingAttachmentsByNetworkId = new HashMap<>();
        existingAttachmentsByNetworkId.put(networkId, oldAttachment);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(networkId);
        attachment.setId(attachmentId);

        assertThat(
                createNetworkAttachmentValidator(attachment).existingAttachmentIsReused(existingAttachmentsByNetworkId),
                isValid());
    }
}
