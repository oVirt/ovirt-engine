package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
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
    }

    private final Driver driver = GWT.create(Driver.class);

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
    TextBoxLabel compatibilityVersion = new TextBoxLabel();

    BooleanLabel isHighlyAvailable;

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();

    public VmGeneralModelForm(ModelProvider<VmGeneralModel> modelProvider, CommonApplicationConstants constants) {
        super(modelProvider, 3, 7);
        isHighlyAvailable = new BooleanLabel(constants.yes(), constants.no());

        driver.initialize(this);

        formBuilder.setColumnsWidth("120px", "240px", "160px"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        formBuilder.addFormItem(new FormItem(constants.nameVm(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionVm(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.templateVm(), template, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.osVm(), oS, 3, 0));
        formBuilder.addFormItem(new FormItem(constants.defaultDisplayTypeVm(), defaultDisplayType, 4, 0));
        formBuilder.addFormItem(new FormItem(constants.priorityVm(), priority, 5, 0) {
            @Override
            public boolean isVisible() {
                return getModel().getHasPriority();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.definedMemoryVm(), definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.physMemGauranteedVm(), minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresVm(), cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem(constants.highlyAvailableVm(), isHighlyAvailable, 3, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasHighlyAvailable();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.numOfMonitorsVm(), monitorCount, 3, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasMonitorCount();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.usbPolicyVm(), usbPolicy, 4, 1) {
            @Override
            public boolean isVisible() {
                return getModel().getHasUsbPolicy();
            }
        });

        formBuilder.addFormItem(new FormItem(constants.originVm(), origin, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.runOnVm(), defaultHost, 1, 2));
        formBuilder.addFormItem(new FormItem(constants.customPropertiesVm(), customProperties, 2, 2));
        formBuilder.addFormItem(new FormItem(constants.clusterCompatibilityVersionVm(), compatibilityVersion, 3, 2));
        formBuilder.addFormItem(new FormItem(constants.quotaVm(), quotaName, 4, 2) {

            @Override
            public boolean isVisible() {
                return getModel().isQuotaAvailable();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.domainVm(), domain, 5, 2) {
            @Override
            public boolean isVisible() {
                return getModel().getHasDomain();
            }
        });

//        Removed because VDSM and guest agent don't support returning of the time zone within the guest.
//        TODO: Uncomment again once this will be implemented.
//
//        formBuilder.addFormItem(new FormItem(constants.timeZoneVm(), timeZone, 6, 2) {
//            @Override
//            public boolean isVisible() {
//                return getModel().getHasTimeZone();
//            }
//        });
    }

    @Override
    protected void doEdit(VmGeneralModel model) {
        driver.edit(model);

        // TODO required because of GWT#5864
        monitorCount.setText(Integer.toString(getModel().getMonitorCount()));
    }

}
