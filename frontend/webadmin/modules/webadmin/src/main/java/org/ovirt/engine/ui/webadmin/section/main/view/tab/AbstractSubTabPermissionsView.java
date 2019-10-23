package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionWithInheritedPermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public abstract class AbstractSubTabPermissionsView<I, M extends ListWithDetailsModel> extends AbstractSubTabTableWidgetView<I, Permission, M, PermissionListModel<I>> {

    @Inject
    public AbstractSubTabPermissionsView(
            SearchableDetailModelProvider<Permission, M, PermissionListModel<I>> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, PermissionActionPanelPresenterWidget<I, ?, PermissionListModel<I>> actionPanel) {
        super(new PermissionWithInheritedPermissionListModelTable<>(
                modelProvider, eventBus, actionPanel, clientStorage));
        generateIds();
        initTable();
        initWidget(getModelBoundTableWidget());

    }

    protected abstract void generateIds();
}
