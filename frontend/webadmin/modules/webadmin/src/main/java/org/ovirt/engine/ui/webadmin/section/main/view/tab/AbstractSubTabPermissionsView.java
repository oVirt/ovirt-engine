package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionWithInheritedPermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public abstract class AbstractSubTabPermissionsView<I, M extends ListWithDetailsModel> extends AbstractSubTabTableWidgetView<I, Permissions, M, PermissionListModel<M>> {

    @Inject
    public AbstractSubTabPermissionsView(
            SearchableDetailModelProvider<Permissions, M, PermissionListModel<M>> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, ApplicationConstants constants) {
        super(new PermissionWithInheritedPermissionListModelTable<PermissionListModel<M>>(
                modelProvider, eventBus, clientStorage));
        generateIds();
        initTable(constants);
        initWidget(getModelBoundTableWidget());

    }

    protected abstract void generateIds();
}
