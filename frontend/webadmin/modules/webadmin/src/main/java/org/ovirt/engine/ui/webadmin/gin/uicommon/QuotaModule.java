package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaUserListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaVmListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.qouta.QuotaPopupPresenterWidget;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class QuotaModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<Quota, QuotaListModel> getQuotaListProvider(ClientGinjector ginjector,
            final Provider<QuotaPopupPresenterWidget> quotaPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<Quota, QuotaListModel>(ginjector, QuotaListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand.equals(getModel().getCreateQuotaCommand())
                        || lastExecutedCommand.equals(getModel().getEditQuotaCommand())) {
                    return quotaPopupProvider.get();
                }
                return super.getModelPopup(lastExecutedCommand);
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand.equals(getModel().getRemoveQuotaCommand())) {
                    return removeConfirmPopupProvider.get();
                }
                return super.getConfirmModelPopup(lastExecutedCommand);
            }
        };
    }

    // Form Detail Models

    // Searchable Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<QuotaVdsGroup, QuotaListModel, QuotaClusterListModel> getQuotaClusterListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<QuotaVdsGroup, QuotaListModel, QuotaClusterListModel>(ginjector,
                QuotaListModel.class,
                QuotaClusterListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<QuotaStorage, QuotaListModel, QuotaStorageListModel> getQuotaStorageListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<QuotaStorage, QuotaListModel, QuotaStorageListModel>(ginjector,
                QuotaListModel.class,
                QuotaStorageListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, QuotaListModel, QuotaVmListModel> getQuotaVmListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VM, QuotaListModel, QuotaVmListModel>(ginjector,
                QuotaListModel.class,
                QuotaVmListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, QuotaListModel, QuotaUserListModel> getQuotaUserListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, QuotaListModel, QuotaUserListModel>(ginjector,
                QuotaListModel.class,
                QuotaUserListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                QuotaUserListModel model = getModel();
                if (lastExecutedCommand == model.getAddCommand()) {
                    return popupProvider.get();
                }
                return super.getModelPopup(lastExecutedCommand);
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
    public SearchableDetailModelProvider<permissions, QuotaListModel, QuotaPermissionListModel> getQuotaPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, QuotaListModel, QuotaPermissionListModel>(ginjector,
                QuotaListModel.class,
                QuotaPermissionListModel.class) {
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
    public SearchableDetailModelProvider<AuditLog, QuotaListModel, QuotaEventListModel> getQuotaEventListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<AuditLog, QuotaListModel, QuotaEventListModel>(ginjector,
                QuotaListModel.class,
                QuotaEventListModel.class);
    }

    @Override
    protected void configure() {
    }

}
