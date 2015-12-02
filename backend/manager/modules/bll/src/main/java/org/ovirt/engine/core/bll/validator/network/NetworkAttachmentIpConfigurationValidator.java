package org.ovirt.engine.core.bll.validator.network;

import java.util.Collection;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

@Singleton
public class NetworkAttachmentIpConfigurationValidator {

    static final String VAR_NETWORK_NAME = "networkName";
    static final String VAR_INTERFACE_NAME = "interfaceName";
    static final String VAR_BOOT_PROTOCOL = "BootProtocol";

    public ValidationResult validateNetworkAttachmentIpConfiguration(
            Collection<NetworkAttachment> attachmentsToConfigure) {
        IpConfiguration networkAttachmentIpConfiguration = null;
        IPv4Address iPv4Address = null;
        for (NetworkAttachment networkAttachment : attachmentsToConfigure) {
            networkAttachmentIpConfiguration = networkAttachment.getIpConfiguration();
            if (networkAttachmentIpConfiguration == null || !networkAttachmentIpConfiguration.hasPrimaryAddressSet()) {
                return incompleteIpConfigurationValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_MISSING_IP_CONFIGURATION,
                        networkAttachment.getNetworkName(),
                        networkAttachment.getNicName());
            }
            iPv4Address = networkAttachmentIpConfiguration.getPrimaryAddress();
            if (iPv4Address.getBootProtocol() == null) {
                return incompleteIpConfigurationValidationResult(
                        EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_MISSING_BOOT_PROTOCOL,
                        networkAttachment.getNetworkName(),
                        networkAttachment.getNicName());
            }
            NetworkBootProtocol bootProtocol = iPv4Address.getBootProtocol();
            if (bootProtocol == NetworkBootProtocol.DHCP || bootProtocol == NetworkBootProtocol.NONE) {
                if (!validDhcpOrNoneIpAddressDetails(iPv4Address)) {
                    return new ValidationResult(
                            EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_INCOMPATIBLE_BOOT_PROTOCOL_AND_IP_ADDRESS_DETAILS,
                            ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME,
                                    networkAttachment.getNetworkName()),
                            ReplacementUtils.createSetVariableString(VAR_INTERFACE_NAME,
                                    networkAttachment.getNicName()),
                            ReplacementUtils.createSetVariableString(VAR_BOOT_PROTOCOL, bootProtocol.getDisplayName()));

                }
            }
            if (bootProtocol == NetworkBootProtocol.STATIC_IP) {
                if (!validStaticAddressDetails(iPv4Address)) {
                    return incompleteIpConfigurationValidationResult(
                            EngineMessage.NETWORK_ATTACHMENT_IP_CONFIGURATION_STATIC_BOOT_PROTOCOL_MISSING_IP_ADDRESS_DETAILS,
                            networkAttachment.getNetworkName(),
                            networkAttachment.getNicName());
                }
            }
        }
        return ValidationResult.VALID;
    }

    private boolean validDhcpOrNoneIpAddressDetails(IPv4Address iPv4Address) {
        return StringUtils.isEmpty(iPv4Address.getAddress()) && StringUtils.isEmpty(iPv4Address.getNetmask())
                && StringUtils.isEmpty(iPv4Address.getGateway());
    }

    private boolean validStaticAddressDetails(IPv4Address iPv4Address) {
        return !(StringUtils.isEmpty(iPv4Address.getAddress()) || StringUtils.isEmpty(iPv4Address.getNetmask()));
    }

    private ValidationResult incompleteIpConfigurationValidationResult(EngineMessage engineMessage,
            String networkName,
            String interfaceName) {
        return new ValidationResult(engineMessage,
                ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, networkName),
                ReplacementUtils.createSetVariableString(VAR_INTERFACE_NAME, interfaceName));
    }

}
