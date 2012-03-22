package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.storage.VMsTree;

import com.google.inject.Inject;

public class SubTabStorageVmView extends AbstractSubTabTreeView<VMsTree<StorageVmListModel>, storage_domains, VM, StorageListModel, StorageVmListModel>
        implements SubTabStorageVmPresenter.ViewDef {

    @Inject
    public SubTabStorageVmView(SearchableDetailModelProvider<VM, StorageListModel, StorageVmListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void initHeader() {
        table.addColumn(new EmptyColumn(), "Name");
        table.addColumn(new EmptyColumn(), "Disks", "80px");
        table.addColumn(new EmptyColumn(), "Template", "160px");
        table.addColumn(new EmptyColumn(), "V-Size", "110px");
        table.addColumn(new EmptyColumn(), "Actual Size", "110px");
        table.addColumn(new EmptyColumn(), "Creation Date", "170px");
    }

    @Override
    protected VMsTree getTree() {
        return new VMsTree(resources, constants);
    }

}
