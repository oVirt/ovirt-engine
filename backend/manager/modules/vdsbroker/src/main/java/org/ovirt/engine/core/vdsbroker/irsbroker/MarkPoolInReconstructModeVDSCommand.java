package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MarkPoolInReconstructModeVDSCommand<P extends IrsBaseVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    public MarkPoolInReconstructModeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        try {
            IrsProxyData proxyData = getCurrentIrsProxyData();
            proxyData.clearPoolTimers();
            proxyData.clearCache();
        } catch (Exception e) {
            log.error("Could not change timers for pool " + getParameters().getStoragePoolId(), e);
        }
        getVDSReturnValue().setSucceeded(true);
    }

    private static Log log = LogFactory.getLog(MarkPoolInReconstructModeVDSCommand.class);
}
