package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.common.widget.refresh.RefreshPanel;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.userportal.SideTabWithDetailsViewStyle;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable.Resources;

public class UserPortalSimpleActionTable<T> extends SimpleActionTable<T> {

    private static final SideTabWithDetailsViewStyle style;

    static {
        // it has to be static - the parent constructor invokes
        // the getBarPanelStyleName and getTableContainerStyleName
        // which already needs the style prepared
        style = ClientGinjectorProvider.instance()
                .getApplicationResources()
                .sideTabWithDetailsViewStyle();
        style.ensureInjected();
    }

    public UserPortalSimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(dataProvider, resources, eventBus, clientStorage);
    }

    public UserPortalSimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources,
            EventBus eventBus,
            AbstractRefreshManager<RefreshPanel> refreshManager) {
        super(dataProvider, resources, eventBus, refreshManager);
    }

    @Override
    protected String getBarPanelStyleName() {
        return style.obrand_mainBarPanel();
    }

    @Override
    protected String getTableContainerStyleName() {
        return style.mainContentPanel();
    }

}
