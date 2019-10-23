package org.ovirt.engine.ui.common.widget.action;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;

import com.google.web.bindery.event.shared.EventBus;

public class VmInterfaceActionPanelPresenterWidget
    extends DetailActionPanelPresenterWidget<VM, VmNetworkInterface, VmListModel<Void>, VmInterfaceListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VmInterfaceActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VM, VmNetworkInterface> view,
            SearchableDetailModelProvider<VmNetworkInterface, VmListModel<Void>, VmInterfaceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<VM, VmNetworkInterface>(getSharedEventBus(),
                constants.newInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VM, VmNetworkInterface>(getSharedEventBus(),
                constants.editInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VM, VmNetworkInterface>(getSharedEventBus(),
                constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
