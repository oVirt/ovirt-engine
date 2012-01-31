package org.ovirt.engine.ui.webadmin.gin.uicommon;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DetachConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostManagementPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ManualFencePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMigratePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailTabModelProvider;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class HostModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VDS, HostListModel> getHostListProvider(ClientGinjector ginjector,
            final Provider<HostPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<ManualFencePopupPresenterWidget> manualFenceConfirmPopupProvider,
            final Provider<AssignTagsPopupPresenterWidget> assignTagsPopupProvider) {
        return new MainTabModelProvider<VDS, HostListModel>(ginjector, HostListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getNewCommand()
                        || lastExecutedCommand == getModel().getEditCommand()
                        || lastExecutedCommand == getModel().getEditWithPMemphasisCommand()
                        || lastExecutedCommand == getModel().getApproveCommand()) {
                    return popupProvider.get();
                } else if (lastExecutedCommand == getModel().getAssignTagsCommand()) {
                    return assignTagsPopupProvider.get();
                }
                return super.getModelPopup(lastExecutedCommand);
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getManualFenceCommand()) {
                    return manualFenceConfirmPopupProvider.get();
                }
                else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<HostListModel, HostGeneralModel> getHostGeneralProvider(ClientGinjector ginjector,
            final Provider<HostInstallPopupPresenterWidget> installPopupProvider) {
        return new DetailTabModelProvider<HostListModel, HostGeneralModel>(ginjector,
                HostListModel.class,
                HostGeneralModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getInstallCommand()) {
                    return installPopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Map<String, String>, HostListModel, HostHooksListModel> getHostHooksListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<Map<String, String>, HostListModel, HostHooksListModel>(ginjector,
                HostListModel.class,
                HostHooksListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel, HostInterfaceListModel> getHostInterfaceListProvider(ClientGinjector ginjector,
            final Provider<DetachConfirmationPopupPresenterWidget> detachConfirmPopupProvider,
            final Provider<HostInterfacePopupPresenterWidget> hostInterfacePopupProvider,
            final Provider<HostManagementPopupPresenterWidget> hostManagementPopupProvider,
            final Provider<HostBondPopupPresenterWidget> hostBondPopupProvider) {
        return new SearchableDetailTabModelProvider<HostInterfaceLineModel, HostListModel, HostInterfaceListModel>(ginjector,
                HostListModel.class,
                HostInterfaceListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getEditCommand()) {
                    return hostInterfacePopupProvider.get();
                }
                if (lastExecutedCommand == getModel().getEditManagementNetworkCommand()) {
                    return hostManagementPopupProvider.get();
                }
                if (lastExecutedCommand == getModel().getBondCommand()) {
                    return hostBondPopupProvider.get();
                }
                if (lastExecutedCommand == getModel().getDetachCommand()) {
                    return detachConfirmPopupProvider.get();
                }
                return super.getModelPopup(lastExecutedCommand);
            }

            @Override
            protected void updateData() {
                // Pass empty data to data provider, since Host NIC table is used as header-only table
                updateDataProvider(new ArrayList<HostInterfaceLineModel>());
            }
        };
    };

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, HostListModel, HostVmListModel> getHostVmListProvider(ClientGinjector ginjector,
            final Provider<VmMigratePopupPresenterWidget> migratePopupProvider) {
        return new SearchableDetailTabModelProvider<VM, HostListModel, HostVmListModel>(ginjector,
                HostListModel.class,
                HostVmListModel.class) {

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getMigrateCommand()) {
                    return migratePopupProvider.get();
                }
                return super.getModelPopup(lastExecutedCommand);
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, HostListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, HostListModel, PermissionListModel>(ginjector,
                HostListModel.class,
                PermissionListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                PermissionListModel model = getModel();

                if (lastExecutedCommand == model.getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, HostListModel, HostEventListModel> getHostEventListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<AuditLog, HostListModel, HostEventListModel>(ginjector,
                HostListModel.class,
                HostEventListModel.class);
    }

    @Override
    protected void configure() {
    }

}
