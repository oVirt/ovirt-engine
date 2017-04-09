package org.ovirt.engine.core.bll.network.vm;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@Singleton
public class BackwardCompatibilityVnicHelper {
    private final PermissionDao permissionDao;
    private final VnicProfileDao vnicProfileDao;
    private final NetworkDao networkDao;

    @Inject
    BackwardCompatibilityVnicHelper(PermissionDao permissionDao,
            VnicProfileDao vnicProfileDao,
            NetworkDao networkDao) {
        this.permissionDao = Objects.requireNonNull(permissionDao);
        this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
        this.networkDao = Objects.requireNonNull(networkDao);
    }

    public boolean isVnicProfilePermitted(DbUser user, VnicProfile profile, boolean portMirroringRequired) {
        return portMirroringRequired == profile.isPortMirroring()
                && permissionDao.getEntityPermissions(user.getId(),
                        ActionGroup.CONFIGURE_VM_NETWORK,
                        profile.getId(),
                        VdcObjectType.VnicProfile) != null;
    }

    /**
     * Since the network name and port mirroring attributed were replaced on the {@link VmNic} with the vnic profile id,
     * a certain logic should be applied to translate a given usage of the former api to the expected one.
     *
     * @param nic
     *            the candidate nic to apply the logic for
     * @param oldNic
     *            the existing nic
     * @param networkName
     *            the network name to be configured for the nic
     * @param portMirroring
     *            indicator if port mirroring should be configured for the network
     * @param vm
     *            the vm which contains the nic
     * @param user
     *            the user which execute the action
     */
    public ValidationResult updateNicForBackwardCompatibility(VmNic nic,
            VmNic oldNic,
            String networkName,
            boolean portMirroring,
            VmBase vm,
            DbUser user) {

        // if network wasn't provided, no need for backward compatibility logic
        if (networkName == null) {
            return ValidationResult.VALID;
        }

        // if the network was provided but unchanged, use the provided vnic profile id
        if (oldNic != null && oldNic.getVnicProfileId() != null) {
            VnicProfile oldProfile = vnicProfileDao.get(oldNic.getVnicProfileId());
            Network oldNetwork = networkDao.get(oldProfile.getNetworkId());
            if (StringUtils.equals(networkName, oldNetwork.getName())) {
                return ValidationResult.VALID;
            }
        }

        // empty network name is considered as an empty (unlinked) network
        if ("".equals(networkName)) {
            if (portMirroring) {
                return new ValidationResult(EngineMessage.PORT_MIRRORING_REQUIRES_NETWORK);
            } else {
                nic.setVnicProfileId(null);
                return ValidationResult.VALID;
            }
        }

        if (vm.getClusterId() == null) {
            return networkOfGivenNameNotExistsInCluster(networkName);
        }

        // if the network was provided with changed name, resolve a suitable profile for it
        Network network = networkDao.getByNameAndCluster(networkName, vm.getClusterId());
        if (network == null) {
            return networkOfGivenNameNotExistsInCluster(networkName);
        }

        List<VnicProfile> vnicProfiles = vnicProfileDao.getAllForNetwork(network.getId());
        for (VnicProfile profile : vnicProfiles) {
            if (isVnicProfilePermitted(user, profile, portMirroring)) {
                nic.setVnicProfileId(profile.getId());
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_FIND_VNIC_PROFILE_FOR_NETWORK);
    }

    private ValidationResult networkOfGivenNameNotExistsInCluster(String networkName) {
        EngineMessage engineMessage = EngineMessage.NETWORK_OF_GIVEN_NAME_NOT_EXISTS_IN_CLUSTER;
        return new ValidationResult(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, networkName));
    }
}
