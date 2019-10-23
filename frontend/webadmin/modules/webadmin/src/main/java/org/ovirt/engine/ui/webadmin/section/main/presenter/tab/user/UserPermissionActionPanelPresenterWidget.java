package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailPermissionActionPanelPresenterWidget;

import com.google.web.bindery.event.shared.EventBus;

public class UserPermissionActionPanelPresenterWidget extends DetailPermissionActionPanelPresenterWidget<DbUser, UserListModel, UserPermissionListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public UserPermissionActionPanelPresenterWidget(EventBus eventBus,
            ViewDef<DbUser, Permission> view,
            SearchableDetailModelProvider<Permission, UserListModel, UserPermissionListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<DbUser, Permission>(getSharedEventBus(), constants.addSystemPermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddRoleToUserCommand();
            }
        });
        super.initializeButtons();
    }
}
