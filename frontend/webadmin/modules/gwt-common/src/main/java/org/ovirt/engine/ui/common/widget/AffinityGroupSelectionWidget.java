package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

public class AffinityGroupSelectionWidget extends AbstractItemSelectionWidget<AffinityGroup> {
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public AffinityGroupSelectionWidget() {
        filterListLabel.setText(constants.affinityGroupsDropDownInstruction());
    }
}
