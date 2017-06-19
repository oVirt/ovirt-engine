package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;

@Singleton
class AttachNetworkClusterPermissionsChecker {

    public boolean checkPermissions(CommandBase<?> command, List<PermissionSubject> permissionCheckSubjects) {
        final List<String> messages = new ArrayList<>();

        for (PermissionSubject permSubject : permissionCheckSubjects) {
            messages.clear();
            if (command.checkSinglePermission(permSubject, messages)) {
                return true;
            }
        }

        command.getReturnValue().getValidationMessages().addAll(messages);
        return false;
    }

    public boolean checkPermissions(
            CommandBase<?> command,
            NetworkCluster attachment,
            ActionType actionType) {
        final List<PermissionSubject> permissionCheckSubjects = findPermissionCheckSubjects(attachment, actionType);

        return checkPermissions(command, permissionCheckSubjects);
    }

    public List<PermissionSubject> findPermissionCheckSubjects(
            NetworkCluster networkCluster,
            ActionType actionType) {

        final List<PermissionSubject> permissions = new ArrayList<>();

        final Guid networkId = networkCluster == null ? null : networkCluster.getNetworkId();
        // require permissions on network
        permissions.add(new PermissionSubject(networkId,
                VdcObjectType.Network,
                actionType.getActionGroup()));

        return permissions;
    }
}
