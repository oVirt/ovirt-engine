package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.DbDependentTestBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class NetworkAttachmentValidatorTest extends DbDependentTestBase {

    @Mock
    private NetworkDao networkDaoMock;

    @Mock
    private NetworkClusterDao networkClusterDaoMock;

    @Mock
    private VdsDAO vdsDaoMock;

    @Mock
    private NetworkValidator networkValidatorMock;

    @Mock
    private NetworkAttachmentDao networkAttachmentDaoMock;

    @Mock
    VmInterfaceManager vmInterfaceManager;

    @Mock
    private ManagementNetworkUtil managementNetworkUtilMock;


    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
        mockConfig(ConfigValues.MultipleGatewaysSupported, Version.v3_5.toString(), false),
        mockConfig(ConfigValues.MultipleGatewaysSupported, Version.v3_6.toString(), true));


    private final VDS host;

    public NetworkAttachmentValidatorTest() {
        host = new VDS();
        host.setId(Guid.newGuid());
        host.setVdsGroupId(Guid.newGuid());
    }

    private NetworkAttachmentValidator createNetworkAttachmentValidator(NetworkAttachment attachment) {
        return new NetworkAttachmentValidator(attachment,
            host,
            managementNetworkUtilMock,
            networkAttachmentDaoMock,
            vmInterfaceManager,
            networkClusterDaoMock,
            networkDaoMock,
            vdsDaoMock);
    }

    @Test
    public void testNetworkAttachmentIsSetWhenAttachmentIsNull() throws Exception {
        assertThat(createNetworkAttachmentValidator(null).networkAttachmentIsSet(),
                failsWith(VdcBllMessages.NETWORK_ATTACHMENT_NOT_EXISTS));
    }

    @Test
    public void testNetworkAttachmentIsSetWhenAttachmentIsNotNull() throws Exception {
        assertThat(createNetworkAttachmentValidator(new NetworkAttachment()).networkAttachmentIsSet(),
                isValid());
    }

    /* tests whether validation is properly delegated. NetworkAttachmentValidator#networkExists
    delegates to NetworkValidator#networkIsSet. This test spies on creation of NetworkValidator, and returns mocked
    implementation which returns failing ValidationResult on NetworkValidator#networkIsSet. Finally it's tested, whether
    this ValidationResult was propagated correctly.
     */
    @Test
    public void testNetworkExistsWhenValidationFails() throws Exception {
        NetworkAttachmentValidator networkAttachmentValidatorSpy = Mockito.spy(
            createNetworkAttachmentValidator(new NetworkAttachment()));

        doReturn(networkValidatorMock).when(networkAttachmentValidatorSpy).getNetworkValidator();

        String variableReplacements = "a";
        ValidationResult propagatedResult = new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS,
            variableReplacements);
        when(networkValidatorMock.networkIsSet()).thenReturn(propagatedResult);

        assertThat("ValidationResult is not correctly propagated",
                networkAttachmentValidatorSpy.networkExists(),
            failsWith(VdcBllMessages.NETWORK_NOT_EXISTS, variableReplacements));
    }

    /* tests whether validation is properly delegated. NetworkAttachmentValidator#networkExists
    delegates to NetworkValidator#networkIsSet. This test spies on creation of NetworkValidator, and returns mocked
    implementation which returns valid ValidationResult on NetworkValidator#networkIsSet. Finally it's tested, whether
    this ValidationResult was propagated correctly.
     */
    @Test
    public void testNetworkExists() throws Exception {
        NetworkAttachmentValidator networkAttachmentValidatorSpy = Mockito.spy(
            createNetworkAttachmentValidator(new NetworkAttachment()));

        doReturn(networkValidatorMock).when(networkAttachmentValidatorSpy).getNetworkValidator();
        when(networkValidatorMock.networkIsSet()).thenReturn(ValidationResult.VALID);

        assertThat("ValidationResult is not correctly propagated",
            networkAttachmentValidatorSpy.networkExists(), isValid());
    }

    @Test
    public void testNotExternalNetworkWhenExternalNetworkIsProvided() throws Exception {
        Network externalNetwork = new Network();
        externalNetwork.setId(Guid.newGuid());
        externalNetwork.setProvidedBy(new ProviderNetwork(Guid.newGuid(), ""));

        when(networkDaoMock.get(eq(externalNetwork.getId()))).thenReturn(externalNetwork);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(externalNetwork.getId());
        assertThat(createNetworkAttachmentValidator(attachment).notExternalNetwork(),
                failsWith(VdcBllMessages.EXTERNAL_NETWORK_CANNOT_BE_PROVISIONED));
    }

    @Test
    public void testNotExternalNetwork() throws Exception {
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
    public void testNotRemovingManagementNetwork() throws Exception {
        NetworkAttachmentValidator networkAttachmentValidatorSpy = Mockito.spy(
            createNetworkAttachmentValidator(new NetworkAttachment()));

        doReturn(networkValidatorMock).when(networkAttachmentValidatorSpy).getNetworkValidator();

        ValidationResult propagatedResult =
                new ValidationResult(VdcBllMessages.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK, "a");
        when(networkValidatorMock.notRemovingManagementNetwork()).thenReturn(propagatedResult);

        assertThat("ValidationResult is not correctly propagated",
                networkAttachmentValidatorSpy.notRemovingManagementNetwork(),
                failsWith(VdcBllMessages.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK, "a"));
    }

    @Test
    public void testNetworkAttachedToClusterWhenAttached() throws Exception {
        Network network = new Network();
        network.setId(Guid.newGuid());




        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(network.getId());

        NetworkClusterId networkClusterId = new NetworkClusterId(host.getVdsGroupId(), attachment.getNetworkId());
        when(networkClusterDaoMock.get(eq(networkClusterId))).thenReturn(new NetworkCluster());
        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);

        assertThat(createNetworkAttachmentValidator(attachment).networkAttachedToCluster(), isValid());
    }

    @Test
    public void testNetworkAttachedToClusterWhenNotAttached() throws Exception {
        Network network = new Network();
        network.setId(Guid.newGuid());




        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(network.getId());

        NetworkClusterId networkClusterId = new NetworkClusterId(host.getVdsGroupId(), network.getId());
        when(networkClusterDaoMock.get(eq(networkClusterId))).thenReturn(null);
        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);

        assertThat(createNetworkAttachmentValidator(attachment).networkAttachedToCluster(),
                failsWith(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CLUSTER));
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNull() throws Exception {
        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setIpConfiguration(null);

        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);
        assertThat(validator.ipConfiguredForStaticBootProtocol(), isValid());
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullAndBootProtocolIsDhcp() throws Exception {
        doTestIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullForBootProtocol(NetworkBootProtocol.DHCP);
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullAndBootProtocolIsNone() throws Exception {
        doTestIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullForBootProtocol(NetworkBootProtocol.NONE);
    }

    private void doTestIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullForBootProtocol(
        NetworkBootProtocol bootProtocol) {
        NetworkAttachment networkAttachmentWithIpConfiguration =
            createNetworkAttachmentWithIpConfiguration(bootProtocol, null, null);

        NetworkAttachmentValidator networkAttachmentValidator =
            createNetworkAttachmentValidator(networkAttachmentWithIpConfiguration);
        assertThat(networkAttachmentValidator.ipConfiguredForStaticBootProtocol(), isValid());
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullAndBootProtocolIsStaticAndAddressIsNull() throws Exception {
        doTestIpConfiguredForStaticBootProtocol(null, "255.255.255.0");
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullAndBootProtocolIsStaticAndAddressIsEmpty() throws Exception {
        doTestIpConfiguredForStaticBootProtocol("", "255.255.255.0");
    }

    private void doTestIpConfiguredForStaticBootProtocol(String address, String netmask) {
        Matcher<ValidationResult> matcher = failsWith(VdcBllMessages.NETWORK_ADDR_MANDATORY_IN_STATIC_IP);

        doTestIpConfiguredForStaticBootProtocol(address, netmask, matcher);
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullAndBootProtocolIsStaticAndNetmaskIsNull() throws Exception {
        doTestIpConfiguredForStaticBootProtocol("192.168.1.1", null);
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullAndBootProtocolIsStaticAndNetmaskIsEmpty() throws Exception {
        doTestIpConfiguredForStaticBootProtocol("192.168.1.1", "");
    }

    @Test
    public void testIpConfiguredForStaticBootProtocolWhenIpConfigurationIsNotNullAndBootProtocolIsStaticAndAddressAndNetmaskIsNotNull() throws Exception {
        doTestIpConfiguredForStaticBootProtocol("192.168.1.1", "255.255.255.0", isValid());
    }

    private void doTestIpConfiguredForStaticBootProtocol(String address,
        String netmask,
        Matcher<ValidationResult> matcher) {
        NetworkAttachment attachment =
            createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol.STATIC_IP, address, netmask);

        assertThat(createNetworkAttachmentValidator(attachment).ipConfiguredForStaticBootProtocol(), matcher);
    }

    private NetworkAttachment createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol bootProtocol,
            String address,
            String netmask) {

        IpConfiguration ipConfiguration = new IpConfiguration();
        IPv4Address primaryAddress = new IPv4Address();
        primaryAddress.setAddress(address);
        primaryAddress.setNetmask(netmask);
        ipConfiguration.getIPv4Addresses().add(primaryAddress);
        ipConfiguration.setBootProtocol(bootProtocol);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setIpConfiguration(ipConfiguration);

        return attachment;
    }

    @Test
    public void testBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplayIsFalse() {
        doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(false,
            isValid());
    }

    @Test
    public void testBootProtocolSetForDisplayNetworkWhenIpConfigurationIsNull() {
        doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(true,
            failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL));
    }

    private void doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(
        boolean displayNetwork, Matcher<ValidationResult> matcher) {
        Network network = new Network();
        network.setId(Guid.newGuid());

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setIpConfiguration(null);
        attachment.setNetworkId(network.getId());

        doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(
            displayNetwork,
            matcher,
            network,
            attachment);
    }

    private void doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(
        boolean displayNetwork,
        Matcher<ValidationResult> matcher,
        Network network,
        NetworkAttachment attachment) {


        NetworkCluster networkCluster = new NetworkCluster();
        networkCluster.setDisplay(displayNetwork);

        NetworkClusterId networkClusterId = new NetworkClusterId(host.getVdsGroupId(), attachment.getNetworkId());
        when(networkClusterDaoMock.get(eq(networkClusterId))).thenReturn(networkCluster);
        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);

        assertThat(createNetworkAttachmentValidator(attachment).bootProtocolSetForDisplayNetwork(), matcher);
    }

    @Test
    public void testBootProtocolSetForDisplayNetworkWhenBootProtocolIsNone() throws Exception {
        Network network = new Network();
        network.setId(Guid.newGuid());

        NetworkAttachment attachment =
                createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol.NONE, null, null);
        attachment.setNetworkId(network.getId());


        doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(true,
            failsWith(VdcBllMessages.ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL),
            network,
            attachment);
    }

    @Test
    public void testBootProtocolSetForDisplayNetworkWhenNetworkIsNonDisplay() throws Exception {
        Network network = new Network();
        network.setId(Guid.newGuid());

        NetworkAttachment attachment =
            createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol.NONE, null, null);
        attachment.setNetworkId(network.getId());


        doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(false,
            isValid(),
            network,
            attachment);
    }

    @Test
    public void testBootProtocolSetForDisplayNetworkWhenBootProtocolIsDhcp() throws Exception {
        Network network = new Network();
        network.setId(Guid.newGuid());

        NetworkAttachment attachment =
            createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol.DHCP, null, null);
        attachment.setNetworkId(network.getId());


        doTestBootProtocolSetForDisplayNetworkWhenNullValuedIpConfigurationAndWhenNetworkClusterDisplay(true,
            isValid(),
            network,
            attachment);
    }

    @Test
    public void testNicExistsWhenNicNameIsNull() throws Exception {
        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNicName(null);
        assertThat(createNetworkAttachmentValidator(attachment).nicExists(),
                failsWith(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    @Test
    public void testNicExistsWhenNicNameIsNotNull() throws Exception {
        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNicId(null);
        attachment.setNicName("whatever");

        assertThat(createNetworkAttachmentValidator(attachment).nicExists(), isValid());
    }

    @Test
    public void testNetworkIpAddressWasSameAsHostnameAndChangedWhenIpConfigurationIsNull() throws Exception {

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setIpConfiguration(null);

        assertThat(createNetworkAttachmentValidator(attachment).networkIpAddressWasSameAsHostnameAndChanged(null),
                isValid());
    }

    @Test
    public void testNetworkIpAddressWasSameAsHostnameAndChangedWhenIpConfigurationIsDhcp() throws Exception {
        doTestNetworkIpAddressWasSameAsHostnameAndChangedForBootProtocol(NetworkBootProtocol.DHCP);
    }

    @Test
    public void testNetworkIpAddressWasSameAsHostnameAndChangedWhenIpConfigurationIsNone() throws Exception {
        doTestNetworkIpAddressWasSameAsHostnameAndChangedForBootProtocol(NetworkBootProtocol.NONE);
    }

    private void doTestNetworkIpAddressWasSameAsHostnameAndChangedForBootProtocol(NetworkBootProtocol bootProtocol) {
        NetworkAttachment attachment = createNetworkAttachmentWithIpConfiguration(bootProtocol, null, null);

        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);
        ValidationResult actual = validator.networkIpAddressWasSameAsHostnameAndChanged(null);
        assertThat(actual, isValid());
    }

    @Test
    public void testNetworkIpAddressWasSameAsHostnameAndChangedWhenIfaceDoesNotExist() throws Exception {

        NetworkAttachment attachment =
                createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol.STATIC_IP, null, null);
        attachment.setNicName("nicName");

        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);
        Map<String, VdsNetworkInterface> nics = Collections.emptyMap();
        assertThat(validator.networkIpAddressWasSameAsHostnameAndChanged(nics), isValid());
    }

    @Test
    public void testNetworkIpAddressWasSameAsHostnameAndChanged() throws Exception {

        VdsNetworkInterface existingInterface = new VdsNetworkInterface();
        existingInterface.setName("nicName");
        existingInterface.setAddress("anyAddress");

        NetworkAttachment attachment =
                createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol.STATIC_IP, null, null);
        attachment.setNicName(existingInterface.getName());

        host.setHostName(existingInterface.getAddress());

        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);
        Map<String, VdsNetworkInterface> nics = Collections.singletonMap(existingInterface.getName(), existingInterface);
        assertThat(validator.networkIpAddressWasSameAsHostnameAndChanged(nics),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED));
    }

    @Test
    public void testNetworkNotChangedWhenOldAttachmentIsNull() throws Exception {
        assertThat(createNetworkAttachmentValidator(null).networkNotChanged(null), isValid());
    }

    @Test
    public void testNetworkNotChangedWhenDifferentNetworkIds() throws Exception {
        NetworkAttachment oldAttachment = new NetworkAttachment();
        oldAttachment.setNetworkId(Guid.newGuid());

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(Guid.newGuid());

        assertThat(createNetworkAttachmentValidator(attachment).networkNotChanged(oldAttachment),
                failsWith(VdcBllMessages.CANNOT_CHANGE_ATTACHED_NETWORK));
    }

    @Test
    public void testNetworkNotChanged() throws Exception {
        Guid networkId = Guid.newGuid();

        NetworkAttachment oldAttachment = new NetworkAttachment();
        oldAttachment.setNetworkId(networkId);

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(networkId);

        assertThat(createNetworkAttachmentValidator(attachment).networkNotChanged(oldAttachment), isValid());
    }

    @Test
    public void testValidateGateway() throws Exception {
        host.setVdsGroupCompatibilityVersion(Version.v3_5);
        doTestValidateGateway("someGateway", false, failsWith(VdcBllMessages.NETWORK_ATTACH_ILLEGAL_GATEWAY));
    }

    @Test
    public void testValidateGatewayWhenIpConfigurationIsNotSet() throws Exception {
        NetworkAttachment attachment = new NetworkAttachment();
        assertThat(createNetworkAttachmentValidator(attachment).validateGateway(), isValid());
    }

    @Test
    public void testValidateGatewayWhenGatewayIsNotSpecified() throws Exception {
        doTestValidateGateway("", false, isValid());
    }

    @Test
    public void testValidateGatewayWhenGatewayIsNull() throws Exception {
        doTestValidateGateway(null, false, isValid());
    }

    @Test
    public void testValidateGatewayWhenRelatedNetworkIsManagementNetwork() throws Exception {
        doTestValidateGateway("someGateway", true, isValid());
    }

    @Test
    public void testValidateGatewayWhenMultipleGatewayIsSupported() throws Exception {
        host.setVdsGroupCompatibilityVersion(Version.v3_6);
        doTestValidateGateway("someGateway", false, isValid());

    }

    private void doTestValidateGateway(String gatewayValue,
        boolean managementNetwork,
        Matcher<ValidationResult> resultMatcher) {

        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName("networkName");

        NetworkAttachment attachment = createNetworkAttachmentWithIpConfiguration(NetworkBootProtocol.NONE, null, null);
        attachment.setNetworkId(network.getId());
        attachment.getIpConfiguration().getPrimaryAddress().setGateway(gatewayValue);

        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);
        when(managementNetworkUtilMock.isManagementNetwork(network.getId())).thenReturn(managementNetwork);

        assertThat(createNetworkAttachmentValidator(attachment).validateGateway(), resultMatcher);
    }

    @Test
    public void testNetworkNotAttachedToHost() throws Exception {
        Guid networkId = Guid.newGuid();

        // no vds for network id.
        when(vdsDaoMock.getAllForNetwork(eq(networkId))).thenReturn(Collections.<VDS> emptyList());

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(networkId);
        assertThat(createNetworkAttachmentValidator(attachment).networkNotAttachedToHost(),
            isValid());
    }

    @Test
    public void testNetworkNotAttachedToHostWhenAttached() throws Exception {
        Guid networkId = Guid.newGuid();

        when(vdsDaoMock.getAllForNetwork(eq(networkId))).thenReturn(Collections.singletonList(host));

        NetworkAttachment attachment = new NetworkAttachment();
        attachment.setNetworkId(networkId);
        assertThat(createNetworkAttachmentValidator(attachment).networkNotAttachedToHost(),
            failsWith(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_HOST));
    }

    @Test
    public void testNetworkNotUsedByVmsWhenNotUsed() throws Exception {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName("name");

        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkId(network.getId());

        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(networkAttachment);

        when(vmInterfaceManager
            .findActiveVmsUsingNetworks(eq(host.getId()), collectionContainingOneGivenNetworkName(network.getName())))
            .thenReturn(Collections.<String> emptyList());

        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);

        assertThat(validator.networkNotUsedByVms(), isValid());
    }

    @Test
    public void testNetworkNotUsedByVmsWhenUsed() throws Exception {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName("name");

        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkId(network.getId());

        NetworkAttachmentValidator validator = createNetworkAttachmentValidator(networkAttachment);

        when(vmInterfaceManager
            .findActiveVmsUsingNetworks(eq(host.getId()), collectionContainingOneGivenNetworkName(network.getName())))
            .thenReturn(Collections.singletonList("networkName"));

        when(networkDaoMock.get(eq(network.getId()))).thenReturn(network);
        assertThat(validator.networkNotUsedByVms(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_ONE_USE));
    }

    private Collection<String> collectionContainingOneGivenNetworkName(final String name) {
        return argThat(new ArgumentMatcher<Collection<String>>() {
            @Override
            public boolean matches(Object argument) {
                return ((Collection<String>) argument).contains(name);
            }
        });
    }

}
