package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.VmErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabErrataCountView;

import com.google.inject.Inject;

/**
 * View for the sub tab that shows errata counts for a VM selected in the main tab.
 */
public class SubTabVirtualMachineErrataView extends AbstractSubTabErrataCountView<VM, VmListModel<Void>, VmErrataCountModel>
        implements SubTabVirtualMachineErrataPresenter.ViewDef {

    @Inject
    public SubTabVirtualMachineErrataView(
            DetailTabModelProvider<VmListModel<Void>, VmErrataCountModel> modelProvider) {
        super(modelProvider);
    }
}
