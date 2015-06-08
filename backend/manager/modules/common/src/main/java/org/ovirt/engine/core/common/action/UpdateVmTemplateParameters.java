package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class UpdateVmTemplateParameters extends VmTemplateParametersBase implements HasVmIcon {
    private static final long serialVersionUID = 7250355162926369307L;
    @Valid
    private VmTemplate _vmTemplate;
    /*
     * This parameter is used to decide if to create or remove sound device
     * if it is null then the current configuration will remain
     */
    private Boolean soundDeviceEnabled;

    /*
     * This parameter is used to decide if to create or remove console device
     * if it is null then the current configuration will remain
     */
    private Boolean consoleEnabled;

    /**
     * If not null, {@code UpdateVmTemplateCommand} should update icon of the template to this value.
     */
    private String vmLargeIcon;

    /**
     * @see #vmLargeIcon
     */
    public String getVmLargeIcon() {
        return vmLargeIcon;
    }

    /**
     * @see #vmLargeIcon
     */
    public void setVmLargeIcon(String vmLargeIcon) {
        this.vmLargeIcon = vmLargeIcon;
    }

    public UpdateVmTemplateParameters(VmTemplate vmTemplate) {
        _vmTemplate = vmTemplate;
    }

    public VmTemplate getVmTemplateData() {
        return _vmTemplate;
    }

    public UpdateVmTemplateParameters() {
    }

    public Boolean isSoundDeviceEnabled() {
        return soundDeviceEnabled;
    }

    public void setSoundDeviceEnabled(boolean soundDeviceEnabled) {
        this.soundDeviceEnabled = soundDeviceEnabled;
    }

    public Boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public void setConsoleEnabled(Boolean consoleEnabled) {
        this.consoleEnabled = consoleEnabled;
    }

}
