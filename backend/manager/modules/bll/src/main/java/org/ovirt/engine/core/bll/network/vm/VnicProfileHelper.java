package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class VnicProfileHelper {
    private Set<String> invalidNetworkNames = new HashSet<>();
    private List<String> invalidIfaceNames = new ArrayList<>();
    private Map<String, Network> networksInClusterByName;
    private List<VnicProfileView> vnicProfilesInDc;
    private Guid clusterId;
    private Guid dataCenterId;
    private AuditLogType logType;

    public VnicProfileHelper(Guid clusterId, Guid dataCenterId, AuditLogType logType) {
        this.clusterId = clusterId;
        this.dataCenterId = dataCenterId;
        this.logType = logType;
    }

    /**
     * Updates the vnic profile id of a given {@code VmNic} by a network name and vnic profile name and marks the vnic
     * as invalid if no match for vnic profile found.
     *
     * @param iface
     *            The vm network interface to be updated
     * @param user
     *            The user which performs the action
     */
    public void updateNicWithVnicProfileForUser(VmNetworkInterface vmInterface, DbUser user) {
        if (!updateNicWithVnicProfile(vmInterface, user)) {
            markNicHasNoProfile(vmInterface);
        }
    }

    /**
     * Updates the vnic profile id of a given {@code VmNic} by a network name and vnic profile name.
     *
     * @param iface
     *            The vm network interface to be updated
     * @param user
     *            The user which performs the action
     * @return {@code true} if the vnic profile id is updated, else {@code false}
     */
    private boolean updateNicWithVnicProfile(VmNetworkInterface iface, DbUser user) {

        if (iface.getNetworkName() == null) {
            iface.setVnicProfileId(null);
            return true;
        }

        Network network = getNetworksInCluster().get(iface.getNetworkName());
        if (network == null || !network.isVmNetwork()) {
            return false;
        }

        VnicProfile vnicProfile = getVnicProfileForNetwork(network, iface.getVnicProfileName());
        if (vnicProfile == null) {
            vnicProfile = findVnicProfileForUser(user, network);
            if (vnicProfile == null) {
                return false;
            }
        }

        iface.setVnicProfileId(vnicProfile.getId());
        return true;
    }

    private VnicProfile findVnicProfileForUser(DbUser user, Network network) {
        List<VnicProfile> networkProfiles = getVnicProfileDao().getAllForNetwork(network.getId());

        for (VnicProfile profile : networkProfiles) {
            if ((user == null && !profile.isPortMirroring())
                    || (user != null && isVnicProfilePermitted(user, profile, false))) {
                return profile;
            }
        }

        return null;
    }

    private VnicProfile getVnicProfileForNetwork(Network network, String vnicProfileName) {

        if (vnicProfileName == null) {
            return null;
        }

        for (VnicProfileView vnicProfile : getVnicProfilesInDc()) {
            if (Objects.equals(vnicProfile.getNetworkId(), network.getId())
                    && vnicProfileName.equals(vnicProfile.getName())) {
                return vnicProfile;
            }
        }

        return null;
    }

    private static boolean isVnicProfilePermitted(DbUser user, VnicProfile profile, boolean portMirroringRequired) {
        return portMirroringRequired == profile.isPortMirroring()
                && getPermissionDao().getEntityPermissions(user.getId(),
                        ActionGroup.CONFIGURE_VM_NETWORK,
                        profile.getId(),
                        VdcObjectType.VnicProfile) != null;
    }

    private void markNicHasNoProfile(VmNetworkInterface iface) {
        invalidNetworkNames.add(iface.getNetworkName());
        invalidIfaceNames.add(iface.getName());
        iface.setVnicProfileId(null);
    }

    public void auditInvalidInterfaces(String entityName) {
        if (!invalidNetworkNames.isEmpty()) {
            AuditLogableBase logable = new AuditLogableBase();
            logable.addCustomValue("EntityName", entityName);
            logable.addCustomValue("Networks", StringUtils.join(invalidNetworkNames, ','));
            logable.addCustomValue("Interfaces", StringUtils.join(invalidIfaceNames, ','));
            new AuditLogDirector().log(logable, logType);
        }
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
    public static ValidationResult updateNicForBackwardCompatibility(VmNic nic,
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
            VnicProfile oldProfile = getVnicProfileDao().get(oldNic.getVnicProfileId());
            Network oldNetwork = getNetworkDao().get(oldProfile.getNetworkId());
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
        Network network = getNetworkDao().getByNameAndCluster(networkName, vm.getClusterId());
        if (network == null) {
            return networkOfGivenNameNotExistsInCluster(networkName);
        }

        List<VnicProfile> vnicProfiles = getVnicProfileDao().getAllForNetwork(network.getId());
        for (VnicProfile profile : vnicProfiles) {
            if (isVnicProfilePermitted(user, profile, portMirroring)) {
                nic.setVnicProfileId(profile.getId());
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_FIND_VNIC_PROFILE_FOR_NETWORK);
    }

    private static ValidationResult networkOfGivenNameNotExistsInCluster(String networkName) {
        EngineMessage engineMessage = EngineMessage.NETWORK_OF_GIVEN_NAME_NOT_EXISTS_IN_CLUSTER;
        return new ValidationResult(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, networkName));
    }

    private Map<String, Network> getNetworksInCluster() {
        if (networksInClusterByName == null) {
            if (clusterId != null) {
                networksInClusterByName = Entities.entitiesByName(getNetworkDao().getAllForCluster(clusterId));
            } else {
                networksInClusterByName = new HashMap<>();
            }
        }

        return networksInClusterByName;
    }

    private List<VnicProfileView> getVnicProfilesInDc() {
        if (vnicProfilesInDc == null) {
            vnicProfilesInDc = getVnicProfileViewDao().getAllForDataCenter(dataCenterId);
        }

        return vnicProfilesInDc;
    }

    private static NetworkDao getNetworkDao() {
        return DbFacade.getInstance().getNetworkDao();
    }

    private VnicProfileViewDao getVnicProfileViewDao() {
        return DbFacade.getInstance().getVnicProfileViewDao();
    }

    private static VnicProfileDao getVnicProfileDao() {
        return DbFacade.getInstance().getVnicProfileDao();
    }

    private static PermissionDao getPermissionDao() {
        return DbFacade.getInstance().getPermissionDao();
    }
}
