package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmSnapshotListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmSnapshotListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmSnapshotPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedVmSnapshotView extends AbstractSubTabTableWidgetView<UserPortalItemModel, Snapshot, UserPortalListModel, UserPortalVmSnapshotListModel>
        implements SubTabExtendedVmSnapshotPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmSnapshotView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedVmSnapshotView(VmSnapshotListModelProvider modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(new VmSnapshotListModelTable<>(modelProvider, eventBus, clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
