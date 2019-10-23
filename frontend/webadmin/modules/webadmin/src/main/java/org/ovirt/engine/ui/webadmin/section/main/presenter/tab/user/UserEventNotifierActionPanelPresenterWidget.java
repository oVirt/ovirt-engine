package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class UserEventNotifierActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<DbUser, EventSubscriber, UserListModel, UserEventNotifierListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public UserEventNotifierActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<DbUser, EventSubscriber> view,
            SearchableDetailModelProvider<EventSubscriber, UserListModel, UserEventNotifierListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<DbUser, EventSubscriber>(constants.manageEventsEventNotifier()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageEventsCommand();
            }
        });
    }

}
