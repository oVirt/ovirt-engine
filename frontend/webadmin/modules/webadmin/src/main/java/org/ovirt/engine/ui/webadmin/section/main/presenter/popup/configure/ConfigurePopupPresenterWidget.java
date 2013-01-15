package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure;

import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the WebAdmin configure dialog.
 */
public class ConfigurePopupPresenterWidget extends AbstractPopupPresenterWidget<ConfigurePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractPopupPresenterWidget.ViewDef {
    }

    private RoleModelProvider roleModelProvider;
    private RolePermissionModelProvider permissionModelProvider;
    private SystemPermissionModelProvider systemPermissionModelProvider;

    @Inject
    public ConfigurePopupPresenterWidget(EventBus eventBus,
            ViewDef view, ClientGinjector ginjector,
            RoleModelProvider roleModelProvider,
            RolePermissionModelProvider permissionModelProvider,
            SystemPermissionModelProvider systemPermissionModelProvider) {
        super(eventBus, view);
        this.roleModelProvider = roleModelProvider;
        this.permissionModelProvider = permissionModelProvider;
        this.systemPermissionModelProvider = systemPermissionModelProvider;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        roleModelProvider.getModel().Search();
        systemPermissionModelProvider.getModel().Search();
    }

    @Override
    protected void onHide() {
        super.onHide();
        roleModelProvider.getModel().EnsureAsyncSearchStopped();
        permissionModelProvider.getModel().EnsureAsyncSearchStopped();
        systemPermissionModelProvider.getModel().EnsureAsyncSearchStopped();
    }

}
