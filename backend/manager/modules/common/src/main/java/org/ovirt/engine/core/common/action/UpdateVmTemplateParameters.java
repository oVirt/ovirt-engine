package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Version;

public class UpdateVmTemplateParameters extends VmTemplateManagementParameters implements HasVmIcon, HasRngDevice {
    private static final long serialVersionUID = 7250355162926369307L;
    @Valid
    private VmTemplate _vmTemplate;
    /*
     * This parameter is used to decide if to create or remove sound device
     * if it is null then the current configuration will remain
     */
    private Boolean soundDeviceEnabled;

    /*
     * This parameter is used to decide if to create or remove TPM device
     * if it is null then the current configuration will remain
     */
    private Boolean tpmEnabled;

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
     * If non-null it indicates that cluster cluster level of current cluster was changed.
     */
    private Version clusterLevelChangeFromVersion;

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

    public Boolean isTpmEnabled() {
        return tpmEnabled;
    }

    public void setTpmEnabled(Boolean tpmEnabled) {
        this.tpmEnabled = tpmEnabled;
    }

    public Boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public void setConsoleEnabled(Boolean consoleEnabled) {
        this.consoleEnabled = consoleEnabled;
    }

    public Version getClusterLevelChangeFromVersion() {
        return clusterLevelChangeFromVersion;
    }

    public void setClusterLevelChangeFromVersion(Version clusterLevelChangeFromVersion) {
        this.clusterLevelChangeFromVersion = clusterLevelChangeFromVersion;
    }
}
