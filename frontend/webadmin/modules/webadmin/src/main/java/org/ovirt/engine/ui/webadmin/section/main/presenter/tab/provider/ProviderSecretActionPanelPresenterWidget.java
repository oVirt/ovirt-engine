package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderSecretListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ProviderSecretActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<Provider, LibvirtSecret, ProviderListModel, ProviderSecretListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ProviderSecretActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Provider, LibvirtSecret> view,
            SearchableDetailModelProvider<LibvirtSecret, ProviderListModel, ProviderSecretListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Provider, LibvirtSecret>(constants.newLibvirtSecret()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Provider, LibvirtSecret>(constants.editLibvirtSecret()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Provider, LibvirtSecret>(constants.removeLibvirtSecret()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
