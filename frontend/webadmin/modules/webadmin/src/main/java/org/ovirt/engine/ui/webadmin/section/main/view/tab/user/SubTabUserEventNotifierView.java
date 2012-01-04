package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventNotifierPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;

public class SubTabUserEventNotifierView extends AbstractSubTabTableView<DbUser, event_subscriber, UserListModel, UserEventNotifierListModel>
        implements SubTabUserEventNotifierPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabUserEventNotifierView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabUserEventNotifierView(SearchableDetailModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<event_subscriber> eventNameColumn = new EnumColumn<event_subscriber, AuditLogType>() {
            @Override
            protected AuditLogType getRawValue(event_subscriber object) {
                return Enum.valueOf(AuditLogType.class, object.getevent_up_name());
            }
        };
        getTable().addColumn(eventNameColumn, "Event Name");

        getTable().addActionButton(new UiCommandButtonDefinition<event_subscriber>("Manage Events") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageEventsCommand();
            }
        });
    }

}
