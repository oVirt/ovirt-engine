package org.ovirt.engine.core.common.action;

import java.util.HashSet;
import java.util.Set;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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
    private Set<GraphicsType> runOnceGraphics;

    private String customEmulatedMachine;
    private String customCpuName;

    @NullOrStringContainedInConfigValueList(configValue = ConfigValues.VncKeyboardLayoutValidValues,
            groups = { StartEntity.class }, message = "VALIDATION.VM.INVALID_KEYBOARD_LAYOUT")
    private String vncKeyboardLayout;

    public RunVmOnceParams() {
        initRunOnceGraphics();
    }

    public RunVmOnceParams(Guid vmId) {
        super(vmId);
        initRunOnceGraphics();
    }

    private void initRunOnceGraphics() {
        runOnceGraphics = new HashSet<GraphicsType>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sysPrepDomainName == null) ? 0 : sysPrepDomainName.hashCode());
        result = prime * result + ((sysPrepUserName == null) ? 0 : sysPrepUserName.hashCode());
        result = prime * result + ((sysPrepPassword == null) ? 0 : sysPrepPassword.hashCode());
        result = prime * result + ((vmInit == null) ? 0 : vmInit.hashCode());
        result = prime * result + ((destinationVdsId == null) ? 0 : destinationVdsId.hashCode());
        result = prime * result + ((customEmulatedMachine == null) ? 0 : customEmulatedMachine.hashCode());
        result = prime * result + ((customCpuName == null) ? 0 : customCpuName.hashCode());
        return result;
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
                && ObjectUtils.objectsEqual(sysPrepDomainName, other.sysPrepDomainName)
                && ObjectUtils.objectsEqual(sysPrepUserName, other.sysPrepUserName)
                && ObjectUtils.objectsEqual(sysPrepPassword, other.sysPrepPassword)
                && ObjectUtils.objectsEqual(vmInit, other.vmInit)
                && ObjectUtils.objectsEqual(destinationVdsId, other.destinationVdsId)
                && ObjectUtils.objectsEqual(customEmulatedMachine, other.customEmulatedMachine)
                && ObjectUtils.objectsEqual(customCpuName, other.customCpuName);
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
        this.customEmulatedMachine = ((customEmulatedMachine == null || customEmulatedMachine.trim().isEmpty()) ? null : customEmulatedMachine);
    }

    public String getCustomCpuName() {
        return customCpuName;
    }

    public void setCustomCpuName(String customCpuName) {
        this.customCpuName = ((customCpuName == null || customCpuName.trim().isEmpty()) ? null : customCpuName);
    }

}
