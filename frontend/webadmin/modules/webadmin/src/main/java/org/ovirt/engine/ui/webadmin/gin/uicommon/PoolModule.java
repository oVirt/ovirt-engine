package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
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
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmHighPerformanceConfigurationModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool.PoolEditPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool.PoolNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmHighPerformanceConfigurationPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolMainSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class PoolModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VmPool, PoolListModel> getPoolListProvider(EventBus eventBus,
            final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<PoolNewPopupPresenterWidget> poolPopupProvider,
            final Provider<PoolEditPopupPresenterWidget> poolEditPopupProvider,
            final Provider<PoolListModel> modelProvider,
        final Provider<VmHighPerformanceConfigurationPresenterWidget> highPerformanceConfigurationProvider) {
        MainViewModelProvider<VmPool, PoolListModel> result =
                new MainViewModelProvider<VmPool, PoolListModel>(eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(PoolListModel source,
                            UICommand lastExecutedCommand, Model windowModel) {
                        if (lastExecutedCommand == getModel().getNewCommand()) {
                            return poolPopupProvider.get();
                        } else if (lastExecutedCommand == getModel().getEditCommand()) {
                            return poolEditPopupProvider.get();
                        } else {
                            return super.getModelPopup(source, lastExecutedCommand, windowModel);
                        }
                    }

                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(PoolListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getRemoveCommand()) {
                            return removeConfirmPopupProvider.get();
                        } else if ("OnSave".equals(lastExecutedCommand.getName())) { //$NON-NLS-1$
                            if (source.getConfirmWindow() instanceof VmHighPerformanceConfigurationModel) {
                                return highPerformanceConfigurationProvider.get();
                            } else {
                                return defaultConfirmPopupProvider.get();
                            }
                        } else {
                            return super.getConfirmModelPopup(source, lastExecutedCommand);
                        }
                    }
                };
        result.setModelProvider(modelProvider);
        return result;
    }

    // Search-able Detail Models

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> getPoolVmListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<PoolListModel> mainModelProvider,
            final Provider<PoolVmListModel> modelProvider) {
        SearchableDetailTabModelProvider<VM, PoolListModel, PoolVmListModel> result =
                new SearchableDetailTabModelProvider<VM, PoolListModel, PoolVmListModel>(
                        eventBus, defaultConfirmPopupProvider) {
                    @Override
                    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(PoolVmListModel source,
                            UICommand lastExecutedCommand) {
                        if (lastExecutedCommand == getModel().getDetachCommand()) {
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
        bind(PoolListModel.class).in(Singleton.class);
        bind(PoolGeneralModel.class).in(Singleton.class);
        bind(PoolVmListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<VmPool>>(){}).in(Singleton.class);
        bind(PoolMainSelectedItems.class).asEagerSingleton();

        // Form Detail Models
        bind(new TypeLiteral<DetailModelProvider<PoolListModel, PoolGeneralModel>>(){})
            .to(new TypeLiteral<DetailTabModelProvider<PoolListModel, PoolGeneralModel>>(){}).in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, PoolListModel, PermissionListModel<VmPool>>>(){})
           .to(new TypeLiteral<PermissionModelProvider<VmPool, PoolListModel>>(){}).in(Singleton.class);
    }

}
