package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderSecretListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ImportNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderSecretPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.ProviderMainSelectedItems;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class ProviderModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<org.ovirt.engine.core.common.businessentities.Provider, ProviderListModel> getProviderListProvider(EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ProviderPopupPresenterWidget> providerPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ProviderListModel> modelProvider) {
        MainViewModelProvider<org.ovirt.engine.core.common.businessentities.Provider, ProviderListModel> result =
                new MainViewModelProvider<org.ovirt.engine.core.common.businessentities.Provider, ProviderListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ProviderListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {

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

                        if (lastExecutedCommand == getModel().getRemoveCommand()
                                || lastExecutedCommand == getModel().getForceRemoveCommand()) {
                            return removeConfirmPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getAddCommand()
                                || lastExecutedCommand == getModel().getEditCommand()) {
                            return defaultConfirmPopupProvider.get();
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }

                };
        result.setModelProvider(modelProvider);
        return result;
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> getProviderNetworkListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ImportNetworksPopupPresenterWidget> discoverNetworkPopupProvider,
            final Provider<ProviderListModel> mainModelProvider,
            final Provider<ProviderNetworkListModel> modelProvider) {
        SearchableDetailTabModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> result =
                new SearchableDetailTabModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel>(
                        eventBus, defaultConfirmPopupProvider) {
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
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<LibvirtSecret, ProviderListModel, ProviderSecretListModel> getProviderSecretListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ProviderSecretPopupPresenterWidget> popupProvider,
            final Provider<ProviderListModel> mainModelProvider,
            final Provider<ProviderSecretListModel> modelProvider) {
        SearchableDetailTabModelProvider<LibvirtSecret, ProviderListModel, ProviderSecretListModel> result =
                new SearchableDetailTabModelProvider<LibvirtSecret, ProviderListModel, ProviderSecretListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(ProviderSecretListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand() ||
                                lastExecutedCommand == getModel().getEditCommand()) {
                            return popupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(
                            ProviderSecretListModel source, UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getRemoveCommand()) {
                            return removeConfirmPopupProvider.get();
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }
                };
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    @Override
    protected void configure() {
        bind(ProviderListModel.class).in(Singleton.class);
        bind(ProviderGeneralModel.class).in(Singleton.class);
        bind(ProviderNetworkListModel.class).in(Singleton.class);
        bind(ProviderSecretListModel.class).in(Singleton.class);
        bind(ProviderMainSelectedItems.class).asEagerSingleton();

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<ProviderListModel, ProviderGeneralModel>>() {})
                .to(new TypeLiteral<DetailTabModelProvider<ProviderListModel, ProviderGeneralModel>>() {
                }).in(Singleton.class);
    }

}
