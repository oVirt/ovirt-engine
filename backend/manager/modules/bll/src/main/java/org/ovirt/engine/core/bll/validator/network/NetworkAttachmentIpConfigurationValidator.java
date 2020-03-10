package org.ovirt.engine.core.bll.validator.network;

import static org.ovirt.engine.core.utils.NetworkUtils.areDifferentId;
import static org.ovirt.engine.core.utils.NetworkUtils.hasIpv6Gateway;

import java.util.Collection;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

@Singleton
public class NetworkAttachmentIpConfigurationValidator {

    static final String VAR_NETWORK_NAME = "networkName";
    static final String VAR_INTERFACE_NAME = "interfaceName";
    static final String VAR_BOOT_PROTOCOL = "BootProtocol";

    public ValidationResult validateNetworkAttachmentIpConfiguration(
            Collection<NetworkAttachment> attachmentsToConfigure, NetworkAttachment currentDefaultRouteNetworkAttachment, boolean isHostOsEl8) {
        for (NetworkAttachment networkAttachment : attachmentsToConfigure) {
            IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
            if (ipConfiguration == null
                    || !(ipConfiguration.hasIpv4PrimaryAddressSet() || ipConfiguration.hasIpv6PrimaryAddressSet())) {
                return incompleteIpConfigurationValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_MISSING_IP_CONFIGURATION,
                        networkAttachment.getNetworkName(),
                        networkAttachment.getNicName());
            }

            final ValidationResult ipv4ValidationResult = validateIpv4Configuration(networkAttachment);
            if (!ipv4ValidationResult.isValid()) {
                return ipv4ValidationResult;
            }
            final ValidationResult ipv6ValidationResult = validateIpv6Configuration(
                    networkAttachment, currentDefaultRouteNetworkAttachment, isHostOsEl8);
            if (!ipv6ValidationResult.isValid()) {
                return ipv6ValidationResult;
            }
        }
        return ValidationResult.VALID;
    }

    private ValidationResult validateIpv4Configuration(NetworkAttachment networkAttachment) {
        IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
        if (!ipConfiguration.hasIpv4PrimaryAddressSet()) {
            return ValidationResult.VALID;
        }
        IPv4Address iPv4Address = ipConfiguration.getIpv4PrimaryAddress();
        String networkName = networkAttachment.getNetworkName();
        String nicName = networkAttachment.getNicName();
        if (iPv4Address.getBootProtocol() == null) {
            return incompleteIpConfigurationValidationResult(
                    EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_MISSING_BOOT_PROTOCOL,
                    networkName,
                    nicName);
        }
        Ipv4BootProtocol bootProtocol = iPv4Address.getBootProtocol();
        if (bootProtocol == Ipv4BootProtocol.STATIC_IP) {
            if (!validStaticIpv4AddressDetails(iPv4Address)) {
                return incompleteIpConfigurationValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_STATIC_BOOT_PROTOCOL_MISSING_IP_ADDRESS_DETAILS,
                        networkName,
                        nicName);
            }
        } else {
            if (!isEmptyIpv4AddressDetails(iPv4Address)) {
                return new ValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_INCOMPATIBLE_BOOT_PROTOCOL_AND_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, networkName),
                        ReplacementUtils.createSetVariableString(VAR_INTERFACE_NAME, nicName),
                        ReplacementUtils.createSetVariableString(VAR_BOOT_PROTOCOL, bootProtocol.getDisplayName()));

            }
        }
        return ValidationResult.VALID;
    }

    private ValidationResult validateIpv6Configuration(
            NetworkAttachment networkAttachment, NetworkAttachment currentDefaultRouteAttachment, boolean isHostOsEl8) {
        final IpConfiguration ipConfiguration = networkAttachment.getIpConfiguration();
        if (!ipConfiguration.hasIpv6PrimaryAddressSet()) {
            return ValidationResult.VALID;
        }
        IpV6Address ipv6Address = ipConfiguration.getIpv6PrimaryAddress();
        String networkName = networkAttachment.getNetworkName();
        String nicName = networkAttachment.getNicName();
        if (ipv6Address.getBootProtocol() == null) {
            return incompleteIpConfigurationValidationResult(
                    EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_MISSING_BOOT_PROTOCOL,
                    networkName,
                    nicName);
        }
        Ipv6BootProtocol bootProtocol = ipv6Address.getBootProtocol();
        if (bootProtocol == Ipv6BootProtocol.STATIC_IP) {
            if (!validStaticIpv6AddressDetails(ipv6Address)) {
                return incompleteIpConfigurationValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_STATIC_BOOT_PROTOCOL_MISSING_IP_ADDRESS_DETAILS,
                        networkName,
                        nicName);
            }
            if (nonDefaultRouteAttachmentHasIpv6Gateway(networkAttachment, currentDefaultRouteAttachment)) {
                return incompleteIpConfigurationValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_STATIC_BOOT_PROTOCOL_IPV6_GATEWAY_ON_NON_DEFAULT_ROUTE_ROLE,
                        networkName,
                        nicName);
            }
        } else {
            if (!isEmptyIpv6AddressDetails(ipv6Address)) {
                return new ValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_INCOMPATIBLE_BOOT_PROTOCOL_AND_IP_ADDRESS_DETAILS,
                        ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, networkName),
                        ReplacementUtils.createSetVariableString(VAR_INTERFACE_NAME, nicName),
                        ReplacementUtils.createSetVariableString(VAR_BOOT_PROTOCOL, bootProtocol.getDisplayName()));
            }
            if (bootProtocol == Ipv6BootProtocol.AUTOCONF && isHostOsEl8) {
                return new ValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_INCOMPATIBLE_BOOT_PROTOCOL_AND_HOST_OS_VERSION,
                        ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, networkName),
                        ReplacementUtils.createSetVariableString(VAR_INTERFACE_NAME, nicName),
                        ReplacementUtils.createSetVariableString(VAR_BOOT_PROTOCOL, bootProtocol.getDisplayName()));
            }
        }
        return ValidationResult.VALID;
    }

    private boolean nonDefaultRouteAttachmentHasIpv6Gateway(NetworkAttachment networkAttachment, NetworkAttachment currentDefaultRoute) {
        if (currentDefaultRoute == null) {
            return false;
        }
        return areDifferentId(networkAttachment, currentDefaultRoute) && hasIpv6Gateway(networkAttachment);
    }

    private boolean isEmptyIpv4AddressDetails(IPv4Address iPv4Address) {
        return StringUtils.isEmpty(iPv4Address.getAddress())
                && StringUtils.isEmpty(iPv4Address.getNetmask())
                && StringUtils.isEmpty(iPv4Address.getGateway());
    }

    private boolean isEmptyIpv6AddressDetails(IpV6Address ipv6Address) {
        return StringUtils.isEmpty(ipv6Address.getAddress())
                && ipv6Address.getPrefix() == null
                && StringUtils.isEmpty(ipv6Address.getGateway());
    }

    private boolean validStaticIpv4AddressDetails(IPv4Address iPv4Address) {
        return !(StringUtils.isEmpty(iPv4Address.getAddress()) || StringUtils.isEmpty(iPv4Address.getNetmask()));
    }

    private boolean validStaticIpv6AddressDetails(IpV6Address ipv6Address) {
        return !(StringUtils.isEmpty(ipv6Address.getAddress()) || ipv6Address.getPrefix() == null);
    }

    private ValidationResult incompleteIpConfigurationValidationResult(EngineMessage engineMessage,
            String networkName,
            String interfaceName) {
        return new ValidationResult(engineMessage,
                ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, networkName),
                ReplacementUtils.createSetVariableString(VAR_INTERFACE_NAME, interfaceName));
    }
}
