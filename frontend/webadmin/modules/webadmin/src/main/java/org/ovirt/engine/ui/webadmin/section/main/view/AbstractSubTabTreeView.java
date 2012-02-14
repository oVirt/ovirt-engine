package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.storage.AbstractSubTabTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSubTabTreeView<E extends AbstractSubTabTree, I, T, M extends ListWithDetailsModel, D extends SearchableListModel> extends AbstractSubTabTableView<I, T, M, D> {

    interface ViewUiBinder extends UiBinder<Widget, AbstractSubTabTreeView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel headerTableContainer;

    @UiField
    SimplePanel treeContainer;

    protected EntityModelCellTable<ListModel> table;

    protected E tree;

    public AbstractSubTabTreeView(SearchableDetailModelProvider modelProvider) {
        super(modelProvider);

        table = new EntityModelCellTable<ListModel>(false, true);
        tree = getTree();

        initHeader();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        headerTableContainer.add(table);
        treeContainer.add(tree);

        getDetailModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                table.setRowData(new ArrayList<EntityModel>());
            }
        });
    }

    public class EmptyColumn extends TextColumn<VM> {
        @Override
        public String getValue(VM object) {
            return null;
        }
    }

    @Override
    public void setMainTabSelectedItem(I selectedItem) {
        table.setLoadingState(LoadingState.LOADING);
        tree.clearTree();
        tree.updateTree(getDetailModel());
    }

    protected abstract void initHeader();

    protected abstract E getTree();
}
