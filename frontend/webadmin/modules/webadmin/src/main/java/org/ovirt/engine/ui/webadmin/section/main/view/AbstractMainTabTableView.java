package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable.Resources;

/**
 * Base class for table-based main tab views.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Main model type.
 */
public abstract class AbstractMainTabTableView<T, M extends SearchableListModel> extends AbstractView {

    private final MainModelProvider<T, M> modelProvider;

    @WithElementId
    public final SimpleActionTable<T> table;

    public AbstractMainTabTableView(MainModelProvider<T, M> modelProvider) {
        this.modelProvider = modelProvider;
        this.table = createActionTable();
    }

    protected SimpleActionTable<T> createActionTable() {
        return new SimpleActionTable<T>(modelProvider, getTableHeaderlessResources(), getTableResources(),
                ClientGinjectorProvider.getEventBus(), ClientGinjectorProvider.getClientStorage()) {
            {
                showRefreshButton();
                showPagingButtons();
                showItemsCount();
                showSelectionCountTooltip();
                enableHeaderContextMenu();
            }
        };
    }

    protected Resources getTableHeaderlessResources() {
        return (Resources) GWT.create(MainTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return (Resources) GWT.create(MainTableResources.class);
    }

    protected M getMainModel() {
        return modelProvider.getModel();
    }

    public SimpleActionTable<T> getTable() {
        return table;
    }

    public MainModelProvider<T, M> getModelProvider() {
        return modelProvider;
    }

}
