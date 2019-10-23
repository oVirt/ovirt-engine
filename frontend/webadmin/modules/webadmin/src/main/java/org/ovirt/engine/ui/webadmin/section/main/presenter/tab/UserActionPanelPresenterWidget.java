package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class UserActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, DbUser, UserListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, DbUser> newButtonDefinition;

    @Inject
    public UserActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, DbUser> view,
            MainModelProvider<DbUser, UserListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, DbUser>(constants.addUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<Void, DbUser>(constants.removeUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, DbUser>(constants.assignTagsUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAssignTagsCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, DbUser> getNewButtonDefinition() {
        return newButtonDefinition;
    }

}
