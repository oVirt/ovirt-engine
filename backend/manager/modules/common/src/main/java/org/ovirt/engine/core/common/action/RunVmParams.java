package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;

public class RunVmParams extends VmOperationParameterBase {
    private static final long serialVersionUID = 3311307677963231320L;

    private BootSequence _bootSequence;
    private String _diskPath;
    private boolean _kvmEnable;
    private Boolean _runAndPause;
    private Boolean _useVnc;
    private boolean _acpiEnable;
    private Boolean _win2kHackEnable;
    private String customProperties;
    private String privateFloppyPath;
    private String privateClientIp;
    private VdcUser privateRequestingUser;
    private Guid _destinationVdsId;
    private InitializationType privateInitializationType;
    private Boolean privateRunAsStateless;
    private String initrd_url;
    private String kernel_url;
    private String kernel_params;
    private VmPayload payload;
    private boolean balloonEnabled;
    private int cpuShares;

    public RunVmParams() {
    }

    public RunVmParams(BootSequence bootSequence, Guid vmId, String diskPath, boolean kvmEnable,
            Boolean runAndPause, Boolean useVnc, boolean acpiEnable, boolean win2kHackEnable,
            String clientIp, boolean runAsStateless) {
        super(vmId);
        _bootSequence = bootSequence;
        _diskPath = diskPath;
        _kvmEnable = kvmEnable;
        _runAndPause = runAndPause;
        _useVnc = useVnc;
        _acpiEnable = acpiEnable;
        _win2kHackEnable = win2kHackEnable;
        setClientIp(clientIp);
        setRunAsStateless(runAsStateless);
    }

    public RunVmParams(Guid vmId) {
        super(vmId);
        _diskPath = "";
        _kvmEnable = true;
        _acpiEnable = true;
    }

    public RunVmParams(Guid vmId, String clientIp) {
        this(vmId);
        setClientIp(clientIp);
    }

    public RunVmParams(Guid vmId, boolean isInternal) {
        this(vmId);
    }

    public Guid getDestinationVdsId() {
        return _destinationVdsId;
    }

    public void setDestinationVdsId(Guid value) {
        _destinationVdsId = value;
    }

    public BootSequence getBootSequence() {
        return _bootSequence;
    }

    public void setBootSequence(BootSequence value) {
        _bootSequence = value;
    }

    public String getFloppyPath() {
        return privateFloppyPath;
    }

    public void setFloppyPath(String value) {
        privateFloppyPath = value;
    }

    public String getDiskPath() {
        return _diskPath;
    }

    public void setDiskPath(String value) {
        _diskPath = value;
    }

    public boolean getKvmEnable() {
        return _kvmEnable;
    }

    public void setKvmEnable(boolean value) {
        _kvmEnable = value;
    }

    public Boolean getRunAndPause() {
        return _runAndPause;
    }

    public void setRunAndPause(Boolean value) {
        _runAndPause = value;
    }

    public Boolean getUseVnc() {
        return _useVnc;
    }

    public void setUseVnc(Boolean value) {
        _useVnc = value;
    }

    public boolean getAcpiEnable() {
        return _acpiEnable;
    }

    public void setAcpiEnable(boolean value) {
        _acpiEnable = value;
    }

    public Boolean getWin2kHackEnable() {
        return _win2kHackEnable;
    }

    public void setWin2kHackEnable(Boolean value) {
        _win2kHackEnable = value;
    }

    public String getClientIp() {
        return privateClientIp;
    }

    public void setClientIp(String value) {
        privateClientIp = value;
    }

    public VdcUser getRequestingUser() {
        return privateRequestingUser;
    }

    public void setRequestingUser(VdcUser value) {
        privateRequestingUser = value;
    }

    public InitializationType getInitializationType() {
        return privateInitializationType;
    }

    public void setInitializationType(InitializationType value) {
        privateInitializationType = value;
    }

    public Boolean getRunAsStateless() {
        return privateRunAsStateless;
    }

    public void setRunAsStateless(Boolean value) {
        privateRunAsStateless = value;
    }

    public String getinitrd_url() {
        return this.initrd_url;
    }

    public void setinitrd_url(String value) {
        this.initrd_url = value;
    }

    public String getkernel_url() {
        return this.kernel_url;
    }

    public void setkernel_url(String value) {
        this.kernel_url = value;
    }

    public String getkernel_params() {
        return this.kernel_params;
    }

    public void setkernel_params(String value) {
        this.kernel_params = value;
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
}
