package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class VmGeneralModelForm extends AbstractModelBoundFormWidget<VmGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<VmGeneralModel, VmGeneralModelForm> {
        Driver driver = GWT.create(Driver.class);
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel quotaName = new TextBoxLabel();
    TextBoxLabel template = new TextBoxLabel();
    TextBoxLabel definedMemory = new TextBoxLabel();
    TextBoxLabel minAllocatedMemory = new TextBoxLabel();
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel cpuInfo = new TextBoxLabel();
    TextBoxLabel defaultDisplayType = new TextBoxLabel();
    TextBoxLabel origin = new TextBoxLabel();
    TextBoxLabel priority = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();
    TextBoxLabel defaultHost = new TextBoxLabel();
    TextBoxLabel customProperties = new TextBoxLabel();
    TextBoxLabel domain = new TextBoxLabel();
    TextBoxLabel timeZone = new TextBoxLabel();

    BooleanLabel isHighlyAvailable = new BooleanLabel("Yes", "No");

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();

    public VmGeneralModelForm(ModelProvider<VmGeneralModel> modelProvider) {
        super(modelProvider, 3, 7);
        Driver.driver.initialize(this);

        formBuilder.setColumnsWidth("120px", "240px", "160px");
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        formBuilder.addFormItem(new FormItem("Description", description, 1, 0));
        formBuilder.addFormItem(new FormItem("Template", template, 2, 0));
        formBuilder.addFormItem(new FormItem("Operating System", oS, 3, 0));
        formBuilder.addFormItem(new FormItem("Default Display Type", defaultDisplayType, 4, 0));
        formBuilder.addFormItem(new FormItem("Priority", priority, 5, 0) {
            @Override
            public boolean isVisible() {
                return getModel().getHasPriority();
            }
        });
        formBuilder.addFormItem(new FormItem("Defined Memory", definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem("Physical Memory Guaranteed", minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem("Number of CPU Cores", cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem("Highly Available", isHighlyAvailable, 3, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasHighlyAvailable();
            }
        });
        formBuilder.addFormItem(new FormItem("Number of Monitors", monitorCount, 3, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasMonitorCount();
            }
        });
        formBuilder.addFormItem(new FormItem("USB Policy", usbPolicy, 4, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasUsbPolicy();
            }
        });

        formBuilder.addFormItem(new FormItem("Quota", quotaName, 5, 2) {
            @Override
            public boolean isVisible() {
                return true;
            }
        });

        formBuilder.addFormItem(new FormItem("Origin", origin, 0, 2));
        formBuilder.addFormItem(new FormItem("Run On", defaultHost, 1, 2));
        formBuilder.addFormItem(new FormItem("Custom Properties", customProperties, 2, 2));
        formBuilder.addFormItem(new FormItem("Domain", domain, 3, 2) {
            @Override
            public boolean isVisible() {
                return getModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem("Time Zone", timeZone, 4, 2) {
            @Override
            public boolean isVisible() {
                return getModel().getHasTimeZone();
            }
        });
    }

    @Override
    protected void doEdit(VmGeneralModel model) {
        Driver.driver.edit(model);

        // TODO required because of GWT#5864
        monitorCount.setText(Integer.toString(getModel().getMonitorCount()));
    }

}
