package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventNotifierPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabUserEventNotifierView extends AbstractSubTabTableView<DbUser, event_subscriber, UserListModel, UserEventNotifierListModel>
        implements SubTabUserEventNotifierPresenter.ViewDef {

    @Inject
    public SubTabUserEventNotifierView(SearchableDetailModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {

        TextColumn<event_subscriber> eventNameColumn = new EnumColumn<event_subscriber, AuditLogType>() {
            @Override
            protected AuditLogType getRawValue(event_subscriber object) {
                return Enum.valueOf(AuditLogType.class, object.getevent_up_name());
            }
        };
        getTable().addColumn(eventNameColumn, "Event Name");

        getTable().addActionButton(new UiCommandButtonDefinition<event_subscriber>(getDetailModel().getManageEventsCommand(),
                "Manage Events"));
    }

}
