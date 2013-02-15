package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.MarkPoolInReconstructModeVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MarkPoolInReconstructModeVDSCommand<P extends MarkPoolInReconstructModeVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    public MarkPoolInReconstructModeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        try {
            IrsProxyData proxyData = getCurrentIrsProxyData();
            switch (getParameters().getReconstructMarkAction()) {
            case ClearJobs:
                proxyData.clearPoolTimers();
                break;
            case ClearCache:
                proxyData.clearCache();
                break;
            default:
                break;
            }
        } catch (Exception e) {
            log.error("Could not change timers for pool " + getParameters().getStoragePoolId(), e);
        }
        getVDSReturnValue().setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(MarkPoolInReconstructModeVDSCommand.class);
}
