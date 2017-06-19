package org.ovirt.engine.core.bll.network.cluster;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.compat.Guid;

@Singleton
class DetachNetworkClusterPermissionFinder {

    public List<PermissionSubject> findPermissionCheckSubjects(Guid networkId, ActionType actionType) {

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                actionType.getActionGroup()));
    }

}
