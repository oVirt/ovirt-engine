package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;

import com.google.inject.Inject;

public class SubTabVirtualMachineEventView extends AbstractSubTabEventView<VM, VmListModel, VmEventListModel>
        implements SubTabVirtualMachineEventPresenter.ViewDef {

    @Inject
    public SubTabVirtualMachineEventView(SearchableDetailModelProvider<AuditLog, VmListModel, VmEventListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
