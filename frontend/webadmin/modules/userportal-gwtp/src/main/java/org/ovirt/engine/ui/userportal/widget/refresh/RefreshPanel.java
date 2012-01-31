package org.ovirt.engine.ui.userportal.widget.refresh;

import org.ovirt.engine.ui.common.widget.refresh.BaseRefreshPanel;
import org.ovirt.engine.ui.common.widget.table.refresh.AbstractRefreshManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public class RefreshPanel extends BaseRefreshPanel implements HasClickHandlers {

    public interface Resources extends BaseRefreshPanel.BaseResources {

        @Override
        @Source("org/ovirt/engine/ui/userportal/images/check_icon.png")
        ImageResource check_icon();

        @Override
        @Source("org/ovirt/engine/ui/userportal/images/refresh_button.png")
        ImageResource refresh_button();

        @Override
        @Source("org/ovirt/engine/ui/userportal/css/RefreshPanel.css")
        RefreshPanelCss refreshPanelCss();

        @Override
        @Source("org/ovirt/engine/ui/userportal/images/separator.gif")
        @ImageOptions(width = 1, height = 9)
        ImageResource separator();

        @Override
        @Source("org/ovirt/engine/ui/userportal/images/triangle_down.gif")
        @ImageOptions(width = 7, height = 5)
        ImageResource triangle_down();

    }

    protected RefreshPanel(AbstractRefreshManager<RefreshPanel> refreshManager) {
        super(refreshManager);
    }

    @Override
    protected BaseResources createResources() {
        return GWT.create(Resources.class);
    }

}
