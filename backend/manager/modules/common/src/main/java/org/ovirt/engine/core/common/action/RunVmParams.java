package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class RunVmParams extends VmOperationParameterBase {
    private static final long serialVersionUID = 3311307677963231320L;

    private BootSequence bootSequence;
    private String diskPath;
    private boolean kvmEnable;
    private Boolean runAndPause;
    private Boolean useVnc;
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

    public Boolean getUseVnc() {
        return useVnc;
    }

    public void setUseVnc(Boolean value) {
        useVnc = value;
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
