package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.VmAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineAffinityLabelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabAffinityLabelsView;

import com.google.gwt.core.client.GWT;

public class SubTabVirtualMachineAffinityLabelView extends AbstractSubTabAffinityLabelsView<VM, VmListModel<Void>, VmAffinityLabelListModel>
        implements SubTabVirtualMachineAffinityLabelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineAffinityLabelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVirtualMachineAffinityLabelView(SearchableDetailModelProvider<Label, VmListModel<Void>, VmAffinityLabelListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
