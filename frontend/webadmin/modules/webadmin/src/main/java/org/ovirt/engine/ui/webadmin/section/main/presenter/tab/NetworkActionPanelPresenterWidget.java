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

public class NetworkActionPanelPresenterWidget extends ActionPanelPresenterWidget<NetworkView, NetworkView, NetworkListModel> {
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<NetworkView, NetworkView> newButtonDefinition;
    private WebAdminButtonDefinition<NetworkView, NetworkView> importButtonDefinition;

    @Inject
    public NetworkActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<NetworkView, NetworkView> view,
            MainModelProvider<NetworkView, NetworkListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<NetworkView, NetworkView>(constants.newNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);
        importButtonDefinition = new WebAdminButtonDefinition<NetworkView, NetworkView>(constants.importNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getImportCommand();
            }
        };
        addActionButton(importButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<NetworkView, NetworkView>(constants.editNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<NetworkView, NetworkView>(constants.removeNetwork()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

    public WebAdminButtonDefinition<NetworkView, NetworkView> getNewButtonDefinition() {
        return newButtonDefinition;
    }

    public WebAdminButtonDefinition<NetworkView, NetworkView> getImportButtonDefinition() {
        return importButtonDefinition;
    }
}
