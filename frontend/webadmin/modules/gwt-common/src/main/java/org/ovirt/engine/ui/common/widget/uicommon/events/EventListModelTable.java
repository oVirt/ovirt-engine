package org.ovirt.engine.ui.common.widget.uicommon.events;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.searchbackend.AuditLogConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;

import com.google.gwt.event.shared.EventBus;

/**
 * Table used to render {@link AuditLog} items of an {@link EventListModel}.
 *
 * @param <T>
 *            Detail model type.
 */
public class EventListModelTable<E, T extends EventListModel> extends AbstractModelBoundTableWidget<E, AuditLog, T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public EventListModelTable(
            SearchableTableModelProvider<AuditLog, T> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        // No action panel for events, so passing null.
        super(modelProvider, eventBus, null, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        AuditLogSeverityColumn severityColumn = new AuditLogSeverityColumn();
        severityColumn.setContextMenuTitle(constants.severityEvent());
        getTable().addColumn(severityColumn, constants.empty(), "20px"); //$NON-NLS-1$

        AbstractTextColumn<AuditLog> logTimeColumn = new AbstractFullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getLogTime();
            }
        };
        logTimeColumn.makeSortable(AuditLogConditionFieldAutoCompleter.TIME);
        getTable().addColumn(logTimeColumn, constants.timeEvent(), "170px"); //$NON-NLS-1$

        AbstractTextColumn<AuditLog> messageColumn = new AbstractTextColumn<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getMessage();
            }
        };
        messageColumn.makeSortable(AuditLogConditionFieldAutoCompleter.MESSAGE);
        getTable().addColumn(messageColumn, constants.messageEvent(), "600px"); //$NON-NLS-1$

        AbstractTextColumn<AuditLog> correlationIdColumn = new AbstractTextColumn<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getCorrelationId();
            }
        };
        correlationIdColumn.makeSortable(AuditLogConditionFieldAutoCompleter.CORRELATION_ID);
        getTable().addColumn(correlationIdColumn, constants.correltaionIdEvent(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<AuditLog> originColumn = new AbstractTextColumn<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getOrigin();
            }
        };
        originColumn.makeSortable(AuditLogConditionFieldAutoCompleter.ORIGIN);
        getTable().addColumn(originColumn, constants.originEvent(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<AuditLog> customEventIdColumn = new AbstractTextColumn<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {

                int id = object.getCustomEventId();
                return id >= 0 ? String.valueOf(id) : "";   //$NON-NLS-1$
            }
        };
        customEventIdColumn.makeSortable(AuditLogConditionFieldAutoCompleter.CUSTOM_EVENT_ID);
        getTable().addColumn(customEventIdColumn, constants.customEventIdEvent(), "120px"); //$NON-NLS-1$
    }

}
