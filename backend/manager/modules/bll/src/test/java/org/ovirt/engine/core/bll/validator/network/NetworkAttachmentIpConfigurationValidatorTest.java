package org.ovirt.engine.core.bll.validator.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.ReplacementUtils.createSetVariableString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
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

    @BeforeEach
    public void init() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkName(NETWORK_NAME);
        networkAttachment.setNicName(INTERFACE_NAME);
        networkAttachments = new ArrayList<>();
        networkAttachments.add(networkAttachment);
    }

    @Test
    public void checkNullIpConfiguration() {
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_MISSING_IP_CONFIGURATION,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkMissingBothAddresses() {
        initIpConfiguration();
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_MISSING_IP_CONFIGURATION,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkMissingPrimaryIpv4Address() {
        initIpConfiguration();
        initIpv6ConfigurationDetails(Ipv6BootProtocol.AUTOCONF, false, false);

        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    @Test
    public void checkMissingPrimaryIpv6Address() {
        initIpConfiguration();
        initIpv4ConfigurationDetails(Ipv4BootProtocol.DHCP, false, false);

        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    @Test
    public void checkMissingV4BootProtocol() {
        initIpv4ConfigurationWithPrimaryAddress();
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_MISSING_BOOT_PROTOCOL,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkMissingV6BootProtocol() {
        initIpv6ConfigurationWithPrimaryAddress();
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_MISSING_BOOT_PROTOCOL,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkIncompatibleIpv4AddressDetailsBootProtocolNone() {
        checkIncompatibleIpv4AddressDetailsBootProtocol(Ipv4BootProtocol.NONE);
    }

    @Test
    public void checkIncompatibleIpv6AddressDetailsBootProtocolNone() {
        checkIncompatibleIpv6AddressDetailsBootProtocol(Ipv6BootProtocol.NONE);
    }

    @Test
    public void checkIncompatibleIpv4AddressDetailsBootProtocolDhcp() {
        checkIncompatibleIpv4AddressDetailsBootProtocol(Ipv4BootProtocol.DHCP);
    }

    @Test
    public void checkIncompatibleIpv6AddressDetailsBootProtocolDhcp() {
        checkIncompatibleIpv6AddressDetailsBootProtocol(Ipv6BootProtocol.DHCP);
    }

    @Test
    public void checkMissingIpv4AddressDetailsBootProtocolStatic() {
        final boolean initAddress = random.nextBoolean();
        initIpv4ConfigurationDetails(Ipv4BootProtocol.STATIC_IP, initAddress, !initAddress);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_STATIC_BOOT_PROTOCOL_MISSING_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkMissingIpv6AddressDetailsBootProtocolStatic() {
        final boolean initAddress = random.nextBoolean();
        initIpv6ConfigurationDetails(Ipv6BootProtocol.STATIC_IP, initAddress, !initAddress);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_STATIC_BOOT_PROTOCOL_MISSING_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME)));
    }

    @Test
    public void checkValidIpv4ConfigurationNoneBootProtocol() {
        initIpv4ConfigurationDetails(Ipv4BootProtocol.NONE, false, false);
        initIpv6Address();
        setIpv6BootProtocol(Ipv6BootProtocol.AUTOCONF);

        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    @Test
    public void checkValidIpv6ConfigurationNoneBootProtocol() {
        initIpv6ConfigurationDetails(Ipv6BootProtocol.NONE, false, false);
        initIpv4Address();
        setIpv4BootProtocol(Ipv4BootProtocol.DHCP);

        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    @Test
    public void checkValidIpv4ConfigurationDHCPBootProtocol() {
        initIpv4ConfigurationDetails(Ipv4BootProtocol.DHCP, false, false);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    @Test
    public void checkValidIpv6ConfigurationDHCPBootProtocol() {
        initIpv6ConfigurationDetails(Ipv6BootProtocol.AUTOCONF, false, false);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    @Test
    public void checkValidIpv4ConfigurationStaticBootProtocol() {
        initIpv4ConfigurationDetails(Ipv4BootProtocol.STATIC_IP, true, true);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    @Test
    public void checkValidIpv6ConfigurationStaticBootProtocol() {
        initIpv6ConfigurationDetails(Ipv6BootProtocol.STATIC_IP, true, true);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false), isValid());
    }

    private void checkIncompatibleIpv4AddressDetailsBootProtocol(Ipv4BootProtocol ipv4BootProtocol) {
        final boolean initAddress = random.nextBoolean();
        initIpv4ConfigurationDetails(ipv4BootProtocol, initAddress, !initAddress);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_INCOMPATIBLE_BOOT_PROTOCOL_AND_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_BOOT_PROTOCOL,
                                ipv4BootProtocol.getDisplayName())));
    }

    private void checkIncompatibleIpv6AddressDetailsBootProtocol(Ipv6BootProtocol ipv6BootProtocol) {
        final boolean initAddress = random.nextBoolean();
        initIpv6ConfigurationDetails(ipv6BootProtocol, initAddress, !initAddress);
        assertThat(underTest.validateNetworkAttachmentIpConfiguration(networkAttachments, null, false),
                failsWith(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_INCOMPATIBLE_BOOT_PROTOCOL_AND_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(
                                NetworkAttachmentIpConfigurationValidator.VAR_NETWORK_NAME, NETWORK_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_INTERFACE_NAME,
                                INTERFACE_NAME),
                        createSetVariableString(NetworkAttachmentIpConfigurationValidator.VAR_BOOT_PROTOCOL,
                                ipv6BootProtocol.getDisplayName())));
    }

    private void initIpv4ConfigurationDetails(Ipv4BootProtocol ipv4BootProtocol,
            boolean initAddress,
            boolean initNetmask) {
        initIpv4ConfigurationWithBootProtocol(ipv4BootProtocol);
        if (initAddress) {
            initIpv4ConfigurationAddress();
        }
        if (initNetmask) {
            initIpv4ConfigurationNetmask();
        }
    }

    private void initIpv6ConfigurationDetails(Ipv6BootProtocol ipv6BootProtocol,
            boolean initAddress,
            boolean initNetmask) {
        initIpv6ConfigurationWithBootProtocol(ipv6BootProtocol);
        if (initAddress) {
            initIpv6ConfigurationAddress();
        }
        if (initNetmask) {
            initIpv6ConfigurationPrefix();
        }
    }

    private NetworkAttachment getTestedNetworkAttachment() {
        return networkAttachments.get(0);
    }

    private void initIpConfiguration() {
        IpConfiguration ipConfiguration = new IpConfiguration();
        getTestedNetworkAttachment().setIpConfiguration(ipConfiguration);
    }

    private void initIpv4ConfigurationWithPrimaryAddress() {
        initIpConfiguration();
        initIpv4Address();
    }

    private void initIpv6ConfigurationWithPrimaryAddress() {
        initIpConfiguration();
        initIpv6Address();
    }

    private void initIpv4ConfigurationWithBootProtocol(Ipv4BootProtocol ipv4BootProtocol) {
        initIpv4ConfigurationWithPrimaryAddress();
        setIpv4BootProtocol(ipv4BootProtocol);
    }

    private void setIpv4BootProtocol(Ipv4BootProtocol ipv4BootProtocol) {
        getTestedNetworkAttachment().getIpConfiguration().getIpv4PrimaryAddress().setBootProtocol(ipv4BootProtocol);
    }

    private void initIpv6ConfigurationWithBootProtocol(Ipv6BootProtocol ipv6BootProtocol) {
        initIpv6ConfigurationWithPrimaryAddress();
        setIpv6BootProtocol(ipv6BootProtocol);
    }

    private void setIpv6BootProtocol(Ipv6BootProtocol ipv6BootProtocol) {
        getTestedNetworkAttachment().getIpConfiguration().getIpv6PrimaryAddress().setBootProtocol(ipv6BootProtocol);
    }

    private void initIpv4ConfigurationAddress() {
        getTestedNetworkAttachment().getIpConfiguration().getIpv4PrimaryAddress().setAddress(ADDRESS);
    }

    private void initIpv4ConfigurationNetmask() {
        getTestedNetworkAttachment().getIpConfiguration().getIpv4PrimaryAddress().setNetmask(NETMASK);
    }

    private void initIpv6ConfigurationAddress() {
        getTestedNetworkAttachment().getIpConfiguration().getIpv6PrimaryAddress().setAddress(ADDRESS);
    }

    private void initIpv6ConfigurationPrefix() {
        getTestedNetworkAttachment().getIpConfiguration().getIpv6PrimaryAddress().setPrefix(666);
    }

    private void initIpv4Address() {
        getTestedNetworkAttachment().getIpConfiguration().setIPv4Addresses(Collections.singletonList(new IPv4Address()));
    }

    private void initIpv6Address() {
        getTestedNetworkAttachment().getIpConfiguration().setIpV6Addresses(Collections.singletonList(new IpV6Address()));
    }

}
