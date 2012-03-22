package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTreeWidgetView;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmSnapshotListModelTree;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmSnapshotPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedVmSnapshotView extends AbstractSubTabTreeWidgetView<UserPortalItemModel, Snapshot, UserPortalListModel, VmSnapshotListModel>
        implements SubTabExtendedVmSnapshotPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmSnapshotView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedVmSnapshotView(VmSnapshotListModelProvider modelProvider,
            EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(new VmSnapshotListModelTree(modelProvider, eventBus, resources, constants), eventBus);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        getModelBoundTreeWidget().initTree(actionPanel, table);
    }

    @Override
    protected SubTabTreeActionPanel createActionPanel(SearchableDetailModelProvider modelProvider) {
        return new SubTabTreeActionPanel<Snapshot>(modelProvider, getEventBus());
    }

}
