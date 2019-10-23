package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RolePermissionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class RolePermissionActionPanelPresenterWidget
    extends ActionPanelPresenterWidget<Role, Permission, RolePermissionListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RolePermissionActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Role, Permission> view,
            RolePermissionModelProvider dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Role, Permission>(constants.removePermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
