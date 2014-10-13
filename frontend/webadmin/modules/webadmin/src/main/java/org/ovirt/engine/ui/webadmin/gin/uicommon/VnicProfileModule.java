package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.VnicProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class VnicProfileModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VnicProfileView, VnicProfileListModel> getVnicProfileListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VnicProfilePopupPresenterWidget> newVnicProfilePopupProvider,
            final Provider<VnicProfilePopupPresenterWidget> editVnicProfilePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<VnicProfileView, VnicProfileListModel>(eventBus, defaultConfirmPopupProvider, VnicProfileListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VnicProfileListModel source,
                    UICommand lastExecutedCommand,
                    Model windowModel) {

                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return newVnicProfilePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getEditCommand()) {
                    return editVnicProfilePopupProvider.get();
                } else {
                    return super.getModelPopup(source, lastExecutedCommand, windowModel);
                }
            }

            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VnicProfileListModel source,
                    UICommand lastExecutedCommand) {

                if (lastExecutedCommand == getModel().getRemoveCommand()) { //$NON-NLS-1$
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(source, lastExecutedCommand);
                }
            }

        };
    }

    // Form Detail Models
    @Provides
    @Singleton
    public SearchableDetailModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel> getVnicProfileVmModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel>(eventBus, defaultConfirmPopupProvider,
                VnicProfileListModel.class,
                VnicProfileVmListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VnicProfileVmListModel source,
                    UICommand lastExecutedCommand) {
                return super.getConfirmModelPopup(source, lastExecutedCommand);
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel> geVnicProfileTemplateModelProvider(EventBus eventBus,
    Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel>(eventBus, defaultConfirmPopupProvider,
                VnicProfileListModel.class,
                VnicProfileTemplateListModel.class) {
            @Override
            public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VnicProfileTemplateListModel source,
                    UICommand lastExecutedCommand) {
                return super.getConfirmModelPopup(source, lastExecutedCommand);
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<Permissions, VnicProfileListModel, PermissionListModel> getVnicProfilePermissionListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {

        return new PermissionModelProvider<VnicProfileListModel>(eventBus,
                defaultConfirmPopupProvider,
                removeConfirmPopupProvider,
                popupProvider,
                VnicProfileListModel.class);
    }

    @Override
    protected void configure() {
    }

}
