package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.template.PermissionWithInheritedPermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.widget.table.column.WebAdminPermissionTypeColumn;

import com.google.gwt.event.shared.EventBus;

public abstract class AbstractSubTabPermissionsView<I, M extends ListWithDetailsModel> extends AbstractSubTabTableWidgetView<I, permissions, M, PermissionListModel> {

    public AbstractSubTabPermissionsView(SearchableDetailModelProvider<permissions, M, PermissionListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(new PermissionWithInheritedPermissionListModelTable(modelProvider,
                eventBus,
                clientStorage,
                new WebAdminPermissionTypeColumn()));
        generateIds();
        initTable();
        initWidget(getModelBoundTableWidget());
    }

    protected abstract void generateIds();
}
