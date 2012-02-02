package org.ovirt.engine.ui.webadmin.widget.refresh;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.uicommonweb.models.GridController;

public class RefreshManager extends AbstractRefreshManager<RefreshPanel> {

    /**
     * Create a Manager for the specified {@link GridController}.
     */
    public RefreshManager(GridController controller, ClientStorage clientStorage) {
        super(controller, clientStorage);
    }

    @Override
    protected RefreshPanel createRefreshPane() {
        return new RefreshPanel(this);
    }
}
