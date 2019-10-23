package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SessionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class SessionActionPanelPresenterWidget extends ActionPanelPresenterWidget<UserSession, UserSession, SessionListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SessionActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<UserSession, UserSession> view,
            MainModelProvider<UserSession, SessionListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<UserSession, UserSession>(constants.terminateSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getTerminateCommand();
            }
        });
    }

}
