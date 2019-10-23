package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class HostInterfaceActionPanelPresenterWidget extends DetailActionPanelPresenterWidget<VDS, HostInterfaceLineModel,
    HostListModel<Void>, HostInterfaceListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostInterfaceActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VDS, HostInterfaceLineModel> view,
            SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel<Void>,
                HostInterfaceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<VDS, HostInterfaceLineModel>(constants.setupHostNetworksInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetupNetworksCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<VDS, HostInterfaceLineModel>(constants.saveNetConfigInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSaveNetworkConfigCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<VDS, HostInterfaceLineModel>(constants.syncAllHostNetworks()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSyncAllHostNetworksCommand();
            }
        });
    }

}
