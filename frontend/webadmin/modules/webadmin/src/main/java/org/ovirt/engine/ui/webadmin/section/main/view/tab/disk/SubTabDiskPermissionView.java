package org.ovirt.engine.ui.webadmin.section.main.view.tab.disk;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabPermissionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabDiskPermissionView extends AbstractSubTabPermissionsView<Disk, DiskListModel>
        implements SubTabDiskPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDiskPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDiskPermissionView(SearchableDetailModelProvider<Permission, DiskListModel,
            PermissionListModel<Disk>> modelProvider, EventBus eventBus,
            PermissionActionPanelPresenterWidget<Disk, DiskListModel, PermissionListModel<Disk>> actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, actionPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
