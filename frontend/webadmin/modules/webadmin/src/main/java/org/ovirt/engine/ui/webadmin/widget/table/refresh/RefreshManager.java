package org.ovirt.engine.ui.webadmin.widget.table.refresh;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.uicommonweb.models.GridController;

public class RefreshManager extends AbstractRefreshManager {

    private final RefreshPanel refreshPanel;

    /**
     * Create a Manager for the specified {@link GridController}
     */
    public RefreshManager(GridController controller, ClientStorage clientStorage) {
        super(controller, clientStorage);
        this.refreshPanel = new RefreshPanel(this);
    }

    @Override
    protected void onRefresh(String status) {
        refreshPanel.showStatus(status);
    }

    public RefreshPanel getRefreshPanel() {
        return refreshPanel;
    }

}
