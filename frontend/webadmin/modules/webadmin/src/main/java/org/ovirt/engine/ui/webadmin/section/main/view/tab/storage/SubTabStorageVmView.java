package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.storage.VMsTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabStorageVmView extends AbstractSubTabTableView<storage_domains, VM, StorageListModel, StorageVmListModel>
        implements SubTabStorageVmPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SubTabStorageVmView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private class EmptyColumn extends TextColumn<VM> {
        @Override
        public String getValue(VM object) {
            return null;
        }
    }

    @UiField
    SimplePanel headerTableContainer;

    @UiField
    SimplePanel vmTreeContainer;

    final EntityModelCellTable<ListModel> table;

    final VMsTree tree;

    @Inject
    public SubTabStorageVmView(SearchableDetailModelProvider<VM, StorageListModel, StorageVmListModel> modelProvider) {
        super(modelProvider);

        table = new EntityModelCellTable<ListModel>(false, true);
        tree = new VMsTree();

        initHeader();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        headerTableContainer.add(table);
        vmTreeContainer.add(tree);

        getDetailModel().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                table.setRowData(new ArrayList<EntityModel>());
            }
        });
    }

    void initHeader() {
        table.addColumn(new EmptyColumn(), "Name");
        table.addColumn(new EmptyColumn(), "Disks", "80px");
        table.addColumn(new EmptyColumn(), "Template", "160px");
        table.addColumn(new EmptyColumn(), "V-Size", "110px");
        table.addColumn(new EmptyColumn(), "Actual Size", "110px");
        table.addColumn(new EmptyColumn(), "Creation Date", "170px");
    }

    @Override
    public void setMainTabSelectedItem(storage_domains selectedItem) {
        table.setLoadingState(LoadingState.LOADING);
        tree.clearTree();
        tree.updateTree(getDetailModel());
    }

}
