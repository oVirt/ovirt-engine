package org.ovirt.engine.ui.common.view;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.ExpandAllButtonPresenterWidget;

public class ExpandAllButtonView extends ToggleActionButtonView implements ExpandAllButtonPresenterWidget.ViewDef {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public ExpandAllButtonView() {
        super(constants.expandAll(), constants.collapseAll(), IconType.ANGLE_DOUBLE_DOWN, IconType.ANGLE_DOUBLE_RIGHT);
    }

}
