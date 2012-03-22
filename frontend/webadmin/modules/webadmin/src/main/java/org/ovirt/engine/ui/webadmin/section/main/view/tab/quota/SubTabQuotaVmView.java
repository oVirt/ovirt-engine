package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.storage.VMsTree;

public class SubTabQuotaVmView extends AbstractSubTabTreeView<VMsTree<QuotaVmListModel>, Quota, VM, QuotaListModel, QuotaVmListModel>
        implements SubTabQuotaVmPresenter.ViewDef {

    @Inject
    public SubTabQuotaVmView(SearchableDetailModelProvider<VM, QuotaListModel, QuotaVmListModel> modelProvider) {
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
