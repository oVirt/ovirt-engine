package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.searchbackend.SessionConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SessionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabSessionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabSessionView extends AbstractMainTabWithDetailsTableView<EngineSession, SessionListModel>
        implements MainTabSessionPresenter.ViewDef {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabSessionView(MainModelProvider<EngineSession, SessionListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<EngineSession> sessionDbIdColumn =
                new AbstractTextColumn<EngineSession>() {
                    @Override
                    public String getValue(EngineSession session) {
                        return Long.toString(session.getId());
                    }
                };
        getTable().addColumn(sessionDbIdColumn, constants.sessionDbId(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<EngineSession> userNameColumn =
                new AbstractTextColumn<EngineSession>() {
                    @Override
                    public String getValue(EngineSession session) {
                        return session.getUserName();
                    }
                };
        userNameColumn.makeSortable(SessionConditionFieldAutoCompleter.USER_NAME);
        getTable().addColumn(userNameColumn, constants.userNameUser(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<EngineSession> userIdColumn = new AbstractTextColumn<EngineSession>() {
            @Override
            public String getValue(EngineSession session) {
                return session.getUserId().toString();
            }
        };
        userIdColumn.makeSortable(SessionConditionFieldAutoCompleter.USER_ID);
        getTable().addColumn(userIdColumn, constants.userId(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<EngineSession>(constants.terminateSession()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getTerminateCommand();
            }
        });
    }

    interface ViewIdHandler extends ElementIdHandler<MainTabSessionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

}
