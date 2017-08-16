package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainViewModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.VnicProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.VnicProfileMainSelectedItems;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class VnicProfileModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VnicProfileView, VnicProfileListModel> getVnicProfileListProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            final Provider<VnicProfilePopupPresenterWidget> newVnicProfilePopupProvider,
            final Provider<VnicProfilePopupPresenterWidget> editVnicProfilePopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            final Provider<VnicProfileListModel> modelProvider) {
        MainViewModelProvider<VnicProfileView, VnicProfileListModel> result =
                new MainViewModelProvider<VnicProfileView, VnicProfileListModel>(eventBus, defaultConfirmPopupProvider) {
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
        result.setModelProvider(modelProvider);
        return result;
    }

    @Override
    protected void configure() {
        bind(VnicProfileListModel.class).in(Singleton.class);
        bind(VnicProfileVmListModel.class).in(Singleton.class);
        bind(VnicProfileTemplateListModel.class).in(Singleton.class);
        bind(new TypeLiteral<PermissionListModel<VnicProfileView>>(){}).in(Singleton.class);
        bind(VnicProfileMainSelectedItems.class).asEagerSingleton();

        // Search-able Detail Models
        bind(new TypeLiteral<SearchableDetailModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel>>(){})
           .to(new TypeLiteral<SearchableDetailTabModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel>>(){})
           .in(Singleton.class);
        bind(new TypeLiteral<SearchableDetailModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel>>(){})
            .to(new TypeLiteral<SearchableDetailTabModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel>>(){})
            .in(Singleton.class);
        // Permission Detail Model
        bind(new TypeLiteral<SearchableDetailModelProvider<Permission, VnicProfileListModel, PermissionListModel<VnicProfileView>>>(){})
           .to(new TypeLiteral<PermissionModelProvider<VnicProfileView, VnicProfileListModel>>(){}).in(Singleton.class);
    }

}
