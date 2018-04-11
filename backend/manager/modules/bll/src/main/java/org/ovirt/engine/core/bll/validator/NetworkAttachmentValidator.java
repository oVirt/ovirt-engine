package org.ovirt.engine.core.bll.validator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class NetworkAttachmentValidator {

    public static final String VAR_ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY = "ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY";
    public static final String VAR_NETWORK_ATTACHMENT_ID = "networkAttachmentID";
    public static final String VAR_NETWORK_NAME = "networkName";

    private final VdsDao vdsDao;
    private final NetworkDao networkDao;
    private final NetworkClusterDao networkClusterDao;

    private final NetworkAttachment attachment;
    private final VDS host;
    private Network network;

    private NetworkCluster networkCluster;
    private NetworkValidator networkValidator;

    public NetworkAttachmentValidator(NetworkAttachment attachment,
            VDS host,
            NetworkClusterDao networkClusterDao,
            NetworkDao networkDao,
            VdsDao vdsDao) {

        this.attachment = attachment;
        this.host = host;
        this.networkClusterDao = networkClusterDao;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
    }

    public ValidationResult networkAttachmentIsSet() {
        EngineMessage engineMessage = EngineMessage.NULL_PASSED_AS_NETWORK_ATTACHMENT;
        return ValidationResult.failWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, null))
            .when(attachment == null);
    }

    public ValidationResult networkExists() {
        Guid networkId = attachment.getNetworkId();
        String networkName = attachment.getNetworkName();

        /*
         * following code relies on fact, that completors were ran and fixed the user input, so we need to consider
         * what original user input looked like, and how it was altered by completors.
         */

        // User did not specify neither id nor name.
        if (networkId ==null && networkName == null) {
            return new ValidationResult(EngineMessage.NETWORK_ATTACHMENT_NETWORK_ID_OR_NAME_IS_NOT_SET);
        }

        // User specified id, for which completors did not find Network record.
        if (networkId != null && networkName == null) {
            EngineMessage engineMessage = EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS;
            return new ValidationResult(engineMessage,
                    ReplacementUtils.getVariableAssignmentString(engineMessage, networkId.toString()));
        }

        // User specified name, for which completors did not find Network record.
        if (networkId == null && networkName != null) {
            EngineMessage engineMessage = EngineMessage.NETWORK_HAVING_NAME_NOT_EXISTS;
            return new ValidationResult(engineMessage,
                    ReplacementUtils.getVariableAssignmentString(engineMessage, networkName));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult notExternalNetwork() {
        EngineMessage engineMessage = EngineMessage.EXTERNAL_NETWORK_HAVING_NAME_CANNOT_BE_PROVISIONED;
        return ValidationResult.failWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, getNetwork().getName()))

            .when(getNetwork().isExternal());
    }

    public ValidationResult notRemovingManagementNetwork() {
        return getNetworkValidator().notRemovingManagementNetwork();
    }

    public ValidationResult networkAttachedToCluster() {
        EngineMessage engineMessage = EngineMessage.NETWORK_OF_GIVEN_NAME_NOT_EXISTS_IN_CLUSTER;
        return ValidationResult.failWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, this.attachment.getNetworkName()))
            .when(getNetworkCluster() == null);
    }

    public ValidationResult bootProtocolSetForRoleNetwork() {
        NetworkCluster networkCluster = getNetworkCluster();
        IsRoleNetworkIpConfigurationValid isRoleNetworkIpConfigurationValid = new IsRoleNetworkIpConfigurationValid(networkCluster);
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL,
                ReplacementUtils.createSetVariableString(
                        VAR_ACTION_TYPE_FAILED_ROLE_NETWORK_HAS_NO_BOOT_PROTOCOL_ENTITY,
                        getNetwork().getName()))
                .unless(isRoleNetworkIpConfigurationValid.validate(attachment));
    }

    public ValidationResult nicNameIsSet() {
        return ValidationResult.failWith(EngineMessage.HOST_NETWORK_INTERFACE_DOES_NOT_HAVE_NAME_SET)
                .when(attachment.getNicName() == null && attachment.getNicId() == null);
    }

    public ValidationResult networkNotChanged(NetworkAttachment oldAttachment) {
        Guid oldAttachmentId = oldAttachment == null ? null : oldAttachment.getId();
        boolean when = oldAttachment != null &&
            !Objects.equals(oldAttachment.getNetworkId(), attachment.getNetworkId());
        return ValidationResult.failWith(EngineMessage.CANNOT_CHANGE_ATTACHED_NETWORK,
            ReplacementUtils.createSetVariableString(VAR_NETWORK_ATTACHMENT_ID, oldAttachmentId))
            .when(when);
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

    public ValidationResult existingAttachmentIsReused(Map<Guid, NetworkAttachment> existingAttachmentsByNetworkId) {
        NetworkAttachment existingAttachmentWithTheSameNetwork =
                existingAttachmentsByNetworkId.get(attachment.getNetworkId());

        if (existingAttachmentWithTheSameNetwork == null) {
            return ValidationResult.VALID;
        }

        return ValidationResult.failWith(EngineMessage.ATTACHMENT_IS_NOT_REUSED,
                ReplacementUtils.createSetVariableString(VAR_NETWORK_ATTACHMENT_ID,
                        existingAttachmentWithTheSameNetwork.getId()),
                ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME,
                        existingAttachmentWithTheSameNetwork.getNetworkName()))
                .unless(existingAttachmentWithTheSameNetwork.getId().equals(attachment.getId()));
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
            NetworkClusterId networkClusterId = new NetworkClusterId(host.getClusterId(), attachment.getNetworkId());
            networkCluster = networkClusterDao.get(networkClusterId);
        }

        return networkCluster;
    }
}
