package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.NullOrStringContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.group.StartEntity;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.compat.Guid;

public class RunVmOnceParams extends RunVmParams {

    private static final long serialVersionUID = -4968552684343593622L;

    private String sysPrepDomainName;
    private String sysPrepUserName;
    private String sysPrepPassword;
    private VmInit vmInit;
    private Guid destinationVdsId;

    private String customEmulatedMachine;
    private String customCpuName;

    @NullOrStringContainedInConfigValueList(configValue = ConfigValues.VncKeyboardLayoutValidValues,
            groups = { StartEntity.class }, message = "VALIDATION.VM.INVALID_KEYBOARD_LAYOUT")
    private String vncKeyboardLayout;

    public RunVmOnceParams() {
    }

    public RunVmOnceParams(Guid vmId) {
        super(vmId);
    }

    public void setSysPrepDomainName(String sysPrepDomainName) {
        this.sysPrepDomainName = sysPrepDomainName;
    }

    public String getSysPrepDomainName() {
        return sysPrepDomainName;
    }

    public void setSysPrepUserName(String sysPrepUserName) {
        this.sysPrepUserName = sysPrepUserName;
    }

    public String getSysPrepUserName() {
        return sysPrepUserName;
    }

    public void setSysPrepPassword(String sysPrepPassword) {
        this.sysPrepPassword = sysPrepPassword;
    }

    @ShouldNotBeLogged
    public String getSysPrepPassword() {
        return sysPrepPassword;
    }

    public void setVmInit(VmInit vmInit) {
        this.vmInit = vmInit;
    }

    public VmInit getVmInit() {
        return vmInit;
    }

    public String getVncKeyboardLayout() {
        return vncKeyboardLayout;
    }

    public void setVncKeyboardLayout(String vncKeyboardLayout) {
        this.vncKeyboardLayout = vncKeyboardLayout;
    }

    public Guid getDestinationVdsId() {
        return destinationVdsId;
    }

    public void setDestinationVdsId(Guid destinationVdsId) {
        this.destinationVdsId = destinationVdsId;
    }

    public String getCustomEmulatedMachine() {
        return customEmulatedMachine;
    }

    public void setCustomEmulatedMachine(String customEmulatedMachine) {
        this.customEmulatedMachine = ((customEmulatedMachine == null || customEmulatedMachine.trim().isEmpty()) ? null : customEmulatedMachine);
    }

    public String getCustomCpuName() {
        return customCpuName;
    }

    public void setCustomCpuName(String customCpuName) {
        this.customCpuName = ((customCpuName == null || customCpuName.trim().isEmpty()) ? null : customCpuName);
    }

}
