package org.ovirt.engine.ui.common.widget.uicommon;

import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.SubTableHeaderlessResources;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for widgets that use {@link SimpleActionTable} to represent UiCommon list models.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            List model type.
 */
public abstract class AbstractModelBoundTableWidget<T, M extends SearchableListModel> extends Composite {

    private final SearchableTableModelProvider<T, M> modelProvider;

    private final EventBus eventBus;
    private final boolean useMainTableResources;

    private final SimpleActionTable<T> table;

    public AbstractModelBoundTableWidget(SearchableTableModelProvider<T, M> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, boolean useMainTableResources) {
        this.modelProvider = modelProvider;
        this.eventBus = eventBus;
        this.useMainTableResources = useMainTableResources;
        this.table = createActionTable(eventBus, clientStorage);
        initWidget(getWrappedWidget());
    }

    SimpleActionTable<T> createActionTable(EventBus eventBus, ClientStorage clientStorage) {
        return new SimpleActionTable<T>(modelProvider,
                getTableHeaderlessResources(), getTableResources(),
                eventBus, clientStorage) {
            {
                if (useTableWidgetForContent()) {
                    enableHeaderContextMenu();
                }
            }
        };
    }

    /**
     * Returns {@code true} if table content is provided by the {@link #table} widget itself.
     * Returns {@code false} if table content is provided by a custom widget, e.g. a tree.
     */
    protected boolean useTableWidgetForContent() {
        return true;
    }

    /**
     * @return Widget passed to the {@linkplain Composite#initWidget initWidget} method.
     */
    protected Widget getWrappedWidget() {
        return table;
    }

    private Resources getTableResources() {
        return useMainTableResources ? GWT.<Resources> create(MainTableResources.class)
                : GWT.<Resources> create(SubTableResources.class);
    }

    private Resources getTableHeaderlessResources() {
        return useMainTableResources ? GWT.<Resources> create(MainTableHeaderlessResources.class)
                : GWT.<Resources> create(SubTableHeaderlessResources.class);
    }

    public M getModel() {
        return modelProvider.getModel();
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    public SimpleActionTable<T> getTable() {
        return table;
    }

    /**
     * Performs initialization of the table widget.
     */
    public abstract void initTable();

    public void addModelListeners() { }
}
