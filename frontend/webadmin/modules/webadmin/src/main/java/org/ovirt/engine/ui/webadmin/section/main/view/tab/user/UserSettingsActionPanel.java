package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserSettingsModel;

import com.google.inject.Inject;

class UserSettingsActionPanel extends DetailActionPanelPresenterWidget<DbUser, UserProfileProperty, UserListModel, UserSettingsModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public UserSettingsActionPanel(com.google.web.bindery.event.shared.EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<DbUser, UserProfileProperty> view,
            SearchableDetailModelProvider<UserProfileProperty, UserListModel, UserSettingsModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {

        addActionButton(new UiCommandButtonDefinition<DbUser, UserProfileProperty>(getSharedEventBus(),
                constants.removeProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });

    }
}
