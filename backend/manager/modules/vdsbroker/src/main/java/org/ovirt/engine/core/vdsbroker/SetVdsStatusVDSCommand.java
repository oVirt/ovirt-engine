package org.ovirt.engine.core.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVdsStatusVDSCommand<P extends SetVdsStatusVDSCommandParameters> extends VdsIdVDSCommandBase<P> {

    private static final Logger log = LoggerFactory.getLogger(SetVdsStatusVDSCommand.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    public SetVdsStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        final SetVdsStatusVDSCommandParameters parameters = getParameters();

        if (_vdsManager != null) {

            final VDS vds = getVds();
            if (vds.getSpmStatus() != VdsSpmStatus.None && parameters.getStatus() != VDSStatus.Up) {
                log.info("VDS '{}' is spm and moved from up calling resetIrs.", vds.getName());
                // check if this host was spm and reset if do.
                getVDSReturnValue().setSucceeded(
                        resourceManager
                                .runVdsCommand(
                                        VDSCommandType.ResetIrs,
                                        new ResetIrsVDSCommandParameters(vds.getStoragePoolId(), vds.getId()))
                                .getSucceeded());

                if (!getVDSReturnValue().getSucceeded()) {
                    if (getParameters().isStopSpmFailureLogged()) {
                        AuditLogable base = new AuditLogableImpl();
                        base.setVdsId(vds.getId());
                        base.setVdsName(vds.getName());
                        auditLogDirector.log(base, AuditLogType.VDS_STATUS_CHANGE_FAILED_DUE_TO_STOP_SPM_FAILURE);
                    }

                    if (parameters.getStatus() == VDSStatus.PreparingForMaintenance) {
                        // ResetIrs command failed, SPM host status cannot be moved to Preparing For Maintenance
                        return;
                    }
                }

            }

            TransactionSupport.executeInNewTransaction(() -> {
                _vdsManager.setStatus(parameters.getStatus(), vds);
                _vdsManager.updatePartialDynamicData(parameters.getNonOperationalReason(),
                        parameters.getMaintenanceReason());
                _vdsManager.updateStatisticsData(vds.getStatisticsData());
                return null;
            });
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
