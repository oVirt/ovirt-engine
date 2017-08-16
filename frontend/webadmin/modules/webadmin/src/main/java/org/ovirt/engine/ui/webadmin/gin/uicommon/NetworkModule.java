package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.utils.PairQueryable;
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
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.NewHostNetworkQosModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkAttachmentModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksBondModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkExternalSubnetListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostNetworkQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.EditNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.NewNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostSetupNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.NetworkAttachmentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.VfsConfigPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.VnicProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ExternalSubnetPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ImportNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.NetworkMainSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.SetupNetworksLabelPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class NetworkModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<NetworkView, NetworkListModel> getNetworkListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NewNetworkPopupPresenterWidget> newNetworkPopupProvider,
            final Provider<ImportNetworksPopupPresenterWidget> importNetworkPopupProvider,
            final Provider<EditNetworkPopupPresenterWidget> editNetworkPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<HostNetworkQosPopupPresenterWidget> addQosPopupProvider,
            final Provider<NetworkListModel> modelProvider) {
        MainViewModelProvider<NetworkView, NetworkListModel> result =
                new MainViewModelProvider<NetworkView, NetworkListModel>(eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(NetworkListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {

                        if (windowModel instanceof NewHostNetworkQosModel) {
                            return addQosPopupProvider.get();
                        }

                        if (lastExecutedCommand == getModel().getNewCommand()) {
                            return newNetworkPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getImportCommand()) {
                            return importNetworkPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getEditCommand()) {
                            return editNetworkPopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(NetworkListModel source,
                            UICommand lastExecutedCommand) {

                        if (lastExecutedCommand == getModel().getRemoveCommand()) {
                            return removeConfirmPopupProvider.get();
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }

                };
        result.setModelProvider(modelProvider);
        return result;
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<NetworkListModel, NetworkGeneralModel> getNetworkGeneralProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<NetworkListModel> mainModelProvider,
            final Provider<NetworkGeneralModel> modelProvider) {
        DetailTabModelProvider<NetworkListModel, NetworkGeneralModel> result =
                new DetailTabModelProvider<>(eventBus, defaultConfirmPopupProvider);
        result.setMainModelProvider(mainModelProvider);
        result.setModelProvider(modelProvider);
        return result;
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VnicProfileView, NetworkListModel, NetworkProfileListModel> getNetworkProfileListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VnicProfilePopupPresenterWidget> newProfilePopupProvider,
            final Provider<VnicProfilePopupPresenterWidget> editProfilePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<NetworkListModel> mainModelProvider,
            final Provider<NetworkProfileListModel> modelProvider) {
        SearchableDetailTabModelProvider<VnicProfileView, NetworkListModel, NetworkProfileListModel> result =
                new SearchableDetailTabModelProvider<VnicProfileView, NetworkListModel, NetworkProfileListModel>(eventBus,
                        defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(NetworkProfileListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()) {
                            return newProfilePopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getEditCommand()) {
                            return editProfilePopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(NetworkProfileListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getRemoveCommand()) { //$NON-NLS-1$
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

    @Provides
    @Singleton
    public SearchableDetailModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel> getExternalSubnetListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ExternalSubnetPopupPresenterWidget> newExternalSubnetPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<NetworkListModel> mainModelProvider,
            final Provider<NetworkExternalSubnetListModel> modelProvider) {
        SearchableDetailTabModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel> result =
                new SearchableDetailTabModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel>(eventBus,
                        defaultConfirmPopupProvider) {

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(NetworkExternalSubnetListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()) {
                            return newExternalSubnetPopupProvider.get();
                        }

                        return super.getModelPopup(source, lastExecutedCommand, windowModel);
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(NetworkExternalSubnetListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getRemoveCommand()) { //$NON-NLS-1$
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

    @Provides
    @Singleton
    public SearchableDetailModelProvider<PairQueryable<Cluster, NetworkCluster>, NetworkListModel, NetworkClusterListModel> getNetworkClusterListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<ClusterManageNetworkPopupPresenterWidget> managePopupProvider,
            final Provider<NetworkListModel> mainModelProvider,
            final Provider<NetworkClusterListModel> modelProvider) {
        SearchableDetailTabModelProvider<PairQueryable<Cluster, NetworkCluster>, NetworkListModel, NetworkClusterListModel> result =
                new SearchableDetailTabModelProvider<PairQueryable<Cluster, NetworkCluster>, NetworkListModel, NetworkClusterListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(NetworkClusterListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {
                        if (lastExecutedCommand == getModel().getManageCommand()) {
                            return managePopupProvider.get();
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
    public SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> getNetworkHostListProvider(
            EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<SetupNetworksBondPopupPresenterWidget> setupNetworksBondPopupProvider,
            final Provider<NetworkAttachmentPopupPresenterWidget> setupNetworksInterfacePopupProvider,
            final Provider<VfsConfigPopupPresenterWidget> vfsConfigPopupProvider,
            final Provider<SetupNetworksLabelPopupPresenterWidget> setupNetworksLabelPopupProvider,
            final Provider<HostSetupNetworksPopupPresenterWidget> hostSetupNetworksPopupProvider,
            final Provider<NetworkListModel> mainModelProvider,
            final Provider<NetworkHostListModel> modelProvider) {
        SearchableDetailTabModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> result =
                new SearchableDetailTabModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(NetworkHostListModel source,
                            UICommand lastExecutedCommand,
                            Model windowModel) {

                        // Resolve by dialog model
                        if (windowModel instanceof SetupNetworksBondModel) {
                            return setupNetworksBondPopupProvider.get();
                        } else if (windowModel instanceof NetworkAttachmentModel) {
                            return setupNetworksInterfacePopupProvider.get();
                        } else if (windowModel instanceof VfsConfigModel) {
                            return vfsConfigPopupProvider.get();
                        } else if (windowModel instanceof SetupNetworksLabelModel) {
                            return setupNetworksLabelPopupProvider.get();
                        }

                        // Resolve by last executed command
                        if (lastExecutedCommand == getModel().getSetupNetworksCommand()) {
                            return hostSetupNetworksPopupProvider.get();
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
    public SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> getNetworkVmModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<NetworkListModel> mainModelProvider,
            final Provider<NetworkVmListModel> modelProvider) {
        SearchableDetailTabModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> result =
                new SearchableDetailTabModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(NetworkVmListModel source,
                            UICommand lastExecutedCommand) {
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

    @Provides
    @Singleton
    public SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VmTemplate>, NetworkListModel, NetworkTemplateListModel> geNetworkTemplateModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<NetworkListModel> mainModelProvider,
            final Provider<NetworkTemplateListModel> modelProvider) {
        SearchableDetailTabModelProvider<PairQueryable<VmNetworkInterface, VmTemplate>, NetworkListModel, NetworkTemplateListModel> result =
                new SearchableDetailTabModelProvider<PairQueryable<VmNetworkInterface, VmTemplate>, NetworkListModel, NetworkTemplateListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(NetworkTemplateListModel source,
                            UICommand lastExecutedCommand) {
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
        bind(NetworkListModel.class).in(Singleton.class);
        bind(NetworkGeneralModel.class).in(Singleton.class);
        bind(NetworkProfileListModel.class).in(Singleton.class);
        bind(NetworkExternalSubnetListModel.class).in(Singleton.class);
        bind(NetworkClusterListModel.class).in(Singleton.class);
        bind(NetworkHostListModel.class).in(Singleton.class);
        bind(NetworkVmListModel.class).in(Singleton.class);
        bind(NetworkTemplateListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<NetworkView>>(){}).in(Singleton.class);
        bind(NetworkMainSelectedItems.class).asEagerSingleton();

        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, NetworkListModel, PermissionListModel<NetworkView>>>(){})
           .to(new TypeLiteral<PermissionModelProvider<NetworkView, NetworkListModel>>(){}).in(Singleton.class);
    }

}
