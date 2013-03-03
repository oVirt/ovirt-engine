package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;

/**
 * Class to hold common static methods that are used in several different places.
 */
public class NetworkHelper {

    /**
     * Grants permissions on the network entity to the given user and for 'everyone' user if publicUse is set to
     * <code>true</code>
     *
     * @param userId
     *            the ID of the user to get the permission
     * @param networkId
     *            the network ID
     * @param publicUse
     *            Indicates of the network is intended for a public user
     */
    public static void addPermissions(Guid userId, Guid networkId, boolean publicUse) {
        addPermission(userId, networkId, PredefinedRoles.NETWORK_ADMIN);

        // if the Network is for public use, set EVERYONE as a NETWORK_USER.
        if (publicUse) {
            addPermission(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID,
                    networkId,
                    PredefinedRoles.NETWORK_USER);
        }
    }

    private static void addPermission(Guid userId, Guid networkId, PredefinedRoles role) {
        permissions perms = new permissions();
        perms.setad_element_id(userId);
        perms.setObjectType(VdcObjectType.Network);
        perms.setObjectId(networkId);
        perms.setrole_id(role.getId());
        MultiLevelAdministrationHandler.addPermission(perms);
    }
}
