package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.SubTableHeaderlessResources;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable.Resources;

/**
 * Base class for sub tab views that use {@link SimpleActionTable} directly.
 *
 * @param <I> Main tab table row data type.
 * @param <T> Sub tab table row data type.
 * @param <M> Main model type (extends ListWithDetailsModel)
 * @param <D> Detail model type (extends SearchableListModel)
 */
public abstract class AbstractSubTabTableView<I, T, M extends ListWithDetailsModel, D extends SearchableListModel> extends AbstractView implements AbstractSubTabPresenter.ViewDef<I> {

    private final SearchableDetailModelProvider<T, M, D> modelProvider;

    @WithElementId
    public final SimpleActionTable<T> table;

    public AbstractSubTabTableView(SearchableDetailModelProvider<T, M, D> modelProvider) {
        this.modelProvider = modelProvider;
        this.table = createActionTable();
        generateIds();
    }

    protected SimpleActionTable<T> createActionTable() {
        return new SimpleActionTable<T>(modelProvider, getTableHeaderlessResources(), getTableResources(),
                ClientGinjectorProvider.getEventBus(), ClientGinjectorProvider.getClientStorage()) {
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

    protected Resources getTableHeaderlessResources() {
        return GWT.<Resources> create(SubTableHeaderlessResources.class);
    }

    protected Resources getTableResources() {
        return GWT.<Resources> create(SubTableResources.class);
    }

    protected D getDetailModel() {
        return modelProvider.getModel();
    }

    @Override
    public SimpleActionTable<T> getTable() {
        return table;
    }

    @Override
    public void setMainTabSelectedItem(I selectedItem) {
        // No-op since table-based sub tab views don't handle main tab selection on their own
    }

    protected abstract void generateIds();
}
