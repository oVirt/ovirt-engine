package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

public class VnicProfileHelper {
    private Set<String> invalidNetworkNames = new HashSet<>();
    private List<String> invalidIfaceNames = new ArrayList<>();
    private Map<String, Network> networksInClusterByName;
    private List<VnicProfileView> vnicProfilesInDc;
    private Version compatibilityVersion;
    private Guid clusterId;
    private Guid dataCenterId;
    private AuditLogType logType;

    public VnicProfileHelper(Guid clusterId, Guid dataCenterId, Version compatibilityVersion, AuditLogType logType) {
        this.clusterId = clusterId;
        this.dataCenterId = dataCenterId;
        this.compatibilityVersion = compatibilityVersion;
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
    public void updateNicWithVnicProfileForUser(VmNetworkInterface vmInterface, VdcUser user) {
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
    private boolean updateNicWithVnicProfile(VmNetworkInterface iface, VdcUser user) {

        if (iface.getNetworkName() == null) {
            if (FeatureSupported.networkLinking(compatibilityVersion)) {
                iface.setVnicProfileId(null);
                return true;
            } else {
                return false;
            }
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

    private VnicProfile findVnicProfileForUser(VdcUser user, Network network) {
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
            if (ObjectUtils.equals(vnicProfile.getNetworkId(), network.getId())
                    && vnicProfileName.equals(vnicProfile.getName())) {
                return vnicProfile;
            }
        }

        return null;
    }

    private static boolean isVnicProfilePermitted(VdcUser user, VnicProfile profile, boolean portMirroringRequired) {
        return portMirroringRequired == profile.isPortMirroring()
                && getPermissionDAO().getEntityPermissions(user.getUserId(),
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
            AuditLogDirector.log(logable, logType);
        }
    }

    public static ValidationResult updateNicForBackwardCompatibility(VmNic nic,
            String networkName,
            boolean portMirroring,
            VmBase vm,
            VdcUser user) {

        if (networkName == null) {
            return ValidationResult.VALID;
        }

        // empty network name is considered as an empty network
        if ("".equals(networkName)) {
            if (portMirroring) {
                return new ValidationResult(VdcBllMessages.PORT_MIRRORING_REQUIRES_NETWORK);
            } else {
                nic.setVnicProfileId(null);
                return ValidationResult.VALID;
            }
        }

        Network network = getNetworkDao().getByNameAndCluster(networkName, vm.getVdsGroupId());
        if (network == null) {
            return new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CLUSTER);
        }

        List<VnicProfile> vnicProfiles = getVnicProfileDao().getAllForNetwork(network.getId());
        for (VnicProfile profile : vnicProfiles) {
            if (isVnicProfilePermitted(user, profile, portMirroring)) {
                nic.setVnicProfileId(profile.getId());
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_FIND_VNIC_PROFILE_FOR_NETWORK);
    }

    private Map<String, Network> getNetworksInCluster() {
        if (networksInClusterByName == null) {
            networksInClusterByName = Entities.entitiesByName(getNetworkDao().getAllForCluster(clusterId));
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

    private static PermissionDAO getPermissionDAO() {
        return DbFacade.getInstance().getPermissionDao();
    }
}
