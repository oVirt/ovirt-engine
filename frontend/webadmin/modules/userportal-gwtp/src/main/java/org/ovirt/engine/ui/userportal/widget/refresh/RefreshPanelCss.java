package org.ovirt.engine.ui.userportal.widget.refresh;

import org.ovirt.engine.ui.common.widget.refresh.BaseRefreshPanelCss;

public interface RefreshPanelCss extends BaseRefreshPanelCss {
    @ClassName("refreshButton-down-hovering")
    String refreshButtonDownHovering();

    @ClassName("refreshRateOption-selected")
    String refreshRateOptionSelected();
}
