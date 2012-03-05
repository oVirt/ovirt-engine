package org.ovirt.engine.ui.common.widget.uicommon.events;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;

import com.google.gwt.event.shared.EventBus;

/**
 * Table used to render {@link AuditLog} items of an {@link EventListModel}.
 *
 * @param <T>
 *            Detail model type.
 */
public class EventListModelTable<T extends EventListModel> extends AbstractModelBoundTableWidget<AuditLog, T> {

    public EventListModelTable(
            SearchableTableModelProvider<AuditLog, T> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().addColumn(new AuditLogSeverityColumn(), "", "20px");

        TextColumnWithTooltip<AuditLog> logTimeColumn = new FullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getlog_time();
            }
        };
        getTable().addColumn(logTimeColumn, "Time");

        TextColumnWithTooltip<AuditLog> messageColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getmessage();
            }
        };
        getTable().addColumn(messageColumn, "Message");
    }

}
