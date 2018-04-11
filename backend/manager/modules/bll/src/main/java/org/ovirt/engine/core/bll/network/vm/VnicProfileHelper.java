package org.ovirt.engine.core.bll.network.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;
import org.ovirt.engine.core.di.Injector;

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
     * @param vmInterface
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
                    || (user != null
                            && getBackwardCompatibilityVnicHelper().isVnicProfilePermitted(user, profile, false))) {
                return profile;
            }
        }

        return null;
    }

    BackwardCompatibilityVnicHelper getBackwardCompatibilityVnicHelper() {
        return Injector.get(BackwardCompatibilityVnicHelper.class);
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

    private void markNicHasNoProfile(VmNetworkInterface iface) {
        invalidNetworkNames.add(iface.getNetworkName());
        invalidIfaceNames.add(iface.getName());
        iface.setVnicProfileId(null);
    }

    public void auditInvalidInterfaces(String entityName) {
        if (!invalidNetworkNames.isEmpty()) {
            AuditLogable logable = new AuditLogableImpl();
            logable.addCustomValue("EntityName", entityName);
            logable.addCustomValue("Networks", StringUtils.join(invalidNetworkNames, ','));
            logable.addCustomValue("Interfaces", StringUtils.join(invalidIfaceNames, ','));
            createAuditLogDirector().log(logable, logType);
        }
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

    NetworkDao getNetworkDao() {
        return Injector.get(NetworkDao.class);
    }

    private VnicProfileViewDao getVnicProfileViewDao() {
        return Injector.get(VnicProfileViewDao.class);
    }

    private VnicProfileDao getVnicProfileDao() {
        return Injector.get(VnicProfileDao.class);
    }

    AuditLogDirector createAuditLogDirector() {
        return Injector.get(AuditLogDirector.class);
    }
}
