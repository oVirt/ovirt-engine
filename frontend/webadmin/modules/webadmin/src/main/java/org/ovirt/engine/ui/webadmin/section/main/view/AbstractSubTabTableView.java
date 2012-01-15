package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.view.AbstractView;
import org.ovirt.engine.ui.webadmin.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.webadmin.widget.table.SimpleActionTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;

/**
 * Base class for table-based sub tab views.
 *
 * @param <I>
 *            Main tab table row data type.
 * @param <T>
 *            Sub tab table row data type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public abstract class AbstractSubTabTableView<I, T, M extends ListWithDetailsModel, D extends SearchableListModel> extends AbstractView implements AbstractSubTabPresenter.ViewDef<I> {

    private final SearchableDetailModelProvider<T, M, D> modelProvider;

    @WithElementId
    public final SimpleActionTable<T> table;

    public AbstractSubTabTableView(SearchableDetailModelProvider<T, M, D> modelProvider) {
        this.modelProvider = modelProvider;
        this.table = new SimpleActionTable<T>(modelProvider, getTableHeaderlessResources(), getTableResources());
    }

    protected Resources getTableHeaderlessResources() {
        return GWT.<Resources> create(SubTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return GWT.<Resources> create(SubTableResources.class);
    }

    @Override
    public void setMainTabSelectedItem(I selectedItem) {
        // No-op since table-based sub tab views don't handle main tab selection on their own
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    protected SimpleActionTable<T> getTable() {
        return table;
    }

    public interface SubTableHeaderlessResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TabCellTableHeaderless.css" })
        TableStyle cellTableStyle();
    }

    public interface SubTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TabCellTable.css" })
        TableStyle cellTableStyle();
    }

    @Override
    public OrderedMultiSelectionModel<?> getTableSelectionModel() {
        return getTable().getSelectionModel();
    }

    @Override
    public void setLoadingState(LoadingState state) {
        getTable().setLoadingState(state);
    }

}
