package org.ovirt.engine.ui.userportal.widget.refresh;

import org.ovirt.engine.ui.common.widget.refresh.BaseRefreshPanelCss;

public interface RefreshPanelCss extends BaseRefreshPanelCss {

    @Override
    @ClassName("refreshButton-down-hovering")
    String refreshButtonDownHovering();

    @Override
    @ClassName("refreshRateOption-selected")
    String refreshRateOptionSelected();

}
