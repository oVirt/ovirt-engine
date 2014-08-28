package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmSessionsModelForm;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSessionsPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabVirtualMachineSessionsView
    extends AbstractSubTabFormView<VM, VmListModel, VmSessionsModel>
    implements SubTabVirtualMachineSessionsPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SubTabVirtualMachineSessionsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineSessionsView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    VmSessionsModelForm form;

    @Inject
    public SubTabVirtualMachineSessionsView(DetailModelProvider<VmListModel, VmSessionsModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);

        this.form = new VmSessionsModelForm(modelProvider, constants);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(VM selectedItem) {
        form.update();
    }

}
