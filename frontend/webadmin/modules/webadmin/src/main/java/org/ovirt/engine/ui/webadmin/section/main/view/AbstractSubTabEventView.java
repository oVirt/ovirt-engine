package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.uicommon.template.AuditLogModelTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.widget.table.column.WebadminAuditLogSeverityColumn;

import com.google.gwt.event.shared.EventBus;

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
public abstract class AbstractSubTabEventView<I, M extends ListWithDetailsModel, D extends EventListModel> extends AbstractSubTabTableWidgetView<I, AuditLog, M, D> {

    public AbstractSubTabEventView(SearchableDetailModelProvider<AuditLog, M, D> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(new AuditLogModelTable<D>(modelProvider, eventBus, clientStorage, new WebadminAuditLogSeverityColumn()));
        generateIds();
        initTable();
        initWidget(getModelBoundTableWidget());
    }

    protected abstract void generateIds();
}
