package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class AffinityGroupListWidget extends AbstractItemListWidget<AffinityGroup> {
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    @Override
    public void init(ListModel<AffinityGroup> model) {
        super.init(model);
        itemListLabel.setText(constants.selectedAffinityGroups());
    }

    @Override
    protected String noItemsText() {
        return constants.noAffinityGroupsSelected();
    }
}
