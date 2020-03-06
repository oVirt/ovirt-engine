package org.ovirt.engine.core.common.action;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
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
    private DisplayType runOnceDisplayType;

    private String customEmulatedMachine;
    private String customCpuName;
    private Boolean bootMenuEnabled;
    private Boolean spiceFileTransferEnabled;
    private Boolean spiceCopyPasteEnabled;
    private String initrdUrl;
    private String kernelUrl;
    private String kernelParams;
    private BootSequence bootSequence;
    private String customProperties;

    @NullOrStringContainedInConfigValueList(configValue = ConfigValues.VncKeyboardLayoutValidValues,
            groups = StartEntity.class, message = "VALIDATION_VM_INVALID_KEYBOARD_LAYOUT")
    private String vncKeyboardLayout;

    private boolean volatileRun;

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
                customCpuName,
                customProperties,
                initrdUrl,
                kernelUrl,
                kernelParams,
                bootMenuEnabled,
                spiceFileTransferEnabled,
                spiceCopyPasteEnabled,
                bootSequence
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
                && bootSequence == other.bootSequence
                && Objects.equals(sysPrepDomainName, other.sysPrepDomainName)
                && Objects.equals(sysPrepUserName, other.sysPrepUserName)
                && Objects.equals(sysPrepPassword, other.sysPrepPassword)
                && Objects.equals(vmInit, other.vmInit)
                && Objects.equals(destinationVdsId, other.destinationVdsId)
                && Objects.equals(customEmulatedMachine, other.customEmulatedMachine)
                && Objects.equals(customCpuName, other.customCpuName)
                && Objects.equals(customProperties, other.customProperties)
                && Objects.equals(initrdUrl, other.initrdUrl)
                && Objects.equals(kernelUrl, other.kernelUrl)
                && Objects.equals(kernelParams, other.kernelParams)
                && Objects.equals(bootMenuEnabled, other.bootMenuEnabled)
                && Objects.equals(spiceFileTransferEnabled, other.spiceFileTransferEnabled)
                && Objects.equals(spiceCopyPasteEnabled, other.spiceCopyPasteEnabled);
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

    public DisplayType getRunOnceDisplayType() {
        return runOnceDisplayType;
    }

    public void setRunOnceDisplayType(DisplayType runOnceDisplayType) {
        this.runOnceDisplayType = runOnceDisplayType;
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

    public Boolean getBootMenuEnabled() {
        return bootMenuEnabled;
    }

    public void setBootMenuEnabled(Boolean bootMenuEnabled) {
        this.bootMenuEnabled = bootMenuEnabled;
    }

    public Boolean getSpiceFileTransferEnabled() {
        return spiceFileTransferEnabled;
    }

    public void setSpiceFileTransferEnabled(Boolean spiceFileTransferEnabled) {
        this.spiceFileTransferEnabled = spiceFileTransferEnabled;
    }

    public Boolean getSpiceCopyPasteEnabled() {
        return spiceCopyPasteEnabled;
    }

    public void setSpiceCopyPasteEnabled(Boolean spiceCopyPasteEnabled) {
        this.spiceCopyPasteEnabled = spiceCopyPasteEnabled;
    }

    public String getInitrdUrl() {
        return this.initrdUrl;
    }

    public void setInitrdUrl(String value) {
        this.initrdUrl = value;
    }

    public String getKernelUrl() {
        return this.kernelUrl;
    }

    public void setKernelUrl(String value) {
        this.kernelUrl = value;
    }

    public String getKernelParams() {
        return this.kernelParams;
    }

    public void setKernelParams(String value) {
        this.kernelParams = value;
    }

    public BootSequence getBootSequence() {
        return bootSequence;
    }

    public void setBootSequence(BootSequence value) {
        bootSequence = value;
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    public boolean isVolatileRun() {
        return volatileRun;
    }

    public void setVolatileRun(boolean volatileRun) {
        this.volatileRun = volatileRun;
    }
}
