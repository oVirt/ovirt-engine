package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import java.util.Comparator;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventNotifierPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;

public class SubTabUserEventNotifierView extends AbstractSubTabTableView<DbUser, event_subscriber, UserListModel, UserEventNotifierListModel>
        implements SubTabUserEventNotifierPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabUserEventNotifierView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabUserEventNotifierView(SearchableDetailModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(ApplicationConstants constants) {
        TextColumnWithTooltip<event_subscriber> eventNameColumn = new EnumColumn<event_subscriber, AuditLogType>() {
            @Override
            protected AuditLogType getRawValue(event_subscriber object) {
                return Enum.valueOf(AuditLogType.class, object.getevent_up_name());
            }
        };
        eventNameColumn.makeSortable(new Comparator<event_subscriber>() {
            private final LexoNumericComparator lexoNumericComparator = new LexoNumericComparator();

            @Override
            public int compare(event_subscriber o1, event_subscriber o2) {
                return lexoNumericComparator.compare(o1.getevent_up_name(), o2.getevent_up_name());
            }
        });
        getTable().addColumn(eventNameColumn, constants.eventNameEventNotifier());

        getTable().addActionButton(new WebAdminButtonDefinition<event_subscriber>(constants.manageEventsEventNotifier()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getManageEventsCommand();
            }
        });
    }

}
