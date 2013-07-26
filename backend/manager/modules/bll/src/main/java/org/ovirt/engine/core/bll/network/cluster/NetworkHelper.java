package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Class to hold common static methods that are used in several different places.
 */
public class NetworkHelper {

    /**
     * Grants permissions on the network entity to the given user
     *
     * @param userId
     *            the ID of the user to get the permission
     * @param networkId
     *            the Network ID
     */
    public static void addPermissionsOnNetwork(Guid userId, Guid networkId) {
        addPermission(userId, networkId, PredefinedRoles.NETWORK_ADMIN);
    }

    /**
     * Grants permissions on the vnic profile entity to its creator and usage permission to 'everyone' if publicUse is
     * set to <code>true</code>
     *
     * @param userId
     *            the ID of the user to get the permission
     * @param vnicProfileId
     *            the VNIC Profile
     * @param publicUse
     *            Indicates of the network is intended for a public user
     */
    public static void addPermissionsOnVnicProfile(Guid userId, Guid vnicProfileId, boolean publicUse) {
        addPermission(userId, vnicProfileId, PredefinedRoles.NETWORK_ADMIN);

        // if the profile is for public use, set EVERYONE as a VNICProfileUser on the profile
        if (publicUse) {
            addPermission(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID,
                    vnicProfileId,
                    PredefinedRoles.VNIC_PROFILE_USER);
        }
    }

    public static VnicProfile createVnicProfile(Network net) {
        VnicProfile profile = new VnicProfile();
        profile.setId(Guid.newGuid());
        profile.setName(net.getName());
        profile.setNetworkId(net.getId());
        profile.setPortMirroring(false);
        return profile;
    }

    private static void addPermission(Guid userId, Guid entityId, PredefinedRoles role) {
        permissions perms = new permissions();
        perms.setad_element_id(userId);
        perms.setObjectType(VdcObjectType.Network);
        perms.setObjectId(entityId);
        perms.setrole_id(role.getId());
        MultiLevelAdministrationHandler.addPermission(perms);
    }

    public static Network getNetworkByVnicProfileId(Guid vnicProfileId) {
        if (vnicProfileId == null) {
            return null;
        }

        Network retVal = null;
        VnicProfile vnicProfile = DbFacade.getInstance().getVnicProfileDao().get(vnicProfileId);
        if (vnicProfile.getNetworkId() != null) {
            retVal = DbFacade.getInstance().getNetworkDao().get(vnicProfile.getNetworkId());
        }
        return retVal;
    }

    public static boolean isNetworkInCluster(Network network, Guid clusterId) {
        List<Network> networks = DbFacade.getInstance().getNetworkDao().getAllForCluster(clusterId);
        for (Network clusterNetwork : networks) {
            if (clusterNetwork.getId().equals(network.getId())) {
                return true;
            }
        }

        return false;
    }
}
