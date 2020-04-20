package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid.Resources;

/**
 * Base class for table-based main tab views.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Main model type.
 */
public abstract class AbstractMainTableView<T, M extends SearchableListModel> extends AbstractView {

    private final MainModelProvider<T, M> modelProvider;

    @WithElementId
    public final SimpleActionTable<Void, T> table;

    public AbstractMainTableView(MainModelProvider<T, M> modelProvider) {
        this.modelProvider = modelProvider;
        this.table = createActionTable();
    }

    protected SimpleActionTable<Void, T> createActionTable() {
        return new SimpleActionTable<Void, T>(modelProvider, getTableResources(),
                ClientGinjectorProvider.getEventBus(), ClientGinjectorProvider.getClientStorage()) {
            {
                showRefreshButton();
                showItemsCount();
                showSelectionCountTooltip();
                enableHeaderContextMenu();
            }
        };
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

    protected M getMainModel() {
        return modelProvider.getModel();
    }

    public SimpleActionTable<Void, T> getTable() {
        return table;
    }

    public MainModelProvider<T, M> getModelProvider() {
        return modelProvider;
    }

}
