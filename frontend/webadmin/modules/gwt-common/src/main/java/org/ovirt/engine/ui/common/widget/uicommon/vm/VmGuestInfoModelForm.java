package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;

import com.google.gwt.core.client.GWT;

public class VmGuestInfoModelForm extends AbstractModelBoundFormWidget<VmGuestInfoModel> {

    interface Driver extends UiCommonEditorDriver<VmGuestInfoModel, VmGuestInfoModelForm> {
    }

    private final Driver driver = GWT.create(Driver.class);

    StringValueLabel guestUserName = new StringValueLabel();
    StringValueLabel guestOsArch = new StringValueLabel();
    StringValueLabel guestOsType = new StringValueLabel();
    StringValueLabel guestOsNamedVersion = new StringValueLabel();
    StringValueLabel guestOsKernelVersion = new StringValueLabel();
    StringValueLabel guestOsTimezone = new StringValueLabel();
    StringValueLabel consoleUserName = new StringValueLabel();
    StringValueLabel clientIp = new StringValueLabel();

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmGuestInfoModelForm(ModelProvider<VmGuestInfoModel> modelProvider) {
        super(modelProvider, 3, 4);
        driver.initialize(this);

        // First row - OS Info
        formBuilder.addFormItem(new FormItem(constants.guestOsType(), guestOsType, 0, 0)
            .withDefaultValue(constants.unknown(), () -> getModel().getGuestOsType().equals(OsType.Other.toString())));
        formBuilder.addFormItem(new FormItem(constants.guestOsArchitecture(), guestOsArch, 1, 0)
            .withDefaultValue(constants.unknown(), () -> getModel().getGuestOsType().equals(OsType.Other.toString())));
        formBuilder.addFormItem(new FormItem(constants.guestOperatingSystem(), guestOsNamedVersion, 2, 0)
            .withDefaultValue(constants.unknown(), () -> getModel().getGuestOsType().equals(OsType.Other.toString())));
        // The kernel version is only reported and displayed for Linux based systems
        formBuilder.addFormItem(new FormItem(constants.guestOsKernelInfo(), guestOsKernelVersion, 3, 0) {
            @Override
            public boolean getIsAvailable() {
                return getModel().getGuestOsType().equals(OsType.Linux.toString());
            }
        });

        // Second row - Timezone Info
        formBuilder.addFormItem(new FormItem(constants.guestOsTimezone(), guestOsTimezone, 0, 1)
            .withDefaultValue(constants.unknown(), () -> getModel().getGuestOsType().equals(OsType.Other.toString())));

        // Third row - Logged In User & Console Info
        formBuilder.addFormItem(new FormItem(constants.loggedInUserVm(), guestUserName, 0, 2));
        formBuilder.addFormItem(new FormItem(constants.consoleConnectedUserVm(), consoleUserName, 1, 2));
        formBuilder.addFormItem(new FormItem(constants.consoleConnectedClientIp(), clientIp, 2, 2));
    }

    @Override
    protected void doEdit(VmGuestInfoModel model) {
        driver.edit(model);
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

}
