package org.ovirt.engine.ui.webadmin.section.main.view.popup.configure;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class RoleActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, Role, RoleListModel> {
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RoleActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, Role> view,
            RoleModelProvider dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Void, Role>(constants.newRole()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Role>(constants.editRole()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Role>(constants.copyRole()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Role>(constants.removeRole()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }
}
