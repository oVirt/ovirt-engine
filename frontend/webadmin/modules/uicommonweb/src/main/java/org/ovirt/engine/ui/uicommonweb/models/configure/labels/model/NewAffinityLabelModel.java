package org.ovirt.engine.ui.uicommonweb.models.configure.labels.model;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewAffinityLabelModel extends AffinityLabelModel {

    public NewAffinityLabelModel(Label affinityLabel,
                                 ListModel<?> sourceListModel,
                                 Guid clusterId,
                                 String clusterName) {
        super(affinityLabel, sourceListModel, VdcActionType.AddLabel, clusterId, clusterName);
        setTitle(ConstantsManager.getInstance().getConstants().newAffinityLabelTitle());
        setHelpTag(HelpTag.new_affinity_label);
        setHashName("new_affinity_label"); //$NON-NLS-1$
    }

}
