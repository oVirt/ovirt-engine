package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaUserListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaVmListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.QuotaPopupPresenterWidget;

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
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(QuotaListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand.equals(getModel().getCreateCommand())
                        || lastExecutedCommand.equals(getModel().getEditCommand())
                        || lastExecutedCommand.equals(getModel().getCloneCommand())) {
                    return quotaPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(QuotaListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand.equals(getModel().getRemoveCommand())) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
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
    public SearchableDetailModelProvider<VmTemplate, QuotaListModel, QuotaTemplateListModel> getQuotaTemplateListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<VmTemplate, QuotaListModel, QuotaTemplateListModel>(ginjector,
                QuotaListModel.class,
                QuotaTemplateListModel.class);
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
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(QuotaUserListModel source,
                    UICommand lastExecutedCommand, Model windowModel) {
                if (lastExecutedCommand == getModel().getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(QuotaUserListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
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
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(QuotaPermissionListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand == getModel().getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(QuotaPermissionListModel source,
                    UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, QuotaListModel, QuotaEventListModel> getQuotaEventListProvider(ClientGinjector ginjector,
            final Provider<EventPopupPresenterWidget> eventPopupProvider) {
        return new SearchableDetailTabModelProvider<AuditLog, QuotaListModel, QuotaEventListModel>(ginjector,
                QuotaListModel.class,
                QuotaEventListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(QuotaEventListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {
                if (lastExecutedCommand.equals(getModel().getDetailsCommand())) {
                    return eventPopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }
        };
    }

    @Override
    protected void configure() {
    }

}
