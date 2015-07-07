package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.PoolInterfaceListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedPoolNetworkInterfacePresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedPoolNetworkInterfaceView extends AbstractSubTabTableWidgetView<UserPortalItemModel, VmNetworkInterface, UserPortalListModel, PoolInterfaceListModel>
        implements SubTabExtendedPoolNetworkInterfacePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedPoolNetworkInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedPoolNetworkInterfaceView(
            UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel, PoolInterfaceListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new PoolInterfaceListModelTable(modelProvider, eventBus, clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
