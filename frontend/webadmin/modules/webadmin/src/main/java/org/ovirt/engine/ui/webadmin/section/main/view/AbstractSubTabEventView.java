package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.widget.action.CommandLocation;
import org.ovirt.engine.ui.common.widget.uicommon.events.EventListModelTable;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.event.shared.EventBus;

/**
 * Base class for sub tab views used to show events using {@link EventListModelTable}.
 *
 * @param <I>
 *            Main tab table row data type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public abstract class AbstractSubTabEventView<I, M extends ListWithDetailsModel, D extends EventListModel> extends AbstractSubTabTableWidgetView<I, AuditLog, M, D> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public AbstractSubTabEventView(SearchableDetailModelProvider<AuditLog, M, D> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(new EventListModelTable<>(modelProvider, eventBus, clientStorage));
        generateIds();
        initTable();
        initWidget(getModelBoundTableWidget());
    }

    @Override
    protected void initTable() {
        super.initTable();

        getTable().addActionButton(new WebAdminButtonDefinition<AuditLog>(constants.details(),
                CommandLocation.OnlyFromContext) {
            @Override
            protected UICommand resolveCommand() {
                return getModelBoundTableWidget().getModel().getDetailsCommand();
            }
        });
    }

    protected abstract void generateIds();

}
