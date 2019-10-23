package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.SystemPermissionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class SystemPermissionActionPanelPresenterWidget
    extends ActionPanelPresenterWidget<Void, Permission, SystemPermissionListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SystemPermissionActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, Permission> view,
            SystemPermissionModelProvider dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Void, Permission>(constants.addPermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Permission>(constants.removePermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
