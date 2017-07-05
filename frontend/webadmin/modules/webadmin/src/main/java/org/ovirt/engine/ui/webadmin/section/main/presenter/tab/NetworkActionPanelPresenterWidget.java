package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class NetworkActionPanelPresenterWidget extends ActionPanelPresenterWidget<NetworkView, NetworkListModel> {
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NetworkActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<NetworkView> view,
            MainModelProvider<NetworkView, NetworkListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.newNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.importNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getImportCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.editNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<NetworkView>(constants.removeNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
