package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class SetVdsStatusVDSCommand<P extends SetVdsStatusVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public SetVdsStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        final SetVdsStatusVDSCommandParameters parameters = getParameters();

        if (_vdsManager != null) {

            final VDS vds = getVds();
            updateVdsFromParameters(parameters, vds);
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                @Override
                public Void runInTransaction() {
                    _vdsManager.setStatus(parameters.getStatus(), vds);
                    _vdsManager.UpdateDynamicData(vds.getDynamicData());
                    _vdsManager.UpdateStatisticsData(vds.getStatisticsData());
                    return null;
                }
            });

            if (vds.getSpmStatus() != VdsSpmStatus.None && parameters.getStatus() != VDSStatus.Up) {
                log.infoFormat("VDS {0} is spm and moved from up calling ResetIrs.", vds.getName());
                // check if this host was spm and reset if do.
                getVDSReturnValue().setSucceeded(
                        ResourceManager
                                .getInstance()
                                .runVdsCommand(
                                        VDSCommandType.ResetIrs,
                                        new ResetIrsVDSCommandParameters(vds.getStoragePoolId(), vds.getId()))
                                .getSucceeded());
            }
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    private void updateVdsFromParameters(SetVdsStatusVDSCommandParameters parameters, VDS vds) {
        vds.getDynamicData().setNonOperationalReason(parameters.getNonOperationalReason());
    }

    private static Log log = LogFactory.getLog(SetVdsStatusVDSCommand.class);
}
