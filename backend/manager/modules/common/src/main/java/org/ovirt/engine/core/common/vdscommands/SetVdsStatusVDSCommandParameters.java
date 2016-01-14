package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SetVdsStatusVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private VDSStatus _status;
    private NonOperationalReason nonOperationalReason;
    private String maintenanceReason;

    /**
     * Flag to display SPM stop command failure in audit log
     */
    private boolean stopSpmFailureLogged;

    public SetVdsStatusVDSCommandParameters(Guid vdsId, VDSStatus status) {
        super(vdsId);
        _status = status;
        nonOperationalReason = NonOperationalReason.NONE;
        stopSpmFailureLogged = false;
    }

    public SetVdsStatusVDSCommandParameters(Guid vdsId, VDSStatus status, String maintenanceReason) {
        this(vdsId, status);
        this.maintenanceReason = maintenanceReason;
    }

    public SetVdsStatusVDSCommandParameters(Guid vdsId, VDSStatus status, NonOperationalReason nonOperationalReason) {
        this(vdsId, status);
        this.nonOperationalReason = nonOperationalReason;
    }

    public VDSStatus getStatus() {
        return _status;
    }

    public SetVdsStatusVDSCommandParameters() {
        _status = VDSStatus.Unassigned;
        nonOperationalReason = NonOperationalReason.NONE;
        stopSpmFailureLogged = false;
    }

    public NonOperationalReason getNonOperationalReason() {
        return nonOperationalReason;
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.nonOperationalReason = nonOperationalReason == null ? NonOperationalReason.NONE : nonOperationalReason;
    }

    public boolean isStopSpmFailureLogged() {
        return stopSpmFailureLogged;
    }

    public void setStopSpmFailureLogged(boolean stopSpmFailureLogged) {
        this.stopSpmFailureLogged = stopSpmFailureLogged;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("status", getStatus())
                .append("nonOperationalReason", getNonOperationalReason())
                .append("stopSpmFailureLogged", isStopSpmFailureLogged())
                .append("maintenanceReason", getMaintenanceReason());
    }
}
