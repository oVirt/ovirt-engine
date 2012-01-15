package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.view.AbstractView;
import org.ovirt.engine.ui.webadmin.widget.table.SimpleActionTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
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
        this.table = new SimpleActionTable<T>(modelProvider, getTableHeaderlessResources(), getTableResources());
        this.table.showRefreshButton();
        this.table.showPagingButtons();
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

    protected SimpleActionTable<T> getTable() {
        return table;
    }

    public interface MainTableHeaderlessResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TabCellTableHeaderless.css" })
        TableStyle cellTableStyle();
    }

    public interface MainTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TabCellTable.css" })
        TableStyle cellTableStyle();
    }

}
