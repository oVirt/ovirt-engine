package org.ovirt.engine.ui.webadmin.uicommon.model;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;

public class MacPoolPermissionModelProvider extends PermissionModelProvider<SharedMacPoolListModel> {

    @Inject
    public MacPoolPermissionModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<RolePermissionsRemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider,
            Provider<PermissionsPopupPresenterWidget> permissionPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, removeConfirmPopupProvider, permissionPopupProvider, SharedMacPoolListModel.class);
    }

    @Override
    public PermissionListModel getModel() {
        for (EntityModel entityModel : getCommonModel().getSharedMacPoolListModel().getDetailModels()) {
            if (entityModel != null && entityModel instanceof PermissionListModel) {
                return (PermissionListModel) entityModel;
            }
        }

        throw new IllegalStateException("Unable to find PermissionListModel in SharedMacPoolListModels DetailModels"); //$NON-NLS-1$
    }
}
