package org.ovirt.engine.core.bll.validator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.PluralMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class NetworkAttachmentValidator {

    protected NetworkAttachment attachment;
    protected Network network;
    protected VDS host;
    protected final ManagementNetworkUtil managementNetworkUtil;

    private NetworkCluster networkCluster;
    private NetworkValidator networkValidator;
    private NetworkAttachment existingNetworkAttachment;

    public NetworkAttachmentValidator(NetworkAttachment attachment,
            VDS host,
            ManagementNetworkUtil managementNetworkUtil) {

        this.attachment = attachment;
        this.host = host;
        this.managementNetworkUtil = managementNetworkUtil;
    }

    public ValidationResult networkAttachmentIsSet() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_ATTACHMENT_NOT_EXISTS).when(attachment == null);
    }

    private NetworkAttachment getExistingNetworkAttachment() {
        if (existingNetworkAttachment == null) {
            existingNetworkAttachment = getDbFacade().getNetworkAttachmentDao().get(attachment.getId());
        }

        return existingNetworkAttachment;
    }

    public ValidationResult networkExists() {
        return getNetworkValidator().networkIsSet();
    }

    public ValidationResult networkNotUsedByVms() {
        return networkNotUsedByVms(getNetwork().getName());
    }

    private ValidationResult networkNotUsedByVms(String networkName) {
        List<String> vmNames =
            new VmInterfaceManager().findActiveVmsUsingNetworks(host.getId(),
                Collections.singleton(networkName));

        return new PluralMessages().getNetworkInUse(vmNames,
            VdcBllMessages.VAR__ENTITIES__VM,
            VdcBllMessages.VAR__ENTITIES__VMS);
    }

    public ValidationResult notExternalNetwork() {
        return ValidationResult.failWith(VdcBllMessages.EXTERNAL_NETWORK_CANNOT_BE_PROVISIONED)
            .when(getNetwork().isExternal());
    }

    public ValidationResult notManagementNetwork() {
        return getNetworkValidator().notManagementNetwork();
    }

    public ValidationResult networkAttachedToCluster() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CLUSTER)
                .when(getNetworkCluster() == null);
    }

    public ValidationResult ipConfiguredForStaticBootProtocol() {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        return ValidationResult.failWith(VdcBllMessages.NETWORK_ADDR_MANDATORY_IN_STATIC_IP)
            .unless(ipConfiguration == null
                || ipConfiguration.getBootProtocol() != NetworkBootProtocol.STATIC_IP
                || (ipConfiguration.hasPrimaryAddressSet()
                && StringUtils.isNotEmpty(ipConfiguration.getPrimaryAddress().getAddress())
                && StringUtils.isNotEmpty(ipConfiguration.getPrimaryAddress().getNetmask())));
    }

    public ValidationResult bootProtocolSetForDisplayNetwork() {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL)
            .unless(!getNetworkCluster().isDisplay()
                || ipConfiguration != null && ipConfiguration.getBootProtocol() != NetworkBootProtocol.NONE);
    }

    public ValidationResult nicExists() {
        return ValidationResult.failWith(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST)
                .when(attachment.getNicName() == null);
    }

    /**
     * Checks if a network is configured incorrectly:
     * <ul>
     * <li>If the host was added to the system using its IP address as the computer name for the certification creation,
     * it is forbidden to modify the IP address without reinstalling the host.</li>
     * </ul>
     */
    public ValidationResult networkIpAddressWasSameAsHostnameAndChanged(Map<String, VdsNetworkInterface> nics) {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        if (ipConfiguration != null && ipConfiguration.getBootProtocol() == NetworkBootProtocol.STATIC_IP) {
            VdsNetworkInterface existingIface = nics.get(attachment.getNicName());
            if (existingIface != null) {
                String oldAddress = existingIface.getAddress();
                return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED)
                        .when(StringUtils.equals(oldAddress, host.getHostName())
                            && !StringUtils.equals(oldAddress, ipConfiguration.getPrimaryAddress().getAddress()));
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult networkNotChanged(NetworkAttachment oldAttachment) {
        return ValidationResult.failWith(VdcBllMessages.CANNOT_CHANGE_ATTACHED_NETWORK)
                .unless(oldAttachment == null
                        || Objects.equals(oldAttachment.getNetworkId(), attachment.getNetworkId()));
    }

    public ValidationResult validateGateway() {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        return ValidationResult.failWith(VdcBllMessages.NETWORK_ATTACH_ILLEGAL_GATEWAY).when(ipConfiguration != null
            && ipConfiguration.hasPrimaryAddressSet()
            && StringUtils.isNotEmpty(ipConfiguration.getPrimaryAddress().getGateway())
            && !managementNetworkUtil.isManagementNetwork(getNetwork().getId())
            && !FeatureSupported.multipleGatewaysSupported(host.getVdsGroupCompatibilityVersion()));
    }

    public ValidationResult networkNotAttachedToHost() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_ALREADY_ATTACHED_TO_HOST).when(networkAttachedToHost());
    }

    private boolean networkAttachedToHost() {
        List<VDS> hostsAttachedToNetwork = getDbFacade().getVdsDao().getAllForNetwork(attachment.getNetworkId());
        for (VDS hostAttachedToNetwork : hostsAttachedToNetwork) {
            if (hostAttachedToNetwork.getId().equals(host.getId())) {
                return true;
            }
        }

        return false;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    protected Network getNetwork() {
        if (network == null) {
            network = getDbFacade().getNetworkDao().get(attachment.getNetworkId());
        }

        return network;
    }

    private NetworkValidator getNetworkValidator() {
        if (networkValidator == null) {
            networkValidator = new NetworkValidator(getNetwork());
        }

        return networkValidator;
    }

    private NetworkCluster getNetworkCluster() {
        if (networkCluster == null) {
            NetworkClusterId networkClusterId = new NetworkClusterId(host.getVdsGroupId(), getNetwork().getId());
            networkCluster = getDbFacade().getNetworkClusterDao().get(networkClusterId);
        }

        return networkCluster;
    }
}
