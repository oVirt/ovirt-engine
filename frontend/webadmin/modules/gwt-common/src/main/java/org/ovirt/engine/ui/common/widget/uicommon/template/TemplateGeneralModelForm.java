package org.ovirt.engine.ui.common.widget.uicommon.template;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class TemplateGeneralModelForm extends AbstractModelBoundFormWidget<TemplateGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<TemplateGeneralModel, TemplateGeneralModelForm> {
        Driver driver = GWT.create(Driver.class);
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel hostCluster = new TextBoxLabel();
    TextBoxLabel definedMemory = new TextBoxLabel();
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel cpuInfo = new TextBoxLabel();
    TextBoxLabel defaultDisplayType = new TextBoxLabel();
    TextBoxLabel origin = new TextBoxLabel();
    TextBoxLabel priority = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();
    TextBoxLabel domain = new TextBoxLabel();
    TextBoxLabel timeZone = new TextBoxLabel();
    TextBoxLabel quotaName = new TextBoxLabel();

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();
    @Ignore
    TextBoxLabel isHighlyAvailable = new TextBoxLabel();
    @Ignore
    TextBoxLabel isStateless = new TextBoxLabel();

    public TemplateGeneralModelForm(ModelProvider<TemplateGeneralModel> modelProvider) {
        super(modelProvider, 3, 6);
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder.setColumnsWidth("120px", "240px", "160px");
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        formBuilder.addFormItem(new FormItem("Description", description, 1, 0));
        formBuilder.addFormItem(new FormItem("Host Cluster", hostCluster, 2, 0));
        formBuilder.addFormItem(new FormItem("Operating System", oS, 3, 0));
        formBuilder.addFormItem(new FormItem("Default Display Type", defaultDisplayType, 4, 0));

        formBuilder.addFormItem(new FormItem("Defined Memory", definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem("Number of CPU Cores", cpuInfo, 1, 1));
        formBuilder.addFormItem(new FormItem("Number of Monitors", monitorCount, 2, 1));
        formBuilder.addFormItem(new FormItem("Highly Available", isHighlyAvailable, 3, 1));
        formBuilder.addFormItem(new FormItem("Priority", priority, 4, 1));
        formBuilder.addFormItem(new FormItem("USB Policy", usbPolicy, 5, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasUsbPolicy();
            }
        });

        formBuilder.addFormItem(new FormItem("Origin", origin, 0, 2));
        formBuilder.addFormItem(new FormItem("Is Stateless", isStateless, 1, 2));
        formBuilder.addFormItem(new FormItem("Domain", domain, 2, 2) {
            @Override
            public boolean isVisible() {
                return getModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem("Time Zone", timeZone, 3, 2) {
            @Override
            public boolean isVisible() {
                return getModel().getHasTimeZone();
            }
        });

        formBuilder.addFormItem(new FormItem("Quota", quotaName, 4, 2));
    }

    @Override
    protected void doEdit(TemplateGeneralModel model) {
        Driver.driver.edit(model);

        // TODO required because of GWT#5864
        monitorCount.setText(Integer.toString(getModel().getMonitorCount()));
        isHighlyAvailable.setText(Boolean.toString(getModel().getIsHighlyAvailable()));
        isStateless.setText(Boolean.toString(getModel().getIsStateless()));
    }
}

