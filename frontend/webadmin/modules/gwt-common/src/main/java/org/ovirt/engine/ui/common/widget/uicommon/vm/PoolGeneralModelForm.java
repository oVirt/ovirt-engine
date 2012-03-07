package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class PoolGeneralModelForm extends AbstractModelBoundFormWidget<PoolGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<PoolGeneralModel, PoolGeneralModelForm> {
        Driver driver = GWT.create(Driver.class);
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel template = new TextBoxLabel();
    TextBoxLabel definedMemory = new TextBoxLabel();
    TextBoxLabel minAllocatedMemory = new TextBoxLabel();
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel cpuInfo = new TextBoxLabel();
    TextBoxLabel defaultDisplayType = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();
    TextBoxLabel defaultHost = new TextBoxLabel();

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();

    public PoolGeneralModelForm(ModelProvider<PoolGeneralModel> modelProvider) {
        super(modelProvider, 3, 7);
        Driver.driver.initialize(this);

        formBuilder.setColumnsWidth("120px", "240px", "160px");
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        formBuilder.addFormItem(new FormItem("Description", description, 1, 0));
        formBuilder.addFormItem(new FormItem("Template", template, 2, 0));
        formBuilder.addFormItem(new FormItem("Operating System", oS, 3, 0));
        formBuilder.addFormItem(new FormItem("Default Display Type", defaultDisplayType, 4, 0));

        formBuilder.addFormItem(new FormItem("Defined Memory", definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem("Physical Memory Guaranteed", minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem("Number of CPU Cores", cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem("Number of Monitors", monitorCount, 3, 1));
        formBuilder.addFormItem(new FormItem("USB Policy", usbPolicy, 4, 1));

        formBuilder.addFormItem(new FormItem("Run On", defaultHost, 0, 2));
    }

    @Override
    protected void doEdit(PoolGeneralModel model) {
        Driver.driver.edit(model);

        // TODO required because of GWT#5864
        monitorCount.setText(Integer.toString(getModel().getMonitorCount()));
    }

}
