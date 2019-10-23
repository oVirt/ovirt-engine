package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class NetworkTemplateActionPanelPresenterWidget
    extends DetailActionPanelPresenterWidget<NetworkView, PairQueryable<VmNetworkInterface, VmTemplate>,
        NetworkListModel, NetworkTemplateListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NetworkTemplateActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<NetworkView, PairQueryable<VmNetworkInterface, VmTemplate>> view,
            SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VmTemplate>,
                NetworkListModel, NetworkTemplateListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<NetworkView, PairQueryable<VmNetworkInterface,
                VmTemplate>>(constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
