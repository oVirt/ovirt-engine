package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmGuestInfoModelForm;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGuestInfoPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabVirtualMachineGuestInfoView
    extends AbstractSubTabFormView<VM, VmListModel<Void>, VmGuestInfoModel>
    implements SubTabVirtualMachineGuestInfoPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SubTabVirtualMachineGuestInfoView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineGuestInfoView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    VmGuestInfoModelForm form;

    @Inject
    public SubTabVirtualMachineGuestInfoView(DetailModelProvider<VmListModel<Void>, VmGuestInfoModel> modelProvider) {
        super(modelProvider);

        this.form = new VmGuestInfoModelForm(modelProvider);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(VM selectedItem) {
        form.update();
    }

}
