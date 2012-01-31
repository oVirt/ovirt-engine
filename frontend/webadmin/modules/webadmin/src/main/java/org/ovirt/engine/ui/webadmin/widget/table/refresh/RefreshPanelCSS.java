package org.ovirt.engine.ui.webadmin.widget.table.refresh;

import org.ovirt.engine.ui.common.widget.refresh.BaseRefreshPanelCss;

public interface RefreshPanelCSS extends BaseRefreshPanelCss {

    @ClassName("refreshButton-down-hovering")
    String refreshButtonDownHovering();

    @ClassName("refreshRateOption-selected")
    String refreshRateOptionSelected();

}
