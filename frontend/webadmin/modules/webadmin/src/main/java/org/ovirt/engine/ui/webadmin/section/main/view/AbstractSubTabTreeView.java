package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.label.NoItemsLabel;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSubTabTreeView<E extends AbstractSubTabTree, I, T, M extends ListWithDetailsModel, D extends SearchableListModel> extends AbstractSubTabTableView<I, T, M, D> {

    interface ViewUiBinder extends UiBinder<Widget, AbstractSubTabTreeView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    protected SimplePanel headerTableContainer;

    @UiField
    protected SimplePanel treeContainer;

    @UiField
    protected SimplePanel actionPanelContainer;

    public EntityModelCellTable<ListModel> table;

    protected E tree;

    public AbstractSubTabTreeView(SearchableDetailModelProvider modelProvider) {
        super(modelProvider);

        table = new EntityModelCellTable<>(false, true);
        tree = getTree();

        initHeader();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        headerTableContainer.add(table);
        treeContainer.add(tree);
        treeContainer.addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainContentPresenter.TYPE_SetContent) {
            actionPanelContainer.add(content);
        } else if (slot == AbstractSubTabPresenter.TYPE_SetActionPanel) {
            if (content != null) {
                actionPanelContainer.add(content);
            } else {
                actionPanelContainer.clear();
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    private final IEventListener<EventArgs> itemsChangedListener = new IEventListener<EventArgs>() {
        @SuppressWarnings("unchecked")
        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            table.setRowData(new ArrayList<EntityModel>());
            // Since tree views don't have an 'emptyTreeWidget to display, we will
            // use the fact that we are using a table to display the 'header' to have
            // it display the no items to display message.
            if (sender instanceof ListModel) {
                ListModel model = (ListModel) sender;
                Iterable<M> items = model.getItems();
                if (model.getItems() == null || (items instanceof List && ((List<M>) items).isEmpty())) {
                    table.setEmptyTableWidget(new NoItemsLabel());
                } else {
                    table.setEmptyTableWidget(null);
                }
            }
        }
    };

    @Override
    public void setMainSelectedItem(I selectedItem) {
        table.setEmptyTableWidget(null);
        if (getDetailModel().getItems() == null) {
            table.setLoadingState(LoadingState.LOADING);
        }
        if (!getDetailModel().getItemsChangedEvent().getListeners().contains(itemsChangedListener)) {
            getDetailModel().getItemsChangedEvent().addListener(itemsChangedListener);
        }

        tree.clearTree();
        tree.updateTree(getDetailModel());
    }

    protected abstract void initHeader();

    protected abstract E getTree();

    @Override
    protected boolean useTableWidgetForContent() {
        return false;
    }

    @Override
    protected void generateIds() {
        //Do nothing, we don't want the tree tables to have ids.
    }
}
