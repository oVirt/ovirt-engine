package org.ovirt.engine.core.bll.validator.network;

import static org.junit.Assert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.utils.ReplacementUtils.createSetVariableString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class NetworkAttachmentIpConfigurationValidatorTest {

    private static final String NETWORK_NAME = "NETWORK_NAME";
    private static final String INTERFACE_NAME = "INTERFACE_NAME";
    private static final String ADDRESS = "ADDRESS";
    private static final String NETMASK = "NETMASK";

    private NetworkAttachmentIpConfigurationValidator underTest = new NetworkAttachmentIpConfigurationValidator();
    private List<NetworkAttachment> networkAttachments;
    private Random random = new Random();

    @Before
    public void init() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkName(NETWORK_NAME);
        networkAttachment.setNicName(INTERFACE_NAME);
        networkAttachments = new ArrayList<>();
        networkAttachments.add(networkAttachment);
    }

    @Test
    public void checkNullIpConfiguration() {
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_MISSING_IP_CONFIGURATION,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkMissingPrimaryAddress() {
        initIpConfiguration();
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_MISSING_IP_CONFIGURATION,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkMissingBootProtocol() {
        initIpConfigurationWithPrimaryAddress();
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_MISSING_BOOT_PROTOCOL,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkIncompatibleIpAddressDetailsBootProtocolNone() {
        checkIncompatibleIpAddressDetailsBootProtocol(NetworkBootProtocol.NONE);
    }

    @Test
    public void checkIncompatibleIpAddressDetailsBootProtocolDhcp() {
        checkIncompatibleIpAddressDetailsBootProtocol(NetworkBootProtocol.DHCP);
    }

    @Test
    public void checkMissingIpAddressDetailsBootProtocolStatic() {
        final boolean initAddress = random.nextBoolean();
        initIpConfigurationDetails(NetworkBootProtocol.STATIC_IP, initAddress, !initAddress);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments),
                failsWith(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_STATIC_BOOT_PROTOCOL_MISSING_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkValidIpConfigurationNoneBootProtocol() {
        initIpConfigurationDetails(NetworkBootProtocol.NONE, false, false);
        ValidationResult actual = underTest.validateNetworkAttachmentIpConfiguration(networkAttachments);
        Assert.assertEquals(ValidationResult.VALID, actual);
    }

    @Test
    public void checkValidIpConfigurationDHCPBootProtocol() {
        initIpConfigurationDetails(NetworkBootProtocol.DHCP, false, false);
        ValidationResult actual = underTest.validateNetworkAttachmentIpConfiguration(networkAttachments);
        Assert.assertEquals(ValidationResult.VALID, actual);
    }

    @Test
    public void checkValidIpConfigurationStaticBootProtocol() {
        initIpConfigurationDetails(NetworkBootProtocol.STATIC_IP, true, true);
        ValidationResult actual = underTest.validateNetworkAttachmentIpConfiguration(networkAttachments);
        Assert.assertEquals(ValidationResult.VALID, actual);
    }

    private void checkIncompatibleIpAddressDetailsBootProtocol(NetworkBootProtocol networkBootProtocol) {
        final boolean initAddress = random.nextBoolean();
        initIpConfigurationDetails(networkBootProtocol, initAddress, !initAddress);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments),
                failsWith(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_INCOMPATIBLE_BOOT_PROTOCOL_AND_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_BOOT_PROTOCOL,
                                networkBootProtocol.getDisplayName())));
    }

    private void initIpConfigurationDetails(NetworkBootProtocol networkBootProtocol,
            boolean initAddress,
            boolean initNetmask) {
        initIpConfigurationWithBootProtocol(networkBootProtocol);
        if (initAddress) {
            initIpConfigurationAddress();
        }
        if (initNetmask) {
            initIpConfigurationNetmask();
        }
    }

    private NetworkAttachment getTestedNetworkAttachment() {
        return networkAttachments.get(0);
    }

    private void initIpConfiguration() {
        IpConfiguration ipConfiguration = new IpConfiguration();
        getTestedNetworkAttachment().setIpConfiguration(ipConfiguration);
    }

    private void initIpConfigurationWithPrimaryAddress() {
        initIpConfiguration();
        getTestedNetworkAttachment().getIpConfiguration().setIPv4Addresses(Arrays.asList(new IPv4Address()));
    }

    private void initIpConfigurationWithBootProtocol(NetworkBootProtocol networkBootProtocol) {
        initIpConfigurationWithPrimaryAddress();
        getTestedNetworkAttachment().getIpConfiguration().getPrimaryAddress().setBootProtocol(networkBootProtocol);
    }

    private void initIpConfigurationAddress() {
        getTestedNetworkAttachment().getIpConfiguration().getPrimaryAddress().setAddress(ADDRESS);
    }

    private void initIpConfigurationNetmask() {
        getTestedNetworkAttachment().getIpConfiguration().getPrimaryAddress().setNetmask(NETMASK);
    }

}
