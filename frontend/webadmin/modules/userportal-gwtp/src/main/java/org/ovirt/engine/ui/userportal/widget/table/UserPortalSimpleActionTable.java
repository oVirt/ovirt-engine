package org.ovirt.engine.ui.userportal.widget.table;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.common.widget.refresh.RefreshPanel;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.SideTabWithDetailsViewStyle;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable.Resources;

public class UserPortalSimpleActionTable<T> extends SimpleActionTable<T> {

    private static final SideTabWithDetailsViewStyle style;

    private static final ApplicationResources resources = AssetProvider.getResources();

    static {
        // it has to be static - the parent constructor invokes
        // the getBarPanelStyleName and getTableContainerStyleName
        // which already needs the style prepared
        style = resources.sideTabWithDetailsViewStyle();
        style.ensureInjected();
    }

    public UserPortalSimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
                                       Resources resources,
                                       Resources headerResources,
                                       EventBus eventBus,
                                       ClientStorage clientStorage) {
        super(dataProvider, resources, headerResources, eventBus, clientStorage);
    }

    public UserPortalSimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources,
            EventBus eventBus,
            ClientStorage clientStorage,
            AbstractRefreshManager<RefreshPanel> refreshManager) {
        super(dataProvider, resources, eventBus, clientStorage, refreshManager);
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
