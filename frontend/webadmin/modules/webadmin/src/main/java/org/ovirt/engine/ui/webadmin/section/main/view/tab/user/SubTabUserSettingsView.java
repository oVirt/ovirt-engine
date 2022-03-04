package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.users.UserSettingsModelTable;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserSettingsModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserSettingsPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabUserSettingsView extends AbstractSubTabTableWidgetView<DbUser, UserProfileProperty, UserListModel, UserSettingsModel> implements SubTabUserSettingsPresenter.ViewDef {

    @Inject
    public SubTabUserSettingsView(
            SearchableDetailModelProvider<UserProfileProperty, UserListModel, UserSettingsModel> modelProvider,
            EventBus eventBus,
            UserSettingsActionPanel actionPanel,
            ClientStorage clientStorage) {
        super(new UserSettingsModelTable(modelProvider,
                eventBus,
                actionPanel,
                clientStorage));
        initTable();
        initWidget(getModelBoundTableWidget());
    }

}
