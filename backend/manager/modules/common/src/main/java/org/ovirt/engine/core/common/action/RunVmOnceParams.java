package org.ovirt.engine.core.common.action;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.NullOrStringContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.group.StartEntity;
import org.ovirt.engine.core.compat.Guid;

public class RunVmOnceParams extends RunVmParams {

    private static final long serialVersionUID = -4968552684343593622L;

    private String sysPrepDomainName;
    private String sysPrepUserName;
    private String sysPrepPassword;
    private VmInit vmInit;
    private Guid destinationVdsId;
    private Set<GraphicsType> runOnceGraphics;

    private String customEmulatedMachine;
    private String customCpuName;

    @NullOrStringContainedInConfigValueList(configValue = ConfigValues.VncKeyboardLayoutValidValues,
            groups = { StartEntity.class }, message = "VALIDATION_VM_INVALID_KEYBOARD_LAYOUT")
    private String vncKeyboardLayout;

    public RunVmOnceParams() {
        initRunOnceGraphics();
    }

    public RunVmOnceParams(Guid vmId) {
        super(vmId);
        initRunOnceGraphics();
    }

    private void initRunOnceGraphics() {
        runOnceGraphics = new HashSet<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                sysPrepDomainName,
                sysPrepUserName,
                sysPrepPassword,
                vmInit,
                destinationVdsId,
                customEmulatedMachine,
                customCpuName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RunVmOnceParams)) {
            return false;
        }

        RunVmOnceParams other = (RunVmOnceParams) obj;
        return super.equals(obj)
                && Objects.equals(sysPrepDomainName, other.sysPrepDomainName)
                && Objects.equals(sysPrepUserName, other.sysPrepUserName)
                && Objects.equals(sysPrepPassword, other.sysPrepPassword)
                && Objects.equals(vmInit, other.vmInit)
                && Objects.equals(destinationVdsId, other.destinationVdsId)
                && Objects.equals(customEmulatedMachine, other.customEmulatedMachine)
                && Objects.equals(customCpuName, other.customCpuName);
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

    public Set<GraphicsType> getRunOnceGraphics() {
        return runOnceGraphics;
    }

    public void setRunOnceGraphics(Set<GraphicsType> runOnceGraphics) {
        this.runOnceGraphics = runOnceGraphics;
    }

    public String getCustomEmulatedMachine() {
        return customEmulatedMachine;
    }

    public void setCustomEmulatedMachine(String customEmulatedMachine) {
        this.customEmulatedMachine = customEmulatedMachine == null || customEmulatedMachine.trim().isEmpty() ? null : customEmulatedMachine;
    }

    public String getCustomCpuName() {
        return customCpuName;
    }

    public void setCustomCpuName(String customCpuName) {
        this.customCpuName = customCpuName == null || customCpuName.trim().isEmpty() ? null : customCpuName;
    }

}
