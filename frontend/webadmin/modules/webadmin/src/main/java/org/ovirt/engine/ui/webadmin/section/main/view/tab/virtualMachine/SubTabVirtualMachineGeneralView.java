package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmGeneralModelForm;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabVirtualMachineGeneralView extends AbstractSubTabFormView<VM, VmListModel, VmGeneralModel> implements SubTabVirtualMachineGeneralPresenter.ViewDef, Editor<VmGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabVirtualMachineGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    VmGeneralModelForm form;

    @Inject
    public SubTabVirtualMachineGeneralView(DetailModelProvider<VmListModel, VmGeneralModel> modelProvider) {
        super(modelProvider);
        this.form = new VmGeneralModelForm(modelProvider);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setMainTabSelectedItem(VM selectedItem) {
        form.update();
    }

}
