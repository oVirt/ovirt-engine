package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.ShowHideVfPresenterWidget;

public class ShowHideVfButtonView extends ToggleActionButtonView implements ShowHideVfPresenterWidget.ViewDef {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public ShowHideVfButtonView() {
        super(constants.showVfLabel(), constants.hideVfLabel());
    }
}
