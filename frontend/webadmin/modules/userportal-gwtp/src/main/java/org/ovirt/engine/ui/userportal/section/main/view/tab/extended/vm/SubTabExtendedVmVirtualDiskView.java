package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmDiskListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmVirtualDiskPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmDiskListModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedVmVirtualDiskView extends AbstractSubTabTableWidgetView<UserPortalItemModel, Disk, UserPortalListModel, VmDiskListModel>
        implements SubTabExtendedVmVirtualDiskPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmVirtualDiskView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedVmVirtualDiskView(VmDiskListModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new VmDiskListModelTable(modelProvider, eventBus, clientStorage, false));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
