package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmPermissionPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmPermissionListModelProvider;
import org.ovirt.engine.ui.userportal.widget.table.column.UserPortalPermissionTypeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedVmPermissionView extends AbstractSubTabTableWidgetView<UserPortalItemModel, permissions, UserPortalListModel, PermissionListModel>
        implements SubTabExtendedVmPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedVmPermissionView(VmPermissionListModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new PermissionListModelTable(modelProvider, eventBus, clientStorage, new UserPortalPermissionTypeColumn()));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
