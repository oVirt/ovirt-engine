package org.ovirt.engine.ui.webadmin.widget.refresh;

import org.ovirt.engine.ui.common.widget.refresh.BaseRefreshPanelCss;

public interface RefreshPanelCSS extends BaseRefreshPanelCss {

    @Override
    @ClassName("refreshButton-down-hovering")
    String refreshButtonDownHovering();

    @Override
    @ClassName("refreshRateOption-selected")
    String refreshRateOptionSelected();

}
