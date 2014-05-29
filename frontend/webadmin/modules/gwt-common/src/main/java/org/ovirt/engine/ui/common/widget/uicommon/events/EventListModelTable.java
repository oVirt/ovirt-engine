package org.ovirt.engine.ui.common.widget.uicommon.events;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.searchbackend.AuditLogConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
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
    public void initTable(CommonApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new AuditLogSeverityColumn(), constants.empty(), "20px"); //$NON-NLS-1$

        TextColumnWithTooltip<AuditLog> logTimeColumn = new FullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getlog_time();
            }
        };
        logTimeColumn.makeSortable(AuditLogConditionFieldAutoCompleter.TIME);
        getTable().addColumn(logTimeColumn, constants.timeEvent(), "170px"); //$NON-NLS-1$

        TextColumnWithTooltip<AuditLog> messageColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getmessage();
            }
        };
        messageColumn.makeSortable(AuditLogConditionFieldAutoCompleter.MESSAGE);
        getTable().addColumn(messageColumn, constants.messageEvent(), "600px"); //$NON-NLS-1$

        TextColumnWithTooltip<AuditLog> correlationIdColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getCorrelationId();
            }
        };
        correlationIdColumn.makeSortable(AuditLogConditionFieldAutoCompleter.CORRELATION_ID);
        getTable().addColumn(correlationIdColumn, constants.correltaionIdEvent(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<AuditLog> originColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getOrigin();
            }
        };
        originColumn.makeSortable(AuditLogConditionFieldAutoCompleter.ORIGIN);
        getTable().addColumn(originColumn, constants.originEvent(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<AuditLog> customEventIdColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {

                int id = object.getCustomEventId();
                return id >= 0 ? String.valueOf(id) : "";   //$NON-NLS-1$
            }
        };
        customEventIdColumn.makeSortable(AuditLogConditionFieldAutoCompleter.CUSTOM_EVENT_ID);
        getTable().addColumn(customEventIdColumn, constants.customEventIdEvent(), "100px"); //$NON-NLS-1$
    }

}
