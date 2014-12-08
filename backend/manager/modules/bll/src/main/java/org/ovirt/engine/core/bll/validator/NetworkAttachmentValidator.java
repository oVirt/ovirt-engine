package org.ovirt.engine.core.bll.validator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.host.HostSetupNetworksValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.PluralMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.ReplacementUtils;


public class NetworkAttachmentValidator {

    public static final String VAR_ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY = "ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY";
    public static final String VAR_ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED_LIST = "ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED_LIST";
    public static final String VAR_NETWORK_ATTACHMENT_ID = "networkAttachmentID";
    private final VdsDao vdsDao;
    private final NetworkDao networkDao;
    private final NetworkAttachmentDao networkAttachmentDao;
    private final NetworkClusterDao networkClusterDao;
    private final VmInterfaceManager vmInterfaceManager;

    private NetworkAttachment attachment;
    private Network network;
    private VDS host;
    private final ManagementNetworkUtil managementNetworkUtil;

    private NetworkCluster networkCluster;
    private NetworkValidator networkValidator;
    private NetworkAttachment existingNetworkAttachment;

    public NetworkAttachmentValidator(NetworkAttachment attachment,
        VDS host,
        ManagementNetworkUtil managementNetworkUtil,
        NetworkAttachmentDao networkAttachmentDao,
        VmInterfaceManager vmInterfaceManager, NetworkClusterDao networkClusterDao,
        NetworkDao networkDao,
        VdsDao vdsDao) {

        this.attachment = attachment;
        this.host = host;
        this.managementNetworkUtil = managementNetworkUtil;
        this.networkAttachmentDao = networkAttachmentDao;
        this.vmInterfaceManager = vmInterfaceManager;
        this.networkClusterDao = networkClusterDao;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
    }

    public ValidationResult networkAttachmentIsSet() {
        //TODO MM: what to do with this? this actually does not mean, that the attachment does not exist, we just did not get one, and we don't even know how one searched for it, so we also don't know what to complain about.
        return ValidationResult.failWith(EngineMessage.NETWORK_ATTACHMENT_NOT_EXISTS,
            HostSetupNetworksValidator.VAR_NETWORK_ATTACHMENT_NOT_EXISTS_ENTITY, "null").when(attachment == null);

    }

    //TODO MM: seems unused.
    private NetworkAttachment getExistingNetworkAttachment() {
        if (existingNetworkAttachment == null) {
            existingNetworkAttachment = networkAttachmentDao.get(attachment.getId());
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
            vmInterfaceManager.findActiveVmsUsingNetworks(host.getId(), Collections.singleton(networkName));

        //TODO MM: this error message seems very crippled & missing some translations.
        return new PluralMessages().getNetworkInUse(vmNames,
            EngineMessage.VAR__ENTITIES__VM,
            EngineMessage.VAR__ENTITIES__VMS);
    }

    public ValidationResult notExternalNetwork() {
        //TODO MM: already used elsewhere, how to fix?
        return ValidationResult.failWith(EngineMessage.EXTERNAL_NETWORK_CANNOT_BE_PROVISIONED)

            .when(getNetwork().isExternal());
    }

    public ValidationResult notRemovingManagementNetwork() {
        return getNetworkValidator().notRemovingManagementNetwork();
    }

    public ValidationResult networkAttachedToCluster() {
        //TODO MM: already used elsewhere, how to fix?

        return ValidationResult.failWith(EngineMessage.NETWORK_NOT_EXISTS_IN_CLUSTER)

                .when(getNetworkCluster() == null);
    }

    public ValidationResult ipConfiguredForStaticBootProtocol() {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();

        boolean failWhen = ipConfiguration != null
            && ipConfiguration.hasPrimaryAddressSet()
            && ipConfiguration.getPrimaryAddress().getBootProtocol() == NetworkBootProtocol.STATIC_IP
            && unsetAddress(ipConfiguration);

        //TODO MM: already used elsewhere, how to fix?
        return ValidationResult.failWith(EngineMessage.NETWORK_ADDR_MANDATORY_IN_STATIC_IP).when(failWhen);

    }

    private boolean unsetAddress(IpConfiguration ipConfiguration) {
        return !ipConfiguration.hasPrimaryAddressSet()
            || StringUtils.isEmpty(ipConfiguration.getPrimaryAddress().getAddress())
            || StringUtils.isEmpty(ipConfiguration.getPrimaryAddress().getNetmask());
    }

    public ValidationResult bootProtocolSetForDisplayNetwork() {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        boolean failWhen = (getNetworkCluster().isDisplay() &&
                (ipConfiguration == null
                        || !ipConfiguration.hasPrimaryAddressSet()
                        || ipConfiguration.getPrimaryAddress().getBootProtocol() == NetworkBootProtocol.NONE));

        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL,
            ReplacementUtils.createSetVariableString(VAR_ACTION_TYPE_FAILED_DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY,
                getNetwork().getName()))

            .when(failWhen);
    }

    public ValidationResult nicExists() {
        //TODO MM: already used elsewhere, how to fix?
        return ValidationResult.failWith(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST)

                .when(attachment.getNicName() == null);
    }

    /**
     * Checks if a network is configured incorrectly:
     * <ul>
     * <li>If the host was added to the system using its IP address as the computer name for the certification creation,
     * it is forbidden to modify the IP address without reinstalling the host.</li>
     * </ul>
     */
    public ValidationResult networkIpAddressWasSameAsHostnameAndChanged(BusinessEntityMap<VdsNetworkInterface> existingInterfaces) {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        if (ipConfiguration != null
                && ipConfiguration.hasPrimaryAddressSet()
                && ipConfiguration.getPrimaryAddress().getBootProtocol() == NetworkBootProtocol.STATIC_IP) {
            VdsNetworkInterface existingIface = existingInterfaces.get(attachment.getNicName());
            if (existingIface != null) {
                String oldAddress = existingIface.getAddress();
                return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED,
                    ReplacementUtils.createSetVariableString(
                        VAR_ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED_LIST,
                        getNetwork().getName()))
                    .when(StringUtils.equals(oldAddress, host.getHostName())
                        && !StringUtils.equals(oldAddress, ipConfiguration.getPrimaryAddress().getAddress()));

            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult networkNotChanged(NetworkAttachment oldAttachment) {
        Guid oldAttachmentId = oldAttachment == null ? null : oldAttachment.getId();
        boolean when = oldAttachment != null &&
            !Objects.equals(oldAttachment.getNetworkId(), attachment.getNetworkId());
        return ValidationResult.failWith(EngineMessage.CANNOT_CHANGE_ATTACHED_NETWORK,
            ReplacementUtils.createSetVariableString(VAR_NETWORK_ATTACHMENT_ID, oldAttachmentId))

            .when(when);
    }

    public ValidationResult validateGateway() {
        IpConfiguration ipConfiguration = attachment.getIpConfiguration();
        //TODO MM: already used elsewhere, how to fix?
        return ValidationResult.failWith(EngineMessage.NETWORK_ATTACH_ILLEGAL_GATEWAY).when(ipConfiguration != null

            && ipConfiguration.hasPrimaryAddressSet()
            && StringUtils.isNotEmpty(ipConfiguration.getPrimaryAddress().getGateway())
            && !managementNetworkUtil.isManagementNetwork(getNetwork().getId())
            && !FeatureSupported.multipleGatewaysSupported(host.getVdsGroupCompatibilityVersion()));
    }

    public ValidationResult networkNotAttachedToHost() {
        return ValidationResult.failWith(EngineMessage.NETWORK_ALREADY_ATTACHED_TO_HOST,
            ReplacementUtils.createSetVariableString("networkName", getNetwork().getName()),
            ReplacementUtils.createSetVariableString("hostName", host.getName())).when(networkAttachedToHost());

    }

    private boolean networkAttachedToHost() {
        List<VDS> hostsAttachedToNetwork = vdsDao.getAllForNetwork(attachment.getNetworkId());
        for (VDS hostAttachedToNetwork : hostsAttachedToNetwork) {
            if (hostAttachedToNetwork.getId().equals(host.getId())) {
                return true;
            }
        }

        return false;
    }

    protected Network getNetwork() {
        if (network == null) {
            network = networkDao.get(attachment.getNetworkId());
        }

        return network;
    }

    NetworkValidator getNetworkValidator() {
        if (networkValidator == null) {
            networkValidator = new NetworkValidator(getNetwork());
        }

        return networkValidator;
    }

    private NetworkCluster getNetworkCluster() {
        if (networkCluster == null) {
            NetworkClusterId networkClusterId = new NetworkClusterId(host.getVdsGroupId(), attachment.getNetworkId());
            networkCluster = networkClusterDao.get(networkClusterId);
        }

        return networkCluster;
    }
}
