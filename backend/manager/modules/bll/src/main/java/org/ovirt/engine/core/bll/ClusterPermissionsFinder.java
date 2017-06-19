package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.compat.Guid;

@Singleton
public class ClusterPermissionsFinder {

    public List<PermissionSubject> findPermissionCheckSubjects(Guid clusterId, ActionType actionType) {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(clusterId,
                VdcObjectType.Cluster,
                actionType.getActionGroup()));
        return permissionList;
    }
}
