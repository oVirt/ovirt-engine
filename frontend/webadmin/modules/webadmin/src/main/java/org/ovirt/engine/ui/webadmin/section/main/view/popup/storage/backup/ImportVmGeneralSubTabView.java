package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmImportGeneralModelForm;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportGeneralModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class ImportVmGeneralSubTabView  extends AbstractSubTabFormView<VM, ImportVmModel, VmImportGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, ImportVmGeneralSubTabView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ImportVmGeneralSubTabView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @WithElementId
    VmImportGeneralModelForm form;

    @Inject
    public ImportVmGeneralSubTabView(DetailModelProvider<ImportVmModel, VmImportGeneralModel> modelProvider) {
        super(modelProvider);
        this.form = new VmImportGeneralModelForm(modelProvider);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        generateIds();

        form.initialize();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(VM selectedItem) {
        form.update();
    }

    public void cleanup() {
        form.cleanup();
    }

}
