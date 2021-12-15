package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineAffinityGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabAffinityGroupsView;

import com.google.gwt.core.client.GWT;

public class SubTabVirtualMachineAffinityGroupView extends AbstractSubTabAffinityGroupsView<VM, VmListModel<Void>, VmAffinityGroupListModel>
        implements SubTabVirtualMachineAffinityGroupPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineAffinityGroupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVirtualMachineAffinityGroupView(SearchableDetailModelProvider<AffinityGroup, VmListModel<Void>, VmAffinityGroupListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected List<String> getVmNames(AffinityGroup group) {
        List<String> entityNames = super.getVmNames(group);
        if (getDetailModel().getEntity() != null) {
            entityNames.remove(getDetailModel().getEntity().getName());
        }
        return entityNames;
    }
}
