package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewAffinityGroupModel extends AffinityGroupModel {

    public NewAffinityGroupModel(AffinityGroup affinityGroup,
            ListModel<?> sourceListModel,
            Guid clusterId,
            String clusterName) {
        super(affinityGroup, sourceListModel, ActionType.AddAffinityGroup, clusterId, clusterName);

        setTitle(ConstantsManager.getInstance().getConstants().newAffinityGroupsTitle());
        setHelpTag(HelpTag.new_affinity_group);
        setHashName("new_affinity_group"); //$NON-NLS-1$
    }

}
