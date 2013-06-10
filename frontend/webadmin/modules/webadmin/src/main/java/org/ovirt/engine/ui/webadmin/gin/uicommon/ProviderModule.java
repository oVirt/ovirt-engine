package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.DiscoverNetworkPopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ProviderModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<org.ovirt.engine.core.common.businessentities.Provider, ProviderListModel> getProviderListProvider(ClientGinjector ginjector,
            final Provider<ProviderPopupPresenterWidget> providerPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new MainTabModelProvider<org.ovirt.engine.core.common.businessentities.Provider, ProviderListModel>(ginjector,
                ProviderListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ProviderListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {

                if (lastExecutedCommand == getModel().getAddCommand()
                        || lastExecutedCommand == getModel().getEditCommand()) {
                    return providerPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ProviderListModel source,
                    UICommand lastExecutedCommand) {

                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getAddCommand() || lastExecutedCommand == getModel().getEditCommand()) {
                    return defaultConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }

        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<ProviderListModel, ProviderGeneralModel> getProviderGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<ProviderListModel, ProviderGeneralModel>(ginjector,
                ProviderListModel.class,
                ProviderGeneralModel.class);
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> getProviderNetworkListProvider(ClientGinjector ginjector,
            final Provider<DiscoverNetworkPopupPresenterWidget> discoverNetworkPopupProvider) {
        return new SearchableDetailTabModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel>(ginjector,
                ProviderListModel.class,
                ProviderNetworkListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ProviderNetworkListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getDiscoverCommand()) {
                    return discoverNetworkPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(ProviderNetworkListModel source,
                    UICommand lastExecutedCommand) {
                return super.getConfirmModelPopup(source, lastExecutedCommand);
            }
        };
    }

    @Override
    protected void configure() {
    }

}
