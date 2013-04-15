package org.ovirt.engine.ui.common.widget.uicommon.template;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.FormItemWithDefaultValue;
import org.ovirt.engine.ui.common.widget.form.FormItemWithDefaultValue.Condition;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class TemplateGeneralModelForm extends AbstractModelBoundFormWidget<TemplateGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<TemplateGeneralModel, TemplateGeneralModelForm> {
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

    BooleanLabel isHighlyAvailable;

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();
    @Ignore
    TextBoxLabel isStateless = new TextBoxLabel();

    private final Driver driver = GWT.create(Driver.class);

    public TemplateGeneralModelForm(ModelProvider<TemplateGeneralModel> modelProvider, CommonApplicationConstants constants) {
        super(modelProvider, 3, 6);
        driver.initialize(this);
        isHighlyAvailable = new BooleanLabel(constants.yes(), constants.no());

        // Build a form using the FormBuilder
        formBuilder.setColumnsWidth("120px", "240px", "160px"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        formBuilder.addFormItem(new FormItem(constants.nameTemplateGeneral(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionTemplateGeneral(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.hostClusterTemplateGeneral(), hostCluster, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osTemplateGeneral(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.defaultDisTypeTemplateGeneral(), defaultDisplayType, 4, 0));

        formBuilder.addFormItem(new FormItem(constants.definedMemTemplateGeneral(), definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresTemplateGeneral(), cpuInfo, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsTemplateGeneral(), monitorCount, 2, 1));
        formBuilder.addFormItem(new FormItem(constants.highlyAvailTemplateGeneral(), isHighlyAvailable, 3, 1){
            @Override
            public boolean isVisible() {
                return getModel().getHasHighlyAvailable();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.priorityTemplateGeneral(), priority, 4, 1));
        formBuilder.addFormItem(new FormItem(constants.usbPolicyTemplateGeneral(), usbPolicy, 5, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasUsbPolicy();
            }
        });

        formBuilder.addFormItem(new FormItem(constants.originTemplateGeneral(), origin, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.isStatelessTemplateGeneral(), isStateless, 1, 2));
        formBuilder.addFormItem(new FormItem(constants.domainTemplateGeneral(), domain, 2, 2) {
            @Override
            public boolean isVisible() {
                return getModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.tzTemplateGeneral(), timeZone, 3, 2) {
            @Override
            public boolean isVisible() {
                return getModel().getHasTimeZone();
            }
        });

        formBuilder.addFormItem(new FormItemWithDefaultValue(constants.quotaTemplateGeneral(),
                quotaName,
                4,
                2,
                constants.notConfigured(),
                new Condition() {

                    @Override
                    public boolean isTrue() {
                        String quotaName = getModel().getQuotaName();
                        return quotaName != null && quotaName != "";
                    }
                }) {
            @Override
            public boolean isVisible() {
                return getModel().isQuotaAvailable();
            }
        });
    }

    @Override
    protected void doEdit(TemplateGeneralModel model) {
        driver.edit(model);

        // TODO required because of GWT#5864
        monitorCount.setText(Integer.toString(getModel().getMonitorCount()));
        isStateless.setText(Boolean.toString(getModel().getIsStateless()));
    }
}

