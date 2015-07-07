package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmInterfaceListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmNetworkInterfacePresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmInterfaceListModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabExtendedVmNetworkInterfaceView extends AbstractSubTabTableWidgetView<UserPortalItemModel, VmNetworkInterface, UserPortalListModel, VmInterfaceListModel>
        implements SubTabExtendedVmNetworkInterfacePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedVmNetworkInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabExtendedVmNetworkInterfaceView(VmInterfaceListModelProvider modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new VmInterfaceListModelTable(modelProvider, eventBus, clientStorage));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
