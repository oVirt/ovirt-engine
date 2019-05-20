package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

public class AffinityLabelSelectionWidget extends AbstractItemSelectionWidget<Label> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public AffinityLabelSelectionWidget() {
        filterListLabel.setText(constants.affinityLabelsDropDownInstruction());
    }
}
