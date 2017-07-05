package org.ovirt.engine.ui.common.widget.action;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;

import com.google.web.bindery.event.shared.EventBus;

public class VmInterfaceActionPanelPresenterWidget
    extends ActionPanelPresenterWidget<VmNetworkInterface, VmInterfaceListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VmInterfaceActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<VmNetworkInterface> view,
            SearchableDetailModelProvider<VmNetworkInterface, VmListModel<Void>, VmInterfaceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getSharedEventBus(),
                constants.newInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDataProvider().getModel().getNewCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getSharedEventBus(),
                constants.editInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDataProvider().getModel().getEditCommand();
            }
        });

        addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getSharedEventBus(),
                constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDataProvider().getModel().getRemoveCommand();
            }
        });
    }

}
