package org.ovirt.engine.ui.uicommonweb.models.configure.labels.model;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditAffinityLabelModel extends AffinityLabelModel {

    public EditAffinityLabelModel(Label affinityLabel,
                                  ListModel<?> sourceListModel,
                                  Guid clusterId,
                                  String clusterName,
                                  boolean affinityGroupAvailable) {
        super(affinityLabel, sourceListModel, ActionType.UpdateLabel, clusterId, clusterName, affinityGroupAvailable);
        setTitle(ConstantsManager.getInstance().getConstants().editAffinityLabelTitle());
        setHelpTag(HelpTag.edit_affinity_label);
        setHashName("edit_affinity_label"); //$NON-NLS-1$

        getName().setEntity(getAffinityLabel().getName());
        getImplicitAffinityGroup().setEntity(getAffinityLabel().isImplicitAffinityGroup());
    }

}
