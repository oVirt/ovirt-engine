package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

/**
 * Base class for sub tab views used to show events ({@link AuditLog} table).
 *
 * @param <I>
 *            Main tab table row data type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public abstract class AbstractSubTabEventView<I, M extends ListWithDetailsModel, D extends EventListModel> extends AbstractSubTabTableView<I, AuditLog, M, D> {

    public AbstractSubTabEventView(SearchableDetailModelProvider<AuditLog, M, D> modelProvider) {
        super(modelProvider);
        initTable();
    }

    void initTable() {
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
