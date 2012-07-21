package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTreeView;
import org.ovirt.engine.ui.webadmin.widget.storage.VMsTree;

public class SubTabQuotaVmView extends AbstractSubTabTreeView<VMsTree<QuotaVmListModel>, Quota, VM, QuotaListModel, QuotaVmListModel>
        implements SubTabQuotaVmPresenter.ViewDef {

    @Inject
    public SubTabQuotaVmView(SearchableDetailModelProvider<VM, QuotaListModel, QuotaVmListModel> modelProvider,
            ApplicationConstants constants, ApplicationTemplates templates, ApplicationResources resources) {
        super(modelProvider, constants, templates, resources);
    }

    @Override
    protected void initHeader(ApplicationConstants constants) {
        table.addColumn(new EmptyColumn(), constants.aliasVm());
        table.addColumn(new EmptyColumn(), constants.disksVm(), "80px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.templateVm(), "160px");  //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.vSizeVm(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.actualSizeVm(), "110px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.creationDateVm(), "170px"); //$NON-NLS-1$
    }

    @Override
    protected VMsTree getTree() {
        return new VMsTree(resources, constants, templates);
    }

}
