package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditAffinityGroupModel extends AffinityGroupModel {

    public EditAffinityGroupModel(AffinityGroup affinityGroup,
            ListModel<?> sourceListModel,
            Guid clusterId,
            String clusterName) {
        super(affinityGroup, sourceListModel, VdcActionType.EditAffinityGroup, clusterId, clusterName);

        setTitle(ConstantsManager.getInstance().getConstants().editAffinityGroupsTitle());
        setHashName("edit_affinity_group"); //$NON-NLS-1$

        getName().setEntity(getAffinityGroup().getName());
        getPositive().setEntity(getAffinityGroup().isPositive());
        getEnforcing().setEntity(getAffinityGroup().isEnforcing());
    }

}
