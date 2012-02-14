package org.ovirt.engine.ui.common.widget.refresh;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public class RefreshPanel extends BaseRefreshPanel {

    public interface Resources extends BaseRefreshPanel.BaseResources {

        @Override
        @Source("org/ovirt/engine/ui/common/images/check_icon.png")
        ImageResource check_icon();

        @Override
        @Source("org/ovirt/engine/ui/common/images/refresh_button.png")
        ImageResource refresh_button();

        @Override
        @Source("org/ovirt/engine/ui/common/css/RefreshPanel.css")
        RefreshPanelCss refreshPanelCss();

        @Override
        @Source("org/ovirt/engine/ui/common/images/separator.gif")
        @ImageOptions(width = 1, height = 9)
        ImageResource separator();

        @Override
        @Source("org/ovirt/engine/ui/common/images/triangle_down.gif")
        @ImageOptions(width = 7, height = 5)
        ImageResource triangle_down();

    }

    public RefreshPanel(AbstractRefreshManager<RefreshPanel> refreshManager) {
        super(refreshManager);
    }

    @Override
    protected BaseResources createResources() {
        return GWT.create(Resources.class);
    }

}
