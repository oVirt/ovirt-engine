package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ProviderActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, Provider, ProviderListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, Provider> newButtonDefinition;

    @Inject
    public ProviderActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, Provider> view,
            MainModelProvider<Provider, ProviderListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, Provider>(constants.addProvider()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        };
        addActionButton(newButtonDefinition);

        addActionButton(new WebAdminButtonDefinition<Void, Provider>(constants.editProvider()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Provider>(constants.removeProvider()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, Provider>(constants.forceRemoveProvider()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getForceRemoveCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, Provider> getNewButtonDefinition() {
        return newButtonDefinition;
    }

}
