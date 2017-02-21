package org.ovirt.engine.core.common.action;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.InitializationType;
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
    private String floppyPath;
    private InitializationType initializationType;
    private Boolean runAsStateless;
    private boolean balloonEnabled;
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
                && Objects.equals(floppyPath, other.floppyPath)
                && initializationType == other.initializationType
                && Objects.equals(runAsStateless, other.runAsStateless)
                && balloonEnabled == other.balloonEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getVmId(),
                diskPath,
                runAndPause,
                floppyPath,
                initializationType,
                runAsStateless,
                balloonEnabled
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

    public boolean isBalloonEnabled() {
        return this.balloonEnabled;
    }

    public void setBalloonEnabled(boolean isBalloonEnabled) {
        this.balloonEnabled = isBalloonEnabled;
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
