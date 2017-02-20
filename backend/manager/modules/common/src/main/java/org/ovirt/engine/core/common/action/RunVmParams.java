package org.ovirt.engine.core.common.action;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class RunVmParams extends VmOperationParameterBase {

    public enum RunVmFlow {
        /** regular flow */
        RUN,
        /** run VM which is paused */
        RESUME_PAUSE,
        /** run VM which is suspended */
        RESUME_HIBERNATE,
        /** create the stateless images in order to run the VM as stateless */
        CREATE_STATELESS_IMAGES,
        /** remove stateless images that remained from last time the VM ran as stateless */
        REMOVE_STATELESS_IMAGES,
        /** wrap things up after the VM reach UP state */
        RUNNING_SUCCEEDED;

        public boolean isStateless() {
            return this == RunVmFlow.CREATE_STATELESS_IMAGES || this == RunVmFlow.REMOVE_STATELESS_IMAGES;
        }
    }

    private static final long serialVersionUID = 3311307677963231320L;

    private String diskPath;
    private Boolean runAndPause;
    private Boolean win2kHackEnable;
    private String customProperties;
    private String floppyPath;
    private String clientIp;
    private DbUser requestingUser;
    private InitializationType initializationType;
    private Boolean runAsStateless;
    private VmPayload payload;
    private boolean balloonEnabled;
    private int cpuShares;
    private boolean runInUnknownStatus;

    private RunVmFlow cachedFlow;

    public RunVmParams() {
    }

    public RunVmParams(Guid vmId) {
        super(vmId);
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
        return Objects.equals(getVmId(), other.getVmId())
                && Objects.equals(diskPath, other.diskPath)
                && Objects.equals(runAndPause, other.runAndPause)
                && Objects.equals(win2kHackEnable, other.win2kHackEnable)
                && Objects.equals(customProperties, other.customProperties)
                && Objects.equals(floppyPath, other.floppyPath)
                && Objects.equals(clientIp, other.clientIp)
                && Objects.equals(requestingUser, other.requestingUser)
                && initializationType == other.initializationType
                && Objects.equals(runAsStateless, other.runAsStateless)
                && Objects.equals(payload, other.payload)
                && balloonEnabled == other.balloonEnabled
                && cpuShares == other.cpuShares;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getVmId(),
                diskPath,
                runAndPause,
                win2kHackEnable,
                customProperties,
                floppyPath,
                clientIp,
                requestingUser,
                initializationType,
                runAsStateless,
                payload,
                balloonEnabled,
                cpuShares
        );
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

    public Boolean getRunAndPause() {
        return runAndPause;
    }

    public void setRunAndPause(Boolean value) {
        runAndPause = value;
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

    public RunVmFlow getCachedFlow() {
        return cachedFlow;
    }

    public void setCachedFlow(RunVmFlow cachedFlow) {
        this.cachedFlow = cachedFlow;
    }

    public boolean isRunInUnknownStatus() {
        return runInUnknownStatus;
    }

    public void setRunInUnknownStatus(boolean runInUnknownStatus) {
        this.runInUnknownStatus = runInUnknownStatus;
    }

}
