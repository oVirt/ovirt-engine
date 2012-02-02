package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.table.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.AbstractSideTabTablePresenter;
import org.ovirt.engine.ui.userportal.widget.table.SimpleActionTable;

public abstract class AbstractSideTabTableView<T, M extends SearchableListModel> extends AbstractView implements AbstractSideTabTablePresenter.ViewDef<T> {

    private final SearchableTableModelProvider<T, M> modelProvider;
    private final SimpleActionTable<T> table;

    public AbstractSideTabTableView(SearchableTableModelProvider<T, M> modelProvider) {
        this.modelProvider = modelProvider;
        this.table = new SimpleActionTable<T>(modelProvider);
        this.table.showRefreshButton();
    }

    @Override
    public OrderedMultiSelectionModel<T> getTableSelectionModel() {
        return table.getSelectionModel();
    }

    protected M getModel() {
        return modelProvider.getModel();
    }

    protected SimpleActionTable<T> getTable() {
        return table;
    }

}
