package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class RunVmParams extends VmOperationParameterBase {
    private static final long serialVersionUID = 3311307677963231320L;

    private BootSequence bootSequence;
    private String diskPath;
    private boolean kvmEnable;
    private Boolean runAndPause;
    private boolean acpiEnable;
    private Boolean win2kHackEnable;
    private String customProperties;
    private String floppyPath;
    private String clientIp;
    private DbUser requestingUser;
    private InitializationType initializationType;
    private Boolean runAsStateless;
    private String initrdUrl;
    private String kernelUrl;
    private String kernelParams;
    private VmPayload payload;
    private boolean balloonEnabled;
    private int cpuShares;
    private Boolean bootMenuEnabled;
    private Boolean spiceFileTransferEnabled;
    private Boolean spiceCopyPasteEnabled;

    public RunVmParams() {
    }

    public RunVmParams(Guid vmId) {
        super(vmId);
        kvmEnable = true;
        acpiEnable = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RunVmParams)) {
            return false;
        }

        RunVmParams other = (RunVmParams) obj;
        return bootSequence == other.bootSequence
                && ObjectUtils.objectsEqual(getVmId(), other.getVmId())
                && ObjectUtils.objectsEqual(diskPath, other.diskPath)
                && kvmEnable == other.kvmEnable
                && ObjectUtils.objectsEqual(runAndPause, other.runAndPause)
                && acpiEnable == other.acpiEnable
                && ObjectUtils.objectsEqual(win2kHackEnable, other.win2kHackEnable)
                && ObjectUtils.objectsEqual(customProperties, other.customProperties)
                && ObjectUtils.objectsEqual(floppyPath, other.floppyPath)
                && ObjectUtils.objectsEqual(clientIp, other.clientIp)
                && ObjectUtils.objectsEqual(requestingUser, other.requestingUser)
                && initializationType == other.initializationType
                && ObjectUtils.objectsEqual(runAsStateless, other.runAsStateless)
                && ObjectUtils.objectsEqual(initrdUrl, other.initrdUrl)
                && ObjectUtils.objectsEqual(kernelUrl, other.kernelUrl)
                && ObjectUtils.objectsEqual(kernelParams, other.kernelParams)
                && ObjectUtils.objectsEqual(payload, other.payload)
                && balloonEnabled == other.balloonEnabled
                && cpuShares == other.cpuShares
                && ObjectUtils.objectsEqual(bootMenuEnabled, other.bootMenuEnabled)
                && ObjectUtils.objectsEqual(spiceFileTransferEnabled, other.spiceFileTransferEnabled)
                && ObjectUtils.objectsEqual(spiceCopyPasteEnabled, other.spiceCopyPasteEnabled);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bootSequence == null) ? 0 : bootSequence.hashCode());
        result = prime * result + ((diskPath == null) ? 0 : diskPath.hashCode());
        result = prime * result + (kvmEnable ? 1231 : 1237);
        result = prime * result + ((runAndPause == null) ? 0 : runAndPause.hashCode());
        result = prime * result + (acpiEnable ? 1231 : 1237);
        result = prime * result + ((win2kHackEnable == null) ? 0 : win2kHackEnable.hashCode());
        result = prime * result + ((customProperties == null) ? 0 : customProperties.hashCode());
        result = prime * result + ((floppyPath == null) ? 0 : floppyPath.hashCode());
        result = prime * result + ((clientIp == null) ? 0 : clientIp.hashCode());
        result = prime * result + ((requestingUser == null) ? 0 : requestingUser.hashCode());
        result = prime * result + ((initializationType == null) ? 0 : initializationType.hashCode());
        result = prime * result + ((runAsStateless == null) ? 0 : runAsStateless.hashCode());
        result = prime * result + ((initrdUrl == null) ? 0 : initrdUrl.hashCode());
        result = prime * result + ((kernelUrl == null) ? 0 : kernelUrl.hashCode());
        result = prime * result + ((kernelParams == null) ? 0 : kernelParams.hashCode());
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        result = prime * result + (balloonEnabled ? 1231 : 1237);
        result = prime * result + cpuShares;
        result = prime * result + ((bootMenuEnabled == null) ? 0 : bootMenuEnabled.hashCode());
        result = prime * result + ((spiceFileTransferEnabled == null) ? 0 : spiceFileTransferEnabled.hashCode());
        result = prime * result + ((spiceCopyPasteEnabled == null) ? 0 : spiceCopyPasteEnabled.hashCode());
        return result;
    }

    public BootSequence getBootSequence() {
        return bootSequence;
    }

    public void setBootSequence(BootSequence value) {
        bootSequence = value;
    }

    public String getFloppyPath() {
        return floppyPath;
    }

    public void setFloppyPath(String value) {
        floppyPath = value;
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void setDiskPath(String value) {
        diskPath = value;
    }

    public boolean getKvmEnable() {
        return kvmEnable;
    }

    public void setKvmEnable(boolean value) {
        kvmEnable = value;
    }

    public Boolean getRunAndPause() {
        return runAndPause;
    }

    public void setRunAndPause(Boolean value) {
        runAndPause = value;
    }

    public boolean getAcpiEnable() {
        return acpiEnable;
    }

    public void setAcpiEnable(boolean value) {
        acpiEnable = value;
    }

    public Boolean getWin2kHackEnable() {
        return win2kHackEnable;
    }

    public void setWin2kHackEnable(Boolean value) {
        win2kHackEnable = value;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String value) {
        clientIp = value;
    }

    public DbUser getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(DbUser value) {
        requestingUser = value;
    }

    public InitializationType getInitializationType() {
        return initializationType;
    }

    public void setInitializationType(InitializationType value) {
        initializationType = value;
    }

    public Boolean getRunAsStateless() {
        return runAsStateless;
    }

    public void setRunAsStateless(Boolean value) {
        runAsStateless = value;
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

    public VmPayload getVmPayload() {
        return this.payload;
    }

    public void setVmPayload(VmPayload value) {
        this.payload = value;
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    public boolean isBalloonEnabled() {
        return this.balloonEnabled;
    }

    public void setBalloonEnabled(boolean isBalloonEnabled) {
        this.balloonEnabled = isBalloonEnabled;
    }
    public int getCpuShares() {
        return cpuShares;
    }

    public void setCpuShares(int cpuShares) {
        this.cpuShares = cpuShares;
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
}
